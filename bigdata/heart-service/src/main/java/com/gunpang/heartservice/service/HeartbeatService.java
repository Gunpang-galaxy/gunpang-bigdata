package com.gunpang.heartservice.service;

import com.gunpang.heartservice.dto.Heartbeat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class HeartbeatService {

    private final KafkaTemplate<String, Heartbeat> kafkaTemplate;

    @Autowired
    public HeartbeatService(KafkaTemplate<String, Heartbeat> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendHeartbeat(Heartbeat heartbeat) {
        kafkaTemplate.send("heartbeat-topic", heartbeat);
    }
}