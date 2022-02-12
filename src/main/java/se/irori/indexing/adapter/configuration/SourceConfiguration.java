package se.irori.indexing.adapter.configuration;

import io.smallrye.config.ConfigMapping;
import java.util.List;

@ConfigMapping(prefix = "source")
public interface SourceConfiguration {
  List<KafkaSourceConfiguration> kafka();

  interface KafkaSourceConfiguration {
    String bootstrapServers();
    String groupId();
    List<String> topics();
  }
}
