package se.irori.rest;

import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.irori.rest.model.MessageDto;
import se.irori.service.MessageService;

@Path("/v1/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageResource {

  @Inject
  private MessageService messageService;

  @GET
  public Uni<List<MessageDto>> listMessages() {
    return messageService.list()
        .map(messageList -> messageList.stream()
            .map(MessageDto::from)
            .collect(Collectors.toList()));
  }
}
