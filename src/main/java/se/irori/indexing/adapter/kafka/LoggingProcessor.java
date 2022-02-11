package se.irori.indexing.adapter.kafka;

import io.quarkus.runtime.StartupEvent;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;
import se.irori.model.Source;

@Slf4j
@ApplicationScoped
public class LoggingProcessor {

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
                .name("spring-cloud-dlq-topic")
                .build())
        .subscribe()
        .with(message -> log.info(
            "Message arrived to processor with id [{}] and payloadString [{}]. Offset [{}], Partition[{}]. Metadata[{}]",
            message.getId(),
            message.getPayloadString(),
            message.getOffset(),
            message.getPartition(),
            message.getMetaDataList()
                .stream().map(metaData -> String.format("Key: [%s], Value: [%s], Type [%s].",
                    metaData.getKey(), metaData.getValue(), metaData.getType()))
                .collect(Collectors.toList())));
  }
}
