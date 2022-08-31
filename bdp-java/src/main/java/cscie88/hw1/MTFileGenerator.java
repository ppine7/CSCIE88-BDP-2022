/**
 * Copyright (c) 2020 CSCIE88 Marina Popova
 * author: mpopova
 * date: 09/22/2020
 */
package cscie88.hw1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MTFileGenerator {
    private static final Logger logger = LoggerFactory.getLogger(MTFileGenerator.class);
    private ExecutorService workersThreadPoolService = null;
    private long generationTimeoutInMS = 5000L; //default timeout for data generation in MS
    // default values for input parameters
    private static int NUM_OF_FILES = 2;
    private static int NUM_OF_LINES = 10;

    public MTFileGenerator() {
    }

    // this is the simplest way to run multiple data generators in parallel - it can be improved in many areas;
    // we are waiting up to 'generationTimeoutInMS' milliseconds for completion of all file generators -
    // if it takes more time, the main program will exit before all threads finished their work
    public void runGenerators(int numberOfGenFiles, int numberOfLines){
        // we are creating as many threads as we want to create files - it's not a good idea in general
        // it's better to have a smaller size pool and just re-use threads; but for this example - it's OK
        workersThreadPoolService = Executors.newFixedThreadPool(numberOfGenFiles);
        for (int i=0; i<numberOfGenFiles; i++) {
            String generatorId = "" + i;
            workersThreadPoolService.submit(new RunnableFileGenerator(generatorId, numberOfLines));
        }
        logger.info("runGenerators(): submitted all generators, waiting for completion");
        workersThreadPoolService.shutdown();
        try {
            workersThreadPoolService.awaitTermination(generationTimeoutInMS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.info("Got InterruptedException while shutting down generators, aborting");
        }
        logger.info("runGenerators(): finished waiting for completion");
    }

    public static void main(String[] args) {
        int numberOfDataFiles = NUM_OF_FILES;
        int numberOfLines = NUM_OF_LINES;

        if (args.length < 2) {
            System.out.println("Usage: java MTFileGenerator <numberOfDataFiles> <numberOfLinesPerFile>");
            System.out.println("Will use default values: {" + NUM_OF_FILES + "} {" + NUM_OF_LINES + "}");
        } else {
            // TODO: should really handle exceptions here
            numberOfDataFiles = Integer.parseInt(args[0]);
            numberOfLines = Integer.parseInt(args[1]);
        }
        MTFileGenerator manager = new MTFileGenerator();
        manager.runGenerators(numberOfDataFiles, numberOfLines);
        System.out.println("MTFileGenerator finished OK");
    }

}
