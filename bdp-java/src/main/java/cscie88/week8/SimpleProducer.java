package cscie88.week8;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleProducer {

	private static final Logger logger = LoggerFactory.getLogger(SimpleProducer.class);
	public static final String INFINITE = "infinite";
	private Properties props = new Properties();
	private Producer<String, String> producer;

	public SimpleProducer(String bootStrapServer) {
		// Assign localhost id
		props.put("bootstrap.servers", bootStrapServer);

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

	public void produceEvents(Integer numberOfEvents, String topicName, Long produceSeconds) throws InterruptedException {
		int currentEventCount = 0;
		do {
			String messageBody = "test_event -->" + currentEventCount;
			producer.send(new ProducerRecord<>(topicName, Integer.toString(currentEventCount), messageBody));
			logger.info("Produced message successfully: {}", messageBody);
			TimeUnit.SECONDS.sleep(produceSeconds);
			currentEventCount++;
		} while (numberOfEvents == null || currentEventCount < numberOfEvents);
		producer.close();
	}

	public static void main(String[] args) throws Exception {
		String topicName = System.getProperty("topic", "java_test_topic");
		String bootStrapServer = System.getProperty("server", "localhost:9092");
		String eventCountString = System.getProperty("eventCount", INFINITE);
		String produceSecondsString = System.getProperty("sec", "5");
		logger.info("Producing to : Server  -->" + bootStrapServer +", Topic -->" + topicName);
		SimpleProducer simpleProducer = new SimpleProducer(bootStrapServer);
		try {
			Long produceSeconds = Long.parseLong(produceSecondsString);
			if(INFINITE.equals(eventCountString)){
				simpleProducer.produceEvents(null, topicName,produceSeconds);
			}else{
				int eventCountInt = Integer.parseInt(eventCountString);
				simpleProducer.produceEvents(eventCountInt, topicName, produceSeconds);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}