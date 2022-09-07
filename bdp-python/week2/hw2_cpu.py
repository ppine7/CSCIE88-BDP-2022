# Copyright (c) 2020 CSCIE88 Marina Popova
# this is a very simple multi-processing application that keeps calculating Fibonacci sequences
# in the specified number of started processes

import argparse
import multiprocessing
import os

parser = argparse.ArgumentParser(
    description="Creates and starts a specified number of threads(processes) and performs CPU intensive work")
parser.add_argument('num_threads', nargs='?', default=4, type=int)
parsed_args = parser.parse_args()
num_threads = parsed_args.num_threads


def calculate_fibonacci_infinite():
    thread_id = str(os.getpid())
    iteration = 0
    while True:
        iteration = iteration + 1
        print("thread " + thread_id + " is processing ... iteration=" + str(iteration))
        fibonacci(1000000)  # calculate 1M fibonacci number


# compute fibonacci to nth decimal
def fibonacci(n):
    a = 0
    b = 1
    if n < 0:
        print("Incorrect input")
    elif n == 0:
        return a
    elif n == 1:
        return b
    else:
        for i in range(2, n):
            c = a + b
            a = b
            b = c
        return b


jobs = []
for i in range(num_threads):
    t = multiprocessing.Process(target=calculate_fibonacci_infinite, args=())
    jobs.append(t)
    t.start()  # new child process is started at this point, it has its own execution flow

for curr_job in jobs:
    curr_job.join()

print("Process Completed")
