# Week 11 Cassandra Lab Notes

Topics
- Starting a cassandra cluster using docker
- Running cql commands in in cqlsh
- Python code walk through

## Links

- [Getting Started with the Python API](https://docs.datastax.com/en/developer/python-driver/3.25/getting_started/)
- [Python API](https://docs.datastax.com/en/developer/python-driver/3.25/api/)
- [Java API](https://docs.datastax.com/en/developer/java-driver/4.3/)


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
