package se.irori.process;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import se.irori.persistence.model.MessageDao;

import java.util.List;

public interface Poller {
  Multi<MessageDao> pollMulti(int limit);

  Uni<List<MessageDao>> pollUni(int limit);
}
