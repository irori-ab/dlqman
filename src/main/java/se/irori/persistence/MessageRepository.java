package se.irori.persistence;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import java.util.UUID;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny;
import se.irori.model.Message;
import se.irori.model.Repository;
import se.irori.persistence.model.MessageDao;

@Slf4j
@RequestScoped
public class MessageRepository implements Repository {

  @Inject
  Mutiny.SessionFactory sessionFactory;

  public Uni<UUID> persist(Message message) {
    return sessionFactory.openSession()
        .chain(session -> session.withTransaction(t -> session.persist(MessageDao.from(message)))
            .eventually(session::close))
        .replaceWith(message.getId());

    //return Uni.createFrom().item(message.getId())
    //    .map(m -> {
    //      log.info("Persisted message with id [{}]", message.getId());
    //      return m;
    //    });
  }
}
