package se.irori.config.matchers;

import io.quarkus.runtime.configuration.ConfigurationException;

import java.util.Map;

public class MatcherFactory {
  public static Matcher create(String className, Map<String, String> config) {
    switch (className) {
      case "HeaderRegexMatcher":
        return new HeaderRegexMatcher(getStringFromConfig(config, HeaderRegexMatcher.CONFIG_HEADER_NAME),
          getStringFromConfig(config, HeaderRegexMatcher.CONFIG_REGEX_PATTERN));
      case "AllMatcher":
        return new AllMatcher();
      default:
        throw new ConfigurationException(String.format("Unsupported matcher: %s", className));
    }
  }

  private static String getStringFromConfig(Map<String, String> config, String name) {
    if (!config.containsKey(name) || config.get(name).isBlank()) {
      throw new ConfigurationException(String.format("Matcher missing config: %s", name));
    }
    return config.get(name);
  }
}
