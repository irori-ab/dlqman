package se.irori.rest;

import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.irori.model.Process;
import se.irori.process.manager.ProcessManager;
import se.irori.rest.model.ProcessDto;

@Path("/v1/processes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProcessResource {

  @Inject
  public ProcessManager processManager;

  @GET
  public List<ProcessDto> listProcesses() {
    return processManager.listProcesses()
        .stream()
        .map(ProcessDto::from)
        .collect(Collectors.toList());
  }

  @GET
  @Path("/{id}")
  public Uni<ProcessDto> getProcess(@PathParam("id") UUID id) {
    return processManager.getProcess(id)
        .map(ProcessDto::from);
  }
}
