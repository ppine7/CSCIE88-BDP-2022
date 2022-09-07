# Copyright (c) 2020 CSCIE88 Marina Popova
# this is a very simple multi-processing application and it can be improved in many ways ...
# it does not stop all processes cleanly on the program termination - so the last "in-progress" files
# may linger in the file system - make sure to clean them out or improve the program to handle interrupts
#
# For IO-intensive tasks, you may also consider using multithreading instead of multiprocessing,
# although this particular task is not purely IO-bound

import argparse
import multiprocessing
import os
from random import randint

parser = argparse.ArgumentParser(
    description="Creates and starts a specified number of threads and performs IO intensive work")
parser.add_argument('num_threads', nargs='?', default=4, type=int)
parsed_args = parser.parse_args()
num_threads = parsed_args.num_threads


def infinite_io():
    thread_id = str(os.getpid())
    iteration = 0
    while True:
        iteration = iteration + 1
        io_work(thread_id, iteration)  # do IO-intensive work


# Write 1M random numbers to a file and delete it
def io_work(thread_id, iteration):
    file_name = "tmp_file_{}_{}.txt".format(thread_id, iteration)
    print("thread " + thread_id + " is writing file: " + file_name)
    file = open(file_name, "w")
    for i in range(1000000):
        file.write("{}\n".format(randint(0, 10000)))
    file.close()
    os.remove(file_name)


jobs = []
for thread in range(num_threads):
    t = multiprocessing.Process(target=infinite_io, args=())
    jobs.append(t)
    t.start()  # new child process is started at this point, it has its own execution flow

for curr_job in jobs:
    curr_job.join()

print("Process Completed")
