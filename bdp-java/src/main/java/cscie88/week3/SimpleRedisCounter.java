/**
 * Copyright (c) 2022 CSCIE88 Marina Popova
 * author: mpopova
 * date: 09/11/2022
 */
package cscie88.week3;

import cscie88.week2.LogLineParser;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class SimpleRedisCounter {
    // Redis URL has to be specified per 'lettuce' redis driver specification:
    // read more on 'lettuce' driver: https://www.baeldung.com/java-redis-lettuce
    // https://lettuce.io/core/release/reference/#basic.redisuri
    private static final String REDIS_URL_DEFAULT = "redis://localhost:6379";
    private static final String REDIS_COUNTER_KEY_DEFAULT = "counts";
    private static final Logger logger = LoggerFactory.getLogger(SimpleRedisCounter.class);
    private ExecutorService workersThreadPoolService = null;
    private List<Future<String>> returnedFutures = new LinkedList<>();

    public SimpleRedisCounter() {
    }

    public void countLinesInFile(String redisUrl, String redisCounterKey, String inputFileName) {
        RedisClient redisClient = RedisClient.create(redisUrl);
        // create a thread-safe connection to Redis
        StatefulRedisConnection<String, String> redisConnection = redisClient.connect();
        // we are assuming the specified redis key is empty when we start all applications
        // if this is not the case - the counts will be added to the existing key[s]
        // you could also cleanup the key manually before or after running all counting apps
        // or programmatically if you have a 'manager' type of an app that runs before the counters
        //redisConnection.sync().del("counts");

        // read specified input file, convert into a stream of lines,
        // parse each line, count the number of lines
        Path fileToProcess = Paths.get(inputFileName);
        try (Stream<String> lines = Files.lines(fileToProcess) ) {
            // parse the line into the LogLine model class
            long eventCount = lines
                    .map(line -> LogLineParser.parseLine(line))
                    .count();
            logger.info("File: " + inputFileName + ", event count: " + eventCount);
            RedisCommands<String, String> syncCommand = redisConnection.sync();
            String eventCountString = Long.toString(eventCount);
            if (!syncCommand.hsetnx(redisCounterKey, "total", eventCountString)){
                syncCommand.hincrby(redisCounterKey, "total", eventCount);
            }
        } catch (Exception e) {
            logger.error("got Exception, exiting: " + e.getMessage());
        }
        System.out.println("Total count : "+ redisConnection.sync().hget("counts", "total"));
        logger.info("runLoad(): finished");
    }

    public static void main(String[] args) throws InterruptedException {
        String redisUrl = REDIS_URL_DEFAULT;
        String redisCounterKey = REDIS_COUNTER_KEY_DEFAULT;
        String inputFileName = "logs/file-input1.csv";
        if (args.length < 3) {
            System.out.println("Usage: java SimpleRedisCounter redis_url redis_key file_to_process");
            System.out.println("Example: java SimpleRedisCounter redis://localhost:6379 counts logs/file-input1.csv");
            System.out.println("Using default values " + redisUrl);
        } else {
            redisUrl = args[0];
            redisCounterKey = args[1];
            inputFileName = args[2];
            System.out.println("Running SimpleRedisCounter with redis_url: " + redisUrl +
                    " , redisCounterKey=" + redisCounterKey + " , inputFileName=" + inputFileName);
        }
        SimpleRedisCounter simpleRedisCounter = new SimpleRedisCounter();
        simpleRedisCounter.countLinesInFile(redisUrl, redisCounterKey, inputFileName);
     }

}
