package com.tcmp.optosval.processors;

import com.tcmp.optosval.model.TradeRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TradeRecordTransformer implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        // Obtener el cuerpo del mensaje (lista de documentos de MongoDB)
        List<Document> data = exchange.getIn().getBody(List.class); // Lista de documentos

        log.info("Datos recibidos: {}", data);

        // Validar que la lista no sea nula o vacía
        if (data == null || data.isEmpty()) {
            log.warn("No se encontraron datos en el cuerpo del mensaje.");
            exchange.getIn().setBody(new ArrayList<>()); // Retornar una lista vacía
            return;
        }

        // Crear una lista para almacenar los objetos TradeRecord
        List<TradeRecord> tradeRecords = new ArrayList<>();

        // Procesar cada documento
        for (Document doc : data) {
            try {
                // Log para depuración
                log.info("Procesando documento: {}", doc.toJson());

                // Crear un nuevo TradeRecord y mapear los datos
                TradeRecord tradeRecord = new TradeRecord();
                tradeRecord.setBRANCH("20"); // Fijo en 20
                tradeRecord.setPRODUCTO(getEmbeddedString(doc, List.of("TradeMessage", "product_FXOption", "underlyingInstrumentName")));
                tradeRecord.setDEAL(getEmbeddedString(doc, List.of("TradeMessage", "trade", "tradeHeader", "tradeIdentifiers", "tradeId", "id")));
                tradeRecord.setFECHA_LIQ(getEmbeddedString(doc, List.of("TradeMessage", "combinedField")));
                tradeRecord.setDESCR(getEmbeddedString(doc, List.of("TradeMessage", "combinedField")));
                tradeRecord.setID_CONTRAPARTE(getEmbeddedString(doc, List.of("TradeMessage", "trade", "parties", "counterparty", "partyName")));
                tradeRecord.setNOMBRE(getEmbeddedString(doc, List.of("TradeMessage", "trade", "parties", "counterparty", "partyName")));
                tradeRecord.setCCOSTOS(getEmbeddedString(doc, List.of("TradeMessage", "product_FXOption", "baseCurrency")));
                tradeRecord.setMONEDA_NOCIONAL(getEmbeddedString(doc, List.of("TradeMessage", "product_FXOption", "underlyingCurrencyCode")));
                tradeRecord.setNOCIONAL(getEmbeddedDouble(doc, List.of("TradeMessage", "product_FXOption", "underlyingAmount")));
                tradeRecord.setTIPO_CAMBIO(getEmbeddedDouble(doc, List.of("TradeMessage", "product_FXOption", "baseAmount")));
                tradeRecord.setCONTRAMONEDA(getEmbeddedString(doc, List.of("TradeMessage", "product_FXOption", "baseCurrency")));
                tradeRecord.setCONTRAMONTO(calculateContramonto(doc));
                tradeRecord.setMONEDA_LIQ(getEmbeddedString(doc, List.of("TradeMessage", "combinedField")));
                tradeRecord.setMONTO_LIQUIDAR(getEmbeddedDouble(doc, List.of("TradeMessage", "combinedField")));
                tradeRecord.setSUBYACENTE(getEmbeddedString(doc, List.of("TradeMessage", "product_FXOption", "underlyingInstrumentName")));

                // Agregar el TradeRecord a la lista
                tradeRecords.add(tradeRecord);
            } catch (Exception e) {
                log.error("Error procesando el documento: {}", doc.toJson(), e);
            }
        }

        // Establecer la lista de TradeRecord como el cuerpo del mensaje
        exchange.getIn().setBody(tradeRecords);
    }

    private double calculateContramonto(Document doc) {
        Double nominalAmount = getEmbeddedDouble(doc, List.of("TradeMessage", "product_FXOption", "underlyingAmount"));
        Double spotRate = getEmbeddedDouble(doc, List.of("TradeMessage", "product_FXOption", "baseAmount"));
        return nominalAmount != null && spotRate != null ? nominalAmount * spotRate : 0.0;
    }

    public void writeCsv(List<TradeRecord> records) throws IOException {
        String[] headers = {
                "BRANCH", "PRODUCTO", "DEAL", "FECHA_LIQ", "DESCR", "ID_CONTRAPARTE", "NOMBRE", "CCOSTOS",
                "MONEDA_NOCIONAL", "NOCIONAL", "TIPO_CAMBIO", "CONTRAMONEDA", "CONTRAMONTO", "MONEDA_LIQ",
                "MONTO_LIQUIDAR", "SUBYACENTE"
        };

        // Crear un FileWriter y un CSVPrinter
        try (FileWriter fileWriter = new FileWriter("output.csv");
             CSVPrinter printer = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader(headers))) {

            // Escribir los registros en el CSV
            for (TradeRecord record : records) {
                printer.printRecord(
                        record.getBRANCH(), record.getPRODUCTO(), record.getDEAL(), record.getFECHA_LIQ(),
                        record.getDESCR(), record.getID_CONTRAPARTE(), record.getNOMBRE(), record.getCCOSTOS(),
                        record.getMONEDA_NOCIONAL(), record.getNOCIONAL(), record.getTIPO_CAMBIO(),
                        record.getCONTRAMONEDA(), record.getCONTRAMONTO(), record.getMONEDA_LIQ(),
                        record.getMONTO_LIQUIDAR(), record.getSUBYACENTE());
            }
        }
    }

    // Métodos auxiliares
    private String getEmbeddedString(Document doc, List<String> path) {
        try {
            Object value = doc.get(path.get(0));  // Get the first part of the path
            for (int i = 1; i < path.size() && value != null; i++) {
                if (value instanceof Document) {
                    value = ((Document) value).get(path.get(i)); // Get nested fields
                } else {
                    log.warn("Path '{}' is not a Document, value: {}", path, value);
                    break;
                }
            }
            if (value == null) {
                return "random"; // If no value found, return "random"
            }
            if (value instanceof String) {
                return (String) value;
            }
            return value.toString(); // Convert other types to String
        } catch (Exception e) {
            log.warn("Error extracting string from path {}: {}", path, e.getMessage());
            return "random"; // Return default value if any error occurs
        }
    }

    private Double getEmbeddedDouble(Document doc, List<String> path) {
        try {
            Object value = doc.get(path.get(0));  // Get the first part of the path
            for (int i = 1; i < path.size() && value != null; i++) {
                if (value instanceof Document) {
                    value = ((Document) value).get(path.get(i)); // Get nested fields
                } else {
                    log.warn("Path '{}' is not a Document, value: {}", path, value);
                    break;
                }
            }
            if (value == null) {
                return 0.0; // Return default value if no value found
            }
            if (value instanceof Double) {
                return (Double) value;
            }
            if (value instanceof String) {
                return Double.parseDouble((String) value); // Try converting from String
            }
            return 0.0; // Return default value if conversion fails
        } catch (Exception e) {
            log.warn("Error extracting double from path {}: {}", path, e.getMessage());
            return 0.0; // Return default value if any error occurs
        }
    }
}