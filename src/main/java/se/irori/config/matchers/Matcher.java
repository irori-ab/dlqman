package se.irori.config.matchers;

import se.irori.model.Message;

public interface Matcher {
  boolean match(Message message);
}
