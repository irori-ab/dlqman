package se.irori.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.smallrye.common.constraint.NotNull;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.Cancellable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

/**
 * Entity defining a source -> sink process and it´s lifecycle.
 *
 */
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Process {

  private final UUID id;

  @JsonIgnore
  private Cancellable callback;
  private ProcessState processState;
  private final AtomicInteger processedMessages = new AtomicInteger();

  @JsonIgnore
  private final Multi<Message> consumeSource;
  private final Function<Message, Multi<Message>> persistFunction;
  private final Source source;

  /**
   * Builder method used to construct a Process.
   *
   * @param source source to consume messages from.
   * @param consume consume function returning a {@link Multi}.
   * @param persistFunction persisting function. Returning a {@link Multi}
   * @return the process.
   */
  public static Process create(
      @NotNull Source source,
      @NotNull Multi<Message> consume,
      @NotNull Function<Message, Multi<Message>> persistFunction) {
    return Process.builder()
        .id(UUID.randomUUID())
        .source(source)
        .consumeSource(consume)
        .persistFunction(persistFunction)
        .processState(ProcessState.CREATED)
        .build();
  }

  public void changeProcessState(ProcessState processState) {
    this.processState = processState;
  }

  public void setCallback(Cancellable callback) {
    this.callback = callback;
  }
}
