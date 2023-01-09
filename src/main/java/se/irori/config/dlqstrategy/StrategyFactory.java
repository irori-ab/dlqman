package se.irori.config.dlqstrategy;

import io.quarkus.runtime.configuration.ConfigurationException;

import java.util.Map;

public class StrategyFactory {
  public static DLQStrategy create(String className, Map<String, String> config) {
    switch (className) {
      case "SimpleResendDLQStrategy":
        return new SimpleResendDLQStrategy(getDurationFromConfig(config));
      case "IgnoreDLQStrategy":
        return new IgnoreDLQStrategy();
      case "DiscardDLQStrategy":
        return new DiscardDLQStrategy();
      case "MaxRetriesDLQStrategy":
        return new MaxRetriesDLQStrategy(getDurationFromConfig(config), getRetriesFromConfig(config));
      default:
        throw new ConfigurationException(String.format("Unsupported DLQStrategy: %s", className));
    }
  }

  private static Long getDurationFromConfig(Map<String, String> config) {
    if (!config.containsKey(ResendDLQStrategy.CONFIG_DURATION)) {
      throw new ConfigurationException(String.format("DLQ Strategy missing config: %s",
        ResendDLQStrategy.CONFIG_DURATION));
    }
    return Long.valueOf(config.get(ResendDLQStrategy.CONFIG_DURATION));
  }

  private static int getRetriesFromConfig(Map<String, String> config) {
    if (!config.containsKey(ResendDLQStrategy.CONFIG_MAX_RETRIES)) {
      throw new ConfigurationException(String.format("DLQ Strategy missing config: %s",
        ResendDLQStrategy.CONFIG_MAX_RETRIES));
    }
    return Integer.valueOf(config.get(ResendDLQStrategy.CONFIG_MAX_RETRIES));
  }
}
