package com.tcmp.optosval.processors;

import com.opencsv.CSVWriter;
import com.tcmp.optosval.model.TradeRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class CsvWriter {

    public void writeToCsv(Exchange exchange) {
        // Obtener la lista de TradeRecord desde el Exchange
        List<TradeRecord> tradeRecords = exchange.getIn().getBody(List.class);

        log.info("Datos recibidos en writeToCsv: {}", tradeRecords.toString());

        if (tradeRecords == null || tradeRecords.isEmpty()) {
            // Si la lista está vacía o es nula, no hacemos nada
            log.warn("La lista de TradeRecord está vacía o es nula.");
            return;
        }

        // Usar CharArrayWriter para generar el CSV en memoria
        try (CharArrayWriter writer = new CharArrayWriter()) {
            CSVWriter csvWriter = new CSVWriter(writer, '|', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

            // Escribir los encabezados del CSV
            String[] header = {
                    "BRANCH", "PRODUCTO", "DEAL", "FECHA_LIQ", "DESCR", "ID_CONTRAPARTE", "NOMBRE", "CCOSTOS",
                    "MONEDA_NOCIONAL", "NOCIONAL", "TIPO_CAMBIO", "CONTRAMONEDA", "CONTRAMONTO", "MONEDA_LIQ",
                    "MONTO_LIQUIDAR", "SUBYACENTE"
            };
            csvWriter.writeNext(header);

            // Escribir cada TradeRecord como una nueva línea en el archivo CSV
            for (TradeRecord record : tradeRecords) {
                String[] data = {
                        safeGet(record::getBRANCH),
                        safeGet(record::getPRODUCTO),
                        safeGet(record::getDEAL),
                        safeGet(record::getFECHA_LIQ),
                        safeGet(record::getDESCR),
                        safeGet(record::getID_CONTRAPARTE),
                        safeGet(record::getNOMBRE),
                        safeGet(record::getCCOSTOS),
                        safeGet(record::getMONEDA_NOCIONAL),
                        safeGetDouble(record::getNOCIONAL),
                        safeGetDouble(record::getTIPO_CAMBIO),
                        safeGet(record::getCONTRAMONEDA),
                        safeGetDouble(record::getCONTRAMONTO),
                        safeGet(record::getMONEDA_LIQ),
                        safeGetDouble(record::getMONTO_LIQUIDAR),
                        safeGet(record::getSUBYACENTE)
                };
                csvWriter.writeNext(data);
            }

            // Agregar el CSV generado al Exchange para enviarlo como respuesta
            exchange.getMessage().setBody(writer.toString());
            exchange.getMessage().setHeader("Content-Type", "text/csv");
            exchange.getMessage().setHeader("Content-Disposition", "attachment; filename=trade_records.csv");

        }
    }

    // Método seguro para obtener valores de los objetos TradeRecord (String)
    private String safeGet(ValueGetter getter) {
        try {
            return getter.getValue() != null ? getter.getValue().toString() : "";
        } catch (Exception e) {
            log.warn("Error al obtener valor de campo: {}", e.getMessage());
            return "";
        }
    }

    // Método seguro para obtener valores de los objetos TradeRecord (double)
    private String safeGetDouble(ValueGetterDouble getter) {
        try {
            return String.valueOf(getter.getValue());
        } catch (Exception e) {
            log.warn("Error al obtener valor de campo (double): {}", e.getMessage());
            return "0.0";  // Devolver un valor predeterminado si no se puede obtener el valor
        }
    }

    // Interfaz funcional para obtener el valor de los métodos (String)
    @FunctionalInterface
    interface ValueGetter {
        Object getValue();
    }

    // Interfaz funcional para obtener el valor de los métodos (double)
    @FunctionalInterface
    interface ValueGetterDouble {
        double getValue();
    }
}