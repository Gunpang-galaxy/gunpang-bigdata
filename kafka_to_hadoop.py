from pyspark.sql import SparkSession, functions as F

# spark session 생성
sc = SparkSession.builder \
    .appName("gunapang") \
    .enableHiveSupport() \
    .getOrCreate()
#sc.sparkContext.setLogLevel('ERROR')

# kafka 서버와 토픽 설정
kafka_params = {
    "kafka.bootstrap.servers": "http://43.202.219.160:9092",
    "subscribe": "heartbeat-raw-topic",
    "startingOffsets": "earliest"
}

# kafka에서 데이터를 읽어 DataFrame 생성
df = sc.readStream.format("kafka") \
    .options(**kafka_params) \
    .load()

# Kafka에서 읽은 데이터를 적절한 스키마로 변환
df = df.selectExpr("CAST(value AS STRING) as json")
df = df.select(F.from_json(df.json, "playerId STRING, heartbeat DOUBLE, createdAt ARRAY<INT>").alias("data"))
df = df.select("data.*")
df = df.withColumn("createdAt", F.to_timestamp(
        F.concat(
           F.expr("createdAt[0]"), F.lit("-"),
           F.expr("createdAt[1]"), F.lit("-"),
           F.expr("createdAt[2]"), F.lit(" "),
           F.expr("createdAt[3]"), F.lit(":"),
           F.expr("createdAt[4]"), F.lit(":"),
           F.expr("createdAt[5]"), F.lit("."),
           F.expr("createdAt[6]")
    )
))

# Hadoop에 쓰기 위한 경로 설정
path = "hdfs://43.202.219.160:9000/user/hadoop/heartrate2"
checkpointLocation = "/home/ubuntu/spark/checkpoint4"

# DataFrame을 Hadoop에 쓰기
query = df.writeStream \
    .format("parquet") \
    .option("path", path) \
    .option("checkpointLocation", checkpointLocation) \
    .start()

query.awaitTermination()
