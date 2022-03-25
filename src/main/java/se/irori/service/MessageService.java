package se.irori.service;

import io.smallrye.mutiny.Uni;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import se.irori.model.Message;
import se.irori.model.MessageStatus;
import se.irori.persistence.MessageRepository;
import se.irori.persistence.model.MessageDao;

@ApplicationScoped
public class MessageService {

  @Inject
  MessageRepository messageRepository;
  public Uni<List<Message>> list() {
    return MessageDao.<MessageDao>listAll()
        .map(list -> list.stream()
            .map(MessageDao::toMessage)
            .collect(Collectors.toList()));
  }

  public Uni<Message> getMessage(UUID id) {
    return MessageDao.<MessageDao>findById(id)
        .map(MessageDao::toMessage);
  }

  public Uni<List<Message>> listMessages(
      LocalDateTime startTime,
      LocalDateTime endTime,
      UUID sourceId,
      MessageStatus messageStatus) {
    return messageRepository.listMessages(startTime, endTime, sourceId, messageStatus)
        .map(list -> list.stream()
            .map(MessageDao::toMessage)
            .collect(Collectors.toList()));
  }
}
