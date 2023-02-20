package se.irori.ingestion;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import se.irori.config.AppConfiguration;
import se.irori.config.SharedContext;
import se.irori.config.Source;
import se.irori.ingestion.kafka.KafkaConsumer;
import se.irori.ingestion.manager.IngesterManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

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
  SharedContext ctx;

  void onApplicationStart(@Observes StartupEvent startupEvent) {
    log.info("Registering ingesters from sources");
    AtomicInteger noSources = new AtomicInteger(0);
    config.sources().values().stream().filter(Source::enabled)
        .forEach(source -> {
          noSources.getAndIncrement();
          Consumer consumer = new KafkaConsumer(ctx, source);

          ingesterManager.registerIngester(
              Ingester.create(
                  source,
                  consumer,
                  ctx));
        });
    if (noSources.get() < 1) {
      log.warn("No sources found in configuration");
    }
  }

  void onShutdown(@Observes ShutdownEvent shutdownEvent) {
    for (Ingester ing : ingesterManager.listIngester()) {
      ingesterManager.cancelIngester(ing.getId());
    }
  }
}
