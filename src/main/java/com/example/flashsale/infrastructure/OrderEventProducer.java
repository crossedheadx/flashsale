package com.example.flashsale.infrastructure;

import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

@Service
public class OrderEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderEvent(Long productId) {
        kafkaTemplate.send("orders_topic", "Purchase completed: Item " + productId);
    }

}
