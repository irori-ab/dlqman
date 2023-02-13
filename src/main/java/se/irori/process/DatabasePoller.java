package se.irori.process;

import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import se.irori.persistence.MessageRepository;
import se.irori.persistence.model.MessageDao;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class DatabasePoller implements Poller {

  @Override
  @ConsumeEvent("poll-resend")
  public Uni<List<MessageDao>> poll(String notused) {
    return MessageDao.toResend();
  }
}
