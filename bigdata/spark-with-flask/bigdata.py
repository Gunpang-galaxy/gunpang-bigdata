from pyspark.sql import SparkSession
from pyspark.sql.types import StructType, StructField, StringType, DoubleType, ArrayType, IntegerType, BinaryType, TimestampType
from pyspark.sql.functions import col, to_timestamp, concat, expr, lit,from_json, lit, to_date
import pyspark.sql.functions as F  # F 별칭으로 functions 모듈 임포트
import pandas as pd

HIVE_USERNAME = "hive"
HIVE_PASSWORD = "1234"
EXAMPLE_PATH = 'hdfs://43.202.219.160:9000/user/hadoop/heartrate2'

def getHiveData(playerId, date, age, gender):
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
    
    __printData("[1] ONLY GET ", df,trunc=False)
    filtered_data = df.filter((df['playerId']==playerId)&(to_date(df['createdAt'])==lit(date)))
    __printData("[2] Filtered data", filtered_data)
    # # 최대 심박수 : 220 - 나이
    # MAX_HEARTBEAT = 220 - age
    # # 안정 심박수 : 남성 : 72, 여성 76
    # STABLE_HEARTBEAT = 72 if gender =='MALE' else 76
    
    # # 저강도 (65%이하)
    # LOW_INTENSITY = __calculateHeartbeat(MAX_HEARTBEAT, STABLE_HEARTBEAT, 65)
    # # 고강도 (85% 이상)
    # HIGH_INTENSITY = __calculateHeartbeat(MAX_HEARTBEAT, STABLE_HEARTBEAT, 85)
    # # 중강도  LOW_INTENSITY < < HIGH_INTENSITY
    # classify_intensity_udf = F.udf(__classifyIntensity, StringType())
    # filtered_new = filtered_data.withColumn("intensity", classify_intensity_udf(filtered_data['heartbeat']))    
    # # intensity_duration = filtered_new.groupBy("intensity").count()
    # __printData("강도 확인  ", filtered_new,False)
    pandas_df = filtered_data.toPandas()
    result = pandas_df.to_dict(orient="records")
    # __printData("요약  ", filtered_data,False)
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
    __printData("[1] ONLY GET ", df,trunc=False)
    spark.stop()
    result = df
    return result

def __printData(comment, df,trunc=True):
    print(comment+"STARTED!!!!!!!!!!!!!!!!==================")
    df.printSchema()
    df.show(3, truncate=trunc)
    print(comment+"PARSED !!!!!!!!!!!!!!!!==================")
