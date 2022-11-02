# CSCIE88 - LAB for Lecture 10 - ksql

## Lab Notes / Topics

- Running Kafka & ksql using Docker Compose
- Create a topic to be used in the lab
- Access the ksql cli
- Access the confluent ksql web ui
- Create a stream from the kafka topic
- Create a persistent stream and table from the kafka stream
- Automate production of json messages for the kafka topic

## Useful links

## Running Kafka & ksql using Docker Compose

- The docker-compose file will start an instance of zookeeper, kafka broker & ksql
- run these commands from the week10_ksql directory
```

cd docker
docker-compose up -d
```
- You can check your services are running by running 
```
docker ps --format "{{.ID}} {{.Names}} {{.State}} {{.Ports}} {{.Status}}"
```
- you should see 
```
9cc6c4a93b41 control-center running 0.0.0.0:9021->9021/tcp Up 3 minutes
b5003dbef67b ksql-datagen running  Up 3 minutes
3401eb1c8c59 ksqldb-cli running  Up 3 minutes
d3ec84916a69 ksqldb-server running 0.0.0.0:8088->8088/tcp Up 3 minutes
b27aed160da4 rest-proxy running 0.0.0.0:8082->8082/tcp Up 3 minutes
f011afce3593 connect running 0.0.0.0:8083->8083/tcp, 9092/tcp Up 3 minutes
5909f0274b01 schema-registry running 0.0.0.0:8081->8081/tcp Up 3 minutes
07c8ad13c1ed broker running 0.0.0.0:9092->9092/tcp, 0.0.0.0:9101->9101/tcp Up 3 minutes
2903f7f2aed3 zookeeper running 2888/tcp, 0.0.0.0:2181->2181/tcp, 3888/tcp Up 3 minutes```
```
- Some other health checks
- attempt to connect to the broker (from docker host)
```
nc -vz localhost 9092
```
- (Optional) If you suspect problems with your kafka broker check the logs with this command
```
docker logs broker -f
```

## Create a topic to be used in the lab
- run the following command
```
docker exec broker kafka-topics --create --topic p5_input --partitions 3 --bootstrap-server localhost:9092
```


## Access the ksql cli

```
# from the week10_ksql/docker directory
docker-compose exec ksqldb-cli ksql http://ksqldb-server:8088

# eg. show topics
ksql> show topics;

 Kafka Topic                 | Partitions | Partition Replicas
---------------------------------------------------------------
 default_ksql_processing_log | 1          | 1
 docker-connect-configs      | 1          | 1
 docker-connect-offsets      | 25         | 1
 docker-connect-status       | 5          | 1
---------------------------------------------------------------

```


## Access the confluent ksql ui

- The ksql web user interface can be accessed (by default) on port 9021 of your docker host. If you are running on AWS you will need to open the port by adding a firewall rule to the instances security group. 

<img src="https://github.com/bmullan-pivotal/CSCIE88-BDP-Staff/blob/master/week10_ksql/week10_ksql_control-center.png?raw=true" width="200">

## Create a stream from the kafka topic

```
# from the ksql cli 
CREATE STREAM logs_stream_logs (
uuid VARCHAR,
eventTimestamp VARCHAR,
url VARCHAR,
userId VARCHAR,
country VARCHAR,
uaBrowser VARCHAR,
uaOs VARCHAR,
responseCode INTEGER,
ttfb DOUBLE
) WITH (
kafka_topic='p5_input',
value_format='JSON',
timestamp='eventTimestamp',
timestamp_format='yyyy-MM-dd HH:mm:ss');
```

## Test your kafka stream. 
We are going to test the stream (and the kafka connection) with the following sequence. 
1. Run a ksql query to continuously query the stream.
2. Use the Confluent web ui to produce a message on the topic.
3. Verify we see the message reflected in the ksql query from 1. 

### 1. Run a ksql query to continuously query the stream.
Start the ksql cli (see above) and execute the following query
```
select * from logs_stream_logs emit changes;
```
### 2. Use the confluent ui to produce a message on the topic.
Access the confluent ksql Control Center user interface (see above) on http://your-host:9021
- Select the Cluster 
- Select Topics from the menu bar on left, select "p5_input" topic, selct "Messages" tab. 
- Paste a message in json format eg. (you can also use values from the files in logs_json directory)
```
{ "uuid":"41daf40b28bd48958eb870bc65b8d78a","eventTimestamp":"2022-09-03 13:48:59","url":"http://example.com/?url=124","userId":"user-035","country":"GN","uaBrowser":"IE","uaOs":"windows","responseCode":207,"ttfb":0.6509  }
```
- Enter a numeric value for key (can be a random number, will be used to select partition)
- and click 'Produce Message' 

### 3. Verify we see the message reflected in the ksql query from 1. 
Go back to the cli from step 1. You should see the message in the output eg. 
```
ksql> select * from logs_stream_logs
>emit changes;
+---------------------+---------------------+---------------------+---------------------+---------------------+---------------------+---------------------+---------------------+---------------------+
|UUID                 |EVENTTIMESTAMP       |URL                  |USERID               |COUNTRY              |UABROWSER            |UAOS                 |RESPONSECODE         |TTFB                 |
+---------------------+---------------------+---------------------+---------------------+---------------------+---------------------+---------------------+---------------------+---------------------+
|41daf40b28bd48958eb87|2022-09-03 13:48:59  |http://example.com/?u|user-035             |GN                   |IE                   |windows              |207                  |0.6509               |
|0bc65b8d78a          |                     |rl=124               |                     |                     |                     |                     |                     |                     |
```


## Create a persistent stream and table from the kafka stream.
The sequence we will follow here is 
1. Create a persistent stream that will continuously query the kafka stream created above.
2. Create a table which is continuous query of the persistent stream. 
3. Verify the table by checking the created internal topics and sending some output

### 1. Create a persistent stream
From the ksql cli run
```
CREATE STREAM logs_stream_logs_ps AS SELECT * FROM logs_stream_logs;
```
Should see ...
```
 Message
