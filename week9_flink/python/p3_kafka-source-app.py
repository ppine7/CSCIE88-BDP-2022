import json
import os

from pyflink.common import SimpleStringSchema
from pyflink.datastream import StreamExecutionEnvironment
from pyflink.datastream.connectors import FlinkKafkaConsumer
from pyflink.datastream.connectors import FlinkKafkaProducer
from pyflink.common.typeinfo import Types
from pyflink.datastream import FlatMapFunction

from pyflink.common import Types
from pyflink.datastream import StreamExecutionEnvironment
from pyflink.table import (DataTypes, TableDescriptor, Schema, StreamTableEnvironment)
from pyflink.table.udf import udf


from pyflink.datastream.window import TimeWindow

class MyFlatMapFunction(FlatMapFunction):
    def flat_map(self, value):
        yield value.upper()

env = StreamExecutionEnvironment.get_execution_environment()
t_env = StreamTableEnvironment.create(stream_execution_environment=env)

# see https://nightlies.apache.org/flink/flink-docs-release-1.12/dev/python/datastream-api-users-guide/intro_to_datastream_api.html
# https://blog.devgenius.io/playing-pyflink-in-a-nutshell-3abd16467677
# flink-sql-connector-kafka_2.12-1.14.0.jar
env.add_jars("file:///opt/flink/lib/flink-connector-kafka_2.12-1.14.0.jar")

deserialization_schema = SimpleStringSchema()

kafkaSource = FlinkKafkaConsumer(
    topics='hw9events',
    deserialization_schema=deserialization_schema,
    properties={'bootstrap.servers': 'broker1:29092', 'group.id': 'test'}
)

serialization_schema = SimpleStringSchema()

kafkaSink = FlinkKafkaProducer(
    topic='hw9results',
    serialization_schema=serialization_schema,
    producer_config={'bootstrap.servers': 'broker1:29092', 'group.id': 'test'}
)

ds = env.add_source(kafkaSource) \
    .key_by(lambda a: a)
    # .add_sink(kafkaSink)

print("creating table")
table = t_env.from_data_stream( \
    ds, \
    # Schema.new_builder().column("f0", DataTypes.BIGINT()).build())
    Schema.new_builder().column("f0", DataTypes.CHAR(255)).build())

ds = t_env.to_data_stream(table) \
.flat_map(MyFlatMapFunction(),Types.STRING())

ds.add_sink(kafkaSink)

    # .flat_map(MyFlatMapFunction(),Types.STRING()) \
    # .window_all(TumblingProcessingTimeWindows.of(Time.seconds(5)) \

# watermark_strategy = WatermarkStrategy.for_bounded_out_of_orderness(Duration.of_seconds(5))\
# .window_all(TumblingProcessingTimeWindows.of(Time.seconds(5)))

#     .with_timestamp_assigner(KafkaRowTimestampAssigner())

#  window_all(window_assigner: pyflink.datastream.window.WindowAssigner) 

print("starting kafkaread")
env.execute('kafkaread')


print("hello!")