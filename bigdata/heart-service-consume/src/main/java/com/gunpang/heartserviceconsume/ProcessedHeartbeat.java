package com.gunpang.heartserviceconsume;

import com.gunpang.heartserviceconsume.dto.Heartbeat;
import java.util.List;

public class ProcessedHeartbeat {

    private String playerId; // 심박 데이터를 보내는 사용자나 장치의 식별자
    private Double averageHeartbeat; // 평균 심박수
    private String timestamp; // 데이터가 처리된 시각

    public ProcessedHeartbeat(String playerId, double averageHeartbeat, String timestamp) {
        this.playerId = playerId;
        this.averageHeartbeat = averageHeartbeat;
        this.timestamp = timestamp;
    }
    public static ProcessedHeartbeat calculateAverage(List<Heartbeat> heartbeats) {
        String playerId = "00";
        Double average= 0.0;
        String timestamp ="";

        return new ProcessedHeartbeat(playerId, average, timestamp);
    }
}
