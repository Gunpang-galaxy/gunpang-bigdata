package com.gunpang.heartserviceconsume.config;

import com.gunpang.heartserviceconsume.dto.ProcessedHeartbeat;
import com.gunpang.heartserviceconsume.dto.Heartbeat;
import java.time.Duration;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@Component
public class KafkaStreamsConfig {
    //심박수 3분 간격으로 시간 집계
    @Bean
    public KStream<String, ProcessedHeartbeat> kStream(StreamsBuilder streamsBuilder) {
        KStream<String, Heartbeat> stream = streamsBuilder.stream("heartbeat-raw-topic");

        KStream<String, ProcessedHeartbeat> averagedHeartRateStream = stream
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofMinutes(3)))// 3분마다 Aggregate
                .aggregate(
                        HeartRateAccumulator::new, // Initializer
                        (key, heartbeat, accumulator) -> {
                            accumulator.addHeartbeat(heartbeat);
                            return accumulator;
                        },
                        Materialized.as("heart-rate-sum-store")
                )
                .toStream()
                .map((key, accumulator) -> {
                    double averageHeartRate = accumulator.calculateAverage(); // 평균 심박수
                    return new KeyValue<>(key.key(), new ProcessedHeartbeat(key.key(), averageHeartRate,accumulator.firstHeartbeatAt));
                });
        Serde<ProcessedHeartbeat> processedHeartbeatSerde = Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(ProcessedHeartbeat.class));

        KStream<String, ProcessedHeartbeat> keyedStream = averagedHeartRateStream
                .selectKey((key, value) -> value.getPlayerId());

        keyedStream.to("heartbeat-processed-topic", Produced.with(Serdes.String(), processedHeartbeatSerde));
        return averagedHeartRateStream;
    }

    @Getter
    @AllArgsConstructor
    private class HeartRateAccumulator {
        static double sum = 0;
        static long count = 0;
        static LocalDateTime firstHeartbeatAt = null; // 윈도우 내 첫 번째 심박수 데이터의 생성 시간

        double calculateAverage() { // 평균 심박수 계산
            return count > 0 ? sum / count : 0;
        }
        void addHeartbeat(Heartbeat heartbeat) { // heartbeat 누적
            if (firstHeartbeatAt == null || heartbeat.getCreatedAt().isBefore(firstHeartbeatAt)) {
                firstHeartbeatAt = heartbeat.getCreatedAt();
            }
            sum += heartbeat.getHeartbeat();
            count++;
        }
    }

}

