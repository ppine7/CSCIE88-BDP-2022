#!/usr/bin/env python

# This is a "reduce" part of a MR job that finds MAX temperature per each day
# from all input weather events
#
import sys

# we are assuming that for practical purposes we cannot get temperature values below -100F
TEMP_MIN = -100.0

current_day = None
current_max_temp = TEMP_MIN

for line in sys.stdin:
    try:
        day_from_line, temp_str = line.strip().split('\t')
        temp_from_line = float(temp_str)

        if not current_day:
            current_day = day_from_line
        if day_from_line == current_day:
            current_max_temp = max(current_max_temp, temp_from_line)
        else:
            print ("%s\t%f" % (current_day, current_max_temp))
            current_max_temp = TEMP_MIN
            current_day = day_from_line
    except ValueError as e:
        continue

# print out the last evaluated day
if current_max_temp > TEMP_MIN:
    print ("%s\t%f" % (current_day, current_max_temp))
