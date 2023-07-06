package se.irori.e2e;

import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumerRecord;
import io.vertx.mutiny.kafka.client.producer.KafkaProducerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.irori.config.SharedContext;

import javax.inject.Inject;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.currentTimeMillis;

@QuarkusTest
public class ResendMessagesIT {
    @Inject
    SharedContext ctx;

    io.vertx.mutiny.kafka.client.consumer.KafkaConsumer<byte[], byte[]> kafkaConsumer;
    io.vertx.mutiny.kafka.client.producer.KafkaProducer<byte[], byte[]> kafkaProducer;

    @BeforeEach
    public void setupKafka() {
        Map<String, String> consumerConfig = new HashMap<>();
        consumerConfig.putAll(ctx.getConfig().kafka().common());
        consumerConfig.putAll(ctx.getConfig().kafka().consumer());
        Map<String, String> producerConfig = new HashMap<>();
        producerConfig.putAll(ctx.getConfig().kafka().common());
        producerConfig.putAll(ctx.getConfig().kafka().producer());
        consumerConfig.put("group.id", "kafka-resend-test");
        consumerConfig.put("auto.offset.reset", "earliest");
        this.kafkaConsumer = io.vertx.mutiny.kafka.client.consumer.KafkaConsumer.create(
                ctx.getVertx(),
                consumerConfig,
                byte[].class, byte[].class);
        this.kafkaProducer = io.vertx.mutiny.kafka.client.producer.KafkaProducer.create(
                ctx.getVertx(),
                producerConfig,
                byte[].class, byte[].class);
        this.kafkaConsumer.subscribeAndAwait("many-dest");
        this.kafkaConsumer.seekToBeginningAndAwait(kafkaConsumer.assignmentAndAwait());
    }


    @Test
    public void resendManyRecords() {
        int noRecords = 200;

        //When 200 messages with resend header are sent
        for (int i = 0; i < noRecords; i++) {
            kafkaProducer.sendAndAwait(KafkaProducerRecord.create("many-source", "key".getBytes(), "value".getBytes())
                    .addHeader("dlqMatcher", "resend"));
        }
        Log.info(String.format("Sent %d items", noRecords));

        //Configure the consumer
        AssertSubscriber<KafkaConsumerRecord<byte[], byte[]>> sub =
                kafkaConsumer.toMulti()
                        .subscribe().withSubscriber(AssertSubscriber.create(noRecords));

        //Then assume they are all sent towards the "many-dest" topic
        sub.awaitItems(noRecords, Duration.ofSeconds(40));
        Log.info(String.format("Received %d records", sub.getItems().size()));
        Assertions.assertEquals(noRecords, sub.getItems().size(), "All messages are resent after 10s");
    }

    @Test
    public void resendRecordWithDelay() {
        //When one message with the delay strategy header is sent
        kafkaProducer.sendAndAwait(KafkaProducerRecord.create("many-source", "key".getBytes(), "value".getBytes())
                .addHeader("dlqMatcher", "resendWithDelay"));
        Log.info(String.format("Sent 1 item at %d", currentTimeMillis()));

        //Configure the consumer
        AssertSubscriber<KafkaConsumerRecord<byte[], byte[]>> sub =
                kafkaConsumer.toMulti()
                        .subscribe().withSubscriber(AssertSubscriber.create(1));

        //Then assume the message is resent only after the configured delay
        try {
            //Await for the item for 20 seconds
            sub.awaitItems(1, Duration.ofSeconds(20));
        } catch (AssertionError e) {
            //Assume the item hasn't been resent in 20 seconds
            Assertions.assertEquals(0, sub.getItems().size(), "No message is resent within 20s");
        }

        //Await for the item to be resent
        sub.awaitItems(1, Duration.ofSeconds(10));
        Assertions.assertEquals(1, sub.getItems().size(), "Message is sent after 20 seconds");
        Log.info(String.format("Received %d records at %d", sub.getItems().size(), currentTimeMillis()));
    }


    @Test
    public void resendHalfOfAllProducedRecords() {
        int noRecords = 200;

        //When 200 messages are sent
        for (int i = 0; i < noRecords; i++) {
            if (i % 2 == 0) {
                //resend header
                kafkaProducer.sendAndAwait(KafkaProducerRecord.create("many-source", "key".getBytes(), "value".getBytes())
                        .addHeader("dlqMatcher", "resend"));
            } else {
                //dismiss header
                kafkaProducer.sendAndAwait(KafkaProducerRecord.create("many-source", "key".getBytes(), "value".getBytes())
                        .addHeader("dlqMatcher", "dismiss"));
            }
        }
        Log.info(String.format("Sent %d items", noRecords));

        //Configure the consumer
        AssertSubscriber<KafkaConsumerRecord<byte[], byte[]>> sub =
                kafkaConsumer.toMulti()
                        .subscribe().withSubscriber(AssertSubscriber.create(100));

        //Then half of the items should be resent
        sub.awaitItems(noRecords / 2, Duration.ofSeconds(40));
        Log.info(String.format("Received %d records", sub.getItems().size()));
        Assertions.assertEquals(noRecords / 2, sub.getItems().size());
    }
}
