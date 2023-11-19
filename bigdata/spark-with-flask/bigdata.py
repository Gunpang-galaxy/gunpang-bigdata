from pyspark.sql import SparkSession
from pyspark.sql.types import StructType, StructField, StringType, DoubleType, ArrayType, IntegerType, BinaryType, TimestampType
from pyspark.sql.functions import col, to_timestamp, concat, expr, lit,from_json, lit
EXAMPLE_PATH = 'hdfs://43.202.219.160:9000/user/hadoop/heartrate'

def getHiveData(playerId, date, age, gender):
    spark = SparkSession.builder.appName("gunpangSpark").enableHiveSupport().getOrCreate()
    hive_table = spark.table("heartrate")
    filtered_data = hive_table.filter((hive_table['playerId']==playerId)&(to_date(hive_table['createdAt'])==lit(date)))

    # 최대 심박수 : 220 - 나이
    MAX_HEARTBEAT = 220 - age
    # 안정 심박수 : 남성 : 72, 여성 76
    STABLE_HEARTBEAT = 72 if gender =='MALE' else 76
    
    # 저강도 (65%이하)
    LOW_INTENSITY = __calculateHeartbeat(MAX_HEARTBEAT, STABLE_HEARTBEAT, 65)
    # 고강도 (85% 이상)
    HIGH_INTENSITY = __calculateHeartbeat(MAX_HEARTBEAT, STABLE_HEARTBEAT, 85)
    # 중강도  LOW_INTENSITY < < HIGH_INTENSITY
    classify_intensity_udf = F.udf(classify_intensity, StringType())
    filtered_data = filtered_data.withColumn("intensity", classify_intensity_udf(filtered_data['heartbeat']))
    intensity_duration = filtered_data.groupBy("intensity").count()

    __printData("강도 확인  ", intensity_duration,False)
    result = intensity_duration
    spark.stop()
    return result

def __classifyIntensity(heartbeat):
    if heartbeat <= LOW_INTENSITY:
        return 'LOW'
    elif heartbeat >= HIGH_INTENSITY:
        return 'HIGH'
    else:
        return 'MEDIUMN'
def __calculateHeartbeat(MAX_HEARTBEAT, STABLE_HEARTBEAT, percent):
    # 계산식 : (최대 심박수 - 안정 심박수) * 운동강도(%) + 안정 심박수
    return (MAX_HEARTBEAT- STABLE_HEARTBEAT) * percent / 100  + STABLE_HEARTBEAT

def getParquetData():
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
    __printData("[1] ONLY GET ", df)

    df_with_string_key = df.withColumn("key_string", col("key").cast(StringType())).withColumn("value_string",col("value").cast(StringType()))
  
    __printData("[2] value->value_string ", df_with_string_key)

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
    __printData("[3] value_json->파싱  ", df_parsed,False)

    
    spark.stop()

    return result

def __printData(comment, df,trunc=True):
    print(comment+"STARTED!!!!!!!!!!!!!!!!==================")
    df.printSchema()
    df.show(3, truncate=trunc)
    print(comment+"PARSED !!!!!!!!!!!!!!!!==================")
