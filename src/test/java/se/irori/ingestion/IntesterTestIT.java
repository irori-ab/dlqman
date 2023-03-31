package se.irori.ingestion;

import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.kafka.client.producer.KafkaProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import se.irori.model.Message;
import se.irori.model.Metadata;
import se.irori.utils.MockHelper;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@QuarkusTest
public class IntesterTestIT {


  @Inject
  @Channel("ingester-test")
  Emitter<KafkaProducerRecord> emitter;

  @Inject
  EventBus bus;



  @Test
  public void testIngestRecord() {
    String uuid = UUID.randomUUID().toString();
    emitter.send(MockHelper.generateMessage("test-key", "test-payload",
      new RecordHeader("test-header-1", "test-header-value1".getBytes()),
      new RecordHeader("mid", uuid.getBytes()),
      new RecordHeader("x-original-topic", "original-topic".getBytes())
    ));
    Log.info("Sent record to Kafka");

    AssertSubscriber<Message> result =
      Multi.createFrom().<Message>emitter(
      em -> {
        bus.localConsumer("ingested-messages").handler(mh -> {
          Message msg = (Message) mh.body();
          if (msg.getSourceTopic().equals("ingester-test")) {
            em.emit((Message) mh.body());
            em.complete();
          }
        });
      })
      .subscribe().withSubscriber(AssertSubscriber.create(1));


    result.awaitItems(1, Duration.ofSeconds(5));
    Message m = result.assertCompleted().getLastItem();
    List<Metadata> headers = m.getMetadataList();
    Assertions.assertEquals(4, headers.size());
    headers.stream().forEach(md -> {
      switch (md.getKey()) {
        case "test-header-1":
          Assertions.assertEquals("test-header-value1", md.getValue());
          break;
        case "mid":
          Assertions.assertEquals(uuid, md.getValue());
          break;
        case "x-original-topic":
          Assertions.assertEquals("original-topic", md.getValue());
          break;
        case "internal-original-topic":
          Assertions.assertEquals("original-topic", md.getValue());
          break;
        default:
          Assertions.fail("Unknown header: " + md.getKey());
      }
    });
  }

}
