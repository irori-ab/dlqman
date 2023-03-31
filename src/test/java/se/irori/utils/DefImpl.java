package se.irori.utils;

import lombok.Builder;
import se.irori.config.AppConfiguration;

import java.util.Map;

@Builder
public class DefImpl implements AppConfiguration.Def {
  String name, className;
  Map<String, String> config;

  public String name() {
    return name;
  }

  public String className() {
    return className;
  }

  public Map<String, String> config() {
    return config;
  }
}
