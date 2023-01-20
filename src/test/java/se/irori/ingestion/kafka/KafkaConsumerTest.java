package se.irori.ingestion.kafka;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import io.vertx.mutiny.kafka.client.producer.KafkaProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.irori.config.AppConfiguration;
import se.irori.ingestion.Consumer;
import se.irori.model.Message;
import se.irori.model.MetaDataType;
import se.irori.model.Metadata;

import javax.inject.Inject;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@QuarkusTest
public class KafkaConsumerTest {

  @Inject
  AppConfiguration config;

  @Inject
  @Channel("dev-source")
  Emitter<KafkaProducerRecord> emitter;

  Consumer consumer;



  @BeforeEach
  public void setupKafka() {
    Map<String, String> producerConfig = new HashMap<>();
    producerConfig.putAll(config.kafka().common());
    producerConfig.putAll(config.kafka().producer());
    consumer = new KafkaConsumer(config, config.sources().stream().filter(source -> source.name().equals("test-source")).findFirst().get());
  }

  @AfterEach
  public void cleanup() {
    consumer.closeConsumer();
  }

  @Test
  public void testConsumeMessage() {
    org.eclipse.microprofile.reactive.messaging.Message msg = org.eclipse.microprofile.reactive.messaging.Message.of("test-payload")
      .addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
        .withKey("test-key")
        .withTopic("dlq-source")
        .withHeaders(List.of(
          new RecordHeader(
            "test-header-1",
            "test-header-value1".getBytes()
          ),
          new RecordHeader(
            "x-original-topic",
            "original-topic".getBytes()
          )))
        .build());
    emitter.send(msg);
    AssertSubscriber<Message> sub = consumer.consume().subscribe().withSubscriber(AssertSubscriber.create())
      .awaitCompletion(Duration.ofMillis(2000l));
    //Message m = sub.getLastItem();
    Message m = sub.assertCompleted().getLastItem();
    Assertions.assertEquals("dev-source",m.getSourceTopic());
    Metadata h1 = m.getMetadataList().stream().filter(h -> h.getKey().equals("test-header-value1")).findFirst().get();
    Assertions.assertEquals("test-header-value1", h1.getValue());
    Assertions.assertEquals(MetaDataType.SOURCE_HEADER, h1.getType());
    Metadata h2 = m.getMetadataList().stream().filter(h -> h.getKey().equals("x-original-topic")).findFirst().get();
    Assertions.assertEquals("original-topic", h2.getValue());
    Assertions.assertEquals(MetaDataType.SOURCE_HEADER, h2.getType());
    Metadata h3 = m.getMetadataList().stream().filter(h -> h.getKey().equals("internal-original-topic")).findFirst().get();
    Assertions.assertEquals("original-topic", h3.getValue());
    Assertions.assertEquals(MetaDataType.INTERNAL, h3.getType());
  }

}