--------------------------------------------------
 Created query with ID CSAS_LOGS_STREAM_LOGS_PS_3
--------------------------------------------------
```

### 2. Create a table 
Also from the ksql cli run the following command. The table aggregates by count of the uaOs column, and groups the results by a tumbling window.
```
CREATE TABLE events_by_os AS SELECT
uaOs,
count(*) as osCount
FROM logs_stream_logs_ps
WINDOW TUMBLING (SIZE 60 SECONDS)
GROUP BY uaOs;
```
should see ...
```
 Message
-------------------------------------------
 Created query with ID CTAS_EVENTS_BY_OS_5
-------------------------------------------
```

### 3. Verify the table
The table is created on demand so first we must send some messages. 
- Follow instructions above using the control center user interface to send some messages (try altering the the timestamp to fall within and without the 60 second tumbling window
- Once we have sent some messags show the list of topics from the ksql cli
```
list topics;
```
- Note that we have a topic for our persistent stream and for our table
```
Kafka Topic                 | Partitions | Partition Replicas
---------------------------------------------------------------
 EVENTS_BY_OS                | 1          | 1
 LOGS_STREAM_LOGS_PS         | 1          | 1
 ```

- Show the results in the table
Simple query
```
select * from events_by_os emit changes;
```
or with formatting of the window start and end
```
SELECT
uaOs,
from_unixtime(WINDOWSTART) as winStart,
from_unixtime(WINDOWEND) as winEnd,
osCount
FROM events_by_os
EMIT CHANGES;

```
- Experiment by sending messages with different timestamps and different values for os. Output will be something similar to the following
```

+--------------------------------------------------+--------------------------------------------------+--------------------------------------------------+--------------------------------------------------+
|UAOS                                              |WINSTART                                          |WINEND                                            |OSCOUNT                                           |
+--------------------------------------------------+--------------------------------------------------+--------------------------------------------------+--------------------------------------------------+
|windows                                           |2022-09-03T13:48:00.000                           |2022-09-03T13:49:00.000                           |4                                                 |
|windows                                           |2022-09-03T13:48:00.000                           |2022-09-03T13:49:00.000                           |5                                                 |
|windows                                           |2022-09-03T13:48:00.000                           |2022-09-03T13:49:00.000                           |6                                                 |
|linux                                             |2022-09-03T13:48:00.000                           |2022-09-03T13:49:00.000                           |1                                                 |
|linux                                             |2022-09-03T13:48:00.000                           |2022-09-03T13:49:00.000                           |2                                                 |
|linux                                             |2022-09-03T13:50:00.000                           |2022-09-03T13:51:00.000                           |1                                                 |

```


## (Optional) Produce json messages for the kafka topic
You can automate the sending of messages to the kafka topic from a file using the kcat/kafkacat tool. https://github.com/edenhill/kcat
- To install on ubuntu
```
sudo apt-get install kafkacat
```
- To install on mac
```
brew install kcat
```
- A simple usage will dump all lines in a file to the topic eg. 
```
kcat -b localhost:9092 -t p5_input -P -l < small.json
```
- we can be a little more clever and send lines one by one in the file using a timed inverval (in this case every 1/10 second)
```
while read -r line;\
do echo $line; sleep .1; done \
< file-input1.json | \
kcat -b localhost:9092 -t p5_input -P -l
```
- finally you can see progress by installing the [pv](https://www.geeksforgeeks.org/pv-command-in-linux-with-examples/) utility ( ubuntu: sudo apt-get install pv mac: brew install pv) and adding it to command pipeline
```
while read -r line; do echo $line; sleep .1; done < file-input1.json | pv | kcat -b localhost:9092 -t p5_input -P -l
```
- this will produce output like this ...
```
49.3KiB 0:00:25 [1.98KiB/s] [                                                                     <=>                                                                     ]
```

