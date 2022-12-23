package se.irori.config;

import io.smallrye.config.ConfigMapping;

import java.util.List;
import java.util.Map;

@ConfigMapping(prefix = "")
public interface AppConfiguration {
  List<Source> sources();

  KafkaConfig kafka();

  interface KafkaConfig {
    boolean enabled();
    Map<String, String> consumer();
    Map<String, String> producer();

  }
}
