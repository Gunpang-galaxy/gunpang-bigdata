# heart-service-consume 프로젝트
Kafka Topic(heartbeat-raw-topic)에서   
Apache Spark Streaming 을 사용해 데이터를 Consume해서  
데이터를 처리하고 3분마다 평균 심박수 구하는 통계작업 (heartbeat-processed-topic)에 다시 reproduce

## 이후의 작업
Spark에서 heartbeat-processed-topic로 부터 데이터 읽어다가 HDFS에 적재하는 작업을 수행한다.