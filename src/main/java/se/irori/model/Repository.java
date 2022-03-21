package se.irori.model;

import io.smallrye.mutiny.Uni;
import java.util.UUID;

/**
 * Interface defining how a message is persisted or sourced.
 */
public interface Repository {

  /**
   * Function taking a {@link Message} as input and Multi as result
   * which will persist the message when subscribed upon.
   * @return the function
   */
  Uni<UUID> persist(Message message);
}
