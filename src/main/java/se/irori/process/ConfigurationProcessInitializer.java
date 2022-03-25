package se.irori.process;

import io.quarkus.runtime.StartupEvent;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.context.ManagedExecutor;
import se.irori.indexing.adapter.Indexer;
import se.irori.indexing.adapter.configuration.SourceConfiguration;
import se.irori.indexing.adapter.kafka.KafkaIndexer;
import se.irori.model.Process;
import se.irori.model.Source;
import se.irori.process.manager.ProcessManager;

/**
 * Class responsible for starting sources defined in properties.
 */
@ApplicationScoped
@Slf4j
public class ConfigurationProcessInitializer {

  @Inject
  SourceConfiguration sourceConfiguration;

  @Inject
  ProcessManager processManager;

  void onApplicationStart(@Observes StartupEvent startupEvent) {
    log.info("Starting configured persistence processes:");
    sourceConfiguration.kafka()
        .forEach(kafkaSourceConfiguration -> {
          Indexer adapter = new KafkaIndexer(kafkaSourceConfiguration);
          kafkaSourceConfiguration.topics().forEach(topic -> {
            Source source = Source.builder()
                .id(UUID.randomUUID())
                .name(topic)
                .build();

            processManager.registerProcess(
                Process.create(
                    source,
                    adapter));
          });
        });
  }
}
