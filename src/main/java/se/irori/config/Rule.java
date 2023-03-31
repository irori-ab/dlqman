package se.irori.config;

import java.util.Optional;

public interface Rule {
  String name();
  int priority();
  String matcher();
  String strategy();

  Optional<String> resendTopicOverride();

}
