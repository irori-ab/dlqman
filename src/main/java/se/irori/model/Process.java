package se.irori.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.smallrye.common.constraint.NotNull;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.irori.indexing.adapter.IndexingAdapter;

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
  private final IndexingAdapter indexingAdapter;

  @JsonIgnore
  private final Repository repository;
  private final Source source;

  /**
   * Builder method used to construct a Process.
   *
   * @param source source to consume messages from.
   * @return the process.
   */
  public static Process create(
      @NotNull Source source,
      @NotNull IndexingAdapter indexingAdapter,
      @NotNull Repository repository) {
    return Process.builder()
        .id(UUID.randomUUID())
        .source(source)
        .indexingAdapter(indexingAdapter)
        .repository(repository)
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
    return indexingAdapter.consume(source);
  }

  public Uni<UUID> persist(Message message) {
    log.info("Persisting message with id [{}]", message.getId());
    return repository.persist(message);
  }
}
