# Copyright (c) 2020 CSCIE88 Marina Popova
# this is a very simple multi-processing application that uses shared CTypes memory counters ...

import argparse
import multiprocessing
from multiprocessing import Value, Lock

prog = "counter_shared_mem"
desc = "run specified number of threads - use shared counter - CTypes"
parser = argparse.ArgumentParser(prog=prog, description=desc)
parser.add_argument('--thread-count', '-tc', default=4, type=int)

parsed_args = parser.parse_args()
thread_count = parsed_args.thread_count


def do_work(shared_counter, lock, thread_id):
    for local_counter in range(100000):
        if (local_counter % 1000 == 0):
            print(thread_id, " thread: local_counter=", local_counter,
                  ", shared_counter=", shared_counter.value)
        with lock:
            shared_counter.value += 1
    print(thread_id, " thread is finished")


if __name__ == '__main__':
    with multiprocessing.Manager() as manager:
        shared_counter = Value('i', 0)
        lock = Lock()
        jobs = []
        for thread_id in range(thread_count):
            t = multiprocessing.Process(
                target=do_work,
                args=(shared_counter, lock, thread_id))
            jobs.append(t)
            t.start()

        for curr_job in jobs:
            curr_job.join()
        print("Process Completed; shared_counter=", shared_counter.value)
