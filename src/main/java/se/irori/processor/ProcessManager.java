package se.irori.processor;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.UUID;
import se.irori.model.Message;
import se.irori.model.Process;
import se.irori.model.Source;

public interface ProcessManager {
  void registerProcess(Multi<Message> process, Source source);

  Uni<Process> getProcess(UUID id);
  Uni<List<Process>> listProcesses();
  void cancelProcess(UUID id);
}
