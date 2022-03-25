package se.irori.rest;

import io.smallrye.mutiny.Uni;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import se.irori.model.MessageStatus;
import se.irori.rest.model.MessageDto;
import se.irori.service.MessageService;

@GraphQLApi
public class MessageResource {

  @Inject
  MessageService messageService;

  @Query("allMessages")
  @Description("Get all messages")
  public Uni<List<MessageDto>> getAllMessages() {
    return messageService.list()
        .map(list -> list.stream()
            .map(MessageDto::from)
            .collect(Collectors.toList()));
  }

  @Query
  @Description("Get message")
  public Uni<MessageDto> getMessage(@Name("id") UUID id) {
    return messageService.getMessage(id)
        .map(MessageDto::from);
  }

  @Query
  @Description("List by index time")
  public Uni<List<MessageDto>> listMessages(
      LocalDateTime startTime,
      LocalDateTime endTime,
      UUID sourceId,
      MessageStatus messageStatus) {
    return messageService.listMessages(startTime, endTime, sourceId, messageStatus)
        .map(list -> list.stream()
            .map(MessageDto::from)
            .collect(Collectors.toList()));
  }
}
