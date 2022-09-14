# Copyright (c) 2022 CSCIE88 Marina Popova
# this is a very simple multi-processing application that reads in files from a directory,
# parses them and uses shared memory CTypes counter as a shared state ...

import argparse
import multiprocessing
from multiprocessing import Process, Value, Lock
from collections import namedtuple, defaultdict


prog = "event_counter_shared_ctypes_mem"
desc = "run specified number of threads - use shared event counter"
parser = argparse.ArgumentParser(prog=prog, description=desc)
parser.add_argument('--thread-count', '-tc', default=4, type=int)
parser.add_argument('--logs-directory', '-ld', required=False,
                    help="Directory where the log files 01-04 are stored. "
                         "If not supplied, this program assumes that all 4 log files"
                         " are directly in the present working directory.")

parsed_args = parser.parse_args()
thread_count = parsed_args.thread_count
logs_dir = parsed_args.logs_directory
Event = namedtuple('Event',
                   ['uuid', 'timestamp', 'url', 'userid', 'country', 'ua_browser', 'ua_os', 'response_status', 'TTFB'])

parsed_args = parser.parse_args()
thread_count = parsed_args.thread_count


def do_work(shared_counter, lock, file_name):
    local_counter =0;
    with open(file_name) as file_handle:
        events = map(parse_line, file_handle)

        for event in events:
            if local_counter == 0:
                print(event)
            local_counter += 1

        with lock:
            shared_counter.value += local_counter
        print(file_name, " file_name has finished processing with its local_counter = :",  local_counter)

def parse_line(line):
    return Event(*line.split(','))

if __name__ == '__main__':
    if logs_dir is None:
        opt_dir = "../logs/"
    elif logs_dir.endswith("/"):
        opt_dir = logs_dir
    else:
        opt_dir = logs_dir + "/"
    with multiprocessing.Manager() as manager:
        shared_counter = Value('i', 0)
        lock = Lock()
        jobs = []
        for thread_id in range(thread_count):
            file_name = opt_dir + "file-input%s.csv" % str(thread_id + 1)
            t = multiprocessing.Process(
                target=do_work,
                args=(shared_counter, lock, file_name))

            jobs.append(t)
            t.start()

        for curr_job in jobs:
            curr_job.join()
        print("Process Completed; shared_counter=", shared_counter.value)
