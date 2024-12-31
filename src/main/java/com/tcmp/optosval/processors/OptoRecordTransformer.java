package com.tcmp.optosval.processors;

import com.tcmp.optosval.model.OptoRecord;
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
public class OptoRecordTransformer implements Processor {

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
        List<OptoRecord> optoRecords = new ArrayList<>();

        // Procesar cada documento
        int index=0;
        for (Document doc : tradeDocs) {
            try {
                Document cashFlowDoc = CashFlowDocs.get(index);
                // Log para depuración
                log.info("Procesando documento: {}", doc.toJson());

                // Crear un nuevo TradeRecord y mapear los datos
                OptoRecord optoRecord = new OptoRecord();
                optoRecord.setINST("040044"); // Fijo en 040044
                optoRecord.setCONT("L3I9ZG2KFGXZ61BMYR72"); // Fijo en L3I9ZG2KFGXZ61BMYR72
                optoRecord.setFECHA(getEmbeddedString(doc, List.of("TradeMessage", "trade", "tradeHeader", "tradeDate")));
                optoRecord.setNU_ID(getEmbeddedString(doc, List.of("TradeMessage", "trade", "tradeHeader", "tradeIdentifiers", "tradeId", "id")));
                optoRecord.setNU_PE_EJE("1"); //Fijo en 1
                optoRecord.setIMPBA_CO(getEmbeddedDouble(cashFlowDoc, List.of("JournalEntryMessage","CashflowMessage", "cashflowDetails", "0", "cashflowAmount")));
                optoRecord.setFEINOP_CO(getEmbeddedString(doc, List.of("TradeMessage", "trade", "product_FXOption", "startDate")));
                optoRecord.setFEVEOP_CO(getEmbeddedString(doc, List.of("TradeMessage", "trade", "product_FXOption", "endDate")));
                optoRecord.setSUBY_CO("840");
                optoRecord.setCVE_TIT_C(getEmbeddedString(doc, List.of("TradeMessage", "trade", "product_FXOption", "underlyingInstrumentName")));
                optoRecord.setPRECIOEJER_C(getEmbeddedDouble(doc, List.of("TradeMessage", "trade", "product_FXOption", "strikeRate")));
                optoRecord.setPRE_SUP(getEmbeddedDouble(doc, List.of("TradeMessage", "trade", "product_FXOption", "barrierFeature", "barrierUpRate")));
                optoRecord.setPRE_INF(getEmbeddedDouble(doc, List.of("TradeMessage", "trade", "product_FXOption", "barrierFeature", "barrierDownRate")));
                optoRecord.setINST_LEI(getEmbeddedString(doc, List.of("TradeMessage", "trade", "parties", "counterparty", "partyLei")));
                optoRecord.setUTI(getEmbeddedString(doc, List.of("TradeMessage", "trade", "tradeHeader", "tradeIdentifiers", "uniqueTransactionId")));
                optoRecord.setIDENTIFICADOR("");

                // Agregar el TradeRecord a la lista
                optoRecords.add(optoRecord);
            } catch (Exception e) {
                log.error("Error procesando el documento: {}", doc.toJson(), e);
            }
        }

        // Establecer la lista de TradeRecord como el cuerpo del mensaje
        exchange.getIn().setBody(optoRecords);
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