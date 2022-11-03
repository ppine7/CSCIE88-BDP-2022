# CSCIE88 - LAB for Lecture 9 - Apache Flink

## Lab Notes / Topics

- Running Kafka & Flink using Docker Compose
- Running the sample wordcount java & python examples
- Creating kafka topics for input and output
- Running the supplied java kafka producer
- Building and running a java flink kafka source app.
- Building and running a python flink kafka source app.
- Running and testing the P3 example python program
- (Optional) Building the pyflink docker image.

## Useful links

- Flink javadocs
https://nightlies.apache.org/flink/flink-docs-release-1.15/api/java/

- Flink pydocs
https://nightlies.apache.org/flink/flink-docs-stable/docs/dev/python/datastream_tutorial/


- Specific Classes
- [DataStream](https://nightlies.apache.org/flink/flink-docs-release-1.15/api/java/org/apache/flink/streaming/api/datastream/DataStream.html)
- [KafkaSource](https://nightlies.apache.org/flink/flink-docs-stable/api/java/org/apache/flink/connector/kafka/source/KafkaSource.html)
- [KafkaSink](https://nightlies.apache.org/flink/flink-docs-stable/api/java/org/apache/flink/connector/kafka/sink/KafkaSink.html)

- David Anderson
https://stackoverflow.com/users/2000823/david-anderson

- Examples
https://github.com/ververica/flink-training-exercises/blob/master/src/main/java/com/ververica/flinktraining/examples/datastream_java/windows/WhyLate.java#L60


## Running Kafka & Flink using Docker Compose

- The docker-compose file will start an instance of zookeeper, kafka broker, flink jobmanager and flink taskmanager
- This uses a custome flink image which includes the python dependencies, if you would like to see how this is build see the section 'Building a py-flink image'

```
# from the week9_flink directory
cd docker
# create a data directory which will mounted by docker as a volume
mkdir flink-data
# open the permissions for the directory
sudo chmod -R 777 flink-data
# start the docker services
docker-compose up -d
```
- You can check your services are running by running 
```
docker ps --format "{{.ID}} {{.Names}} {{.State}} {{.Ports}} {{.Status}}"
```
- you should see 
```
3eb503d68024 taskmanager running 6123/tcp, 8081/tcp Up 12 minutes
80cf8e3b7e83 jobmanager running 6123/tcp, 0.0.0.0:8081->8081/tcp Up 12 minutes
9edfc3d2392d broker1 running 0.0.0.0:9092->9092/tcp Up 12 minutes (healthy)
29dbc1e98894 zookeeper running 2888/tcp, 0.0.0.0:2181->2181/tcp, 3888/tcp Up 12 minutes
```
- Some other health checks
```
# attempt to connect to the broker (from docker host)

nc -vz localhost 9092

# attempt to connect to the broker from within the docker network

docker run --rm --network=lab9-demo-net subfuzion/netcat -vz broker1 9092
```
- (Optional) If you suspect problems with your kafka broker check the logs with this command
```
docker logs broker1 -f
```


## Flink user interface

- You can access the flink ui on port 8081 (by default) on the host flink jobmanager is running on.

![flink ui screenshot](https://github.com/bmullan-pivotal/CSCIE88-BDP-Staff/blob/master/week9_flink/flink-ui.png?raw=true)

## Running flink wordcount example (java)

- Note, flink user interface is available on port 8081, you will need to add a firewall rule to the security group for your instance to expose port 8081
- You can then access the interface using the public ip of your instance eg. http:public-ip:8081
- connect to the flink jobmanager instance
```
# from week9_flink directory
cd docker
# download a sample text file
curl -o flink-data/ulysses.txt https://www.gutenberg.org/files/4300/4300-0.txt
# note: the 'flink-data' directory is available in the flink image as '/flink-data'
docker exec -it jobmanager bash
#
root@jobmanager:/opt/flink# 
```
- Run the flink java batch job with 
```
bin/flink run --detached \
examples/batch/WordCount.jar \
--input file:///flink-data/ulysses.txt \
--output file:///flink-data/results-java
```
- Should see something like this 
```
Job has been submitted with JobID 62332a4f12d3a0949ffdc7834064e480
```
- You can list jobs with 
```
bin/flink list
```
- Check the user interface for the status of your job

- When finished you can examine the output in the output file
```
cat /flink-data/results-java | sort -k 2n | tail -n 10
s 2839
i 2989
his 3328
he 4221
in 4992
to 5036
a 6567
and 7275
of 8264
the 15092
```

- You can find the source for this sample here
[WordCount.java](https://github.com/apache/flink/blob/master/flink-examples/flink-examples-streaming/src/main/java/org/apache/flink/streaming/examples/wordcount/WordCount.java)


## Running flink wordcount example (python)

- Note, flink user interface is available on port 8081, you will need to add a firewall rule to the security group for your instance to expose port 8081
- You can then access the interface using the public ip of your instance eg. http://<public-ip>:8081
- connect to the flink jobmanager instance
```
# from week9_flink directory
cd docker

# note: the 'flink-data' directory is available in the flink image as '/flink-data'
docker exec -it jobmanager bash
#
root@jobmanager:/opt/flink# 
```
- Run the flink python batch job with 
```
bin/flink run --detached -py examples/python/datastream/word_count.py --output file:///flink-data/results-python

```
- should see 
```
Job has been submitted with JobID dd551159906ac66b51dbce8d799ca5a8
root@jobmanager:/opt/flink#
```
- you can check the job is running with ...
```
 ./bin/flink list
```
- output
```
Waiting for response...
------------------ Running/Restarting Jobs -------------------
25.10.2022 20:52:32 : dd551159906ac66b51dbce8d799ca5a8 : Flink Batch Job (RUNNING)
--------------------------------------------------------------
No scheduled jobs.
```
- when job finishes, you can check the output, for example list the 10 most common words
```
find /flink-data/results-python -name "*.ext" -exec cat {} \; |\
awk -F "," '{gsub(/\(/,"",$1); gsub(/\)/,"",$2); print $1 " " $2}' |\
sort -k 2n |\
tail -n 10
```
- output
```
us 3
To 4
we 4
And 5
a 5
The 7
and 7
to 7
of 14
the 15
```

- Here is the source of the python example
```

import argparse
import logging
import sys

from pyflink.common import WatermarkStrategy, Encoder, Types
from pyflink.datastream import StreamExecutionEnvironment, RuntimeExecutionMode
from pyflink.datastream.connectors import (FileSource, StreamFormat, FileSink, OutputFileConfig,
                                           RollingPolicy)

word_count_data = ["To be, or not to be,--that is the question:--",
                   "Whether 'tis nobler in the mind to suffer",
...
                   "Be all my sins remember'd."]

def word_count(input_path, output_path):
    env = StreamExecutionEnvironment.get_execution_environment()
    env.set_runtime_mode(RuntimeExecutionMode.BATCH)
    # write all the data to one file
    env.set_parallelism(1)

    # define the source
    if input_path is not None:
        ds = env.from_source(
            source=FileSource.for_record_stream_format(StreamFormat.text_line_format(),
                                                       input_path)
                             .process_static_file_set().build(),
            watermark_strategy=WatermarkStrategy.for_monotonous_timestamps(),
            source_name="file_source"
        )
    else:
        print("Executing word_count example with default input data set.")
        print("Use --input to specify file input.")
        ds = env.from_collection(word_count_data)

    def split(line):
        yield from line.split()

    # compute word count
    ds = ds.flat_map(split) \
           .map(lambda i: (i, 1), output_type=Types.TUPLE([Types.STRING(), Types.INT()])) \
           .key_by(lambda i: i[0]) \
           .reduce(lambda i, j: (i[0], i[1] + j[1]))

    # define the sink
    if output_path is not None:
        ds.sink_to(
            sink=FileSink.for_row_format(
                base_path=output_path,
                encoder=Encoder.simple_string_encoder())
            .with_output_file_config(
                OutputFileConfig.builder()
                .with_part_prefix("prefix")
                .with_part_suffix(".ext")
                .build())
            .with_rolling_policy(RollingPolicy.default_rolling_policy())
            .build()
        )
    else:
        print("Printing result to stdout. Use --output to specify output path.")
        ds.print()

    # submit for execution
    env.execute()


if __name__ == '__main__':
    logging.basicConfig(stream=sys.stdout, level=logging.INFO, format="%(message)s")

    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--input',
        dest='input',
        required=False,
        help='Input file to process.')
    parser.add_argument(
        '--output',
        dest='output',
        required=False,
        help='Output file to write results to.')

    argv = sys.argv[1:]
    known_args, _ = parser.parse_known_args(argv)

    word_count(known_args.input, known_args.output)
```


## Create the topics required by the assignment

![hw9](https://github.com/bmullan-pivotal/CSCIE88-BDP-2022/blob/main/week9_flink/hw9.png?raw=true)


```
docker exec broker1 kafka-topics --create --topic p2_input --bootstrap-server localhost:9092

docker exec broker1 kafka-topics --create --topic p2_output  --bootstrap-server localhost:9092

```
- should see
```
Created topic p2_input.

Created topic p2_output.
```


## Running the supplied java kafka producer

- We have supplied a kafka producer which will put log line events on the 'p2_input' topic.
- To run it we have supplied a java docker image to run the java code in the same network as the kafka broker. Its optional, but if you would like to see how the image is built examine the 'Dockerfile' file in the 'java-producer' sub-directory.
- Recommend starting another terminal session to run the producer in so you can leave it running. To run the producer run these commands from the week9_flink directory
```
# start the java command line image
# important: cd to the week9_flink directory before running this 
docker run --network=lab9-demo-net -v $PWD/java-producer:/java-producer --rm -it openjdk:11-jdk bash
```
- You should see a prompt in the container like this 
```
root@04305733852b:/# 
```
- Run these commands to build and run the producer
```
cd /java-producer
```
- Now we can run the producer with this command. The value -Dsec=100 will producer an event on the topic every 100 ms , you can play with this value to send events at a faster or slower rate
```
java \
-DinputFile=./logs/file-input1.csv \
-Dserver=broker1:29092 \
-Dtopic=p2_input \
-Dsec=100 \
-jar ./build/libs/hw9-java-producer-1.0-SNAPSHOT.jar

```
- All going well you should see something like 
```
[kafka-producer-network-thread | producer-1] INFO org.apache.kafka.clients.Metadata - [Producer clientId=producer-1] Cluster ID: 4pwvQEA9R4u7GbFcNlVU0Q
[main] INFO org.cscie88.kafka.SimpleConsumer - Produced message topic:p2_input timestamp:1666562224664 partition:0
[main] INFO org.cscie88.kafka.SimpleConsumer - Producing message:1928c255065b49e0b87a70314076fdac 2022-09-03T13:49:25.490998800Z http://example.com/?url=056 user-080 BW Firefox windows 402 0.3237
[main] INFO org.cscie88.kafka.SimpleConsumer - Produced message topic:p2_input timestamp:1666562227698 partition:0
[main] INFO org.cscie88.kafka.SimpleConsumer - Producing message:d3d08e7edb8443e0a2f54e8a0851abc9 2022-09-03T13:49:51.410998800Z http://example.com/?url=066 user-068 BT Firefox windows 207 0.4069
```
- You can test consuming messags from the topics by running this command in another terminal session
```
docker exec broker1 kafka-console-consumer \
   --topic p2_input \
   --bootstrap-server broker1:9092
```
- You should see something like this ...
```
f83062a1b2eb497e9225430b53a223f7 2022-09-03T14:20:05.810998800Z http://example.com/?url=024 user-061 BZ Firefox windows 504 0.3337
260bdfaf1d9c43caaa8930d73becf899 2022-09-03T14:20:31.730998800Z http://example.com/?url=163 user-026 MX Edge Linux 205 0.5401
95b13197f05e4707a815c1b31e90d2c0 2022-09-03T14:20:57.650998800Z http://example.com/?url=166 user-072 HR Opera IOS 431 0.6431
```

## Build and Run a java flink kafka application.

- build the jar
```
# from week9_flink directory
cd java/p2_flink_kafka_test
gradle build
# copy jar to flink directory
cp build/libs/p2_flink_kafka_test-1.0-SNAPSHOT.jar ../../docker/flink-data
```
- submit the job to the flink job manager
```
# connect to the flink job manager and submit the job
docker exec -it jobmanager bash

bin/flink run --detached -c cscie88.KafkaSourceApp /flink-data/p2_flink_kafka_test-1.0-SNAPSHOT.jar
```
- check the results topic (also check the user interface http://ip:8081)
```
docker exec broker1 kafka-console-consumer --topic p2_output --bootstrap-server localhost:9092
```
- Note, this job runs continuously. To cancel it select it in the UI and select "Cancel Job"
- or run this command
```
./bin/flink cancel <job-id>
```


## Build and Run a python flink kafka application.
- copy the python file
```
# from week9_flink directory
cp python/p2_kafka-source-app.py ./docker/flink-data
```
- submit the python job, first connect to the jobmanager
```
docker exec -it jobmanager bash
```
- then submit the python job
```
./bin/flink run --detached -py /flink-data/p2_kafka-source-app.py
```
- check the results topic (also check the user interface http://ip:8081)
```
docker exec broker1 kafka-console-consumer --topic p2_output --bootstrap-server localhost:9092
```

## Running and testing the P3 example python program
In order to test and run the p3_flink-kafka-python.py program we will follow this sequence. 
1. Copy the program so we can run it from our docker image (if you have installed your own pyflink environment this step is not required)
2. Setup to produce json kafka messages (either sending individual messages or running a script to produce them).
3. Running our python program and inspecting the results

### 1. Copy the program
```
# from the week9_flink directory
cp python/p3_kafka-source-example.py docker/flink-data
```
### 2. Generate test messages on the kafka topic
- Option 1: Create messages manually by running the kafka-console-producer
```
docker exec broker1 kafka-console-producer --topic p3_input --bootstrap-server localhost:9092
```
Then copy and paste json messages. This is useful for testing as you can edit the messages to test with different values. For sample input review the files in the /week10_ksql/logs_json folder
- Option 2: Generate the messages from a json input file.
A script 'python-json-producer.sh' is provided which will read one of the json logs file into the topic 'p3_input'. You can edit the script to change the file, message send rate, topic etc.
```
# install utilities
sudo apt-get install kafkacat && sudo apt-get install pv
# run the script
./python-json-producer.sh
```
Should see ...
```
ubuntu@ip-172-31-16-46:~/nov-3/CSCIE88-BDP-2022/week9_flink/python$ ./python-json-producer.sh 
26.2KiB 0:00:12 [2.16KiB/s] [                              <=>                                                                                                   ] 
```
You can check that messages are being published to the topic with 
```
docker exec broker1 kafka-console-consumer --topic p3_input --bootstrap-server localhost:9092
```
### 3. Running the python program and inspecting the results
Connect to the jobmanager instance (we do this because pyflink is installed in this image, if you did a local install of pyflink this step is not necessary)
```
docker exec -it jobmanager bash
```
Run the python code (locally)
```
cd /flink-data
python p3_kafka-source-example.py 
```
should see ...
```
root@jobmanager:/flink-data# python p3_kafka-source-example.py 

Source Schema
(
  `uuid` STRING,
  `eventTimestamp` STRING,
  `url` STRING,
  `userid` STRING,
  `country` STRING,
  `uaBrowser` STRING,
  `uaOs` STRING,
  `responseCode` STRING,
  `ttfb` STRING,
  `proctime` TIMESTAMP_LTZ(3) NOT NULL *PROCTIME* AS PROCTIME()
)

Process Sink Schema
(
  `uaOs` STRING,
  `window_end` TIMESTAMP(3) NOT NULL,
  `cnt` BIGINT NOT NULL
)
```
You can check what values are being written to the output topic with 
```
docker exec broker1 kafka-console-consumer --topic p3_output --bootstrap-server localhost:9092
```
eg.
```
ubuntu@ip-172-31-16-46:~$ docker exec broker1 kafka-console-consumer --topic p3_output --bootstrap-server localhost:9092
{"uaOs":"IOS","window_end":"2022-11-03 20:51:45","cnt":13}
{"uaOs":"windows","window_end":"2022-11-03 20:51:45","cnt":9}
{"uaOs":"Android","window_end":"2022-11-03 20:51:45","cnt":7}
{"uaOs":"Linux","window_end":"2022-11-03 20:51:45","cnt":7}
{"uaOs":"Mac","window_end":"2022-11-03 20:51:45","cnt":12}
{"uaOs":"windows","window_end":"2022-11-03 20:51:50","cnt":10}
{"uaOs":"Android","window_end":"2022-11-03 20:51:50","cnt":12}
{"uaOs":"IOS","window_end":"2022-11-03 20:51:50","cnt":8}
{"uaOs":"Mac","window_end":"2022-11-03 20:51:50","cnt":9}
{"uaOs":"Linux","window_end":"2022-11-03 20:51:50","cnt":10}
```
- To run as a flink job in the cluster see the [example](https://github.com/ppine7/CSCIE88-BDP-2022/blob/main/week9_flink/README.md#running-flink-wordcount-example-python) above. 



 

## (Optional) Building the pyflink image

- This is not required for the assignment. You can find the Dockerfile to build the image in
[py-flink/Dockerfile](py-flink/Dockerfile) 

- This custom image is based on this article 
https://wicaksonodiaz.medium.com/setup-pyflink-development-environment-76d8491a9ad7

- To build the image run these commands

```
cd CSCIE88-BDP-Class/week9_flink/py-flink
# download the kafka sql connector
wget https://repo1.maven.org/maven2/org/apache/flink/flink-sql-connector-kafka_2.12/1.14.0/flink-sql-connector-kafka_2.12-1.14.0.jar

# download flink 1.14.0 distribution
wget https://files.pythonhosted.org/packages/33/6b/5f173570d61b164a41b30f56df7d16cd7c055809d244a236d3f6787c153e/apache-flink-1.14.0.tar.gz 

# download flink libraries
wget https://files.pythonhosted.org/packages/3d/a3/685916e56b1e5ebdb3fe0ce6692c0e17a300bddf5cfa8ab7957289714aac/apache-flink-libraries-1.14.0.tar.gz

# Build the docker image
docker build --tag pyflink:1.14.0 
# you can push this to your dockerhub repo by doing the following
docker tag pyflink:1.14.0 <repo-account>/pyflink:1.14.0 
docker push <repo-account>/pyflink:1.14.0 

```

