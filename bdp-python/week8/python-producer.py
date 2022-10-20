from kafka import KafkaProducer
import time
import random

producer = KafkaProducer(bootstrap_servers='localhost:9092')

var = 1
while var == 1 :
    num = random.randint(0,10)
    print(str(num))
#    producer.send('python_test_topic' ,value=str(num), key=str(num) )
    producer.send('python_test_topic', value=b'msg %d' % num, key=b'msg %d' % num )
    time.sleep(1)
