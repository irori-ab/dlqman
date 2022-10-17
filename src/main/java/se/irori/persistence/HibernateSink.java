package se.irori.persistence;

import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import se.irori.model.Message;
import se.irori.persistence.model.MessageDao;

@Slf4j
@ApplicationScoped
public class HibernateSink implements Sink {

  @ConsumeEvent("message-stream")
  public Uni<UUID> persist(Message message) {
    log.debug("Persisting message with id [{}]", message.getId());
    return MessageDao.from(message)
        .<MessageDao>persistAndFlush()
        .map(MessageDao::getId);
  }
}
