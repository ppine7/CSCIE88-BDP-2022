/**
 * Copyright (c) 2020 CSCIE88 Marina Popova
 * author: mpopova
 * date: 09/22/2020
 */
package cscie88.hw2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class LoadRunner {
    private static final Logger logger = LoggerFactory.getLogger(LoadRunner.class);
    private ExecutorService workersThreadPoolService = null;
    private List<Future<String>> returnedFutures = new LinkedList<>();
    private long generationTimeoutInMS = 5000l; //default timeout for data generation in MS

    public LoadRunner() {
    }

    public void runLoad(boolean isIO, int numThreads){
        // create specified number of threads and run load in each runnable job
        workersThreadPoolService = Executors.newFixedThreadPool(numThreads);
        // add shutdown hook to catch and react to a Cntrl+C termination of the program
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                logger.info("interrupt received, killing all threads ... ");
                if (!workersThreadPoolService.isTerminated() && !workersThreadPoolService.isShutdown()) {
                    workersThreadPoolService.shutdownNow();
                }
                for (Future<String> futureOfWorker: returnedFutures) {
                    if (!futureOfWorker.isCancelled() && !futureOfWorker.isDone())
                        futureOfWorker.cancel(true);
                }
            }
        });

        for (int i=0; i<numThreads; i++) {
            String threadId = "" + i;
            Future<String> futureResult = null;
            if (isIO) {
                futureResult = workersThreadPoolService.submit(new IOWorker(threadId));
            }
            else {
                futureResult = workersThreadPoolService.submit(new CPUWorker(threadId));
            }
            returnedFutures.add(futureResult);
        }
        logger.info("runLoad(): submitted all workers ...");
        // stop accepting new jobs and wait till all threads are done (won't happen in our case)
        workersThreadPoolService.shutdown();
        while (!workersThreadPoolService.isTerminated()) {
            logger.info("LoadRunner: I'm still alive ... ");
            // sleep a bit, let the tasks to continue their work
            try {
                Thread.sleep(40000);
            } catch (InterruptedException e) {
                logger.info("Got InterruptedException in the main thread - aborting");
                for (Future<String> futureOfWorker: returnedFutures) {
                    futureOfWorker.cancel(true);
                }
                break;
            }
        }

        logger.info("runLoad(): finished");
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java LoadRunner <cpu OR io> <numberOfThreads>");
            System.out.println("example for io load: java LoadRunner io <numberOfThreads>");
            System.exit(-1);
        }
        String mode = args[0];
        boolean isIO = false;
        if ("io".equalsIgnoreCase(mode))
            isIO = true;
        int numThreads = Integer.parseInt(args[1]);
        LoadRunner runner = new LoadRunner();
        runner.runLoad(isIO, numThreads);
        System.out.println("LoadRunner finished OK");
    }

}
