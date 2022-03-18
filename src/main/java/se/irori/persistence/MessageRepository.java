package se.irori.persistence;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Multi;
import java.util.UUID;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;
import se.irori.model.Message;

@ApplicationScoped
public class MessageRepository implements PanacheRepositoryBase<Message, UUID> {

  @Inject
  SessionFactory sessionFactory;

  public Function<Message, Multi<Message>> persist() {
    return (message) -> sessionFactory.withTransaction((s, t) -> s.persist(message))
        .replaceWith(message)
        .toMulti();
  }
}
