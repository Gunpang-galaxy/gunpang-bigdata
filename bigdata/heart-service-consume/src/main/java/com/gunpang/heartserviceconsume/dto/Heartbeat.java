package com.gunpang.heartservice.dto;

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

    public Heartbeat(Long playerId, Double heartbeat) {
        this.playerId = playerId;
        this.heartbeat = heartbeat;
    }
}
