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
        List<Document> CashFlowDocs =  data.get("CashFlow");

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
        for (Document doc : tradeDocs) {
            try {
                Document cashFlowDoc = CashFlowDocs.get(index);
                // Log para depuración
                log.info("Procesando documento: {}", doc.toJson());

                // Crear un nuevo TradeRecord y mapear los datos
                OptoSeccionIICRecord optoSeccionIICRecord = new OptoSeccionIICRecord();
                optoSeccionIICRecord.setINST("040044"); // Fijo en 040044
                optoSeccionIICRecord.setCONT("L3I9ZG2KFGXZ61BMYR72"); // Fijo en L3I9ZG2KFGXZ61BMYR72
                optoSeccionIICRecord.setFECHA(getEmbeddedString(doc, List.of("TradeMessage", "trade", "tradeHeader", "tradeDate")));
                optoSeccionIICRecord.setNU_ID(getEmbeddedString(doc, List.of("TradeMessage", "trade", "tradeHeader", "tradeIdentifiers", "tradeId", "id")));
                optoSeccionIICRecord.setNU_PE_EJE("1"); //Fijo en 1
                optoSeccionIICRecord.setIMPBA_CO(getEmbeddedDouble(cashFlowDoc, List.of("JournalEntryMessage","CashflowMessage", "cashflowDetails", "0", "cashflowAmount")));
                optoSeccionIICRecord.setFEINOP_CO(getEmbeddedString(doc, List.of("TradeMessage", "trade", "product_FXOption", "startDate")));
                optoSeccionIICRecord.setFEVEOP_CO(getEmbeddedString(doc, List.of("TradeMessage", "trade", "product_FXOption", "endDate")));
                optoSeccionIICRecord.setSUBY_CO("840");
                optoSeccionIICRecord.setCVE_TIT_C(getEmbeddedString(doc, List.of("TradeMessage", "trade", "product_FXOption", "underlyingInstrumentName")));
                optoSeccionIICRecord.setPRECIOEJER_C(getEmbeddedDouble(doc, List.of("TradeMessage", "trade", "product_FXOption", "strikeRate")));
                optoSeccionIICRecord.setPRE_SUP(getEmbeddedDouble(doc, List.of("TradeMessage", "trade", "product_FXOption", "barrierFeature", "barrierUpRate")));
                optoSeccionIICRecord.setPRE_INF(getEmbeddedDouble(doc, List.of("TradeMessage", "trade", "product_FXOption", "barrierFeature", "barrierDownRate")));
                optoSeccionIICRecord.setINST_LEI(getEmbeddedString(doc, List.of("TradeMessage", "trade", "parties", "counterparty", "partyLei")));
                optoSeccionIICRecord.setUTI(getEmbeddedString(doc, List.of("TradeMessage", "trade", "tradeHeader", "tradeIdentifiers", "uniqueTransactionId")));
                optoSeccionIICRecord.setIDENTIFICADOR("");

                // Agregar el TradeRecord a la lista
                optoSeccionIICRecords.add(optoSeccionIICRecord);
            } catch (Exception e) {
                log.error("Error procesando el documento: {}", doc.toJson(), e);
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