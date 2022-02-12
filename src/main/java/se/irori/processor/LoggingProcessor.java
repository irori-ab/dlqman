package se.irori.processor;

import io.quarkus.runtime.StartupEvent;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import se.irori.indexing.adapter.configuration.SourceConfiguration;
import se.irori.indexing.adapter.kafka.KafkaAdapter;
import se.irori.model.Source;

@Slf4j
@ApplicationScoped
public class LoggingProcessor {

  @Inject
  SourceConfiguration sourceConfiguration;

  void onApplicationStart(@Observes StartupEvent startupEvent) {
    log.info("Starting configured indexing processes:");
    startIndexingProcess();
  }

  public void startIndexingProcess() {
    sourceConfiguration.kafka()
        .forEach(kafkaSourceConfiguration -> {
          KafkaAdapter kafkaAdapter = new KafkaAdapter(kafkaSourceConfiguration);
          kafkaSourceConfiguration.topics().forEach(topic -> {
            kafkaAdapter.consumeSource(
                    Source.builder()
                        .name(topic)
                        .build())
                .subscribe()
                .with(message -> log.info(
                    "Message arrived to processor with id [{}] and payloadString [{}]. Offset [{}], Partition[{}]. Metadata[{}]",
                    message.getId(),
                    message.getPayloadString(),
                    message.getOffset(),
                    message.getPartition(),
                    message.getMetaDataList()
                        .stream()
                        .map(metaData -> String.format("Key: [%s], Value: [%s], Type [%s].",
                            metaData.getKey(), metaData.getValue(), metaData.getType()))
                        .collect(Collectors.toList())));
          });
        });
  }
}
