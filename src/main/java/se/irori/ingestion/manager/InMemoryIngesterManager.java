package se.irori.ingestion.manager;

import io.quarkus.logging.Log;
import io.quarkus.runtime.ShutdownEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import io.vertx.mutiny.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.context.ManagedExecutor;
import se.irori.config.AppConfiguration;
import se.irori.ingestion.Ingester;
import se.irori.model.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
@ApplicationScoped
public class InMemoryIngesterManager implements IngesterManager {

  Map<UUID, Ingester> ingesterMap = new HashMap<>();

  @Inject
  ManagedExecutor managedExecutor;

  @Inject
  EventBus eventBus;

  @Inject
  AppConfiguration config;

  void onApplicationTermination(@Observes ShutdownEvent shutdownEvent) {
    log.info("Received shutdown event, starting cancelation of processes");
    ingesterMap.keySet()
        .forEach(this::cancelIngester);
  }

  /**
   * Register a process consuming a source and sending a message in the `message-stream` event bus.
   *
   * @param ingester to start.
   */
  @Override
  public void registerIngester(Ingester ingester) {
    log.info("Registering & starting process with id [{}], with source id [{}]",
        ingester.getId(), ingester.getSource().name());
    Cancellable callback =
        ingester.consume()
            .flatMap(message ->
                eventBus.<String>request("message-stream", message)
                  .map(io.vertx.mutiny.core.eventbus.Message::body)
                  .call(() -> handlePublishMessage(message))
                  .toMulti())
            .subscribe()
            .with(messageId -> handleOnItemEvent(messageId, ingester),
                t -> handleOnFailureEvent(t, ingester),
                () -> handleOnCompletedEvent(ingester));

    ingester.setCallback(callback);
    ingester.changeIngesterState(IngesterState.RUNNING);
    ingesterMap.put(ingester.getId(), ingester);
  }

  @Override
  public Uni<Ingester> getIngester(UUID id) {
    return Uni.createFrom().item(ingesterMap.get(id));
  }

  @Override
  public List<Ingester> listIngester() {
    log.info("Reading [{}] processes", ingesterMap.size());
    return new ArrayList<>(ingesterMap.values());
  }

  @Override
  public void cancelIngester(UUID id) {
    Ingester ingester = ingesterMap.get(id);
    try {
      log.info("Shutting down ingester with id [{}]", ingester.getId());
      ingester.changeIngesterState(IngesterState.CANCELLED);
      managedExecutor.runAsync(() -> {
        ingester.getCallback().cancel();
        ingester.getConsumer().closeConsumer();
        })
          .get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Ungraceful shutdown of ingester with id [{}]", ingester.getId(), e);
    }
  }

  private void handleOnItemEvent(String tpo, Ingester ingester) {
    ingester.getProcessedMessages().getAndIncrement();
    log.debug("Finished ingesting message with TPO [{}]", tpo);
  }

  private void handleOnFailureEvent(Throwable t, Ingester ingester) {
    log.error("Ingester with id [{}] failed", ingester.getId(), t);
    ingester.changeIngesterState(IngesterState.FAILED);
  }

  private void handleOnCompletedEvent(Ingester ingester) {
    log.info("Ingester with id [{}] completed", ingester.getId());
    ingester.changeIngesterState(IngesterState.COMPLETED);
  }

  private Uni<Void> handlePublishMessage(Message message) {
    if (config.publishConsumedMessages()) {
      // Publish sucessful messages on the bus for further processing
      Log.trace(String.format("Internally republishing to ingested-messages TPO %s", message.getTPO()));
      eventBus.publish("ingested-messages", message);
    }
    return Uni.createFrom().voidItem();
  }
}
