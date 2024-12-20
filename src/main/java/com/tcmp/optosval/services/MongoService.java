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
import java.util.List;

@Service
@Slf4j
public class MongoService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void printRealtimeData(Exchange exchange) {
        // Get the MongoDB database
        MongoDatabase database = mongoTemplate.getDb();
        MongoCollection<Document> collection = database.getCollection("Realtime");

        // Create the projection to fetch only the required fields
        Document projection = new Document("TradeMessage.trade.tradeHeader.tradeIdentifiers.tradeId.id", 1)
                .append("CashflowMessage.cashflowDetails.actualCashflowDate", 1)
                .append("TradeMessage.trade.parties.counterparty.partyName", 1)
                .append("TradeMessage.trade.product_FXOption.underlyingCurrencyCode", 1)
                .append("TradeMessage.trade.product_FXOption.underlyingAmount", 1)
                .append("TradeMessage.trade.product_FXOption.baseCurrency", 1)
                .append("TradeMessage.trade.product_FXOption.baseAmount", 1)
                .append("CashflowMessage.cashflowDetails.cashflowCurrencyCode", 1)
                .append("CashflowMessage.cashflowDetails.cashflowAmount", 1)
                .append("TradeMessage.trade.product_FXOption.underlyingInstrumentName", 1)
                .append("CashflowMessage.cashflowDetails.cashflowCurrencyCode", 1)
                .append("_id", 0); // Exclude the _id field

        // Query the collection and apply the projection
        List<Document> results = collection.find(new Document()).projection(projection).into(new ArrayList<>());

        // Process the results
        List<Document> formattedResults = new ArrayList<>();
        for (Document doc : results) {
            // Create a new field combining underlyingInstrumentName and cashflowCurrencyCode
            String combinedField = "dummy" + "/" +
                    "string";
            doc.append("combinedField", combinedField);

            // Add the processed document to the results
            formattedResults.add(doc);
        }

        // Log the formatted results
        //formattedResults.forEach(doc -> log.info("Formatted Data: {}", doc.toJson()));

        // Set the formatted results in the body of the exchange
        exchange.getIn().setBody(formattedResults);
    }
}