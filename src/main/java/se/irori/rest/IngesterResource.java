package se.irori.rest;

import io.smallrye.mutiny.Uni;
import se.irori.ingestion.manager.IngesterManager;
import se.irori.rest.model.IngesterDto;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Rest resource exposing process endpoints.
 */
@Path("/v1/processes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class IngesterResource {

  @Inject
  public IngesterManager ingesterManager;

  @GET
  public List<IngesterDto> listProcesses() {
    return ingesterManager.listIngester()
        .stream()
        .map(IngesterDto::from)
        .collect(Collectors.toList());
  }

  @GET
  @Path("/{id}")
  public Uni<IngesterDto> getIngester(@PathParam("id") UUID id) {
    return ingesterManager.getIngester(id)
        .map(IngesterDto::from);
  }
}
