/**
 * Copyright (c) 2020 CSCIE88 Marina Popova
 * author: mpopova
 * date: 09/22/2020
 */
package cscie88.hw2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class CPUWorker implements Callable<String> {
    private static final Logger logger = LoggerFactory.getLogger(CPUWorker.class);
    private String threadId;

    public CPUWorker(String threadId) {
        this.threadId = threadId;
    }

    @Override
    public String call() {
        long iterNumber = 0;

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
            iterNumber ++;
            // print a heart beat message every 1000 cycles
            if (iterNumber % 1000 == 0)
                logger.info("Thread {} is working, iterNumber={} ...", threadId, iterNumber);
        }
    }

}
