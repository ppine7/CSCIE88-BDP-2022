package cscie88.week2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class FileCounterWorker implements Callable<String> {
    private static final Logger logger = LoggerFactory.getLogger(FileCounterWorker.class);
    private File fileToProcess;
    private AtomicLong sharedIterationCounter;

    public FileCounterWorker(File fileToProcess, AtomicLong sharedIterationCounter) {
        this.sharedIterationCounter = sharedIterationCounter;
        this.fileToProcess = fileToProcess;
    }

    @Override
    public String call() {
        try (Stream<String> lines = Files.lines(fileToProcess.toPath()) ) {
            // parse the line into the LogLine model class
            long eventCount = lines
                    .map(line -> LogLineParser.parseLine(line))
                    .count();
            System.out.println("File: "+ fileToProcess.getName()+ ", event count: " + eventCount +" thread id: " +Thread.currentThread().getName());
            sharedIterationCounter.addAndGet(eventCount);
        } catch (Exception e) {
            System.out.println("got Exception, exiting: " + e.getMessage());
        }
        return null;


    }

}
