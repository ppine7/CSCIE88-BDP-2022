package cscie88.hw1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ThreadLocalRandom;

public class RunnableFileGenerator implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(RunnableFileGenerator.class);
    public static String FILE_NAME_PREFIX = "cscie88_fall2022_";
    public static String FILE_NAME_ENDING = ".txt";
    public static int ITEMS_NUMBER_PER_LINE = 3;
    public static String LINE_ITEMS_SEPARATOR = " ";

    private String threadId;
    private String threadFileName;
    private int numberOfLines;

    public RunnableFileGenerator(String threadId, int numberOfLines) {
        this.threadId = threadId;
        this.numberOfLines = numberOfLines;
        threadFileName = FILE_NAME_PREFIX + threadId + FILE_NAME_ENDING;
    }

    @Override
    public void run() {
        try (BufferedWriter writer = Files.newBufferedWriter( Paths.get(threadFileName) )) {
            for (int i=0; i<numberOfLines; i++) {
                StringBuilder line = new StringBuilder();
                for (int j=0; j<ITEMS_NUMBER_PER_LINE; j++) {
                    int randomNum = ThreadLocalRandom.current().nextInt(0, 11);
                    line.append(randomNum);
                    if (j < ITEMS_NUMBER_PER_LINE-1) {
                        line.append(LINE_ITEMS_SEPARATOR);
                    }
                }
                writer.write(line.toString());
                if (i < numberOfLines-1 ) {
                    writer.newLine();
                }
            }
            logger.info("thread {} generated data file OK: {}", threadId, threadFileName);
        } catch (Exception e) {
            logger.info("thread {}}: ERROR: no data file was generated: ", threadId, /**
                     * Copyright (c) 2020 CSCIE88 Marina Popova
                     * author: mpopova
                     * date: 09/22/2020
                     */
                    e.getMessage());
        }
    }

}
