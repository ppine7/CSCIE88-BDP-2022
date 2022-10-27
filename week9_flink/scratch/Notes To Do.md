

Flink To Do 
- Finalize and test 2 docker files
-- one for flink (java)
-- one for flink (python) - custom image, instructions, push to docker repo

- Instructions for running the
-- java examples
-- python examples

- Demo running the java example in eclipse. 

- Demo creating EMR cluster 
- Demo running demo in EMR cluster
- Does emr support python ? 

- DataStream api, explore examples eg. windowing

- kafka connector

- Connectivity to kafka emr.

- test with latest ami

- configure parallelism in flink job. 

- rename java proj to hw9 etc.

- make sure it works with log format. 

- use hw8 example to produce logs.

- read from kafka logs, 


# running java in docker
docker run --network=lab9-demo-net -v ./java-producer:/java-producer --rm -it openjdk:11-jdk bash

docker run --network=lab9-demo-net -v $PWD/java-producer:/java-producer --rm -it wrackzone/java-producer:11-jdk bash

java -classpath ./target/hw9-java-producer-1.0-SNAPSHOT.jar -DinputFile=./logs/file-input1.csv -Dserver=broker1:29092 -Dtopic=hw9events -Dsec=3000 org.cscie88.kafka.SimpleProducer




# test kafka broker connection from within docker network
docker run --rm --network=lab9-demo-net subfuzion/netcat -vz broker1 9092

docker tag pyflink:1.14.0 wrackzone/pyflink:1.14.0 

docker cp target/quickstart-0.1.jar hw9_taskmanager_1:/opt/flink/quickstart-0.1.jar

./bin/flink run --detached ./quickstart-0.1.jar --input file:///opt/flink/ulysses.txt --output file:///opt/flink/results.txt

./bin/flink run --detached /flink-data/quickstart-0.1.jar --input file:///flink-data/ulysses.txt --output file:///flink-data/results2



./bin/flink run --detached /flink-data/quickstart-0.1.jar --input file:///flink-data/ulysses.txt --output file:///flink-data/results.txt

./bin/flink run -p 2 --detached /flink-data/quickstart-0.1.jar --input file:///flink-data/ulysses.txt --output file:///flink-data/results.txt

./bin/flink run -p 4 --detached /flink-data/quickstart-0.1.jar --input file:///flink-data/ulysses.txt --output file:///flink-data/results.txt




./bin/flink run --detached -py examples/python/datastream/word_count.py --input file:///flink-data/ulysses.txt --output file:///flink-data/results

./bin/flink run --detached -py examples/python/datastream/word_count.py --input file:///flink-data/ulysses.txt --output file:///flink-data/results-python

./bin/flink run -p 5 --detached -py examples/python/datastream/word_count.py --input file:///flink-data/ulysses.txt --output file:///flink-data/results-python


apt-get install nano


pyflink

https://wicaksonodiaz.medium.com/setup-pyflink-development-environment-76d8491a9ad7

wget https://files.pythonhosted.org/packages/33/6b/5f173570d61b164a41b30f56df7d16cd7c055809d244a236d3f6787c153e/apache-flink-1.14.0.tar.gz 

wget https://files.pythonhosted.org/packages/3d/a3/685916e56b1e5ebdb3fe0ce6692c0e17a300bddf5cfa8ab7957289714aac/apache-flink-libraries-1.14.0.tar.gz



process output

echo "(spoils,2)" |  awk -F "," '{gsub(/\(/,"",$1); gsub(/\)/,"",$2); print $1 $2}' ;


