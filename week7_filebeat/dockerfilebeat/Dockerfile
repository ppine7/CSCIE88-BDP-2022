FROM docker.elastic.co/beats/filebeat:8.4.3
COPY filebeat.yml /usr/share/filebeat/filebeat.yml
COPY input_logs/input1.txt /usr/share/filebeat/input_logs/input1.txt
USER root
RUN chmod +x filebeat.yml
