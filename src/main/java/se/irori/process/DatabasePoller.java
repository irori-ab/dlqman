package se.irori.process;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import se.irori.persistence.model.MessageDao;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
@Slf4j
public class DatabasePoller implements Poller {

  @Override
  public Multi<MessageDao> pollMulti(int limit) {
//    return Multi.createBy().repeating().uni(AtomicInteger::new,
//        page -> MessageDao.toResendLimit(limit))
//      .until(List::isEmpty)
      return MessageDao.toResendLimit(limit)
      .onItem().disjoint();
  }

  @Override
  public Uni<List<MessageDao>> pollUni(int limit) {
    return MessageDao.toResendLimit(limit);
  }


}
