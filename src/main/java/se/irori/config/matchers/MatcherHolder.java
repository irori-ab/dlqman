package se.irori.config.matchers;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ConfigurationException;
import se.irori.config.AppConfiguration;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class MatcherHolder {

  @Inject
  AppConfiguration config;

  Map<String, Matcher> matchers = new HashMap<>();
  Map<String, Matcher> builtin = new HashMap<>(1)
  {{
    put("all", new AllMatcher());
  }};

  public MatcherHolder(AppConfiguration config) {
    this.config = config;
  }

  public Matcher getMatcher(String name) {
    if (matchers.containsKey(name)) {
      return matchers.get(name);
    }
    throw new ConfigurationException(String.format("Matcher not found: %s", name));
  }

  void initialize(@Observes StartupEvent startupEvent) {
    for (AppConfiguration.Def def : config.matchers()) {
      if (this.matchers.containsKey(def.name())) {
        throw new ConfigurationException("Duplicate matcher names");
      }
      this.matchers.put(def.name(), MatcherFactory.create(def.className(), def.config()));
    }
    this.matchers.putAll(builtin);
  }

}
