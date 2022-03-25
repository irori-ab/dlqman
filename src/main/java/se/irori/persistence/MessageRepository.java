package se.irori.persistence;

import io.smallrye.mutiny.Uni;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import se.irori.model.MessageStatus;
import se.irori.persistence.model.MessageDao;

@ApplicationScoped
public class MessageRepository {

  public Uni<List<MessageDao>> listMessages(
      LocalDateTime startTime,
      LocalDateTime endTime,
      UUID sourceId,
      MessageStatus messageStatus) {

    MessageQuery messageQuery = new MessageQuery(startTime, endTime, sourceId, messageStatus);
      return MessageDao.<MessageDao>find(messageQuery.getQuery(), messageQuery.getParams())
          .list();
  }
}
