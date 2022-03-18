package se.irori.service;

import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import se.irori.model.Message;
import se.irori.persistence.model.MessageDao;

@ApplicationScoped
public class MessageService {
  public Uni<List<Message>> list() {
    return MessageDao.<MessageDao>listAll()
        .map(list -> list.stream()
            .map(MessageDao::toMessage)
            .collect(Collectors.toList()));
  }
}
