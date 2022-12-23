package se.irori.ingestion;

import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import se.irori.config.AppConfiguration;
import se.irori.ingestion.kafka.KafkaConsumer;
import se.irori.ingestion.manager.IngesterManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for starting sources defined in properties.
 */
@ApplicationScoped
@Slf4j
public class IngesterInitializer {

  @Inject
  AppConfiguration sourceConfiguration;

  @Inject
  IngesterManager ingesterManager;

  void onApplicationStart(@Observes StartupEvent startupEvent) {
    log.info("Starting configured persistence processes:");
    sourceConfiguration.sources()
        .forEach(sc -> {
          Map<String, String> consumerProperties = new HashMap<>();
          consumerProperties.putAll(sourceConfiguration.kafka().consumer());
          consumerProperties.putAll(sc.getConsumerPropertiesOverrides());

          Consumer consumer = new KafkaConsumer(consumerProperties);

          ingesterManager.registerIngester(
              Ingester.create(
                  sc,
                  consumer));
        });
  }
}
