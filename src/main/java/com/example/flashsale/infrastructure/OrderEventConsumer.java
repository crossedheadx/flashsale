package com.example.flashsale.infrastructure;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {

    @KafkaListener(topics = "orders_topic", groupId = "flashsale-group", properties = { "auto.offset.reset=earliest" })
    public void consume(String message) {
        System.out.println("New message from broker: " + message);

        // add here some logic to manage message consumption, but this is the base
    }
}
