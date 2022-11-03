#!/bin/bash
# sudo apt-get install kafkacat && sudo apt-get install pv
while read -r line; do echo $line; sleep .1; done < ../../week10_ksql/logs_json/file-input1.json | pv | kafkacat -b localhost:9092 -t p3_input -P -l

