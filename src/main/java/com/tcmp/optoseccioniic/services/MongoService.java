package com.tcmp.optoseccioniic.services;

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
        MongoCollection<Document> collection = database.getCollection("RealtimeMexico");
        MongoCollection<Document> collectionCF = database.getCollection("Cashflow");

        // Create the projection to fetch only the required fields
        Document projection = new Document(
                "TradeMessage.trade.tradeHeader.tradeDate", 1) // FECHA
                .append("TradeMessage.trade.tradeHeader.tradeIdentifiers.tradeId.id", 1) // NU_ID
                .append("TradeMessage.trade.tradeHeader.tradeDate",1) // FEINOP_CO
                .append("TradeMessage.trade.product.exerciseStyle.expiryDate",1) // FEVEOP_CO
                .append("TradeMessage.trade.product.underlyingInstrumentName", 1) // CVE_TIT_C // TODO ANEXO AF
                .append("TradeMessage.trade.product.strikeRate", 1) // PRECIOEJER_C
                .append("TradeMessage.trade.product.barrierFeature.barrierUpRate", 1) // PRE_SUP
                .append("TradeMessage.trade.product.barrierFeature.barrierDownRate", 1) // PRE_INF
                .append("TradeMessage.trade.parties.counterparty.partyLei", 1) // INST_LEI
                .append("TradeMessage.trade.tradeHeader.tradeIdentifiers.uniqueTransactionId", 1) // UTI

                // Campos GBM Faltantes
                // TODO NU_PE_EJE Pending to create
                // TODO SUBY_CO CATALOGO ANEXO F
                // TODO IDENTIFICADOR VanillaOption de SECCIONII se deja vacío
                
        .append("_id", 0);  // Exclude the _id field

        Document projectionCF = new Document(
                "CashflowMessage.cashflowDetails.cashflowAmount", 1) // IMPBA_CO
                .append("_id", 0);  // Exclude the _id field

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