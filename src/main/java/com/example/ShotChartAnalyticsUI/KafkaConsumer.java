package com.example.ShotChartAnalyticsUI;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {
    private final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);
    @Autowired
    AnalyticsController analyticsController;

    @KafkaListener(topics = {"ShotChartSimpleSearches", "ShotChartAdvancedSearches"}, groupId = "UIConsumerGroup1")
    public void listen(String message) {
        LOGGER.info("\nReceived Message: " + message);
        final Search newSearch = AnalyticsCalculator.processNewSearchFromKafka(message);
        Platform.runLater(()->{
            analyticsController.addRowToTable(newSearch);
            analyticsController.updateStatistics();
        });
    }
}

