/**
 * Copyright (c) 2020 CSCIE88 Marina Popova
 * author: mpopova
 * date: 09/22/2020
 */
package cscie88.week4;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * This is a Reduce part of a MR job that counts the number of events per hour
 *
 * EventCounterReducer takes as an input a <key,value[]> entry
 * where the key is a date-hour string and value[] is a list of
 * counts of events for that date-hour.
 * 
 * The reducer iterates over the list of counters and sums them up.  The result
 * of the sum is written as the output value of the reducer.
 */
public class EventCounterReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
	//Serializable writer that encapsulates the count of events per date-hour
	private IntWritable result = new IntWritable();

	public void reduce(Text dateHrKey, Iterable<IntWritable> eventCounts, Context context) throws IOException, InterruptedException {
		
		//Total number of events for a date-hour key
		int totalEvents = 0;
		//Events counts are summed up
		for(IntWritable eventCount:eventCounts) {
			totalEvents += eventCount.get();
		}
		//Total number of events is set as the output value
		result.set(totalEvents);
		//Key and value (date-hour and event count) are written to the context output
		context.write(dateHrKey, result);
	}
}
