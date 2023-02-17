package se.irori.config;

import io.smallrye.config.ConfigMapping;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Map;

@ConfigMapping(prefix = "dlqman")
public interface AppConfiguration {

  List<Source> sources();

  List<Def> matchers();

  List<Def> dlqStrategies();

  KafkaConfig kafka();

  interface KafkaConfig {
    //@ConfigProperty(defaultValue = "true")
    //boolean enabled();
    Map<String, String> common();
    Map<String, String> consumer();
    Map<String, String> producer();

    @ConfigProperty(defaultValue = "2000")
    long pollTimeout();

  }

  interface Def {
    String name();

    /**
     * Simple name of the implementing class.
     * @return
     */
    String className();

    Map<String, String> config();
  }

}
