package se.irori.config.matchers;

import io.quarkus.runtime.configuration.ConfigurationException;
import se.irori.config.AppConfiguration;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class MatcherHolder {
  Map<String, Matcher> matchers = new HashMap<>();
  Map<String, Matcher> builtin = new HashMap<>(1)
  {{
    put("all", new AllMatcher());
  }};

  public MatcherHolder(AppConfiguration config) {
    for (AppConfiguration.Def def : config.matchers()) {
      if (matchers.containsKey(def.name())) {
        throw new ConfigurationException("Duplicate matcher names");
      }
      matchers.put(def.name(), MatcherFactory.create(def.className(), def.config()));
    }
    matchers.putAll(builtin);
  }

  public Matcher getMatcher(String name) {
    if (matchers.containsKey(name)) {
      return matchers.get(name);
    }
    throw new ConfigurationException(String.format("Matcher not found: %s", name));
  }

  public void startup(AppConfiguration config) {
    for (AppConfiguration.Def def : config.matchers()) {
      if (matchers.containsKey(def.name())) {
        throw new ConfigurationException("Duplicate matcher names");
      }
      matchers.put(def.name(), MatcherFactory.create(def.className(), def.config()));
    }
  }
}
