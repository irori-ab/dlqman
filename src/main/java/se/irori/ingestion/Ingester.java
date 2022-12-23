package se.irori.ingestion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.Cancellable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.irori.config.Source;
import se.irori.ingestion.manager.IngesterState;
import se.irori.model.Message;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The responsibility of the ingester instance is to read and filter the data stream from one source.
 */
@Builder(access = AccessLevel.PRIVATE)
@Getter
@Slf4j
public class Ingester {
  private UUID id;
  @JsonIgnore
  private Cancellable callback;
  private IngesterState ingesterState;
  private final AtomicInteger processedMessages = new AtomicInteger();

  @JsonIgnore
  private Consumer consumer;

  @JsonIgnore
  private Source source;

  public static Ingester create(Source source, Consumer consumer) {
    return Ingester.builder()
      .id(UUID.randomUUID())
      .consumer(consumer)
      .source(source)
      .ingesterState(IngesterState.CREATED)
      .build();
  }

  public void changeIngesterState(IngesterState ingesterState) {
    this.ingesterState = ingesterState;
  }

  public void setCallback(Cancellable callback) {
    this.callback = callback;
  }

  public Multi<Message> consume() {
    return consumer.consume(source);
  }
}
