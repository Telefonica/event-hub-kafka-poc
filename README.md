# event-hub-kafka-poc

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

## Getting started: EventHub Throughput POC

1. Create a sample of data using the `utils/json-generator` util:
- Resolve util dependencies: `npm i utils/json-generator`
- Build data sample running: `node utils/json-generator/main.js -r 409600`
- Display util options: `node utils/json-generator/main.js help`
```bash
Usage: main.js [options] [command]

Commands:
  help     Display help

Options:
  -h, --help         Output usage information
  -o, --out [value]  Output file (defaults to "event-hub-kafka-poc/data/data.json")
  -r, --records <n>  Amount of records (defaults to 409600; 409600 == 1GB)
```

2. Assembly application jar:
```bash
sbt assembly  
```
The output jar will be located in `dist/event-hub-kafka-poc.jar`

3. Create an EventHub with manage shared access policies at the Azure developers console:
  - Standard EventHub:
    - 1 TU 
    - Enable Auto-Inflate
    - Auto-Inflate Maximum Throughput Units: 20
    - Partition count: 32
  - Dedicated EventHub:
    - 1 CU 
    - Partition count: 600
4. Run application:
```bash
export NAMESPACE="namespace-name"
export EVENTHUB="eventhub-name"
export SASK_KEY="eventhub-shared-connection-string"

java -jar /opt/event-hub-kafka-poc/event-hub-kafka-poc.jar \
  --mode=producer \
  --namespace=${NAMESPACE} \
  --eventhub=${EVENTHUB} \
  --sas="${SASK_KEY}" \
  --input=event-hub-kafka-poc/data/data.json
```

5. Check the EventHub namespace throughput metrics


