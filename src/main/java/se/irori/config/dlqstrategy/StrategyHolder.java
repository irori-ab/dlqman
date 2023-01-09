package se.irori.config.dlqstrategy;

import io.quarkus.runtime.configuration.ConfigurationException;
import se.irori.config.AppConfiguration;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class StrategyHolder {
  Map<String, DLQStrategy> strategies = new HashMap<>();
  Map<String, DLQStrategy> builtin = new HashMap<>(2)
  {{
    put("ignore", new IgnoreDLQStrategy());
    put("discard", new DiscardDLQStrategy());
  }};

  public StrategyHolder(AppConfiguration config) {
    for (AppConfiguration.Def def : config.dlqStrategies()) {
      if (strategies.containsKey(def.name())) {
        throw new ConfigurationException("Duplicate DLQ strategy names");
      }
      strategies.put(def.name(), StrategyFactory.create(def.className(), def.config()));
    }
    strategies.putAll(builtin);
  }

  public DLQStrategy getStrategy(String name) {
    if (strategies.containsKey(name)) {
      return strategies.get(name);
    }
    throw new ConfigurationException(String.format("DLQStrategy not found: %s", name));
  }

  public void OnStartup(AppConfiguration config) {
    for (AppConfiguration.Def def : config.dlqStrategies()) {
      if (strategies.containsKey(def.name())) {
        throw new ConfigurationException("Duplicate DLQ strategy names");
      }
      strategies.put(def.name(), StrategyFactory.create(def.className(), def.config()));
    }
  }
}
