package cscie88;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.MultipleParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.util.Collector;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;

import java.time.Duration;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.functions.KeySelector;


public class KafkaSourceApp 
{
    /**
     * FlatMap function to convert a string to a LogLine instance using the 
     * LogLineParser class
     */
    public static final class LogTokenizer
        implements FlatMapFunction<String,LogLine> {
        @Override
        public void flatMap(String value, Collector<LogLine> out) {
            out.collect(LogLineParser.parseLine(value.replace(" ",",")));
        }
    }

    /**
     * FlatMap function to convert a LogLine instance to a tuple containing the log
     * UaOs value and numeric value 1 (used to sum) eg. ("windows",1)
     */
    public static final class UaOsTokenizer
        implements FlatMapFunction<LogLine,Tuple2<String,Integer>> {

        @Override
        public void flatMap(LogLine value, Collector<Tuple2<String,Integer>> out) throws Exception {
            out.collect(new Tuple2<String,Integer>(value.getUaOs(),1));
        }
    }

    /**
     * Flatmap function used to convert the tuple back to a string to send to 
     * the kafka sink
     */
    public static final class TupleSerializer
    implements MapFunction<Tuple2<String,Integer>,String> {
        @Override
        public String map(Tuple2<String, Integer> value) throws Exception {
            // System.out.println(""+value.toString());
            return "" + value.toString();
        }
    }

    /*
     * A KeySelector to select the UaOs value from the Tuple
     */
    public static final class TupleUaOsKeySelector
    implements KeySelector<Tuple2<String,Integer>,String> {
        @Override
        public String getKey(Tuple2<String,Integer> value) throws Exception {
            // return the first value in the tuple, so if ("windows",1) returns "window"
            return value.getField(0);
        }
    }

    public static void main( String[] args ) throws Exception
    {
        final MultipleParameterTool params = MultipleParameterTool.fromArgs(args);

        // uncomment the following line if executing in a flink cluster
        /**
         * final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
         */
        
        // comment the following 3 lines if running in a flink cluster, uncomment if 
        // running locally or from an ide
        Configuration conf = new Configuration();
        // set the web user interface to 8082 so it doesnt clash with the default 8081
        conf.setString("rest.port", "8082");
        // final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        // web user interface will be available on localhost:8082
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);

        // kafka source, specify the broker host and port and the topic to read from
        // note if running as job in flink host should be broker1:29092
        // if running localy then it is localhost:9092
        KafkaSource<String> source = KafkaSource.<String>builder()
            .setBootstrapServers("localhost:9092")
            .setTopics("p3_input")
            .setGroupId("test")
            .setStartingOffsets(OffsetsInitializer.latest())
            // .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new SimpleStringSchema())
            .build();

        // kafka sink, specify the broker host and port and the topic to write to
        KafkaSink<String> sink = KafkaSink.<String>builder()
            .setBootstrapServers("localhost:9092")
            .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                .setTopic("p3_output")
                .setValueSerializationSchema(new SimpleStringSchema())
                .build()
            )
            .build();

        // set job parallelism
        env.setParallelism(1);

        System.out.println("starting stream");

        // create the stream from the kafka source
        DataStream<String> stream = env.fromSource(
            source, 
            WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds((1))), 
            "kafka-source");

        // for testing you can also create a stream from an array of elements
        // final DataStream<String> stream = env.fromElements(logs);

        // Problem 3:  (Points: TBD) Flink Streaming job
        // Configure your Flink Streaming job to read data from the "hw9events" Kafka topic
        // Implement a job that does a "windowed" count of events - per 5 second windows, use tumbling windows:
        // counts the number of clicks per URL per window and prints the results after each window

        stream
            // convert log string to LogLine
            .flatMap(new LogTokenizer())
            // convert LogLine to Tuple<uaos,1>
            .flatMap(new UaOsTokenizer())
            // key by UaOS
            .keyBy(new TupleUaOsKeySelector())
            // window
            .window(TumblingEventTimeWindows.of(Time.seconds(5)))
            // sum the contents of window
            .sum(1)
            // and map results to a tuple
            .map(new TupleSerializer())
            // name it for job output
            .name("kafka-sink")
            // .addSink(new PrintSinkFunction<String>());
            .sinkTo(sink);
        
        // sink to print output to stdout
        stream.addSink(new PrintSinkFunction<String>());
        
        env.execute("hw9events-kafka-source");
    }
}
