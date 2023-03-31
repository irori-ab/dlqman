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

@QuarkusTest
public class ResendManyMessagesIT {
  @Inject
  SharedContext ctx;

  io.vertx.mutiny.kafka.client.consumer.KafkaConsumer<byte[],byte[]> kafkaConsumer;
  io.vertx.mutiny.kafka.client.producer.KafkaProducer<byte[],byte[]> kafkaProducer;

  int noRecords = 200;

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

    for (int i = 0;i < noRecords;i++) {
      kafkaProducer.sendAndAwait(KafkaProducerRecord.create("many-source", "key".getBytes(), "value".getBytes())
        .addHeader("resend", "test"));
    }
    Log.info(String.format("Sent %d items", noRecords));
  }


  @Test
  public void resend200Records() {
    AssertSubscriber<KafkaConsumerRecord<byte[],byte[]>> sub =
      kafkaConsumer.toMulti()
      .subscribe().withSubscriber(AssertSubscriber.create(noRecords));

    sub.awaitItems(noRecords, Duration.ofSeconds(40));
    Log.info(String.format("Received %d records", sub.getItems().size()));
    Assertions.assertEquals(noRecords, sub.getItems().size());

  }
}
