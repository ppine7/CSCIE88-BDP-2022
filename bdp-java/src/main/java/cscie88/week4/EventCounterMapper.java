/**
 * Copyright (c) 2020 CSCIE88 Marina Popova
 * author: mpopova
 * date: 09/22/2020
 */
package cscie88.week4;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import cscie88.week2.LogLine;
import cscie88.week2.LogLineParser;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * This is a Map part of a MR job that counts the number of events per hour
 *
 * UniqueEventsPerUrlPerHourMapper converts a line in the input file
 * to a <key,value> pair where key is the date-hour in the line
 * and the value is '1'.  It is because each line represents a unique event.
 * So, each line adds '1' to the sum of events for a date-hour-url.
 * Input key is an Object.  It is not used in this mapper, so the type is kept
 * as Object so that it can accept any type. Input value is a Text that encloses an
 * entire line from the input file.  Output key is a Text that
 * encapsulates the date-hour-url string, and Output value is
 * an IntWritable containing the number '1'.
 */
public class EventCounterMapper extends Mapper<Object, Text, Text, IntWritable> {

	//Formatter used to convert the date in the line to uuuu-MM-dd:HH format
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd:HH");
	//Serializable wrapper containing the date-hour key
	private Text dateHourKey = new Text();
	//Serializable wrapper containing the number '1'
	private final IntWritable one = new IntWritable(1);

	public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
		// parse the input line
		LogLine parsedLogLine = LogLineParser.parseLine(value.toString());
		//get the date:hour value using specified formatter
		String dateHrStr = parsedLogLine.getEventDateTime().format(formatter);
		// set the output key
		dateHourKey.set(dateHrStr);
		//write the key and value pair (date-hour-url and 1) to the context output
		context.write(dateHourKey, one);
	}
}
