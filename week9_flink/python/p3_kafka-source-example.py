import os

from pyflink.datastream import StreamExecutionEnvironment
from pyflink.table import StreamTableEnvironment, EnvironmentSettings


# Converting between datastream and table api in pyflink
# https://nightlies.apache.org/flink/flink-docs-stable/docs/dev/table/data_stream_api/

def main():
    # Create streaming environment
    env = StreamExecutionEnvironment.get_execution_environment()

    settings = EnvironmentSettings.new_instance()\
                      .in_streaming_mode()\
                      .use_blink_planner()\
                      .build()

    # create table environment
    tbl_env = StreamTableEnvironment.create(stream_execution_environment=env,
                                            environment_settings=settings)

    # add kafka connector dependency
    # kafka_jar = os.path.join(os.path.abspath(os.path.dirname(__file__)),
    #                         'flink-sql-connector-kafka_2.11-1.13.0.jar')
    kafka_jar = "file:///opt/flink/lib/flink-connector-kafka_2.12-1.14.0.jar"

    tbl_env.get_config()\
            .get_configuration()\
            .set_string("pipeline.jars", kafka_jar)
            # .set_string("pipeline.jars", "file://{}".format(kafka_jar))

    #######################################################################
    # Create Kafka Source Table with DDL
    #######################################################################

    # { "uuid":"41daf40b28bd48958eb870bc65b8d78a",
    # "eventTimestamp":"2022-09-03 13:48:59",
    # "url":"http://example.com/?url=124",
    # "userId":"user-035",
    # "country":"GN",
    # "uaBrowser":"IE","uaOs":"windows","responseCode":207,"ttfb":0.6509  }

    src_ddl = """
        CREATE TABLE logs (
            uuid VARCHAR,
            eventTimestamp VARCHAR,
            url VARCHAR,
            userid VARCHAR,
            country VARCHAR,
            uaBrowser VARCHAR,
            uaOs VARCHAR,
            responseCode VARCHAR,
            ttfb VARCHAR,
            proctime AS PROCTIME()
        ) WITH (
            'connector' = 'kafka',
            'topic' = 'p3_input',
            'properties.bootstrap.servers' = 'broker1:29092',
            'properties.group.id' = 'logs',
            'scan.startup.mode' = 'latest-offset',
            'format' = 'json'
        )
    """

    tbl_env.execute_sql(src_ddl)

    # create and initiate loading of source Table
    tbl = tbl_env.from_path('logs')

    print('\nSource Schema')
    tbl.print_schema()

    #####################################################################
    # Define Tumbling Window Aggregate Calculation (Seller Sales Per Minute)
    #####################################################################
            #   SUM(1) * 0.85 AS cnt

    sql = """
        SELECT
          uaOs,
          TUMBLE_END(proctime, INTERVAL '5' SECONDS) AS window_end,
          count(*) as cnt
        FROM logs
        GROUP BY
          TUMBLE(proctime, INTERVAL '5' SECONDS),
          uaOs
    """
    logs_tbl = tbl_env.sql_query(sql)

    print('\nProcess Sink Schema')
    logs_tbl.print_schema()

    ###############################################################
    # Create Kafka Sink Table
    ###############################################################
    sink_ddl = """
        CREATE TABLE logs_uaos (
            uaOs VARCHAR,
            window_end TIMESTAMP(3),
            cnt BIGINT
        ) WITH (
            'connector' = 'kafka',
            'topic' = 'p3_output',
            'properties.bootstrap.servers' = 'broker1:29092',
            'format' = 'json'
        )
    """
    tbl_env.execute_sql(sink_ddl)

    # write time windowed aggregations to sink table
    logs_tbl.execute_insert('logs_uaos').wait()

    tbl_env.execute('windowed-logs')

if __name__ == '__main__':
    main()

