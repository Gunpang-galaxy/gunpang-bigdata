package com.gunpang.heartserviceconsume;

import com.gunpang.heartserviceconsume.dto.Heartbeat;
import java.time.Duration;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@Component
public class HeartbeatKafkaStreams {
    final int HEART_RATE_THRESHOLD = 100;
    //심박수가 임계값 이상인 데이터만 필터링, 3분 간격으로 시간 집계
    @Bean
    public KStream<String, Heartbeat> kStream(StreamsBuilder streamsBuilder) {
        KStream<String, Heartbeat> stream = streamsBuilder.stream("heartbeat-raw-topic");

        KStream<String, Heartbeat> validExerciseStream = stream
            .filter((key, value) -> value.getHeartbeat() >= HEART_RATE_THRESHOLD)
            .groupByKey()
            .windowedBy(TimeWindows.of(Duration.ofMinutes(3)))
            .aggregate(
                () -> 0L,
                (key, value, aggregate) -> aggregate + 1, // 유효한 심박수 데이터마다 카운트 증가
                Materialized.as("exercise-time-store")
            )
            .toStream()
            .map((key, value) -> new KeyValue<>(key.key(), new ProcessedHeartbeat(key.key(), value, key.window().end())))
            .to("heartbeat-processed-topic");

        return stream;
    }
}