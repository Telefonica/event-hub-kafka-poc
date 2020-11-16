# baikal-api

POC of Producer/Consumer implementation for Azure EventHub Kafka interface.

## Build instructions

This project is build using SBT:

```sh
sbt assembly
```

The output jar will be located in `dist/event-hub-kafka-poc.jar`

## Usage

```sh
java -jar dist/event-hub-kafka-poc.jar --help

EventHub consumer/producer using Kafka interface
  -e, --eventhub  <arg>    EventHub name
  -i, --input  <arg>       Input data file
  -m, --mode  <arg>        Consume or produce messages to the desired EventHub
                           Choices: consumer, producer
  -n, --namespace  <arg>   EventHub namespace name
  -o, --output  <arg>      Output data file
  -s, --sas  <arg>         SAS connection string between ""
  -h, --help               Show help message

- Producer example: --mode=producer --namespace=ns-test --eventhub=eh-test --sas="Endpoint=sb://ns-test.servicebus.windows.net/;SharedAccessKeyName=sas-keys;SharedAccessKey=xxx;EntityPath=eh-test" --input=/path/to/data/1gb.json
- Consumer example: --mode=consumer --namespace=ns-test --eventhub=eh-test --sas="Endpoint=sb://ns-test.servicebus.windows.net/;SharedAccessKeyName=sas-keys;SharedAccessKey=xxx;EntityPath=eh-test" --output=/path/to/data/out.json
```

