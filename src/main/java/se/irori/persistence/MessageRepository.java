package se.irori.persistence;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import se.irori.persistence.model.MessageDao;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@ActivateRequestContext
@NamedQueries({
  @NamedQuery(name = "Message.toResend", query = "from Message where status = 'RESEND'"),
  @NamedQuery(name = "Message.resent", query = "update Message set status = 'RESENT' where id = ?1")
})
public class MessageRepository implements PanacheRepositoryBase<MessageDao, UUID> {
  @Inject
  Mutiny.SessionFactory factory;

  @ReactiveTransactional
  public Uni<List<MessageDao>> toResend() {
    return find("#Message.toResend").<MessageDao>list();
  }

  @ReactiveTransactional
  public Uni<String> setResent(MessageDao dao) {
    return update("#Message.resent", dao.getId())
      .flatMap(updates -> MessageDao.getTPO(dao));
  }

  @ReactiveTransactional
  public Uni<MessageDao> persist(MessageDao dao) {
    return persistAndFlush(dao);
  }
}
