package com.tcmp.optoseccioniic.processors;

import com.tcmp.optoseccioniic.model.OptoSeccionIICRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class OptoSeccionIICRecordTransformer implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        // Obtener el cuerpo del mensaje (lista de documentos de MongoDB)
        HashMap<String,List<Document>> data =  new HashMap<>();
        data = exchange.getIn().getBody(data.getClass());
        List<Document> tradeDocs = data.get("RealTime");
        List<Document> CashflowDocs =  data.get("CashFlow");

        log.info("Datos recibidos: {}", data);

        // Validar que la lista no sea nula o vacía
        if (data == null || data.isEmpty()) {
            log.warn("No se encontraron datos en el cuerpo del mensaje.");
            exchange.getIn().setBody(new ArrayList<>()); // Retornar una lista vacía
            return;
        }

        // Crear una lista para almacenar los objetos TradeRecord
        List<OptoSeccionIICRecord> optoSeccionIICRecords = new ArrayList<>();

        // Procesar cada documento
        int index=0;
        for (Document tradedoc : tradeDocs) {
            try {
                Document cashflowDoc = CashflowDocs.get(index);
                // Log para depuración
                log.info("Procesando documento tradedoc: {}", tradedoc.toJson());
                log.info("Procesando documento cashFlowDoc: {}", cashflowDoc.toJson());

                // Crear un nuevo TradeRecord y mapear los datos
                OptoSeccionIICRecord optoSeccionIICRecord = new OptoSeccionIICRecord();
                optoSeccionIICRecord.setInst("040044"); // Fijo en 040044 // TODO ANEXO B
                optoSeccionIICRecord.setCont("L3I9ZG2KFGXZ61BMYR72"); // Fijo en L3I9ZG2KFGXZ61BMYR72
                optoSeccionIICRecord.setFecha(getEmbeddedString(tradedoc, List.of("TradeMessage", "trade", "tradeHeader", "tradeDate")));
                optoSeccionIICRecord.setNuID(getEmbeddedString(tradedoc, List.of("TradeMessage", "trade", "tradeHeader", "tradeIdentifiers", "tradeId", "id")));
                optoSeccionIICRecord.setImpbaCo(getEmbeddedDouble(cashflowDoc, List.of("CashflowMessage", "cashflowDetails", "cashflowAmount")));
                optoSeccionIICRecord.setFeinopCo(getEmbeddedString(tradedoc, List.of("TradeMessage", "trade", "tradeHeader", "tradeDate")));
                optoSeccionIICRecord.setFeveopCo(getEmbeddedString(tradedoc, List.of("TradeMessage", "trade", "product", "exerciseStyle", "expiryDate")));
                optoSeccionIICRecord.setSubyCo("840");
                optoSeccionIICRecord.setCveTitC(getEmbeddedString(tradedoc, List.of("TradeMessage", "trade", "product", "underlyingInstrumentName"))); // TODO ANEXO AF
                optoSeccionIICRecord.setPrecioEjerC(getEmbeddedDouble(tradedoc, List.of("TradeMessage", "trade", "product", "strikeRate")));
                optoSeccionIICRecord.setPreSup(getEmbeddedDouble(tradedoc, List.of("TradeMessage", "trade", "product", "barrierFeature", "barrierUpRate")));
                optoSeccionIICRecord.setPreInf(getEmbeddedDouble(tradedoc, List.of("TradeMessage", "trade", "product", "barrierFeature", "barrierDownRate")));
                optoSeccionIICRecord.setInstLei(getEmbeddedString(tradedoc, List.of("TradeMessage", "trade", "parties", "counterparty", "partyLei")));
                optoSeccionIICRecord.setUti(getEmbeddedString(tradedoc, List.of("TradeMessage", "trade", "tradeHeader", "tradeIdentifiers", "uniqueTransactionId")));
                optoSeccionIICRecord.setIdentificador("");

                // Campos GBM Faltantes
                optoSeccionIICRecord.setNuPeEje("1"); //Fijo en 1 // TODO NU_PE_EJE Pending to create
                // TODO SUBY_CO CATALOGO ANEXO F
                // TODO IDENTIFICADOR VanillaOption de SECCIONII se deja vacío

                // Agregar el TradeRecord a la lista
                optoSeccionIICRecords.add(optoSeccionIICRecord);
            } catch (Exception e) {
                log.error("Error procesando el documento: {}", tradedoc.toJson(), e);
            }
        }

        // Establecer la lista de TradeRecord como el cuerpo del mensaje
        exchange.getIn().setBody(optoSeccionIICRecords);
    }

    // Métodos auxiliares
    private String getEmbeddedString(Document doc, List<String> path) {
        try {
            Object value = doc.get(path.get(0));  // Get the first part of the path
            for (int i = 1; i < path.size() && value != null; i++) {
                if (value instanceof Document) {
                    value = ((Document) value).get(path.get(i)); // Get nested fields
                } else if (value instanceof List) {
                    value = ((List<?>) value).get(0); // Get the first element if it's a list
                    value = ((Document) value).get(path.get(i)); // Continue with the next part of the path
                } else {
                    log.warn("Path '{}' is not a Document or List, value: {}", path, value);
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
                } else if (value instanceof List) {
                    value = ((List<?>) value).get(0); // Get the first element if it's a list
                    value = ((Document) value).get(path.get(i)); // Continue with the next part of the path
                } else {
                    log.warn("Path '{}' is not a Document or List, value: {}", path, value);
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

    private Integer getEmbeddedInteger(Document doc, List<String> path) {
        try {
            Object value = doc.get(path.get(0)); // Obtener la primera parte del path
            for (int i = 1; i < path.size() && value != null; i++) {
                if (value instanceof Document) {
                    value = ((Document) value).get(path.get(i)); // Obtener campos anidados
                } else if (value instanceof List) {
                    value = ((List<?>) value).get(0); // Get the first element if it's a list
                    value = ((Document) value).get(path.get(i)); // Continue with the next part of the path
                } else {
                    log.warn("Path '{}' is not a Document or List, value: {}", path, value);
                    break;
                }
            }
            if (value == null) {
                return 0; // Valor por defecto si no se encuentra valor
            }
            if (value instanceof Integer) {
                return (Integer) value;
            }
            if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value); // Convertir desde String
                } catch (NumberFormatException e) {
                    log.warn("Cannot parse String '{}' as an integer", value);
                    return 0;
                }
            }
            return 0; // Valor por defecto si no es un entero o cadena convertible
        } catch (Exception e) {
            log.warn("Error extracting integer from path {}: {}", path, e.getMessage());
            return 0; // Valor por defecto si ocurre un error
        }
    }

}