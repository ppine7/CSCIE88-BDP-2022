#!/usr/bin/env python
import fileinput
import sys

from collections import namedtuple
from datetime import datetime

event_fields = ['uuid', 'timestamp', 'url', 'userid', 'country', 'ua_browser', 'ua_os', 'response_status', 'TTFB']
Event = namedtuple('Event', event_fields)


def parse_line(line):
    # return Event(*line.split(','))
    return Event._make(line.split(','))


def parse_date(raw_time):
    try:
        # 2022-09-06T13:47:41.810998800Z if this format need to strip microseconds down
        raw_time =  (raw_time[:-4] + "Z") if len(raw_time)==30 else raw_time
        event_datetime = datetime.strptime(raw_time, '%Y-%m-%dT%H:%M:%S.%fZ')
        return event_datetime
    except ValueError as e:
        try:
            event_datetime = datetime.strptime(raw_time, '%Y-%m-%dT%H:%M:%SZ')
            return event_datetime
        except ValueError as e:
            return None


# with open("../logs/file-input1.csv") as file_handle:
#     for line in file_handle:
for line in sys.stdin:
    # parse the log line - and get the timestamp only
    # convert to a format we want - only date and hour - since we are only interested in the hour
    parsed_line = parse_line(line.strip())
    if parsed_line is not None:
        event_datetime = parse_date(parsed_line.timestamp)
        if event_datetime is not None:
            event_hour = datetime.strftime(event_datetime, "%y-%m-%d %H")
            print ("%s\t%d" % (event_hour, 1))

print ("Mapper is done")
