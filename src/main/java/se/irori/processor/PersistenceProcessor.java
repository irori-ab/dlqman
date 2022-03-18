package se.irori.processor;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;
import se.irori.indexing.adapter.configuration.SourceConfiguration;
import se.irori.indexing.adapter.kafka.KafkaAdapter;
import se.irori.model.Message;
import se.irori.model.Source;

@ApplicationScoped
@Slf4j
public class PersistenceProcessor {

  @Inject
  SourceConfiguration sourceConfiguration;

  @Inject
  ProcessManager processManager;

  @Inject
  SessionFactory sessionFactory;

  void onApplicationStart(@Observes StartupEvent startupEvent) {
    log.info("Starting configured persistence processes:");
    sourceConfiguration.kafka()
        .forEach(kafkaSourceConfiguration -> {
          KafkaAdapter kafkaAdapter = new KafkaAdapter(kafkaSourceConfiguration);
          kafkaSourceConfiguration.topics().forEach(topic ->
              startSourcePersistence(
                  Source.builder()
                      .name(topic)
                      .build(),
                  kafkaAdapter));
        });

  }

  private void startSourcePersistence(Source source, KafkaAdapter kafkaAdapter) {
    Multi<Message> messageMulti = persistSourceMulti(source, kafkaAdapter);
    processManager.registerProcess(messageMulti, source);
  }

  private Multi<Message> persistSourceMulti(Source source, KafkaAdapter kafkaAdapter) {
    return kafkaAdapter.consumeSource(source)
        .flatMap(message ->
            sessionFactory.withTransaction((s, t) -> s.persist(message))
                .replaceWith(message)
                .toMulti());
  }
}
