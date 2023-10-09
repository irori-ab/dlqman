# DLQman
Open source application that simplifies error handling in your Kafka integrations by handling messages that could not be processed by a consumer.

## Overview design

![DLQman module diagram](/docs/DLQman_modules.png)

## Usage
DLQman requires connectivity to a kafka broker and a database. The connectivity details can be configured inside [application.yaml](src/main/resources/application.yaml)

## Tutorial
See the [tutorial](tutorial.md) for a longer example scenario.

### Config reference
```yaml
  dlqman:
    kafka: # Kafka related configuration
      common: # Map of config options to send to the producer or consumer instance.
      producer: # Producer specific configs to use for Kafka.
      consumer: # Consumer specific configs to use for Kafka.
      poll-timeout: # Timeout of the poll query when processing messages to be resent.
    sources: # List of sources to subscribe to handle DLQ messages
      <source-name>:
        enabled: # Can be used to temporarily disable the listener for this topic.
        name: # Name of this Source configuration.
        description: # Description of the Source configuration. (for reference)
        source-topic: # The source topic where this Source subscribes to DLQ messages.
        resend-topic: # A default topic to resend messages to unless overridden by the rule.
        match-rules: # A list of Rule objects for this Source.
          - name: # The name of the rule.
            priority: # The priority of the rule. Lower priority will match before a higher.
            matcher: # The name of the Matcher to use. This can be either the built in(all) or one defined in the matchers section.
            strategy: # The name of the DLQ Strategy to use. This can be either one of the built in strategies(void or dismiss) or one defined in the strategy section.
            resend-topic-override: # Optional. Set this to override the general resend topic for the Source.
    matchers: # List of Matchers, defined by a Def object. Configures the Matcher to be used in the Source configuration.
      - name: # Name of the Matcher or DLQStrategy. Used in the source configuration to map the object.
        class-name: # FQDN of the implementation of the Matcher or DLQStrategy to configure.
        config: # Map describing specific configuration needed for the Matcher or DLQStrategy.
          headerName: 
          regexPattern: 
    dlq-strategies: # List of DLQ Strategies, defined by a Def object. Configures the Strategy to be used in the Source configuration.
      - name: # Name of the Matcher or DLQStrategy. Used in the source configuration to map the object.
        class-name: # FQDN of the implementation of the Matcher or DLQStrategy to configure.
        config: # Map describing specific configuration needed for the Matcher or DLQStrategy.
            nextWaitDuration: 
```

## Building

```shell script 
./mvnw clean install
```

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```
> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Access the Kafka Dev UI

http://localhost:8000/q/dev/io.quarkus.quarkus-kafka-client/kafka-dev-ui


## Testing with Docker Compose

Either build the image yourself:
```
./mvnw install -DskipTests --batch-mode -Dquarkus.container-image.build=true
```

Or set an appropriate release tag in `docker-compose.yml`. Then: 
```
docker-compose up

docker run --tty --network dlqman edenhill/kcat:1.7.1 kcat -b kafka:9092 -L

# send a message
echo tryMeAgain | docker run -i --network dlqman edenhill/kcat:1.7.1 kcat -b kafka:9092 -t dlq-source -P -H "internal-dlq-exception-class=QuiteRetryableException"

docker run --tty --network dlqman edenhill/kcat:1.7.1 kcat -b kafka:9092 -C -t resend-override

# follow the tutorial.md with kcat commands run via above Docker exec kcat mechanism
```

## Reference Documentation

- [Irori Product Page](https://irori.se/products/dlqman/)
- [Irori Blog Post](https://irori.se/blog/dlqman-to-the-rescue/)
- [Quarkus, the Supersonic Subatomic Java Framework](https://quarkus.io/)