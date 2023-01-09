package se.irori.ingestion;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import se.irori.config.AppConfiguration;
import se.irori.config.matchers.MatcherHolder;
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
public class IngesterHandler {

  @Inject
  AppConfiguration config;

  @Inject
  IngesterManager ingesterManager;

  @Inject
  MatcherHolder matcherHolder;

  void onApplicationStart(@Observes StartupEvent startupEvent) {
    log.info("Starting configured persistence processes:");
    config.sources()
        .forEach(source -> {
          Map<String, String> consumerProperties = new HashMap<>();
          consumerProperties.putAll(config.kafka().common());
          consumerProperties.putAll(config.kafka().consumer());
          consumerProperties.putAll(source.consumerPropertiesOverrides());

          Consumer consumer = new KafkaConsumer(consumerProperties);

          ingesterManager.registerIngester(
              Ingester.create(
                  source,
                  consumer,
                  matcherHolder));
        });
  }

  void onShutdown(@Observes ShutdownEvent shutdownEvent) {
    for (Ingester ing : ingesterManager.listIngester()) {
      ingesterManager.cancelIngester(ing.getId());
    }
  }
}
