package com.tcmp.optosval.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MongoService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void printRealtimeData(Exchange exchange) {
        // Get the MongoDB database
        MongoDatabase database = mongoTemplate.getDb();
        MongoCollection<Document> collection = database.getCollection("Realtime");
        MongoCollection<Document> collectionCF = database.getCollection("Cashflow");

        // Create the projection to fetch only the required fields
        Document projection = new Document(

                // TODO CONT ANEXO B

                // NU_ID RUTA trade.id
                "TradeMessage.trade.tradeHeader.tradeIdentifiers.tradeId.id", 1)
                // FECHA
                .append("TradeMessage.trade.tradeHeader.tradeDate", 1)
                // TODO NU_PE_EJE Pending to create

                // FEINOP_CO
                .append("TradeMessage.trade.product_FXOption.startDate",1)
                //FEVEOP_CO
                .append("TradeMessage.trade.product_FXOption.endDate",1)
                // TODO SUBY_CO CATALOGO ANEXO F

                // TODO CVE_TIT_C ANEXO AF
                .append("TradeMessage.trade.product_FXOption.underlyingInstrumentName", 1)
                // PRECIOEJER_C
                .append("TradeMessage.trade.product_FXOption.strikeRate", 1)
                // PRE_SUP
                .append("TradeMessage.trade.product_FXOption.barrierFeature.barrierUpRate", 1)
                // PRE_INF
                .append("TradeMessage.trade.product_FXOption.barrierFeature.barrierDownRate", 1)
                // INST_LEI
                .append("TradeMessage.trade.parties.counterparty.partyLei", 1)
                // UTI
                .append("TradeMessage.trade.tradeHeader.tradeIdentifiers.uniqueTransactionId", 1)
                // TODO IDENTIFICADOR VanillaOption de SECCIONII se deja vacío

        .append("_id", 0);  // Exclude the _id field

        Document projectionCF = new Document("_id", 1)
                .append("JournalEntryMessage.CashflowMessage.cashflowDetails.0.cashflowAmount", 1);

        // Query the collection and apply the projection
        List<Document> resultsRT = collection.find(new Document()).projection(projection).into(new ArrayList<>());
        List<Document> resultsCF = collectionCF.find(new Document()).projection(projectionCF).into(new ArrayList<>());

        // Process the results
        Map<String, List<Document>> formattedResults = new HashMap<>();
        formattedResults.put("RealTime",resultsRT);
        formattedResults.put("CashFlow",resultsCF);

        // Set the formatted results in the body of the exchange
        exchange.getIn().setBody(formattedResults);

        // Log the formatted results
        //formattedResults.forEach(doc -> log.info("Formatted Data: {}", doc.toJson()));
    }
}