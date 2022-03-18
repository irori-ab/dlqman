package se.irori.process.manager;

import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.UUID;
import se.irori.model.Process;

/**
 * A process-manager is responsible to manage source -> sink processes.
 * It exposes functionality to create, fetch and cancel processes on demand.
 */
public interface ProcessManager {

  /**
   * Register and start a process.

   * @param process to start.
   */
  void registerProcess(Process process);

  Uni<Process> getProcess(UUID id);

  List<Process> listProcesses();

  void cancelProcess(UUID id);
}
