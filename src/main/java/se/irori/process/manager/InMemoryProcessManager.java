package se.irori.process.manager;

import io.quarkus.runtime.ShutdownEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.subscription.Cancellable;
import io.smallrye.mutiny.subscription.MultiSubscriber;
import io.vertx.mutiny.core.eventbus.EventBus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.context.ManagedExecutor;
import se.irori.model.Message;
import se.irori.model.Process;
import se.irori.model.ProcessState;
import se.irori.model.Source;

@Slf4j
@ApplicationScoped
public class InMemoryProcessManager implements ProcessManager {

  Map<UUID, Process> processMap = new HashMap<>();

  @Inject
  ManagedExecutor managedExecutor;

  @Inject
  EventBus eventBus;

  void onApplicationTermination(@Observes ShutdownEvent shutdownEvent) {
    log.info("Received shutdown event, starting cancelation of processes");
    processMap.keySet()
        .forEach(this::cancelProcess);
  }

  /**
   * Register a process consuming a source and sending a message in the `message-stream` event bus.
   *
   * @param process to start.
   */
  @Override
  public void registerProcess(Process process) {
    log.info("Registering & starting process with id [{}], with source id [{}]",
        process.getId(), process.getSource().getId());
    Cancellable callback =
        process.consume()
            .flatMap(message ->
                eventBus.<UUID>request("message-stream", message)
                    .map(io.vertx.mutiny.core.eventbus.Message::body)
                    .toMulti())
            .runSubscriptionOn(managedExecutor)
            .subscribe()
            .with(
                messageId -> handleOnItemEvent(messageId, process),
                t -> handleOnFailureEvent(t, process),
                () -> handleOnCompletedEvent(process));

    process.setCallback(callback);
    process.changeProcessState(ProcessState.RUNNING);
    processMap.put(process.getId(), process);
  }

  @Override
  public Uni<Process> getProcess(UUID id) {
    return Uni.createFrom().item(processMap.get(id));
  }

  @Override
  public List<Process> listProcesses() {
    log.info("Reading [{}] processes", processMap.size());
    return new ArrayList<>(processMap.values());
  }

  @Override
  public void cancelProcess(UUID id) {
    Process process = processMap.get(id);
    try {
      log.info("Shutting down process with id [{}]", process.getId());
      process.changeProcessState(ProcessState.CANCELLED);
      managedExecutor.runAsync(() -> process.getCallback().cancel())
          .get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Ungraceful shutdown of process with id [{}]", process.getId(), e);
    }
  }

  private void handleOnItemEvent(UUID messageId, Process process) {
    process.getProcessedMessages().getAndIncrement();
    log.debug("Finished process message with id [{}]", messageId);
  }

  private void handleOnFailureEvent(Throwable t, Process process) {
    log.error("Process with id [{}] failed", process.getId(), t);
    process.changeProcessState(ProcessState.FAILED);
  }

  private void handleOnCompletedEvent(Process process) {
    log.info("Process with id [{}] completed", process.getId());
    process.changeProcessState(ProcessState.COMPLETED);
  }
}
