package se.irori.utils;

import lombok.Builder;
import se.irori.config.Rule;
import se.irori.config.Source;

import java.util.List;
import java.util.Map;


@Builder
public class SourceImpl implements Source {
  String name, description, sourceTopic, resendTopic;
  List<Rule> matchRules;

  Map<String, String> consumerPropertiesOverrides;
  @Override
  public Boolean enabled() {
    return true;
  }
  @Override
  public String name() {
    return name;
  }

  @Override
  public String description() {
    return description;
  }

  @Override
  public String sourceTopic() {
    return sourceTopic;
  }

  @Override
  public String resendTopic() {
    return resendTopic;
  }

  @Override
  public List<Rule> matchRules() {
    return matchRules;
  }

  @Override
  public Map<String, String> consumerPropertiesOverrides() {
    return consumerPropertiesOverrides;
  }
}
