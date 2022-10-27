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
    topics='p2_input',
    deserialization_schema=deserialization_schema,
    properties={'bootstrap.servers': 'broker1:29092', 'group.id': 'test'}
)

serialization_schema = SimpleStringSchema()

kafkaSink = FlinkKafkaProducer(
    topic='p2_output',
    serialization_schema=serialization_schema,
    producer_config={'bootstrap.servers': 'broker1:29092', 'group.id': 'test'}
)

ds = env.add_source(kafkaSource) \
    .key_by(lambda a: a) \
    .flat_map(MyFlatMapFunction(),Types.STRING()) \
    .add_sink(kafkaSink)

env.execute('kafkaread')
