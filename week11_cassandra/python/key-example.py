#!/usr/bin/env python

import logging
import uuid

log = logging.getLogger()
log.setLevel('INFO')
handler = logging.StreamHandler()
handler.setFormatter(logging.Formatter("%(asctime)s [%(levelname)s] %(name)s: %(message)s"))
log.addHandler(handler)

from cassandra import ConsistencyLevel
from cassandra.cluster import Cluster
from cassandra.query import SimpleStatement

KEYSPACE = "testkeyspace"

def main():
    cluster = Cluster(['127.0.0.1'])
    session = cluster.connect()

    rows = session.execute("SELECT keyspace_name FROM system_schema.keyspaces")
    if KEYSPACE in [row[0] for row in rows]:
        log.info("dropping existing keyspace...")
        session.execute("DROP KEYSPACE " + KEYSPACE)

    log.info("creating keyspace...")
    session.execute("""
        CREATE KEYSPACE %s
        WITH replication = { 'class': 'SimpleStrategy', 'replication_factor': '2' }
        """ % KEYSPACE)

    log.info("setting keyspace...")
    session.set_keyspace(KEYSPACE)

    log.info("creating table...")
    session.execute("""
        CREATE TABLE mytable (
            country text,
            url text,
            uuid text,
            col2 text,
            PRIMARY KEY (country, url,uuid)
        )
        """)

    # query = SimpleStatement("""
    #     INSERT INTO mytable (thekey, col1, col2)
    #     VALUES (%(key)s, %(a)s, %(b)s)
    #     """, consistency_level=ConsistencyLevel.ONE)

    prepared = session.prepare("""
        INSERT INTO mytable (country, url, uuid,col2)
        VALUES (?, ?, ?, ?)
        """)

    countries = ["ie","us","fr","uk"]
    urls = ["u1","u2","u3","u4"]

    for i in range(1000):
        log.info("inserting row %d" % i)
        # session.execute(query, dict(key="key%d" % i, a='a', b='b'))
        session.execute(prepared.bind((countries[i % len(countries)], urls[i % len(urls)], str(uuid.uuid4()), 'b')))

    # future = session.execute_async("SELECT * FROM mytable")
    future = session.execute_async("SELECT country,url,count(*) FROM mytable group by country,url")
    log.info("key\tcol1\tcol2")
    log.info("---\t----\t----")

    try:
        rows = future.result()
    except Exception:
        log.exeception()

    for row in rows:
        row = map(lambda c: str(c),row)
        log.info('\t'.join(row))

    session.execute("DROP KEYSPACE " + KEYSPACE)

if __name__ == "__main__":
    main()