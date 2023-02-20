package se.irori.config;

import io.smallrye.config.WithDefault;

import java.util.List;
import java.util.Map;

public interface Source {
  @WithDefault("true")
  Boolean enabled();
  String name();
  String description();
  String sourceTopic();
  String resendTopic();

  List<Rule> matchRules();

  Map<String, String> consumerPropertiesOverrides();
}
