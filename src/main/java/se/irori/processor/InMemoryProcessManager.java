package se.irori.processor;

import io.quarkus.runtime.ShutdownEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
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

  void onApplicationTermination(@Observes ShutdownEvent shutdownEvent) {
    log.info("Received shutdown event, starting cancelation of processes");
    processMap.values()
        .forEach(process -> {
          try {
            log.info("Shutting down process with id [{}]", process.getId());
            managedExecutor.runAsync(() -> process.getCallback().cancel())
                .get();
          } catch (InterruptedException | ExecutionException e) {
            log.error("Ungraceful shutdown of process with id [{}]", process.getId(), e);
          }
        });
  }

  @Override
  public void registerProcess(Multi<Message> messageMulti, Source source) {
    Process process = Process.builder()
        .id(UUID.randomUUID())
        .processState(ProcessState.RUNNING)
        .build();

    log.info("Registering & starting process with id [{}], for source with id [{}]",
        process.getId(), source.getId());

    Cancellable callback = messageMulti
        //TODO should we manage the execution threads more precisely?
        // Risk is that we run out of threads?
        // https://smallrye.io/smallrye-mutiny/guides/emit-subscription
        // We could also skip specifying executor, and let Quarkus decide.
        .runSubscriptionOn(managedExecutor)
        .subscribe()
        .with(
            message -> handleOnItemEvent(message, process),
            t -> handleOnFailureEvent(t, process),
            () -> handleOnCompletedEvent(process));
    process.setCallback(callback);

    processMap.put(process.getId(), process);
  }

  @Override
  public Uni<Process> getProcess(UUID id) {
    return Uni.createFrom().item(processMap.get(id));
  }

  @Override
  public Uni<List<Process>> listProcesses() {
    return Uni.createFrom().item(new ArrayList<>(processMap.values()));
  }

  @Override
  public void cancelProcess(UUID id) {
    cancelProcess(processMap.get(id));
  }

  public void cancelProcess(Process process) {
    process.getCallback().cancel();
    process.setProcessState(ProcessState.CANCELLED);
  }

  private void handleOnItemEvent(Message message, Process process) {
    process.getMessageCount().getAndIncrement();
    log.info("Received message with id [{}]", message.getId());
  }

  private void handleOnFailureEvent(Throwable t, Process process) {
    log.error("Process with id [{}] failed", process.getId(), t);
    process.setProcessState(ProcessState.FAILED);
  }

  private void handleOnCompletedEvent(Process process) {
    log.info("Process with id [{}] completed", process.getId());
    process.setProcessState(ProcessState.COMPLETED);
  }
}
