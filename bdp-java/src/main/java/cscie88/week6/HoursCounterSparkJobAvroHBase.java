package cscie88.week6;

import org.apache.spark.api.java.JavaPairRDD;
//import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import scala.Tuple2;

import cscie88.week2.LogLineParser;
import java.time.format.DateTimeFormatter;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.TableName;

/**
 * This Spark job :
 *          reads all log files from the specified directory,
 *          parses them using the LogLineParser from week2,
 *          extracts event date and formats it in the "yyyy-MM-dd:HH" format (up to the hour),
 *          counts the number of events that happened per each hour
 *          and stores them into an HBase table
 */
public class HoursCounterSparkJobAvroHBase {

    private static final DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH");

    public static void main(String[] args) throws IOException {

        if (args.length < 1) {
            System.out.println("Usage: HoursCounterSparkJobAvroHBase <input_dir>");
            System.exit(0);
        }

        String inputDirectory = args[0];

        //Create Spark Session
        SparkSession spark = SparkSession
                .builder()
                // uncomment this next line to run locally
                //.master("local[*]")
                .appName("HoursCounterSparkJobAvroHBase")
                .getOrCreate();

        // Creates a DataFrame from an avro file in the input directory.
        Dataset<Row> dfavro = spark.read().format("avro")
                .load(inputDirectory);

        //Get hourly counts
        JavaPairRDD<String, Long> counts = dfavro
                .toJavaRDD()
                .map(row -> LogLineParser.parseLine(row.mkString(",")))
                .mapToPair(parsedLine -> {
                    String absoluteHourKey = parsedLine.getEventDateTime().format(hourFormatter);
                    return new Tuple2<>(absoluteHourKey, parsedLine.getUrl());
                }).distinct()
                .mapToPair(s -> new Tuple2<String, Long>(s._1(), 1L)) // count of 1 for each distinct entry 2
                .reduceByKey((a, b) -> a + b); // reduce; get the total Urls in a dateHour


        // instantiate Configuration class
        Configuration config = HBaseConfiguration.create();
        Connection connection = ConnectionFactory.createConnection(config);

        System.out.println(connection.isClosed());

        // instantiate Table class

        //Table table = connection.getTable(TableName.valueOf("lab6:log"));
        Table table = connection.getTable(TableName.valueOf("lab:date_hour"));

        for (Tuple2<String, Long> l : counts.collect())
        {
            table.put(new Put(Bytes.toBytes(l._1))
                    .addColumn(Bytes.toBytes("url"),
                            Bytes.toBytes("count"),
                            Bytes.toBytes(l._2.toString())));
        }

        // close Table instance
        table.close();

        //close connection
        connection.close();

        //counts.saveAsTextFile("jobs_output_avro_new_p");

    }
}
