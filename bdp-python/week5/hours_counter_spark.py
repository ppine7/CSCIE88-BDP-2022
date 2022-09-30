from pyspark import SparkContext, SparkConf
from pyspark.sql import SparkSession

ss = SparkSession.builder.appName("HoursCounterSparkJobAvroPython").getOrCreate()

# Read avro files
df = ss.read.format("avro").load("file-input-avro")

# Get the counts
counts = df.rdd\
    .map(lambda s: s[1])\
    .map(lambda hour: (hour[0:13], 1))\
    .reduceByKey(lambda a, b: a + b)

print(counts.toDebugString().decode("utf-8"))

counts.saveAsTextFile("outputlab5p")
