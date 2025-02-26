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
                log.info("Procesando documento Realtime: {}", tradedoc.toJson());
                log.info("Procesando documento Cashflow: {}", cashflowDoc.toJson());

                // Crear un nuevo TradeRecord y mapear los datos
                OptoSeccionIICRecord optoSeccionIICRecord = new OptoSeccionIICRecord();

                //FIJO
                optoSeccionIICRecord.setInst("040044");
                optoSeccionIICRecord.setCont("L3I9ZG2KFGXZ61BMYR72"); // Fijo en L3I9ZG2KFGXZ61BMYR72 // ANEXO B

                //Ruta Service
                optoSeccionIICRecord.setFecha(getEmbeddedString(tradedoc, List.of("TradeMessage", "trade", "tradeHeader", "tradeDate")));
                optoSeccionIICRecord.setNuID(getEmbeddedString(tradedoc, List.of("TradeMessage", "trade", "tradeHeader", "tradeIdentifiers", "tradeId", "id")));
                optoSeccionIICRecord.setImpbaCo(getEmbeddedDouble(cashflowDoc, List.of("CashflowMessage", "cashflowDetails", "cashflowAmount")));
                optoSeccionIICRecord.setFeinopCo(getEmbeddedString(tradedoc, List.of("TradeMessage", "trade", "product", "tradeDate")));
                optoSeccionIICRecord.setFeveopCo(getEmbeddedString(tradedoc, List.of("TradeMessage", "trade", "product", "exerciseStyle", "expiryDate")));
                optoSeccionIICRecord.setCveTitC(getEmbeddedString(tradedoc, List.of("TradeMessage", "trade", "product", "underlyingInstrumentName"))); // TODO ANEXO AF
                optoSeccionIICRecord.setPrecioEjerC(getEmbeddedDouble(tradedoc, List.of("TradeMessage", "trade", "product", "strikeRate")));
                optoSeccionIICRecord.setPreSup(getEmbeddedDouble(tradedoc, List.of("TradeMessage", "trade", "product", "barrierFeature", "barrierUpRate")));
                optoSeccionIICRecord.setPreInf(getEmbeddedDouble(tradedoc, List.of("TradeMessage", "trade", "product", "barrierFeature", "barrierDownRate")));
                optoSeccionIICRecord.setInstLei(getEmbeddedString(tradedoc, List.of("TradeMessage", "trade", "parties", "counterparty", "partyLei")));
                optoSeccionIICRecord.setUti(getEmbeddedString(tradedoc, List.of("TradeMessage", "trade", "tradeHeader", "tradeIdentifiers", "uniqueTransactionId")));

                // TODO Campos GBM Faltantes
                optoSeccionIICRecord.setNuPeEje("PENDIENTE");
                optoSeccionIICRecord.setSubyCo(""); // CATALOGO ANEXO F

                // Mapping
                optoSeccionIICRecord.setIdentificador(""); // TODO IDENTIFICADOR VanillaOption de SECCIONII se deja vacío

                // Agregar el TradeRecord a la lista
                optoSeccionIICRecords.add(optoSeccionIICRecord);
                index++;
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
            Object value = getNestedValue(doc, path);
            if (value == null) {
                return "N/A";
            }
            return value.toString();
        } catch (Exception e) {
            log.warn("Error al extraer string del path {}: {}", path, e.getMessage());
            return "N/A";
        }
    }


    private Double getEmbeddedDouble(Document doc, List<String> path) {
        try {
            Object value = getNestedValue(doc, path);
            if (value == null) {
                return 0.0;
            }
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            if (value instanceof String) {
                try {
                    return Double.parseDouble((String) value);
                } catch (NumberFormatException e) {
                    log.warn("No se pudo convertir el valor '{}' a Double en la ruta {}", value, path, e);
                }
            }
            log.warn("Valor no compatible encontrado en la ruta {}: {}", path, value);
            return 0.0;
        } catch (Exception e) {
            log.warn("Error al extraer double del path {}: {}", path, e.getMessage());
            return 0.0;
        }
    }

    private Object getNestedValue(Document doc, List<String> path) {
        Object value = doc;
        for (String key : path) {
            if (value instanceof Document) {
                value = ((Document) value).get(key);
            } else if (value instanceof List) {
                if (!((List<?>) value).isEmpty() && ((List<?>) value).get(0) instanceof Document) {
                    value = ((Document) ((List<?>) value).get(0)).get(key);
                } else {
                    log.warn("Valor inesperado en la ruta {}: {}", path, value);
                    return null;
                }
            } else {
                log.warn("Clave '{}' no encontrada en la ruta {}", key, path);
                return null;
            }
        }
        return value;
    }
}