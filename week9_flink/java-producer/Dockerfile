# Build image with
# docker build --tag java-producer:11-jdk .
# docker tag java-producer:11-jdk wrackzone/java-producer:11-jdk
# docker push wrackzone/java-producer:11-jdk
# to run image in network and mounting directory
# docker run --network=lab9-demo-net -v $PWD/java-producer:/java-producer --rm -it wrackzone/java-producer:11-jdk bash

FROM openjdk:11-jdk

RUN apt-get update -y && apt-get install -y maven