FROM adoptopenjdk/openjdk8
COPY dist /opt/event-hub-kafka-poc
COPY data /opt/event-hub-kafka-poc/data
WORKDIR /opt/event-hub-kafka-poc
ENTRYPOINT ["java", "-jar", "/opt/event-hub-kafka-poc/event-hub-kafka-poc.jar"]

