# Week 11 Cassandra Lab Notes

Topics
- Starting a cassandra cluster using docker
- Running cql commands in in cqlsh
- Python code walk through

## Useful Links

- [Basic Rules of Cassandra Data Modelling](https://www.datastax.com/blog/basic-rules-cassandra-data-modeling)
- [Getting Started with the Python API](https://docs.datastax.com/en/developer/python-driver/3.25/getting_started/)
- [Python API](https://docs.datastax.com/en/developer/python-driver/3.25/api/)
- [Java Quickstart](https://docs.datastax.com/en/developer/java-driver/4.3/manual/core/)
- [Java API](https://docs.datastax.com/en/developer/java-driver/4.3/)
- [CQL Aggregate Functions](https://docs.datastax.com/en/dse/6.0/cql/cql/cql_reference/cqlAggregates.html)

## Starting a Cassandra cluster using docker
I was unable to get cassandra working well with docker-compose so we are going to start a cassandra cluster using individual docker commands. 
- First, create a docker network
```
docker network create cn
```
- Then create the first node of our cluster
```
docker run --name node1 -p 9042:9042 --network cn -d cassandra
```
- Allow it a couple of minutes to come up, then check the status with the cassandra nodetool utility
```
docker exec node1 nodetool status
```
You should see something like this
```
--  Address     Load        Tokens  Owns (effective)  Host ID                               Rack 
UN  172.19.0.2  274.64 KiB  16      67.2%             c88d353a-4be5-4d9f-987f-5711bb29e254  rack1
```
The "UN" means status is "up" and "normal"
- Then start nodes 2 and 3, allow a minute between running each command
```
docker run --name node2 --network cn -d -e CASSANDRA_SEEDS=node1 cassandra
# wait ~ 1 minute
docker run --name node3 --network cn -d -e CASSANDRA_SEEDS=node1 cassandra
```
All going well, running nodetool status again and you should see the following
```
 docker exec node1 nodetool status
Datacenter: datacenter1
=======================
Status=Up/Down
|/ State=Normal/Leaving/Joining/Moving
--  Address     Load        Tokens  Owns (effective)  Host ID                               Rack 
UN  172.19.0.2  274.64 KiB  16      67.2%             c88d353a-4be5-4d9f-987f-5711bb29e254  rack1
UN  172.19.0.4  299.83 KiB  16      73.9%             37dcb4dc-ef8e-492b-be05-a80062a81930  rack1
UN  172.19.0.3  303.36 KiB  16      58.9%             b2a1a4e6-fd0f-4842-aea9-21fc2c1d9ec9  rack1
```

## Running cql commands in in cqlsh
You can access the cqlsh cli utility by running 
```
docker exec -it node1 cqlsh
```
- to make sure its working properly try running this command
```
SELECT cluster_name, listen_address FROM system.local;
```
Should see something like 
```
cqlsh> SELECT cluster_name, listen_address FROM system.local;

 cluster_name | listen_address
--------------+----------------
 Test Cluster |     172.19.0.2
```

## Running the example python program
Having started the cassandra cluster (see above), install the python cassandra driver with 
```
pip install cassandra-driver
```
- Review the source code for the example [key-example.py](python/key-example.py)
- run the example with 
```
cd python
python3 key-example.py
```
Should see output similar to 
```
2022-11-06 21:11:14,146 [INFO] root: inserting row 0
2022-11-06 21:11:14,149 [INFO] root: inserting row 1
2022-11-06 21:11:14,151 [INFO] root: inserting row 2
2022-11-06 21:11:14,153 [INFO] root: inserting row 3
2022-11-06 21:11:14,155 [INFO] root: inserting row 4
2022-11-06 21:11:14,156 [INFO] root: inserting row 5
2022-11-06 21:11:14,158 [INFO] root: inserting row 6
2022-11-06 21:11:14,159 [INFO] root: inserting row 7
2022-11-06 21:11:14,161 [INFO] root: inserting row 8
2022-11-06 21:11:14,164 [INFO] root: inserting row 9
2022-11-06 21:11:14,165 [INFO] root: key        col1    col2
2022-11-06 21:11:14,165 [INFO] root: ---        ----    ----
2022-11-06 21:11:14,178 [WARNING] cassandra.protocol: Server warning: Aggregation query used without partition key
2022-11-06 21:11:14,178 [INFO] root: dave       2022-11-06 00:00:00     57.780000000000001580957587066222913563251495361328125
2022-11-06 21:11:14,179 [INFO] root: archana    2022-11-06 00:00:00     109.78000000000000824229573481716215610504150390625
2022-11-06 21:11:14,179 [INFO] root: ashwin     2022-11-06 00:00:00     72.2399999999999948840923025272786617279052734375
2022-11-06 21:11:14,179 [INFO] root: mary       2022-11-06 00:00:00     22.9500000000000010658141036401502788066864013671875
2022-11-06 21:11:14,179 [INFO] root: john       2022-11-06 00:00:00     149.930000000000003268496584496460855007171630859375
ubuntu@ip-172-31-35-248:~/CSCIE88-BDP-2022/week11_cassandra/python$ 
```

