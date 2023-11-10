package com.gunpang.heartserviceconsume.dto;


import lombok.Getter;

import java.time.LocalDateTime;
@Getter
public class ProcessedHeartbeat {

    private String playerId; // 심박 데이터를 보내는 사용자나 장치의 식별자
    private Double averageHeartbeat; // 평균 심박수
    private LocalDateTime firstHeartbeatAt; // 데이터가 처리된 시각

    public ProcessedHeartbeat(String playerId, double averageHeartbeat, LocalDateTime firstHeartbeatAt) {
        this.playerId = playerId;
        this.averageHeartbeat = averageHeartbeat;
        this.firstHeartbeatAt = firstHeartbeatAt;
    }
}
