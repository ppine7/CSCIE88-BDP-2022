package cscie88.week8;

import java.util.Properties;
import java.time.Duration;
import java.util.Arrays;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class SimpleConsumer {
	private static final Logger logger = LoggerFactory.getLogger(SimpleConsumer.class);
	private Properties props = new Properties();


	KafkaConsumer<String, String> consumer;

	public SimpleConsumer(String bootStrapServer, String topicName) {
		props.put("bootstrap.servers", bootStrapServer);
		props.put("group.id", "java-consumer");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		consumer = new KafkaConsumer<>(props);

		// subscribe to a list of topics
		consumer.subscribe(Arrays.asList(topicName));
		logger.info("SimpleConsumer initialized Ok and subscribed to topic: {}", topicName);

	}

	public void run() {
		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
			if (records.isEmpty()) {
				logger.info("called poll() - no records read");
			} else {
				logger.info("called poll() - processing records ...");
			}
			for (ConsumerRecord<String, String> record : records) {
				// print the offset,key and value for the consumer records.
				logger.info("Received event: offset = {},partition = {},  key = {}, value = {}\n", record.offset(),
						record.partition(), record.key(), record.value());
			}
		}
	}

	public static void main(String[] args) throws Exception {
		String topicName = System.getProperty("topic", "java_test_topic");
		String bootStrapServer = System.getProperty("server", "localhost:9092");
		logger.info("Consuming from : Server  -->" + bootStrapServer +", Topic -->" + topicName);
		SimpleConsumer simpleConsumer = new SimpleConsumer(bootStrapServer, topicName);
		simpleConsumer.run();
	}

}