package se.irori.persistence;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import se.irori.persistence.model.MessageDao;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

//@ApplicationScoped
//@ActivateRequestContext
//@NamedQueries({
//  @NamedQuery(name = "Message.toResend", query = "from Message where status = 'RESEND'"),
//  @NamedQuery(name = "Message.resent", query = "update Message set status = 'RESENT' where id = ?1")
//})
public class MessageRepository implements PanacheRepositoryBase<MessageDao, UUID> {
  @Inject
  Mutiny.SessionFactory factory;

  @ReactiveTransactional
  public Uni<List<MessageDao>> toResend() {
    return find("#Message.toResend", OffsetDateTime.now()).<MessageDao>list();
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


  @ReactiveTransactional
  public Uni<List<MessageDao>> toResendPage(int pageIndex, int pageSize) {
    return factory.withSession(s -> find("#Message.toResend", OffsetDateTime.now()).page(pageIndex, pageSize).list());
  }

  public Multi<MessageDao> toResendMulti() {
    return Multi.createBy().repeating().uni(AtomicInteger::new,
        page -> toResendPage(page.incrementAndGet(), 50))
      .until(List::isEmpty)
      .onItem().disjoint();
  }
}
