package com.tcmp.optosval.processors;

import com.opencsv.CSVWriter;
import com.tcmp.optosval.model.OptoRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.io.CharArrayWriter;
import java.util.List;

@Component
@Slf4j
public class CsvWriter {

    public void writeToCsv(Exchange exchange) {
        // Obtener la lista de TradeRecord desde el Exchange
        List<OptoRecord> optoRecords = exchange.getIn().getBody(List.class);

        log.info("Datos recibidos en writeToCsv: {}", optoRecords.toString());

        if (optoRecords == null || optoRecords.isEmpty()) {
            // Si la lista está vacía o es nula, no hacemos nada
            log.warn("La lista de TradeRecord está vacía o es nula.");
            return;
        }

        // Usar CharArrayWriter para generar el CSV en memoria
        try (CharArrayWriter writer = new CharArrayWriter()) {
            CSVWriter csvWriter = new CSVWriter(writer, '|', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

            // Escribir los encabezados del CSV
            String[] header = {
                    "INST", "CONT", "FECHA", "NU_ID", "NU_PE_EJE", "IMPBA_CO", "FEINOP_CO", "FEVEOP_CO", "SUBY_CO", "CVE_TIT_C",
                    "PRECIOEJER_C", "PRE_SUP", "PRE_INF", "INST_LEI", "UTI", "IDENTIFICADOR"
            };
            csvWriter.writeNext(header);

            // Escribir cada TradeRecord como una nueva línea en el archivo CSV
            for (OptoRecord record : optoRecords) {
                String[] data = {
                        safeGet(record::getINST),
                        safeGet(record::getCONT),
                        safeGet(record::getFECHA),
                        safeGet(record::getNU_ID),
                        safeGet(record::getNU_PE_EJE),
                        safeGetDouble(record::getIMPBA_CO),
                        safeGet(record::getFEINOP_CO),
                        safeGet(record::getFEVEOP_CO),
                        safeGet(record::getSUBY_CO),
                        safeGet(record::getCVE_TIT_C),
                        safeGetDouble(record::getPRECIOEJER_C),
                        safeGetDouble(record::getPRE_SUP),
                        safeGetDouble(record::getPRE_INF),
                        safeGet(record::getINST_LEI),
                        safeGet(record::getUTI),
                        safeGet(record::getIDENTIFICADOR)
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