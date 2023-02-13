package se.irori.ingestion;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import se.irori.config.AppConfiguration;
import se.irori.config.matchers.MatcherHolder;
import se.irori.ingestion.kafka.KafkaConsumer;
import se.irori.ingestion.manager.IngesterManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

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

  @Inject
  Vertx vertx;

  void onApplicationStart(@Observes StartupEvent startupEvent) {
    log.info("Starting configured persistence processes:");
    config.sources()
        .forEach(source -> {
          Consumer consumer = new KafkaConsumer(config, source, vertx);

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
