# Tutorial
A simple tutorial on how try out DLQMan on your local machine in Quarkus Dev Mode.

## Pre-requisites

- Java 11+
- Docker compatible environment (e.g. Colima and docker cli)
- kcat - Kafka CLI tool

### OSX setup

```
# install tools
brew install openjdk
brew install colima 
brew install kcat

brew install docker docker-compose

# Start docker VM
colima start
# test docker backend is ok
docker ps
```

## Starting DLQMan
We will start DLQMan in Quarkus Dev mode with dev services for Kafka (Redpanda) and Postgres, as docker containers.

```
# Needed when using Colima on OSX
export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"

# Seems this might sometimes be needed to start
export TESTCONTAINERS_RYUK_DISABLED=true

# Actually building and starting with Maven
./mvnw compile quarkus:dev
```

It will trigger the download of appropriate PostreSQL and Redpanda docker images and start them up. When
started you will see `Resent 0 messages` outputted at regular intervals, which means everything is working
fine.

## Reviewing the default configuration

The `%dev` configuration used is found in `src/main/resources/application.yaml`:

```
  dlqman:
    sources:
      test-source:
        name: test-source
        description: Test source
        source-topic: dlq-source
        resend-topic: dlq-resend
        match-rules:
          - name: save-fatal
            priority: 5
            matcher: exception-is-fatal
            strategy: dismiss
          - name: resend-retryable
            priority: 6
            matcher: exception-is-retryable
            strategy: resend10s
            resend-topic-override: resend-override
          - name: catchtherest
            priority: 100
            matcher: all
            strategy: void
    matchers:
      - name: exception-is-retryable
        class-name: HeaderRegexMatcher
        config:
          headerName: internal-dlq-exception-class
          regexPattern: .*RetryableException.*
      - name: exception-is-fatal
        class-name: HeaderRegexMatcher
        config:
          headerName: internal-dlq-exception-class
          regexPattern: .*FatalException.*

    dlq-strategies:
      - name: resend10s
        class-name: SimpleResendDLQStrategy
        config:
          nextWaitDuration: 10000

```

On a high level:
* One DLQ source is defined, `test-source`
* It reads from topic `dlq-source` and resends messages to `dlq-resend`
* There are three match rules applied in order. Each has a different `matcher` condition and an action strategy defined.
  1. `save-fatal`
     * matcher: `exception-is-fatal` (defined further down in config)
       * matches if the header with key `internal-dlq-exception-class` matches the regex `.*FatalException.*`
     * strategy: `dismiss` (built-in DismissDLQStrategy)
       * Dismiss the message but save it in the database
  2. `resend-retryable`
     * matcher: `exception-is-retryable`
       * matches if the header with key `internal-dlq-exception-class` matches the regex `.*RetryableException.*`
     * strategy: `resend10s` (defined further down in config)
       * Resend the message after 10 s
     * also overrides the resend topic to `resend-override`
  3. `catchtherest`
      * matcher: `all` (built-in matcher)
        * matches all messages
      * strategy: `void` (built-in strategy)
        * do nothing, don't persist

## Kicking the tires

Let's test it out with `kcat`! First we lookup what port our Kafka cluster is using:

```
> docker ps
CONTAINER ID   IMAGE                          COMMAND                  CREATED          STATUS          PORTS                                                                  NAMES
9c48b011cead   vectorized/redpanda:v22.3.11   "sh -c 'while [ ! -f…"   39 seconds ago   Up 38 seconds   8081-8082/tcp, 9644/tcp, 0.0.0.0:19092->9092/tcp, :::19092->9092/tcp   distracted_bohr
25b3d4216736   postgres:14                    "docker-entrypoint.s…"   40 seconds ago   Up 38 seconds   0.0.0.0:15432->5432/tcp, :::15432->5432/tcp                            vibrant_mcnulty
```

Aha `0.0.0.0:19092->9092/tcp`, the local port 19092 is mapped to the Kafka service port `9092` in the container. 
Similarly we'll remember `15432` later for PostgreSQL. Let's check out the Kafka cluster by listing topics:
```
> kcat -L -b localhost:19092
Metadata for all topics (from broker 0: localhost:19092/0):
 1 brokers:
  broker 0 at localhost:19092 (controller)
 2 topics:
  topic "__consumer_offsets" with 3 partitions:
    partition 0, leader 0, replicas: 0, isrs: 0
    partition 1, leader 0, replicas: 0, isrs: 0
    partition 2, leader 0, replicas: 0, isrs: 0
  topic "dlq-source" with 1 partitions:
    partition 0, leader 0, replicas: 0, isrs: 0
```

The `dlq-source` topic is up and running, great! Let's send a dummy message!

```
> echo "is my letter dead?" | kcat -b localhost:19092 -t dlq-source
```

You can actually see the messages in the Quarkus Kafka Dev UI:
http://localhost:8000/q/dev/io.quarkus.quarkus-kafka-client/kafka-dev-ui

And inspect the DLQman log:
```
... Processing message with TPO [dlq-source:0:0]
... Voiding message with TPO [dlq-source:0:0]
```

Since no particular headers were set, that is exactly the handler we expected.

## Actually matching headers

Lets commit a fatal mistake:

```
> echo "this is an ex-letter, it's pining for the fjords" \
 | kcat -b localhost:19092 -t dlq-source -H "internal-dlq-exception-class=SuperFatalException"
```

Now the logs:
```
... Ingesting record from TPO[dlq-source:0:1]
... Rule matched: save-fatal
... Processing message with TPO [dlq-source:0:1]
... Saving message [74e6f24e-63c4-4391-a7c2-cb44f1a2310f] TPO [dlq-source:0:1]
```

Finally, let's try a message that will be resent:
```
> echo "I'm a zombie parrot" \
 | kcat -b localhost:19092 -t dlq-source -H "internal-dlq-exception-class=QuiteRetryableException"
```

And the logs:
```
... Rule matched: resend-retryable
... Processing message with TPO [dlq-source:0:2]
... Saving message [a4062082-6399-4440-b5bc-14bc5ea521da] TPO [dlq-source:0:2]
... Finished ingesting message with TPO [dlq-source:0:2]

... 

... Resent message with [oldtpo -> newtpo] [dlq-source:0:2 -> resend-override:0:0], ts: 
... Updating status on message message [a4062082-6399-4440-b5bc-14bc5ea521da]
```

And inspecting the resend topic:
```
> kcat -b localhost:19092 -C -t resend-override -f 'Headers: %h; Message value: %s\n'
Headers: internal-dlq-exception-class=QuiteRetryableException; Message value: I'm a zombie parrot
% Reached end of topic resend-override [0] at offset 1
```

And that's it for basic DLQMan functionality! 

In the future you might be able to send messages with headers directly in the Quarkus Kafka Dev 
UI, but that doesn't seem to be working at the moment.