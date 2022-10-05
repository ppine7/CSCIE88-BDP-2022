#!/usr/bin/env python

import sys
import fileinput

current_word = None
current_count = 0

# for line in fileinput.input():
for line in sys.stdin:
    try:
        word, count = line.strip().split('\t')
        if current_word:
            if word == current_word:
                current_count += int(count)
            else:
                print ("%s\t%d" % (current_word, current_count))
                current_count = 1

        current_word = word
    except ValueError as e:
        continue
if current_count > 1:
    print ("%s\t%d" % (current_word, current_count))
