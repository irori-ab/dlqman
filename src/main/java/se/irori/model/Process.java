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

  public static Process create(
      @NotNull Source source,
      @NotNull Multi<Message> consumeSource,
      @NotNull Function<Message, Multi<Message>> persistFunction) {
    return Process.builder()
        .id(UUID.randomUUID())
        .source(source)
        .consumeSource(consumeSource)
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
