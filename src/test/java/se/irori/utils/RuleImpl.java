package se.irori.utils;

import lombok.Builder;
import se.irori.config.Rule;

import java.util.Optional;

@Builder
public class RuleImpl implements Rule {
  String name, matcher, strategy;
  int priority;

  Optional<String> resendTopicOverride = Optional.empty();
  @Override
  public String name() {
    return name;
  }

  @Override
  public int priority() {
    return priority;
  }

  @Override
  public String matcher() {
    return matcher;
  }

  @Override
  public String strategy() {
    return strategy;
  }

  @Override
  public Optional<String> resendTopicOverride() {
    return resendTopicOverride;
  }
}
