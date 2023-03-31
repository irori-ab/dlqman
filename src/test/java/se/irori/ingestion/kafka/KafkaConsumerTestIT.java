package se.irori.ingestion.kafka;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.irori.config.AppConfiguration;
import se.irori.config.SharedContext;
import se.irori.ingestion.Consumer;
import se.irori.model.Message;
import se.irori.model.MetaDataType;
import se.irori.utils.AssertHelper;
import se.irori.utils.MockHelper;

import javax.inject.Inject;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@QuarkusTest
public class KafkaConsumerTestIT {

  @Inject
  AppConfiguration config;

  @Inject
  SharedContext ctx;

  @Inject
  @Channel("consumer-test")
  Emitter<KafkaProducerRecord> emitter;

  Consumer consumer;


  @BeforeEach
  public void setupKafka() {
    Map<String, String> producerConfig = new HashMap<>();
    producerConfig.putAll(config.kafka().common());
    producerConfig.putAll(config.kafka().producer());
    config.kafka().consumer().put("group.id", "kafka-consumer-test");
    consumer = new KafkaConsumer(ctx, config.sources().get("consumer-test"));

  }

  @AfterEach
  public void cleanup() {
    consumer.closeConsumer();
  }

  @Test
  public void testConsumeMessage() throws InterruptedException {
    emitter.send(MockHelper.generateMessage("test-key", "test-payload",
      new RecordHeader("test-header-1", "test-header-value1".getBytes()),
      new RecordHeader("x-original-topic", "original-topic".getBytes())
    ));

    AssertSubscriber<Message> sub = consumer.consume().subscribe().withSubscriber(AssertSubscriber.create(1l));

    Message m = sub.awaitItems(1, Duration.ofSeconds(5)).getLastItem();
    Assertions.assertEquals("consumer-test",m.getSourceTopic());

    AssertHelper.headerTypeValue("test-header-1", MetaDataType.SOURCE_HEADER, "test-header-value1", m);
    AssertHelper.headerTypeValue("x-original-topic", MetaDataType.SOURCE_HEADER, "original-topic", m);
    AssertHelper.headerTypeValue("internal-original-topic", MetaDataType.INTERNAL, "original-topic", m);

  }


}
