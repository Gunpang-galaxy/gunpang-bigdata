package com.gunpang.heartservice.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class Heartbeat {
    private Long playerId;
    private Double heartbeat;
    private LocalDateTime createdAt;

    public Heartbeat(Long playerId, Double heartbeat,LocalDateTime createdAt) {
        this.playerId = playerId;
        this.heartbeat = heartbeat;
        this.createdAt = createdAt;
    }
}
