package se.irori.config;

import java.util.List;
import java.util.Map;

public interface Source {
  String name();
  String description();
  String sourceTopic();
  String resendTopic();

  List<Rule> matchRules();

  Map<String, String> consumerPropertiesOverrides();
}
