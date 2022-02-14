package se.irori.rest;

import io.smallrye.mutiny.Uni;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.irori.model.Process;
import se.irori.processor.ProcessManager;

@Path("/v1/processes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProcessResource {

  @Inject
  public ProcessManager processManager;

  @GET
  public Uni<List<Process>> listProcesses() {
    return processManager.listProcesses();
  }
}
