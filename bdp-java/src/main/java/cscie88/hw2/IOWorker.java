/**
 * Copyright (c) 2020 CSCIE88 Marina Popova
 * author: mpopova
 * date: 09/22/2020
 */
package cscie88.hw2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

public class IOWorker implements Callable<String> {
    private static final Logger logger = LoggerFactory.getLogger(IOWorker.class);
    private String threadId;

    public IOWorker(String threadId) {
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
            iterNumber++;
            //The file is generated on the same directory where the program is run from
            Path path = Paths.get("./", "tmp_file_" + threadId + "_" + iterNumber + ".txt");
            logger.info("creating and writing numbers into a file {} ... ", path.toString());
            // write a lot of lines with random numbers into the file
            // to increase IO pressure, a FileWriter is used instead of a BufferedFileWriter
            try (FileWriter writer = new FileWriter(path.toFile())) {
                for (int i = 0; i < 1000000; i++) {
                    // stop gracefully if asked to
                    if (Thread.currentThread().isInterrupted()) {
                        logger.info("Received interrupt signal - stopping the thread {}", threadId);
                        Files.delete(path);
                        return "interrupted";
                    }
                    int randomNum = ThreadLocalRandom.current().nextInt(0, 10000);
                    writer.write(randomNum);
                    writer.write(" ... \n");
                }
                // delete the file
                Files.delete(path);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
