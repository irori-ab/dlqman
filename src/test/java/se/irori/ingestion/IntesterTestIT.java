package se.irori.ingestion;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import se.irori.config.AppConfiguration;
import se.irori.utils.MockHelper;

import javax.inject.Inject;

@QuarkusTest
public class IntesterTestIT {
  @Inject
  AppConfiguration config;


  @Inject
  @Channel("dlq-source")
  Emitter<KafkaProducerRecord> emitter;



  @Test
  public void testIngestRecord() {
    emitter.send(MockHelper.generateMessage("test-key", "test-payload",
      new RecordHeader("test-header-1", "test-header-value1".getBytes()),
      new RecordHeader("x-original-topic", "original-topic".getBytes())
    ));

  }
  

}
