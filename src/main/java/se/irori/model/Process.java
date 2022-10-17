package se.irori.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.smallrye.common.constraint.NotNull;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.Cancellable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.irori.indexing.adapter.Indexer;

/**
 * Entity defining a source -> sink process and it´s lifecycle.
 *
 */
@Builder(access = AccessLevel.PRIVATE)
@Getter
@Slf4j
public class Process {

  private final UUID id;

  @JsonIgnore
  private Cancellable callback;
  private ProcessState processState;
  private final AtomicInteger processedMessages = new AtomicInteger();

  @JsonIgnore
  private final Indexer indexer;

  @JsonIgnore
  private final Source source;

  /**
   * Builder method used to construct a Process.
   *
   * @param source source to consume messages from.
   * @return the process.
   */
  public static Process create(
      @NotNull Source source,
      @NotNull Indexer indexer) {
    return Process.builder()
        .id(UUID.randomUUID())
        .source(source)
        .indexer(indexer)
        .processState(ProcessState.CREATED)
        .build();
  }

  public void changeProcessState(ProcessState processState) {
    this.processState = processState;
  }

  public void setCallback(Cancellable callback) {
    this.callback = callback;
  }

  public Multi<Message> consume() {
    return indexer.consume(source);
  }
}
