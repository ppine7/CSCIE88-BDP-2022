package cscie88.week5;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

public class HoursCounterSparkJobAvro {

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: HoursCounterSparkJobAvro <input_dir>");
            System.exit(0);
        }
        String inputDirectory = args[0];
        //Create Spark Session
        SparkSession spark = SparkSession
                .builder()
                // uncomment this line to run the job locally in an IDE or on your laptop
                //.master("local[*]")
                .appName("HoursCounterSparkJobAvro")
             //   .config("spark.eventLog.enabled","true")
             //   .config("spark.eventLog.dir","/tmp/spark")
                .getOrCreate();

        // Creates a DataFrame from an avro file in the input directory.
        Dataset<Row> dfavro = spark.read().format("avro")
                .load(inputDirectory);

        //      dfavro.show();
        dfavro.printSchema();
        JavaPairRDD<String, Long> counts = dfavro
                .toJavaRDD()
                .map(s -> s.getString(1))
                .mapToPair(s -> new Tuple2 <String, Long> (s.substring(0, 13), 1L))
                .reduceByKey((a, b) -> a + b);

        counts.saveAsTextFile("jobs_output_avro");
        spark.stop();
    }
}

