package se.irori.indexing.adapter.kafka;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.record.TimestampType;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@Slf4j
@ApplicationScoped
public class TestRecordProducer {

  @Outgoing("test-topic")
  public Multi<Message<String>> generateSpringCloudTestRecords() {
    ConsumerRecord<Object, Object> record;
    Random random = new Random();
    return Multi.createFrom().ticks().every(Duration.ofSeconds(1))
        .map(x -> {
          log.info("Sending test record");
          return Message.of("payload")
              .addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
                  .withKey("key")
                  .withTopic("spring-cloud-dlq-topic")
                  .withHeaders(List.of(
                      new RecordHeader(
                          "X_ORIGINAL_TOPIC",
                          "original-topic".getBytes(StandardCharsets.UTF_8)),
                      new RecordHeader(
                          "X_ORIGINAL_PARTITION",
                          String.valueOf(random.nextInt(10)).getBytes(StandardCharsets.UTF_8)),
                      new RecordHeader(
                          "X_ORIGINAL_OFFSET",
                          String.valueOf(random.nextInt(5000)).getBytes(StandardCharsets.UTF_8)),
                      new RecordHeader(
                          "X_ORIGINAL_TIMESTAMP",
                          String.valueOf(Instant.now().toEpochMilli()).getBytes(StandardCharsets.UTF_8)),
                      new RecordHeader(
                          "X_ORIGINAL_TIMESTAMP_TYPE",
                          TimestampType.CREATE_TIME.name().getBytes(StandardCharsets.UTF_8)
                      )))
                  .build());
        });
  }
}
