package com.gunpang.heartservice.service;

import com.gunpang.heartservice.dto.Heartbeat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HeartbeatService {
    private static final String TOPIC = "heartbeat-topic";

    private final KafkaTemplate<String, Heartbeat> kafkaTemplate;

    @Autowired
    public HeartbeatService(KafkaTemplate<String, Heartbeat> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendHeartbeat(Heartbeat heartbeat) {
        log.debug("HEARTBEAT\t"+heartbeat.getPlayerId()+" "+heartbeat.getHeartbeat());
        kafkaTemplate.send(TOPIC, heartbeat);
    }
}