package se.irori.config.matchers;

import se.irori.model.Message;

/**
 * Fallback matcher to allow default handling of messages not yet matched.
 */
public class AllMatcher implements Matcher {
  @Override
  public boolean match(Message message) {
    return true;
  }
}
