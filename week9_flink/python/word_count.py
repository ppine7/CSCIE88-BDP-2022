import argparse
import logging
import sys

from pyflink.common import WatermarkStrategy, Encoder, Types
from pyflink.datastream import StreamExecutionEnvironment, RuntimeExecutionMode
from pyflink.datastream.connectors import (FileSource, StreamFormat, FileSink, OutputFileConfig,
                                           RollingPolicy)
# run using: ./bin/flink run --python /<my_dir>/word_count.py



def word_count():
  # set up the execution environment
  env = StreamExecutionEnvironment.get_execution_environment()
  env.set_runtime_mode(RuntimeExecutionMode.BATCH)
  env.set_parallelism(1)

  # get input data
  input_path = "file:////Users/ESumitra/tools/flink-1.14.0/README.txt"
  ds = env.from_source(
            source=FileSource.for_record_stream_format(StreamFormat.text_line_format(),
                                                       input_path)
                             .process_static_file_set().build(),
            watermark_strategy=WatermarkStrategy.for_monotonous_timestamps(),
            source_name="file_source"
        )
  
  # function to split line into words
  def split_line(line):
    return line.split(" ")

  # compute word count
  counts = ds.flat_map(split_line).map(lambda w: (w, 1),output_type=Types.TUPLE([Types.STRING(), Types.INT()])) \
    .key_by(lambda t: t[0]) \
    .reduce(lambda a, b: (a[0], a[1] + b[1]))

  # print or write to sink
  counts.print()

  env.execute("word count")

# main entry point
if __name__ == "__main__":
  logging.basicConfig(stream=sys.stdout, level=logging.INFO)
  word_count()
  logging.info("Finished.")



