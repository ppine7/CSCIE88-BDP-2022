/**
 * This is an implementation of a MR job that counts the number of events per hour
 *
 * Copyright (c) 2020 CSCIE88 Marina Popova
 * author: mpopova
 * date: 09/22/2020
 */
package cscie88.week4;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * Main MR runner/job that creates, configures and runs a MR job that counts the number of events
 * happened per date/hour
 */
public class EventCounterMRJob {

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("Usage: java EventCounterMRJob <input_dir> <output_dir>");
			System.exit(-1);
		}
		// create a new MR job
		Configuration conf = new Configuration();
	    Job job = Job.getInstance(conf, "Count events per hour");
	    
	    //This class is used as reference point to locate the jar file
	    job.setJarByClass(EventCounterMRJob.class);
	    
	    //set the Mapper class for the job
	    job.setMapperClass(EventCounterMapper.class);
	    
	    //Since every line in the input file is considered to be a
	    //unique event, there are no duplicate event entries among
	    //the mappers.  Therefore it is possible to use
	    //the Reducer also as a Combiner to aggregate the data before
	    //it is passed to the reducers without affecting data consistency
	    job.setCombinerClass(EventCounterReducer.class);
	    
	    //set the Reducer for the job
	    job.setReducerClass(EventCounterReducer.class);

	    //set output key and value classes/types for the reducer.
		//Since these types are the same as what is used by the mappers,
	    //it is not necessary to explicitly set the input key and value types for mappers.
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);

	    // set input and output formats - even though the ones we use are the default ones
	    job.setInputFormatClass(TextInputFormat.class);
	    job.setOutputFormatClass(TextOutputFormat.class);
	    //Input path is the first argument given on the command line
	    //when the job is kicked off.  And Output path is the second argument.
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	    
	    //wait for the job to complete
	    System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
