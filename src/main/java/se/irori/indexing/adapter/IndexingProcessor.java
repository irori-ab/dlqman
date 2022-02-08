package se.irori.indexing.adapter;

import io.quarkus.runtime.StartupEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;
import se.irori.indexing.adapter.kafka.KafkaAdapter;
import se.irori.indexing.adapter.kafka.KafkaAdapterConfiguration;
import se.irori.model.Source;

@Slf4j
@ApplicationScoped
public class IndexingProcessor {

  void onApplicationStart(@Observes StartupEvent startupEvent) {
    log.info("Starting configured indexing processes:");
    startIndexingProcess();
  }


  public void startIndexingProcess() {
    KafkaAdapter testKafkaAdapter = new KafkaAdapter(
        KafkaAdapterConfiguration.builder()
            .bootstrapServers("localhost:9992")
            .groupId("dlqman-indexing-process-1")
            .build());

    testKafkaAdapter.consumeSource(
            Source.builder()
                .name("test-topic")
                .build())
        .subscribe()
        .with(message -> log.info(
            "Message arrived to processor with id [{}] and payloadString [{}]. Offset [{}], Partition[{}]",
            message.getId(),
            message.getPayloadString(),
            message.getOffset(),
            message.getPartition()));
  }
}
