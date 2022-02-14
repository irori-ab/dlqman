package se.irori.persistence;

import io.smallrye.mutiny.Multi;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;
import se.irori.model.Message;

@ApplicationScoped
@Slf4j
public class DatabaseProcessor {

  @Inject
  SessionFactory sessionFactory;

  public Function<Message, Multi<Message>> persist() {
    return (message) -> sessionFactory.withTransaction((s, t) -> s.persist(message))
        .replaceWith(message)
        .toMulti();
  }
}
