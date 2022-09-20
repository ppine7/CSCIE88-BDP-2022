#!/usr/bin/env python

# This is a "map" part of a MR job that finds MAX temperature for each day
# from all input weather events
#
import sys
from collections import namedtuple
from datetime import datetime

LogLineTemp = namedtuple('LogLineIP', ['uuid', 'timestamp', 'temp'])

def parse_log_line_with_temp(line):
    tokens = line.strip().split(",")
    if len(tokens) < 3:
        return None
    else:
        # parse the time
        try:
            event_datetime = datetime.strptime(tokens[1], '%Y-%m-%dT%H:%M:%S.%fZ')
            return LogLineTemp(tokens[0], event_datetime, tokens[2])
        except ValueError as e:
            return None

for line in sys.stdin:
#with open("temp-input1.csv") as file_handle:
#for line in file_handle:
    # parse the log line - and get the timestamp and temp only
    # convert to a format we want - only day - since we are only interested in the Max temp per day
    parsed_line = parse_log_line_with_temp(line)
    if parsed_line is not None:
        event_day = parsed_line.timestamp.strftime("%y-%m-%d")
        print ("%s\t%s" % (event_day, parsed_line.temp))

#print ("Mapper is done")
