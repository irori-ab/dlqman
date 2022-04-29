package se.irori.persistence;

import io.smallrye.mutiny.Uni;
import java.util.UUID;
import se.irori.model.Message;

/**
 * Interface defining how a message is persisted or sourced.
 */
public interface Sink {

  /**
   * Function taking a {@link Message} as input and Multi as result
   * which will persist the message when subscribed upon.
   * @return a uni which persists the message when subscribed on and returns message id.
   */
  Uni<UUID> persist(Message message);
}
