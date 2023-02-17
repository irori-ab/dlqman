package se.irori.config.dlqstrategy;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ConfigurationException;
import se.irori.config.AppConfiguration;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class StrategyHolder {
  @Inject
  AppConfiguration config;

  Map<String, DLQStrategy> strategies = new HashMap<>();

  Map<String, DLQStrategy> builtin = new HashMap<>(2)
  {{
    put("dismiss", new DismissDLQStrategy());
    put("void", new VoidDLQStrategy());
  }};

  public StrategyHolder(AppConfiguration config) {
    this.config = config;
  }

  public DLQStrategy getStrategy(String name) {
    if (strategies.containsKey(name)) {
      return strategies.get(name);
    }
    throw new ConfigurationException(String.format("DLQStrategy not found: %s", name));
  }

  public void initialize(@Observes StartupEvent startupEvent) {
    for (AppConfiguration.Def def : config.dlqStrategies()) {
      if (this.strategies.containsKey(def.name())) {
        throw new ConfigurationException("Duplicate DLQ strategy names");
      }
      this.strategies.put(def.name(), StrategyFactory.create(def.className(), def.config()));
    }
    this.strategies.putAll(builtin);
    Log.info(String.format("StrategyHolder initialized with %d strategies", this.strategies.size()));
  }
}
