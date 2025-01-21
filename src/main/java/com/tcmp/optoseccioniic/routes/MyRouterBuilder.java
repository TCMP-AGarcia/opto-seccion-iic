package com.tcmp.optoseccioniic.routes;

import com.tcmp.optoseccioniic.processors.CsvWriter;
import com.tcmp.optoseccioniic.processors.OptoSeccionIICRecordTransformer;
import com.tcmp.optoseccioniic.services.MongoService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyRouterBuilder extends RouteBuilder {

    @Autowired
    private MongoService mongoService;

    @Autowired
    private OptoSeccionIICRecordTransformer optoSeccionIICRecordTransformer;

    @Autowired
    private CsvWriter csvWriter;

    @Override
    public void configure() throws Exception {
        log.info("Camel route is being initialized...");

        from("direct:start")
                .routeId("mongoServiceRoute")
                .log("Iniciando procesamiento de datos desde MongoService...")
                .bean(mongoService, "printRealtimeData")
                .log("Datos obtenidos de MongoDB: ${body}")
                .bean(optoSeccionIICRecordTransformer)
                .log("Datos procesados con éxito desde TradeRecordTransformer.")
                .bean(csvWriter, "writeToCsv")
                .log("Datos exportados con éxito desde CsvWriter.")
                .end();
    }
}
