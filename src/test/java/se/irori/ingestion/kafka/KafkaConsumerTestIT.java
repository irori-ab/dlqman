package se.irori.ingestion.kafka;

import io.quarkus.arc.Priority;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.mutiny.core.Vertx;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.irori.config.AppConfiguration;
import se.irori.ingestion.Consumer;
import se.irori.ingestion.Ingester;
import se.irori.ingestion.manager.IngesterManager;
import se.irori.model.Message;
import se.irori.model.MetaDataType;
import se.irori.utils.AssertHelper;
import se.irori.utils.MockHelper;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.*;

@QuarkusTest
public class KafkaConsumerTestIT {

  @Inject
  AppConfiguration config;


  @Inject
  @Channel("dlq-source")
  Emitter<KafkaProducerRecord> emitter;

  Consumer consumer;



  @BeforeEach
  public void setupKafka() {
    Map<String, String> producerConfig = new HashMap<>();
    producerConfig.putAll(config.kafka().common());
    producerConfig.putAll(config.kafka().producer());
    config.kafka().consumer().put("group.id", "kafka-consumer-test");
    consumer = new KafkaConsumer(config, config.sources().stream().filter(source -> source.name().equals("test-source")).findFirst().get(), Vertx.vertx());
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

    Message m = sub.awaitItems(1, Duration.ofMillis(2000l)).getLastItem();
    Assertions.assertEquals("dlq-source",m.getSourceTopic());

    AssertHelper.headerTypeValue("test-header-1", MetaDataType.SOURCE_HEADER, "test-header-value1", m);
    AssertHelper.headerTypeValue("x-original-topic", MetaDataType.SOURCE_HEADER, "original-topic", m);
    AssertHelper.headerTypeValue("internal-original-topic", MetaDataType.INTERNAL, "original-topic", m);

  }

  @Priority(1)
  @Alternative
  @Singleton
  static
  class IngesterMock implements IngesterManager {

    @Override
    public void registerIngester(Ingester ingester) {

    }

    @Override
    public Uni<Ingester> getIngester(UUID id) {
      return Uni.createFrom().nullItem();
    }

    @Override
    public List<Ingester> listIngester() {
      return Collections.emptyList();
    }

    @Override
    public void cancelIngester(UUID id) {

    }
  }

}
