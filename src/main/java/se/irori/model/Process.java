package se.irori.model;

import io.smallrye.mutiny.subscription.Cancellable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Process {
  private UUID id;
  private Cancellable callback;
  private ProcessState processState;
  private final AtomicInteger messageCount = new AtomicInteger();
  private Source source;
}
