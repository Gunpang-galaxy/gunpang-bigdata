package com.gunpang.heartservice.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class HeartRateAccumulator {
    private double sum;
    private long count = 1;
    private LocalDateTime firstHeartbeatAt; // 윈도우 내 첫 번째 심박수 데이터의 생성 시간


    public double calculateAverage() { // 평균 심박수 계산
        //System.out.println("COUNT: "+count);
        //System.out.println("SUM: "+sum);
        return count > 0 ? sum / count : 0;
    }

    public void addHeartbeat(Heartbeat heartbeat) { // heartbeat 누적
        if (firstHeartbeatAt == null || heartbeat.getCreatedAt().isBefore(firstHeartbeatAt)) {
            //System.out.println("ADDHEARTBEAT"+heartbeat.getHeartbeat());
            firstHeartbeatAt = heartbeat.getCreatedAt();
        }
        sum += heartbeat.getHeartbeat();
        count++;
        System.out.println("누적?"+sum+" "+count+" "+firstHeartbeatAt);
    }
}
