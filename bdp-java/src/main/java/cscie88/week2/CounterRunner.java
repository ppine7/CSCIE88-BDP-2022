package cscie88.week2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class CounterRunner {
    // Default values for input parameters
    // quick and dirty way to switch counter workers:
    // RUN_MODE = "FILE" --> run FileCounterWorker
    // else --> run CounterRunner with Fibonacci calculations
    private static String RUN_MODE = "FILE"; // default value is FILE - run FileCounterWorker;
    private static int NUM_THREADS = 4; // default value

    // counter shared between multiple threads
    private AtomicLong sharedIterationCounter = new AtomicLong(0);
    private static final Logger logger = LoggerFactory.getLogger(CounterRunner.class);
    private ExecutorService workersThreadPoolService = null;
    private List<Future<String>> returnedFutures = new LinkedList<>();

    public CounterRunner() {
    }

    public void runLoad(String runMode, int numThreads) throws InterruptedException {
        // create specified number of threads and run load in each runnable job
        workersThreadPoolService = Executors.newFixedThreadPool(numThreads);
        // add shutdown hook to catch and react to a Cntrl+C termination of the program
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                logger.info("ShutdownHook: interrupt received, killing all threads ... ");
                if (!workersThreadPoolService.isTerminated() && !workersThreadPoolService.isShutdown()) {
                    workersThreadPoolService.shutdownNow();
                }
                for (Future<String> futureOfWorker: returnedFutures) {
                    if (!futureOfWorker.isCancelled() && !futureOfWorker.isDone())
                        futureOfWorker.cancel(true);
                }
            }
        });

        List<Callable<String>> callableTasks = new ArrayList<>();
        if (runMode.equalsIgnoreCase("FILE")) {
            Path folderPath = Paths.get("logs");
            FilenameFilter filter = (dir, name) -> name.endsWith(".csv");
            for (File currentFile : folderPath.toFile().listFiles(filter)) {
                callableTasks.add(new FileCounterWorker(currentFile, sharedIterationCounter));
            }
        } else {
            for (int threadId = 0; threadId < numThreads; threadId++) {
                callableTasks.add(new CounterWorker(String.valueOf(threadId), sharedIterationCounter));
            }
        }
        workersThreadPoolService.invokeAll(callableTasks);
        logger.info("Total count "+ sharedIterationCounter);
        // stop accepting new jobs and wait till all threads are done (won't happen in our case)
        workersThreadPoolService.shutdown();

        logger.info("runLoad(): finished");
    }

    public static void main(String[] args) throws InterruptedException {
        String runMode = RUN_MODE;
        int numThreads = NUM_THREADS;
        if (args.length < 2) {
            System.out.println("Usage: java CounterRunner <runMode: FILE or FIB> <numberOfThreads>");
            System.out.println("using default values: runMode=FILE, numberOfThreads=4");
        } else {
            runMode = args[0];
            numThreads = Integer.parseInt(args[1]);
        }
        CounterRunner runner = new CounterRunner();
        runner.runLoad(runMode, numThreads);
        System.out.println("CounterRunner finished OK");
    }

}
