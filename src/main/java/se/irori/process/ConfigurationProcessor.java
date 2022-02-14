package se.irori.process;

import io.quarkus.runtime.StartupEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import se.irori.indexing.adapter.configuration.SourceConfiguration;
import se.irori.indexing.adapter.kafka.KafkaAdapter;
import se.irori.model.Process;
import se.irori.model.Source;
import se.irori.persistence.DatabaseProcessor;
import se.irori.process.manager.ProcessManager;

/**
 * Class responsible for starting sources defined in properties.
 */
@ApplicationScoped
@Slf4j
public class ConfigurationProcessor {

  @Inject
  SourceConfiguration sourceConfiguration;

  @Inject
  ProcessManager processManager;

  @Inject
  DatabaseProcessor databaseProcessor;

  void onApplicationStart(@Observes StartupEvent startupEvent) {
    log.info("Starting configured persistence processes:");
    sourceConfiguration.kafka()
        .forEach(kafkaSourceConfiguration -> {
          KafkaAdapter kafkaAdapter = new KafkaAdapter(kafkaSourceConfiguration);
          kafkaSourceConfiguration.topics().forEach(topic -> {
            Source source = Source.builder()
                .name(topic)
                .build();

            processManager.registerProcess(
                Process.create(
                    source,
                    kafkaAdapter.consumeSource(source),
                    databaseProcessor.persist()));
          });
        });
  }
}
