package com.gunpang.heartservice.config;

import com.gunpang.heartservice.dto.HeartRateAccumulator;
import com.gunpang.heartservice.dto.ProcessedHeartbeat;
import com.gunpang.heartservice.dto.Heartbeat;
import java.time.Duration;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
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
        KStream<String, Heartbeat> stream = streamsBuilder
            .stream("heartbeat-raw-topic", Consumed.with(Serdes.String(), new JsonSerde<>(Heartbeat.class)));

        KStream<String, ProcessedHeartbeat> averagedHeartRateStream = stream
            .groupByKey()
            .windowedBy(TimeWindows.of(Duration.ofMinutes(3)))// 3분마다 Aggregate
            .aggregate(
                HeartRateAccumulator::new, // Initializer
                (key, heartbeat, accumulator) -> {
                    //System.out.println("[RESULT] "+heartbeat);
                    accumulator.addHeartbeat(heartbeat);
                    //System.out.println(accumulator.getFirstHeartbeatAt()+" "+accumulator.getSum());
                    return accumulator;
                },
                Materialized.<String, HeartRateAccumulator, WindowStore<Bytes, byte[]>>as("heart-rate-sum-store")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(new JsonSerde<>(HeartRateAccumulator.class))
            )
            .toStream()
            .map((key, accumulator) -> {
                double averageHeartRate = accumulator.calculateAverage(); // 평균 심박수
                System.out.println("[RESULT: avgHeartRate] "+averageHeartRate);

                return new KeyValue<>(key.key(), new ProcessedHeartbeat(key.key(), averageHeartRate,accumulator.getFirstHeartbeatAt()));
            });
        Serde<ProcessedHeartbeat> processedHeartbeatSerde = Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(ProcessedHeartbeat.class));

        KStream<String, ProcessedHeartbeat> keyedStream = averagedHeartRateStream
            .selectKey((key, value) -> value.getPlayerId());

        keyedStream.to("heartbeat-processed-topic", Produced.with(Serdes.String(), processedHeartbeatSerde));

        System.out.println("여기!"+averagedHeartRateStream);
        return averagedHeartRateStream;
    }



}

