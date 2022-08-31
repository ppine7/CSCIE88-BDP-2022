# Copyright (c) 2020 CSCIE88 Marina Popova
# this is a very simple multi-processing application and it can be improved in many ways ...
# it takes two input arguments: num_files and num_lines , and creates the specified number of files
# each files has num_lines lines with 3 random numbers per line

import argparse
import multiprocessing
import random

# default number of files and lines
NUM_OF_FILES = 2
NUM_OF_LINES = 10

# Function to generate a file with the specified number of lines
def generate_file(num_lines, file_number):
    '''
    Function to generate a file with the specified number of lines, each with 3 random numbers
    '''
    filename = "cscie88_fall2022_" + str(file_number) + ".txt"

    # Open writer and output lines
    file = open(filename, "w")
    for i in range(num_lines):
        line = str(random.randint(0, 10)) + " " + str(random.randint(0, 10)) + " " + str(random.randint(0, 10))
        file.write(line + "\n")
    file.close()
    print(filename + " written!")


def parse_arguments():
    '''
    Argument parser
    num_files = Number of files
    num_lines = Number of lines
    '''
    parser = argparse.ArgumentParser(description='Set the number of files and number of lines')
    parser.add_argument("num_files", nargs='?', type=int, help="Number of files to create", default=NUM_OF_FILES)
    parser.add_argument("num_lines", nargs='?', type=int, help="Number of lines per file", default=NUM_OF_LINES)
    args = parser.parse_args()
    return (args)


def main():
    '''
    Get arguments, setup multiprocessing, create files
    '''
    arguments = parse_arguments()
    num_files = arguments.num_files
    num_lines = arguments.num_lines
    print("Program arguments: num_files = " + str(num_files) + "; num_lines = " + str(num_lines))

    jobs = []
    for file_number in range(num_files):
        t = multiprocessing.Process(target=generate_file, args=(num_lines, file_number))
        jobs.append(t)
        t.start()  # new child process is started at this point, it has its own execution flow

    for curr_job in jobs:  # wait for all processes to finish
        curr_job.join()

    print("Program completed OK")


if __name__ == "__main__":
    main()
