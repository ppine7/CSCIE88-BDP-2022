# CSCIE88 - LAB for Lecture 4 - Hadoop Map Reduce

## Lab Notes / Topics

- Creating an AWS EMR Cluster
- Building and running the class Java sample.
- Building and running the class python example.
- AWS EMR Step Execution
- S3 Buckets
- AWS cli
- Data processing on the linux cli
- Book: ['Designing Data-Intensive Applications': Martin Kleppmann](https://www.amazon.com/Designing-Data-Intensive-Applications-Reliable-Maintainable/dp/1449373321/ref=sr_1_1?keywords=data+intensive+applications&qid=1663546252&sprefix=data+in%2Caps%2C78&sr=8-1)
- Article: ['Map Reduce: Googles secret weapon'](https://hbr.org/2008/12/mapreduce-googles-secret-weapo)
- Running Hadoop in Docker



## Intro to data processing on the linux cli

- Unix philosophy : simple commands that do one thing, and do it well and efficiently and allow them to be chained together into a pipeline 
- Useful for doing an exploratory analysis of your data before committing to writing code, or to verify the results of your code. 
- Example, from our log files find the top 5 most called urls ...
```
cat ./logs/*.csv | \
awk -F "," '{print $3}' | \
sort | \
uniq -c | \
sort -r -n | \
head -n 5
```
output ...
```
 234 http://example.com/?url=151
 234 http://example.com/?url=034
 231 http://example.com/?url=036
 229 http://example.com/?url=003
 228 http://example.com/?url=172
```

Solving Query 1 - Count of unique urls per hour
```
cat ./logs/*.csv | \
awk -F "," '{print substr($2,1,13) " " substr($3,21,7)}' | \
sort | \
uniq | \
awk '{print $2 " " $1}' | \
uniq -c -f 1 |
awk '{print $3 " " $1}'
```
```
2022-09-03T13 80
2022-09-03T14 184
2022-09-03T15 190
2022-09-03T16 185
2022-09-03T17 190
2022-09-03T18 190
....
```
whats going on ?
```
# concatenate all the csv files into one stream and send to next command | 
cat ./logs/*.csv | \
# extract the data and hour part of column 2 and the last 7 chars of our url (output will be two fields eg. "2022-09-06T13 url=196"
awk -F "," '{print substr($2,1,13) " " substr($3,21,7)}' | \
# lexigraphical sort of both columns (date:hour and url)
sort | \
# make unique to discard repeated urls
uniq | \
# reverse the columns (needed for next step) eg. "url=196 2022-09-06T13"
awk '{print $2 " " $1}' | \
# unique again, but this time skip the first column for uniqueness (so will only unique based on date:hour) and output a count (-c)
uniq -c -f 1 |
# finally just print the date:hour ($3) and count ($1)
awk '{print $3 " " $1}'
```

## Creating an AWS EMR Cluster

<img src="https://github.com/bmullan-pivotal/hw4/raw/main/images/emr-create-cluster.png" height="400">

- Find EMR in the list of AWS Services (Search for it, or its under the Analytics services grouping)
- Select Create Cluster
- Name your cluster
- Leave Logging and Launch Mode as default
- Select the default release (emr-5.36.0) (This will package stable Hadoop release 2.10.1)
- Select your instance type
- Leave # of nodes as default (3)
- Leave Auto Termination enabled (this will shut down your cluster if it is idle more than an hour, saving unexpected billing charges)
- Select or create a key pair (be sure to save the .pem file)
- Select **Create Cluster**
- Creating the cluster will take about 5-10 minutes. 
- Once created you will be able to connect to the hadoop cluster master node using the "Master Public DNS" information on the Summary page
<img src="https://github.com/bmullan-pivotal/hw4/raw/main/images/emr-master-domain-ssh.png">


## Building and Running the java CSCIE88 Example (cscie88.hw4.EventCounterMRJob)

If you have not already done so, clone the github repository
```
git clone https://github.com/ppine7/CSCIE88-BDP-2022
```
Import as an eclipse project (see Marina's example from class)
For visual studio code
```
cd CSCIE88-BDP-2022
cd bdp-java
code .
```
In your ide set the args to add the logs and an output director
```
# vscode
        {
            "type": "java",
            "name": "Launch EventCounterMRJob",
            "request": "launch",
            "mainClass": "cscie88.hw4.EventCounterMRJob",
            "projectName": "bdp-java",
            "args": ["./logs/","./output"]
        }
```
Run the project from your ide (can also debug, setting breakpoints etc.)
Output should end with ...
```
21:24:28.010 [main] INFO org.apache.hadoop.mapreduce.Job - Job job_local693855424_0001 completed successfully
```
You will be able to examine the output in your output directory.
Once you are happy that your code compiles and produces the expected output, follow these steps to run it in the EMR cluster.
1. Locate the jar file (you may have to run build in your ide). It should be <project-dir>/build/libs/bdp-java-1.0-SNAPSHOT.jar
2. From a command line use the scp command to copy it to the emr cluster. Locate the ssh information in the cluster summary page. 
```
#eg. 
scp -i harvard-2022-key.pem build/libs/bdp-java-1.0-SNAPSHOT.jar hadoop@ec2-3-145-27-74.us-east-2.compute.amazonaws.com:~
```
once copied, ssh into the cluster using the same ssh information from above
```
#eg.
ssh -i harvard-2022-key.pem hadoop@ec2-3-145-27-74.us-east-2.compute.amazonaws.com
```
Submit your hadoop job with a command like this (replacing the s3 bucket paths with your bucket and folder names) ...
```
hadoop jar ./bdp-java-1.0-SNAPSHOT.jar s3://cscie88-bucket/logs/ s3://cscie88-bucket/output
```

## Building and Running the python CSCIE88 Example (hours_mapper_demo)

If you have not already done so, clone the github repository
```
git clone https://github.com/ppine7/CSCIE88-BDP-2022
```
To open in visual studio code
```
cd CSCIE88-BDP-2022
cd bdp-python/week4
code .
```
Examine the files hours_mapper_demo.py and hours_reducer_demo.py

You can also run them locally eg.
```

```

Copy the files to your hadoop cluster eg. 
```
scp -i ../../../harvard-2022-key.pem hours_mapper_demo.py hours_reducer_demo.py hadoop@ec2-18-119-120-181.us-east-2.compute.amazonaws.com:~
```
Then ssh into your cluster eg.
```
ssh -i harvard-2022-key.pem hadoop@ec2-18-119-120-181.us-east-2.compute.amazonaws.com
```
Run your python hadoop job
```
hadoop jar /usr/lib/hadoop/hadoop-streaming.jar \
-file ./hours_mapper_demo.py \
-file ./hours_reducer_demo.py \
-mapper "python hours_mapper_demo.py" \
-reducer "python hours_reducer_demo.py" \
-input s3://cscie88-bucket/logs \
-output s3://cscie88-bucket/output/python/
```
Output should look like this ...
```
hadoop jar /usr/lib/hadoop/hadoop-streaming.jar -file ./hours_mapper_demo.py -file ./hours_reducer_demo.py -mapper "python hours_mapper_demo.py" -reducer "python hours_reducer_demo.py" -input s3://cscie88-bucket/logs -output s3://cscie88-bucket/output/python/
22/09/21 23:10:19 WARN streaming.StreamJob: -file option is deprecated, please use generic option -files instead.
packageJobJar: [./hours_mapper_demo.py, ./hours_reducer_demo.py] [/usr/lib/hadoop/hadoop-streaming-2.10.1-amzn-4.jar] /tmp/streamjob8311962095760149440.jar tmpDir=null
22/09/21 23:10:21 INFO client.RMProxy: Connecting to ResourceManager at ip-172-31-6-235.us-east-2.compute.internal/172.31.6.235:8032
22/09/21 23:10:21 INFO client.AHSProxy: Connecting to Application History server at ip-172-31-6-235.us-east-2.compute.internal/172.31.6.235:10200
22/09/21 23:10:21 INFO client.RMProxy: Connecting to ResourceManager at ip-172-31-6-235.us-east-2.compute.internal/172.31.6.235:8032
22/09/21 23:10:21 INFO client.AHSProxy: Connecting to Application History server at ip-172-31-6-235.us-east-2.compute.internal/172.31.6.235:10200
22/09/21 23:10:29 INFO lzo.GPLNativeCodeLoader: Loaded native gpl library
22/09/21 23:10:29 INFO lzo.LzoCodec: Successfully loaded & initialized native-lzo library [hadoop-lzo rev 049362b7cf53ff5f739d6b1532457f2c6cd495e8]
22/09/21 23:10:29 INFO mapred.FileInputFormat: Total input files to process : 4
22/09/21 23:10:29 INFO mapreduce.JobSubmitter: number of splits:8
22/09/21 23:10:29 INFO mapreduce.JobSubmitter: Submitting tokens for job: job_1663800302649_0002
22/09/21 23:10:30 INFO conf.Configuration: resource-types.xml not found
22/09/21 23:10:30 INFO resource.ResourceUtils: Unable to find 'resource-types.xml'.
22/09/21 23:10:30 INFO resource.ResourceUtils: Adding resource type - name = memory-mb, units = Mi, type = COUNTABLE
22/09/21 23:10:30 INFO resource.ResourceUtils: Adding resource type - name = vcores, units = , type = COUNTABLE
22/09/21 23:10:30 INFO impl.YarnClientImpl: Submitted application application_1663800302649_0002
22/09/21 23:10:30 INFO mapreduce.Job: The url to track the job: http://ip-172-31-6-235.us-east-2.compute.internal:20888/proxy/application_1663800302649_0002/
22/09/21 23:10:30 INFO mapreduce.Job: Running job: job_1663800302649_0002
22/09/21 23:10:41 INFO mapreduce.Job: Job job_1663800302649_0002 running in uber mode : false
22/09/21 23:10:41 INFO mapreduce.Job:  map 0% reduce 0%
22/09/21 23:11:02 INFO mapreduce.Job:  map 13% reduce 0%
22/09/21 23:11:03 INFO mapreduce.Job:  map 25% reduce 0%
22/09/21 23:11:17 INFO mapreduce.Job:  map 38% reduce 0%
22/09/21 23:11:18 INFO mapreduce.Job:  map 50% reduce 0%
22/09/21 23:11:22 INFO mapreduce.Job:  map 100% reduce 0%
22/09/21 23:11:28 INFO mapreduce.Job:  map 100% reduce 33%
22/09/21 23:11:36 INFO mapreduce.Job:  map 100% reduce 67%
22/09/21 23:11:38 INFO mapreduce.Job:  map 100% reduce 100%
22/09/21 23:11:39 INFO mapreduce.Job: Job job_1663800302649_0002 completed successfully
22/09/21 23:11:39 INFO mapreduce.Job: Counters: 56
	File System Counters
		FILE: Number of bytes read=30752
		FILE: Number of bytes written=2539187
		FILE: Number of read operations=0
		FILE: Number of large read operations=0
		FILE: Number of write operations=0
		HDFS: Number of bytes read=736
		HDFS: Number of bytes written=0
		HDFS: Number of read operations=8
		HDFS: Number of large read operations=0
		HDFS: Number of write operations=0
		S3: Number of bytes read=5145392
		S3: Number of bytes written=1168
		S3: Number of read operations=0
		S3: Number of large read operations=0
		S3: Number of write operations=0
	Job Counters
		Killed map tasks=1
		Killed reduce tasks=1
		Launched map tasks=8
		Launched reduce tasks=4
		Data-local map tasks=8
		Total time spent by all maps in occupied slots (ms)=10098864
		Total time spent by all reduces in occupied slots (ms)=3520032
		Total time spent by all map tasks (ms)=210393
		Total time spent by all reduce tasks (ms)=36667
		Total vcore-milliseconds taken by all map tasks=210393
		Total vcore-milliseconds taken by all reduce tasks=36667
		Total megabyte-milliseconds taken by all map tasks=323163648
		Total megabyte-milliseconds taken by all reduce tasks=112641024
	Map-Reduce Framework
		Map input records=40000
		Map output records=40008
		Map output bytes=560128
		Map output materialized bytes=32821
		Input split bytes=736
		Combine input records=0
		Combine output records=0
		Reduce input groups=74
		Reduce shuffle bytes=32821
		Reduce input records=40008
		Reduce output records=73
		Spilled Records=80016
		Shuffled Maps =24
		Failed Shuffles=0
		Merged Map outputs=24
		GC time elapsed (ms)=6948
		CPU time spent (ms)=38280
		Physical memory (bytes) snapshot=5661511680
		Virtual memory (bytes) snapshot=40669827072
		Total committed heap usage (bytes)=4680318976
	Shuffle Errors
		BAD_ID=0
		CONNECTION=0
		IO_ERROR=0
		WRONG_LENGTH=0
		WRONG_MAP=0
		WRONG_REDUCE=0
	File Input Format Counters
		Bytes Read=5145392
	File Output Format Counters
		Bytes Written=1168
22/09/21 23:11:39 INFO streaming.StreamJob: Output directory: s3://cscie88-bucket/output/python/
```





## Running the bundled word count example
- Create (or reuse) an S3 bucket, create folders eg. "docs" and "output" on the bucket. 
- Upload a text file (preferably large) to the "docs" folder. eg. https://www.gutenberg.org/files/4300/4300-0.txt
- Connect to your EMR Hadoop cluster using the ssh information on the cluster summary page (see link "Connect to the Master Node Using SSH")
- Run the following command
```
hadoop jar /usr/lib/hadoop-mapreduce/hadoop-mapreduce-examples.jar wordcount s3://cscie88-bucket/docs/ s3://cscie88-bucket/output/output1/
```
When complete, use the aws s3 command to copy the output
```
aws s3 cp --recursive s3://cscie88-bucket/output/output1/ .
```
Then, examine the output (shows the top 10 words)
```
cat output1/part* | sort -k 2n | tail -n 10
```
You should see something like the following
```
root@2de8d7eec93f:/docs/java# cat output/part* | sort -k 2n | tail -n 10
with    2391
I       2429
he      2712
his     3034
in      4612
to      4788
a       5839
and     6543
of      8137
the     13613
```


## Submitting Hadoop Map Reduce Jobs (Python)

- We will run a simple python wordcount example against a large text file. 
- Obtain a text file. You can use this one from the Guttenberg project.
- https://www.gutenberg.org/files/4300/4300-0.txt
- Create an S3 bucket (or reuse an existing one) and upload the file to there. Note the s3 url of the file.
- Connect to the cluster master node using the ssh information from the cluster summary page
- Once on the master node follow these steps. 
```
# Create a directory for our files
mkdir hw4
cd hw4
```
- Create Files

1. Create a python mapper program (copy and paste the following as one command)
```
cat <<'EOF' >> mapper.py
#!/usr/bin/env python
"""mapper.py"""

import sys

# input comes from STDIN (standard input)
for line in sys.stdin:
    # remove leading and trailing whitespace
    line = line.strip()
    # split the line into words
    words = line.split()
    # increase counters
    for word in words:
        # write the results to STDOUT (standard output);
        # what we output here will be the input for the
        # Reduce step, i.e. the input for reducer.py
        #
        # tab-delimited; the trivial word count is 1
        print( '%s\t%s' % (word, 1))
EOF
```

2. Create the reducer.py program (copy and paste the following as one complete command)
```
cat <<'EOF' >> reducer.py
#!/usr/bin/env python3
"""reducer.py"""

from operator import itemgetter
import sys

current_word = None
current_count = 0
word = None

# input comes from STDIN
for line in sys.stdin:
    # remove leading and trailing whitespace
    line = line.strip()

    # parse the input we got from mapper.py
    word, count = line.split('\t', 1)

    # convert count (currently a string) to int
    try:
        count = int(count)
    except ValueError:
        # count was not a number, so silently
        # ignore/discard this line
        continue

    # this IF-switch only works because Hadoop sorts map output
    # by key (here: word) before it is passed to the reducer
    if current_word == word:
        current_count += count
    else:
        if current_word:
            # write result to STDOUT
            print ('%s\t%s' % (current_word, current_count))
        current_count = count
        current_word = word

# do not forget to output the last word if needed!
if current_word == word:
    print ('%s\t%s' % (current_word, current_count))
EOF
```

- (Optional) Test Locally (Optional)
Its always a good idea to test your code before submitting it to Hadoop. This will find simple syntax errors etc. 
```
echo "foo foo quux labs foo bar quux" | python ./mapper.py
```
Sort the keys (will be required by reducer.py
```
echo "foo foo quux labs foo bar quux" | python3 ./mapper.py | sort -k1,1
```
Finally put it all together
```
echo "foo foo quux labs foo bar quux" | python3 ./mapper.py | sort -k1,1 | python3 ./reducer.py 
```
Should see something like this …
```
bar     1
foo     3
labs    1
quux    2
```
- Submit job to hadoop using the 'hadoop' command
```
# submit hadoop job
hadoop jar /usr/lib/hadoop/hadoop-streaming.jar \
-file ./mapper.py \
-file ./reducer.py \
-mapper "mapper.py" \
-reducer "reducer.py" \
-input s3://cscie88-bucket/ulysses.txt \
-output s3://cscie88-bucket/output/output/
```

- should see the following at end of output
```
2022-09-11 20:43:37,282 INFO streaming.StreamJob: Output directory: /user/hadoop/output
```
- Examine the output
```
# get the output files from the output directory
hdfs dfs -get /user/hadoop/output
```
Concatenate and sort the results
```
cat output/part* | sort -k 2n | tail -n 10
```
Should see something like this ...
```
with    2391
I       2429
he      2712
his     3034
in      4612
to      4788
a       5839
and     6543
of      8137
the     13613
```

- (note) use this command to remove the output dir when done
```
hdfs dfs -rm -r /user/hadoop/output
```

## Submitting Hadoop Map Reduce Jobs (Java)

- See [hadoop tutorial](https://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html) for full details on this example
- For this lab we are going to use hdfs for simplicity, for the assignment you must use s3 buckets
- In this lab, we are going to compile and run the java WordCount sample against a sample text file. 
1. SSH to the master node of your cluster using the connection information in the summary page of your cluster. 
2. Create a directory to work in
```
mkdir hw4
cd hw4
```
3. Obtain a text file and copy it into a hdfs directory.
```
curl https://www.gutenberg.org/files/4300/4300-0.txt -o ulysses.txt
hdfs dfs -mkdir /tmp/docs
hdfs dfs -put ./ulysses.txt /tmp/docs
# verify its there
hdfs dfs -ls /tmp/docs
```
4. Create the java WordCount file (Copy and paste the following as a single complete command)
```
cat <<'EOF' >> WordCount.java
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordCount {

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken());
        context.write(word, one);
      }
    }
  }

  public static class IntSumReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(WordCount.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
EOF
```
5. Set the HADOOP classpath
```
export HADOOP_CLASSPATH=/usr/lib/jvm/java/lib/tools.jar
```
6. Compile the code
```
hadoop com.sun.tools.javac.Main WordCount.java
```
7. Create a jar file
```
jar cf wc.jar WordCount*.class
```
8. Submit jar as a hadoop job
```
hadoop jar wc.jar WordCount /tmp/docs /tmp/output             
```
9. Once complete you can examine the output
```
hdfs dfs -get /tmp/output
cat output/part* | sort -k 2n | tail -n 10
```
You should see something like the following
```
root@2de8d7eec93f:/docs/java# cat output/part* | sort -k 2n | tail -n 10
with    2391
I       2429
he      2712
his     3034
in      4612
to      4788
a       5839
and     6543
of      8137
the     13613
```
10. When done you can remove it with 
```
hdfs dfs -rm -r /tmp/output
```


## (Optional) Creating and Running Python MRJob jobs

The python MRJob library allows you to specify map reduce jobs in python and run them on several platforms including hadoop (and running locally).

To demonstrate MRJob we will download a sample dataset and run a python mrjob against it.

- Download kaggle Hotel_Reviews.csv file
```
curl https://cscie88-bucket.s3.us-east-2.amazonaws.com/Hotel_Reviews.csv -o Hotel_Reviews.csv
```
- copy it to hdfs
```
hdfs dfs -mkdir /tmp/noratings
hdfs dfs -put ./Hotel_Reviews.csv /tmp/noratings/Hotel_Reviews.csv
```
- Create the noratings.py file
- Copy and paste the following as a single command
```
cat <<'EOF' >> noratings.py
#!/usr/bin/env python
"""noratings.py"""

from mrjob.job import MRJob
from mrjob.step import MRStep
import csv

#split by ,
columns = 'Review,Rating'.split(',')

class NoRatings(MRJob):

   def steps(self):
       return[
           MRStep(mapper=self.mapper_get_ratings,
                 reducer=self.reducer_count_ratings)
       ]

   #Mapper function
   def mapper_get_ratings(self, _, line):
      reader = csv.reader([line])
      for row in reader:
          zipped=zip(columns,row)
          diction=dict(zipped)
          ratings=diction['Rating']
          #outputing as key value pairs
          yield ratings, 1

   #Reducer function
   def reducer_count_ratings(self, key, values):
       yield key, sum(values)

if __name__ == "__main__":
   NoRatings.run()
EOF
```

- Run it locally
```
python3 noratings.py Hotel_Reviews.csv 
```
Output should look like this.
```
Creating temp directory /tmp/noratings.root.20220915.142441.465410
Running step 1 of 1...
job output is in /tmp/noratings.root.20220915.142441.465410/output
Streaming final output from /tmp/noratings.root.20220915.142441.465410/output...
"Rating"        1
"3"     2184
"4"     6039
"5"     9054
"1"     1421
"2"     1793
```

- Run it in hadoop
```
python3 noratings.py -r hadoop hdfs://namenode:9000/tmp/noratings/Hotel_Reviews.csv 
```
Output should be like this
```
Running step 1 of 1...
  packageJobJar: [/tmp/hadoop-unjar915989040711424550/] [] /tmp/streamjob3136139107407519522.jar tmpDir=null
  Connecting to ResourceManager at resourcemanager/172.20.0.2:8032
  Connecting to Application History server at historyserver/172.20.0.6:10200
  Connecting to ResourceManager at resourcemanager/172.20.0.2:8032
  Connecting to Application History server at historyserver/172.20.0.6:1020
…
 Running job: job_1662646142390_0028
  Job job_1662646142390_0028 running in uber mode : false
   map 0% reduce 0%
   map 50% reduce 0%
   map 100% reduce 0%
   map 100% reduce 100%
  Job job_1662646142390_0028 completed successfully
…
"1"     1421
"2"     1793
"3"     2184
"4"     6039
"5"     9054
```

## TODO (Optional) EMR Step Execution

## (Optional) Enabling EMR User Interfaces via ssh tunnelling

By default the ports for an EMR Cluster user interfaces are not open. While you could modify the security groups linked in the cluster summary page and open the ports via firewall rules AWS does not recommend doing this. Instead they suggest using ssh tunnelling which will connect a port on your local machine, through a secure ssh tunnel, to a port on the destination machine. 

<img src="https://github.com/bmullan-pivotal/hw4/raw/main/images/emr-ui-tab.png">

To demonstrate this we will forward the port for the Resource Manager (8088). 
- On the EMR Cluster Application User Interfaces tab, copy the host and port number of the service you want to enable (note dont include the "http://" part). 
- From your local machine (one you can run a web browser on) start a terminal session and use the following ssh command
```
# replace the hostname below with the hostname for your cluster
ssh -i <key-pair-pem-file>.pem -N -L 8088:aws-emr-host-name:8088 hadoop@aws-emr-host-name
```
<img src="https://github.com/bmullan-pivotal/hw4/raw/main/images/emr-terminal-ssh.png">
- Now you can browse on your local web browser to http://localhost:8088
<img src="https://github.com/bmullan-pivotal/hw4/raw/main/images/emr-localhost-browser.png">
          
## (Optional) Running Hadoop in Docker 

Its possible to run hadoop in docker, which may be useful for testing locally or experimentation. Note, for the assignment to get full points you must still run your job in an AWS EMR cluster. These instructions use docker-compose which is a way of declaratively running a set of docker container services.

1. Install docker-compose
```
# downloads the executable
sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
```
```
# sets the execute permission on the file
sudo chmod +x /usr/local/bin/docker-compose
```
```
# verifies the installation
docker-compose --version
```

2. Clone the docker compose hadoop repository
```
git clone https://github.com/big-data-europe/docker-hadoop
```
Start the cluster
```
# changes into the cloned directory
cd docker-hadoop
```
```
# start the cluster (detached, runs in the background)
docker-compose up -d
```

3. Verify cluster is running
```
# list running containers (formatted)
docker ps --format "{{.ID}}: {{.Names}} {{.Ports}}"
```
```
#should see something like this …
f6ed848a4ed2: historyserver 8188/tcp 
2de8d7eec93f: namenode 0.0.0.0:9000->9000/tcp, :::9000->9000/tcp, 0.0.0.0:9870->9870/tcp, :::9870->9870/tcp
ca8cc6c9e9a1: datanode 9864/tcp
056b9a5c9b09: nodemanager 8042/tcp
a35e238de33c: resourcemanager 8088/tcp
```

4. Enable python in the running cluster

Run the following commands (installs python,pip and mrjob into each of the containers
```
docker exec -it namenode bash -c "apt update && apt install python3 -y && apt install python3-pip -y && pip3 install mrjob"
```
```
docker exec -it datanode bash -c "apt update && apt install python3 -y && apt install python3-pip -y && pip3 install mrjob"
```
```
docker exec -it resourcemanager bash -c "apt update && apt install python3 -y && apt install python3-pip -y && pip3 install mrjob"
```
```
docker exec -it nodemanager bash -c "apt update && apt install python3 -y && apt install python3-pip -y && pip3 install mrjob"
```

5. Verify the cluster by running a python map/reduce job

```
# exec into the namenode container
docker exec -it namenode bash
```
```
# in the container, create a directory for our files
mkdir hw4
cd hw4
```
- Create Files
```
# download a sample file,downloads the complete text of ‘Ulysses’
curl https://www.gutenberg.org/files/4300/4300-0.txt -o ulysses.txt
```
```
# copy it into our hdfs tmp directory 
hdfs dfs -mkdir /user/root/docs
hdfs dfs -put ./ulysses.txt /user/root/docs
```
- Create a python mapper program (copy and paste the following as one command)
```
cat <<'EOF' >> mapper.py
#!/usr/bin/env python3
"""mapper.py"""

import sys

# input comes from STDIN (standard input)
for line in sys.stdin:
    # remove leading and trailing whitespace
    line = line.strip()
    # split the line into words
    words = line.split()
    # increase counters
    for word in words:
        # write the results to STDOUT (standard output);
        # what we output here will be the input for the
        # Reduce step, i.e. the input for reducer.py
        #
        # tab-delimited; the trivial word count is 1
        print( '%s\t%s' % (word, 1))
EOF
```


- Create the reducer.py program (copy and paste the following as one complete command)
```
cat <<'EOF' >> reducer.py
#!/usr/bin/env python3
"""reducer.py"""

from operator import itemgetter
import sys

current_word = None
current_count = 0
word = None

# input comes from STDIN
for line in sys.stdin:
    # remove leading and trailing whitespace
    line = line.strip()

    # parse the input we got from mapper.py
    word, count = line.split('\t', 1)

    # convert count (currently a string) to int
    try:
        count = int(count)
    except ValueError:
        # count was not a number, so silently
        # ignore/discard this line
        continue

    # this IF-switch only works because Hadoop sorts map output
    # by key (here: word) before it is passed to the reducer
    if current_word == word:
        current_count += count
    else:
        if current_word:
            # write result to STDOUT
            print ('%s\t%s' % (current_word, current_count))
        current_count = count
        current_word = word

# do not forget to output the last word if needed!
if current_word == word:
    print ('%s\t%s' % (current_word, current_count))
EOF
```

- Test Locally (Optional)

(optional) test it locally, its always a good idea if you can test your code locally before submitting as a job. This will find simple syntax errors etc. 
```
echo "foo foo quux labs foo bar quux" | python3 ./mapper.py
```
Sort the keys (will be required by reducer.py
```
echo "foo foo quux labs foo bar quux" | python3 ./mapper.py | sort -k1,1
```
Finally put it all together
```
echo "foo foo quux labs foo bar quux" | python3 ./mapper.py | sort -k1,1 | python3 ./reducer.py 
```
Should see …
```
bar     1
foo     3
labs    1
quux    2
```

- Submit job to hadoop

- (Option1) submit your job to the hadoop cluster as a streaming job using the mapred cli
```
mapred streaming -files ./mapper.py,./reducer.py -input hdfs://namenode:9000/user/root/docs -output hdfs://namenode:9000/user/root/output -mapper "python3 mapper.py" -reducer "python3 reducer.py"
```
- (Option2) Submit job using the “traditional” hadoop command
```
# docker: to match the file locations in AWS EMR run these 2 commands mkdir /usr/lib/hadoop
ln -s /opt/hadoop-3.2.1/share/hadoop/tools/lib/hadoop-streaming-3.2.1.jar /usr/lib/hadoop/hadoop-streaming.jar 

# submit hadoop job
hadoop jar /usr/lib/hadoop/hadoop-streaming.jar \
-file ./mapper.py \
-file ./reducer.py \
-mapper "mapper.py" \
-reducer "reducer.py" \
-input hdfs://namenode:9000/user/root/docs \
-output hdfs://namenode:9000/user/root/output
```

- should see the following at end of output
```
2022-09-11 20:43:37,282 INFO streaming.StreamJob: Output directory: hdfs://namenode:9000/user/root/output
```
- Examine the output
```
# get the output file
hdfs dfs -get /user/root/output/part-00000

# examine it
sort -k 2n part-00000 | tail -n 10


with    2391
I       2429
he      2712
his     3034
in      4612
to      4788
a       5839
and     6543
of      8137
the     13613
```

- (note) use this command to remove the output dir when done
```
hdfs dfs -rm -r /user/root/output
```

