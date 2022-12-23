package se.irori.config;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class Source {
  private String name;
  private String description;
  private String sourceTopic;
  private String resendTopic;

  private Rule[] matchRules;

  private Map<String, String> consumerPropertiesOverrides;
}
