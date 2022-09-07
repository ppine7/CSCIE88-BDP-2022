package cscie88.week2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

public class CounterWorker implements Callable<String> {
    private static final Logger logger = LoggerFactory.getLogger(CounterWorker.class);
    private String threadId;
    private AtomicLong sharedIterationCounter;

    public CounterWorker(String threadId, AtomicLong sharedIterationCounter) {
        this.sharedIterationCounter = sharedIterationCounter;
        this.threadId = threadId;
    }

    @Override
    public String call() {
        long localIterationCounter = 0;
        long sharedIterationCounterValue = 0;
        while (true) {
            // stop gracefully if asked to
            if (Thread.currentThread().isInterrupted()) {
                logger.info("Received interrupt signal - stopping the thread {}", threadId);
                return "interrupted";
            }
            //calculate 1M Fibonacci numbers
            long fib1 = 1, fib2 = 1, temp;
            for(int i = 0; i < 1000000; i++) {
                temp = fib2;
                fib2 += fib1;
                fib1 = temp;
            }
            localIterationCounter ++;
            sharedIterationCounterValue = sharedIterationCounter.incrementAndGet();
            // print a heart beat message every 1000 cycles
            if (localIterationCounter % 1000 == 0)
                logger.info("Thread {} is working, localIterationCounter={}, sharedIterationCounterValue={} ...",
                        threadId, localIterationCounter, sharedIterationCounterValue);
        }
    }

}
