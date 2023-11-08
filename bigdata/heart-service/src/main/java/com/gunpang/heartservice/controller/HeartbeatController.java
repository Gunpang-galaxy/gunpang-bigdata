package com.gunpang.heartservice.controller;

import com.gunpang.heartservice.dto.Heartbeat;
import com.gunpang.heartservice.service.HeartbeatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

// 클라이언트 -> 백엔드 : /watch/heartbeat
// 서버 -> 클라이언트 : /topic/heartbeat
@Controller
@Slf4j
public class HeartbeatController {

   private final HeartbeatService heartbeatService;

    public HeartbeatController(HeartbeatService heartbeatService) {
        this.heartbeatService = heartbeatService;
    }

    @MessageMapping("/heartbeat") // /watch/heartbeat
    @SendTo("/topic/heartbeat") // 단순한 퍼블리시-서브스크라이브(pub-sub) 패턴을 구현하는 데 사용
    public Heartbeat topicHeartbeatSave(Heartbeat message) throws Exception {
        log.debug("[HEARTBEAT] "+message.toString());
        Thread.sleep(1000); // simulated delay
        heartbeatService.sendHeartbeat(message);
        return message;
    }


}
