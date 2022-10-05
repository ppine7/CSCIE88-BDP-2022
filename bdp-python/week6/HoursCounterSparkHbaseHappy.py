from pyspark.sql import SparkSession
from pyspark.sql.types import *
import collections
import happybase

ss = SparkSession.builder.appName("lab6").getOrCreate() # spark session

# Load avro files
df = ss.read.format("avro").load("s3://e88-data/file-input-avro")

def getDateHourUrl(file_line):
    datetime = file_line[1]
    datetime_hour = datetime[:10] + ' ' + datetime.split(':')[0][-2:]
    url = file_line[2]
    key = datetime_hour + ':' + url
    return key

# Get the unique url counts
counts_unique_urls = df.rdd\
    .map(getDateHourUrl)\
    .distinct()\
    .map(lambda key: (key.split(":")[0], 1))\
    .reduceByKey(lambda a, b: a + b).collect()

conn = happybase.Connection(host = "localhost",table_prefix="lab",table_prefix_separator = ":")
table = conn.table('date_hour')

for key, value in counts_unique_urls:
    value = str(value)
    table.put(key, {b'url:count':value})

ss.stop()

