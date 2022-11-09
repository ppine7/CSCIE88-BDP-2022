#!/usr/bin/env python

import logging
import uuid
import random
from datetime import date
from datetime import timedelta
from datetime import datetime


log = logging.getLogger()
log.setLevel('INFO')
handler = logging.StreamHandler()
handler.setFormatter(logging.Formatter("%(asctime)s [%(levelname)s] %(name)s: %(message)s"))
log.addHandler(handler)

from cassandra import ConsistencyLevel
from cassandra.cluster import Cluster
from cassandra.query import SimpleStatement
from cassandra.util import Date

KEYSPACE = "testkeyspace"

# for testing, creates a payment tuple with random values
names = ["dave","mary","ashwin"] #,"john","archana","akira"]
def make_payment():
    name = names[random.randrange(0,len(names)-1)]   
    dt = date.today() - timedelta(days=random.randint(1,7))
    # dt = datetime.now() - timedelta(seconds=random.randint(1,5000))
    uuid_ = str(uuid.uuid4())
    amt = round(random.uniform(0, 1) * 100,2)
    return (name,dt,uuid_,amt)

def main():
    # connect to the cluster
    cluster = Cluster(['127.0.0.1'])
    session = cluster.connect()

    # if the keyspace already exists then drop it
    rows = session.execute("SELECT keyspace_name FROM system_schema.keyspaces")
    if KEYSPACE in [row[0] for row in rows]:
        log.info("dropping existing keyspace...")
        session.execute("DROP KEYSPACE " + KEYSPACE)

    # create the keyspace
    log.info("creating keyspace...")
    session.execute("""
        CREATE KEYSPACE %s
        WITH replication = { 'class': 'SimpleStrategy', 'replication_factor': '2' }
        """ % KEYSPACE)

    log.info("setting keyspace...")
    session.set_keyspace(KEYSPACE)

    # create the table
    log.info("creating table...")
    session.execute("""
        CREATE TABLE payment_table (
            name text,
            dt timestamp,
            uuid text,
            amt decimal,
            PRIMARY KEY ((name,dt),uuid)
        )
        """)

    # query = SimpleStatement("""
    #     INSERT INTO mytable (thekey, col1, col2)
    #     VALUES (%(key)s, %(a)s, %(b)s)
    #     """, consistency_level=ConsistencyLevel.ONE)

    # prepared statement query to insert values
    prepared = session.prepare("""
        INSERT INTO payment_table (name, dt, uuid,amt)
        VALUES (?, ?, ?, ?)
        """)

    # insert rows
    for i in range(1000):
        log.info("inserting row %d" % i)
        pmt = make_payment()
        session.execute(prepared.bind(pmt))

    # query results, aggregate (sum) amt by name and date (dt)
    future = session.execute_async( \
    "SELECT name, dt, sum(amt) FROM payment_table group by name, dt")

    log.info("Results...")

    try:
        rows = future.result()
    except Exception:
        log.exeception()

    # iterate the result rows
    for row in rows:
        # map cols in row to a string type
        row = map(lambda c: str(c),row)
        log.info('\t'.join(row))

    # delete the keyspace when done
    # session.execute("DROP KEYSPACE " + KEYSPACE)

if __name__ == "__main__":
    main()
