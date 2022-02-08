package se.irori.indexing.adapter.kafka;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import java.time.Duration;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@Slf4j
@ApplicationScoped
public class TestRecordProducer {

  @Outgoing("test-topic")
  public Multi<Message<String>> generateTestRecords() {
    return Multi.createFrom().ticks().every(Duration.ofSeconds(1))
        .map(x -> {
          log.info("Sending test record");
          return Message.of("testste")
              .addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
                  .withKey("my-key")
                  .withTopic("test-topic")
                  .build());
        });
  }
}
