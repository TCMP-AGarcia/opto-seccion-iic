package com.tcmp.optosval.routes;

import com.tcmp.optosval.processors.CsvWriter;
import com.tcmp.optosval.processors.TradeRecordTransformer;
import com.tcmp.optosval.services.MongoService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyRouterBuilder extends RouteBuilder {

    @Autowired
    private MongoService mongoService;

    @Autowired
    private TradeRecordTransformer tradeRecordTransformer;

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
                .bean(tradeRecordTransformer)
                .log("Datos procesados con éxito desde TradeRecordTransformer.")
                .bean(csvWriter, "writeToCsv")
                .log("Datos exportados con éxito desde CsvWriter.")
                .end();
    }
}
