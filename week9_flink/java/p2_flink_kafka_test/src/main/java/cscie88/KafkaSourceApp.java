package cscie88;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.MultipleParameterTool;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.util.Collector;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;

/**
 * Hello world!
 */
public class KafkaSourceApp 
{
    /**
     * Implements the string tokenizer that splits sentences into words as a user-defined
     * FlatMapFunction. The function takes a line (String) and splits it into multiple pairs in the
     * form of "(word,1)" ({@code Tuple2<String, Integer>}).
     */
    public static final class Tokenizer
            // will convert String -> String
            implements FlatMapFunction<String, String> {

        @Override
        // public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
        public void flatMap(String value, Collector<String> out) {
            // out.collect(new Tuple2<>(value,1));
            out.collect(value);
        }
    }

    public static void main( String[] args ) throws Exception
    {
        final MultipleParameterTool params = MultipleParameterTool.fromArgs(args);

        // set up the execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // make parameters available in the web interface
        env.getConfig().setGlobalJobParameters(params);

        KafkaSource<String> source = KafkaSource.<String>builder()
            .setBootstrapServers("broker1:29092")
            .setTopics("p2_input")
            .setGroupId("test")
            .setStartingOffsets(OffsetsInitializer.latest())
            // .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new SimpleStringSchema())
            .build();

        KafkaSink<String> sink = KafkaSink.<String>builder()
            .setBootstrapServers("broker1:29092")
            .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                .setTopic("p2_output")
                .setValueSerializationSchema(new SimpleStringSchema())
                .build()
            )
            .build();

        // examples
        // https://github.com/ververica/flink-training-exercises/blob/master/src/main/java/com/ververica/flinktraining/examples/datastream_java/windows/WhyLate.java#L60
        
        DataStream<String> stream = env.fromSource(
            source, 
            WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds((5))), 
            "kafka-source");

        stream.flatMap(new Tokenizer())
        .map( i -> i.toUpperCase())
        .sinkTo(sink);

        env.execute("hw9events-kafka-source");
    }
}
