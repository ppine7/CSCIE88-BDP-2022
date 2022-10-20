from kafka import KafkaConsumer

var = 1
while var == 1 :
    consumer = KafkaConsumer(bootstrap_servers='localhost:9092',group_id='consumer-1',auto_offset_reset='latest')
    consumer.subscribe(['python_test_topic'])

    for message in consumer:
        print(message)
