# Copyright (c) 2022 CSCIE88 Marina Popova
'''
This is a very simple Python application that reads one file, parses each line per the specified schema
(event_fields), and counts the number of lines in the file - by incrementing it's local counter (event_count);
It also increments a shared counter maintained in Redis - by 1 for each line - so that after all instances
of this application are done processing their own files - we have a total count of lines available in Redis.

In this app - we chose to increment the shared Redis counter per each processed line - to see the running total count
in progress in Redis; One could choose to increment the shared counter only once, when all line are counted locally -
to decrease the number of calls to Redis. However, in this approach - the shared counter will not show the running total
'''
import argparse
from collections import namedtuple
import redis

event_fields = ['uuid', 'timestamp', 'url', 'userid', 'country', 'ua_browser', 'ua_os', 'response_status', 'TTFB']
Event = namedtuple('Event', event_fields)


def parse_arguments():
    prog = "counter_process_redis"
    desc = "application that reads a file, parses all lines, counts the lines and " \
           "stores/increments the counter maintained in Redis"

    parser = argparse.ArgumentParser(prog=prog, description=desc)
    # name of a simple String field in Redis - that will be use as a shared counter
    parser.add_argument('--redis_counter_name', '-rc', required=False, default="counter")
    parser.add_argument('--file_name', '-f', required=False, default="../logs/file-input1.csv",
                        help="a csv log file to process")
    parser.add_argument('--redis_url', '-ru', required=False, default="redis://localhost:6379",
                        help="Redis end point url; Eg: redis://localhost:6379")

    parsed_args = parser.parse_args()
    return parsed_args


def do_work(redis_url, redis_counter_name, file_name):
    redis_client = redis.Redis.from_url(redis_url)
    event_count = 0;
    # set initial value of the redis counter to 0 - if the counter does not exits yet
    #   (was not set by some other thread or app)
    redis_client.setnx(redis_counter_name, 0)
    with open(file_name) as file_handle:
        events = map(parse_line, file_handle)
        for event in events:
            if event_count % 1000 == 0:
                print(f"processing event #{event_count} ... ")
            event_count += 1
            # increment Redis counter by 1
            redis_client.incr(redis_counter_name)

        shared_counter = redis_client.get(redis_counter_name)
        print(f"processing of {file_name} has finished processing with local event_count={event_count}, "
              f"shared counter from Redis: {shared_counter}")

    redis_client.close()


def parse_line(line):
    return Event(*line.split(','))


def main():
    parsed_args = parse_arguments()
    redis_counter_name = parsed_args.redis_counter_name
    file_name = parsed_args.file_name
    redis_url = parsed_args.redis_url
    do_work(redis_url, redis_counter_name, file_name)


if __name__ == '__main__':
    main()

