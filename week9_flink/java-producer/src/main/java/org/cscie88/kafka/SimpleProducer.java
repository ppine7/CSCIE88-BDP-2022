package org.cscie88.kafka;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleProducer {

	private static final Logger logger = LoggerFactory.getLogger(SimpleConsumer.class);
	public static final String INFINITE = "infinite";
	private Properties props = new Properties();
	private Producer<String, String> producer;

	public SimpleProducer(String bootStrapServer) {

		// Assign localhost id
		props.put("bootstrap.servers", bootStrapServer);
		// props.put("security.protocol", "SASL_SSL");
		// props.put("security.protocol", "SASL_PLAINTEXT");
		// props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
		// props.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username='EBC4A3GUWCPKZB77' password='b1YzApuZiHz+YCsnoRDSs6By2NarAF93+tFDgXUENfZWu3gMZX4iIoAvzrigB7+s';" );
		// props.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required serviceName='producer-logs' username='user' password='user';" );
		// Set acknowledgements for producer requests.
		props.put("acks", "all");
		// If the request fails, the producer can automatically retry,
		props.put("retries", 0);
		// Specify buffer size in config
		props.put("batch.size", 16384);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		producer = new KafkaProducer<String, String>(props);
		logger.info("SimpleProducer initialized Ok");
	}

	public void produceEvents(Integer numberOfEvents, String topicName, Long produceSeconds,String inputFile) throws InterruptedException, IOException {

		// Gson gson = new Gson();
		// a stream consumer which will be called for each line in the stream. 
		// it formats the logline as a string and sends to kafka topic using the 
		// producer send method
		Consumer<LogLine> kafkaConsumer = new Consumer<LogLine>() {
			public void accept(LogLine line) {
				//  you can generate the same type of log events we've worked with so far: 
				// one event is one line in the format: <uuid> <timestamp> <url> <userId>
				/* 
				# 41daf40b28bd48958eb870bc65b8d78a,
				# 2022-09-03T13:48:59.570998800Z,
				# http://example.com/?url=124,
				# user-035,
				# GN,
				# IE,
				# windows,
				# 207,
				# 0.6509 */

				// String uuid, ZonedDateTime eventDateTime, String url, String userId, String country,
				// String uaBrowser, String uaOs, int responseCode, float ttfb) {
	
				String messageBody = String.format(
					"%s %s %s %s %s %s %s %s %s", 
					line.getUuid(),
					line.getEventDateTime(),
					line.getUrl(),
					line.getUserId(),
					line.getCountry(),
					line.getUaBrowser(),
					line.getUaOs(),
					line.getResponseCode(),
					line.getTtfb()
				);

				logger.info("Producing message:{}", messageBody );
				
				// publish body to kafka topic.
				// Future<RecordMetadata> metadata = producer.send(new ProducerRecord<String,String>(topicName,messageBody));
				Future<RecordMetadata> metadata = producer.send(new ProducerRecord<String,String>(topicName,messageBody));

				// get and log the publish results
				RecordMetadata rec;
				try {
					rec = metadata.get();
					logger.info("Produced message topic:{} timestamp:{} partition:{}", rec.topic(),rec.timestamp(),rec.partition() );
					// TimeUnit.SECONDS.sleep(produceSeconds);
					TimeUnit.MILLISECONDS.sleep(produceSeconds);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			};
		};

		// open the input file as a stream of lines
		Stream<String> lines = Files.lines(new File(inputFile).toPath());
		// iterate each of the lines and pass to the kafka consumer
		lines.limit(numberOfEvents!= 0 ? numberOfEvents : Long.MAX_VALUE)
			.map(line -> LogLineParser.parseLine(line))
			.forEach(kafkaConsumer);
		lines.close();

	}

	// run with
	// java -classpath ./target/hw8-1.0-SNAPSHOT.jar -Dserver=localhost:9092,localhost:9093,localhost:9094 -Dtopic=problem3 -Dsec=1 -DinputFile=/Users/bmullan/Documents/Apps/ccsi-e88/assignment3/logs/file-input1.csv org.cscie88.kafka.SimpleProducer
	// or (limits event count)
	// java -classpath ./target/hw8-1.0-SNAPSHOT.jar -DeventCount=5 -Dserver=localhost:9092,localhost:9093,localhost:9094 -Dtopic=problem3 -Dsec=1 -DinputFile=/Users/bmullan/Documents/Apps/ccsi-e88/assignment3/logs/file-input1.csv org.cscie88.kafka.SimpleProducer

	public static void main(String[] args) throws Exception {

		String topicName = System.getProperty("topic", "java_test_topic");
		String bootStrapServer = System.getProperty("server", "kafka:9092");
		String eventCountString = System.getProperty("eventCount", INFINITE);
		String produceSecondsString = System.getProperty("sec", "60");
		
		// get the input log file as a property
		String inputFile = System.getProperty("inputFile","");
		logger.info("Producing to : Server  -->" + bootStrapServer +", Topic -->" + topicName);
		SimpleProducer simpleProducer = new SimpleProducer(bootStrapServer);
		try {
			Long produceSeconds = Long.parseLong(produceSecondsString);
			int eventCountInt = eventCountString != INFINITE ? Integer.parseInt(eventCountString) : 0;
			simpleProducer.produceEvents(eventCountInt, topicName, produceSeconds, inputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}