cat flink-data/results/2022-10-22--22/*.ext | awk -F "," '{gsub(/\(/,"",$1); gsub(/\)/,"",$2); print $1 " " $2}' | sort -k 2n | tail -n 10



# kafka create topic
bin/kafka-topics.sh --create --topic java_test_topic --bootstrap-server localhost:9092


# kafka producer

java -classpath ./hw8-1.0-SNAPSHOT.jar -DinputFile=./file-input1.csv -Dserver=hw9_kafka_1:9092 -Dtopic=java_test_topic -Dsec=3000 org.cscie88.kafka.SimpleProducer

java -classpath ./hw8/target/hw8-1.0-SNAPSHOT.jar -DinputFile=./hw8/file-input1.csv -Dserver=hw9_kafka_1:9092 -Dtopic=java_test_topic -Dsec=3000 org.cscie88.kafka.SimpleProducer

java -classpath ./target/hw9-java-producer-1.0-SNAPSHOT.jar -DinputFile=./logs/file-input1.csv -Dserver=broker1:9092 -Dtopic=hw9events -Dsec=3000 org.cscie88.kafka.SimpleProducer

# working
java -classpath ./target/hw9-java-producer-1.0-SNAPSHOT.jar -DinputFile=./logs/file-input1.csv -Dserver=broker1:29092 -Dtopic=hw9events -Dsec=3000 org.cscie88.kafka.SimpleProducer

# test kafka consumer
docker exec broker1 kafka-console-consumer \
   --topic hw9events \
   --bootstrap-server broker1:9092
   --from-beginning

# creating a sample project
mvn archetype:generate -DgroupId=cscie88 -DartifactId=hw9kafkaconsumer -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false

/Users/bmullan/Documents/Apps/cscie88/hw9/kafka-consumer/hw9kafkaconsumer/target/hw9kafkaconsumer-1.0-SNAPSHOT.jar

# kafka source
./bin/flink run --detached /flink-data/hw9kafkaconsumer-1.0-SNAPSHOT.jar 

./bin/flink run --detached -c cscie88.KafkaSourceApp /flink-data/hw9kafkaconsumer-1.0-SNAPSHOT.jar --output file:///flink-data/output


# flink use cases
https://flink.apache.org/usecases.html

flink docker
https://nightlies.apache.org/flink/flink-docs-master/docs/deployment/resource-providers/standalone/docker/

flink kafka
https://nightlies.apache.org/flink/flink-docs-release-1.15/docs/connectors/table/kafka/

bin/sql-client.sh
add jar 'opt/flink-sql-connector-kafka-1.15.2.jar';
flink-sql-connector-kafka-1.15.2.jar

DROP TABLE KafkaLogTable;

CREATE TABLE KafkaLogTable (
  `log` STRING
) WITH (
  'connector' = 'kafka',
  'topic' = 'java_test_topic',
  'properties.bootstrap.servers' = 'hw9_kafka_1:9092',
  'scan.startup.mode' = 'earliest-offset',
  'properties.group.id' = 'testGroup',
  'format' = 'raw'
)

CREATE TABLE KafkaLogTable (
  line STRING
)
WITH (
  'connector' = 'kafka',
  'property-version' = 'universal',
  'properties.bootstrap.servers' = 'hw9_kafka_1:9092',
  'topic' = 'java_test_topic',
  'scan.startup.mode' = 'earliest-offset',
  'value.format' = 'raw',
  'properties.security.protocol' = 'PLAINTEXT',
  'properties.group.id' = 'my-working-group'
);

a4156ae1faf74acc9e0706e9c53f0d29,
2022-01-25T00:02:09.600Z,
http://example.com/?url=104,
user-054,
ZW,
Edge,
Android,
208,
0.7311

CREATE TABLE KafkaLogTable (
  `uuid` STRING,
	`eventDateTime` STRING,
	`url` STRING,
	`userId` STRING,
	`country` STRING,
	`uaBrowser` STRING,
	`uaOs` STRING,
	`responseCode` BIGINT,
	`ttfb` FLOAT
)
WITH (
  'connector' = 'kafka',
  'property-version' = 'universal',
  'properties.bootstrap.servers' = 'hw9_kafka_1:9092',
  'topic' = 'java_test_topic',
  'scan.startup.mode' = 'earliest-offset',
  'value.format' = 'json',
  'properties.security.protocol' = 'PLAINTEXT',
  'properties.group.id' = 'my-working-group'
);



package cscie88;

import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.MultipleParameterTool;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.connectors.kafka.*;
import org.apache.flink.util.Collector;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.BasePathBucketAssigner;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.windowing.windows.Window;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;

/**
 * Hello world!
 *
 */
public class KafkaSourceApp 
{

    public static void main( String[] args ) throws Exception
    {
        final MultipleParameterTool params = MultipleParameterTool.fromArgs(args);

        // set up the execution environment
        // final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // make parameters available in the web interface
        env.getConfig().setGlobalJobParameters(params);

        KafkaSource<String> source = KafkaSource.<String>builder()
            .setBootstrapServers("broker1:29092")
            .setTopics("hw9events")
            .setGroupId("my-group")
            .setStartingOffsets(OffsetsInitializer.latest())
            // .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new SimpleStringSchema())
            .build();

        KafkaSink<String> sink = KafkaSink.<String>builder()
            .setBootstrapServers("broker1:29092")
            .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                .setTopic("hw9results")
                .setValueSerializationSchema(new SimpleStringSchema())
                .build()
            )
            // .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
            .build();

        // StreamingFileSink<String> streamingFileSink = StreamingFileSink.forRowFormat(
        //     new Path(params.get("output")), new SimpleStringEncoder<String>())
        //         .withBucketAssigner(new BasePathBucketAssigner<>())
        //         .build();

        DataStream<String> stream = env.fromSource(source, WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds((5))), "Kafka Source");

        stream.flatMap(new Tokenizer())
        .windowAll(TumblingEventTimeWindows.of(Time.seconds(5)))
        // .reduce(null)
        .apply(new AllWindowFunction<String, String, TimeWindow>() {
            @Override
            public void apply(TimeWindow tw,
                              Iterable<String> values,
                              Collector<String> out) throws Exception {
                int sum = 0;
                for (String c : values) {
                    sum++;
                }
                out.collect("Count:"+sum);
            }
        }).sinkTo(sink);

        // .keyBy(value -> value.f0)
		// .sum(1)
        // .windowAll()
        env.execute("hw9events-kafka-source");
    }


    /**
     * Implements the string tokenizer that splits sentences into words as a user-defined
     * FlatMapFunction. The function takes a line (String) and splits it into multiple pairs in the
     * form of "(word,1)" ({@code Tuple2<String, Integer>}).
     */
    public static final class Tokenizer
            implements FlatMapFunction<String, String> {

        @Override
        // public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
        public void flatMap(String value, Collector<String> out) {
            // out.collect(new Tuple2<>(value,1));
            out.collect(value);
        }
    }
}
