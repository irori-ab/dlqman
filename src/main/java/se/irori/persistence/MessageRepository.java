package se.irori.persistence;

import io.smallrye.mutiny.Multi;
import java.util.UUID;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;
import se.irori.model.Message;
import se.irori.model.Repository;
import se.irori.persistence.model.MessageDao;

@ApplicationScoped
public class MessageRepository implements Repository {

  @Inject
  SessionFactory sessionFactory;

  @Transactional
  public Function<Message, Multi<UUID>> persist() {
    return (message) ->
        MessageDao.from(message)
            .<MessageDao>persistAndFlush()
            .replaceWith(message.getId())
            .toMulti();
  }
}
