from pyspark.sql import SparkSession
from pyspark.sql.types import StructType, StructField, StringType, DoubleType, ArrayType, IntegerType, BinaryType, TimestampType
from pyspark.sql.functions import col, to_timestamp, concat, expr, lit,from_json
EXAMPLE_PATH = 'hdfs://43.202.219.160:9000/user/hadoop/heartbeat'

def getData():
    spark = SparkSession.builder.appName("gunpangSpark").getOrCreate()
    spark.conf.set("spark.sql.files.ignoreCorruptFiles", "true") # 손상된 파일 읽지 않기

    sc = spark.sparkContext
    #sc.setLogLevel("DEBUG")
    json_schema = StructType([
        StructField("playerId", StringType(), True),
        StructField("heartbeat", DoubleType(), True),
        StructField("createdAt", ArrayType(IntegerType()), True)
    ])
    df = spark.read.parquet(EXAMPLE_PATH)
    printData("[1] ONLY GET ", df)

    df_with_string_key = df.withColumn("key_string", col("key").cast(StringType())).withColumn("value_string",col("value").cast(StringType()))
  
    printData("[2] value->value_string ", df_with_string_key)

    # Binary 형식의 value를 JSON으로 변환
    df_with_json = df_with_string_key.withColumn("value_json", from_json(col("value_string"), json_schema))
    df_final = df_with_json.withColumn("createdAt", to_timestamp(
        concat(
            expr("value_json.createdAt[0]"), lit("-"),
            expr("value_json.createdAt[1]"), lit("-"),
            expr("value_json.createdAt[2]"), lit(" "),
            expr("value_json.createdAt[3]"), lit(":"),
            expr("value_json.createdAt[4]"), lit(":"),
            expr("value_json.createdAt[5]"), lit("."),
            expr("value_json.createdAt[6]")
    )
))

    df_parsed = df_final.select(
        col("value_json.playerId"),
        col("value_json.heartbeat"),
        col("createdAt"),
    )
    printData("[3] value_json->파싱  ", df_parsed,False)
    spark.stop()

def printData(comment, df,trunc=True):
    print(comment+"STARTED!!!!!!!!!!!!!!!!==================")
    df.printSchema()
    df.show(3, truncate=trunc)
    print(comment+"PARSED !!!!!!!!!!!!!!!!==================")
