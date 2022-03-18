package se.irori.model;

import io.smallrye.mutiny.Multi;
import java.util.UUID;
import java.util.function.Function;

/**
 * Interface defining how a message is persisted or sourced.
 */
public interface Repository {

  /**
   * Function taking a {@link Message} as input and Multi as result
   * which will persist the message when subscribed upon.
   * @return the function
   */
  Function<Message, Multi<UUID>> persist();
}
