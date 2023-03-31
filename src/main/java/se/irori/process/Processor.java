package se.irori.process;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import se.irori.persistence.model.MessageDao;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ApplicationScoped
@ActivateRequestContext
@Path("")
public class Processor {

  @Inject
  Poller poller;

  @Inject
  ResendProducer producer;

  @GET
  @Path("/poll")
  public Uni<Void> poller() {
    return scheduler();
  }

  Semaphore semaphore = new Semaphore(1);

  @Scheduled(every = "5s", delay = 5, delayUnit = TimeUnit.SECONDS,
    concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
  public Uni<Void> scheduler() {
    if (!semaphore.tryAcquire()) {
      return Uni.createFrom().voidItem();
    }
    return Multi.createBy().repeating()
              .uni(() -> poller.pollMulti(100)
                          .onItem().transformToUniAndMerge(dao -> Panache.withTransaction(() ->
                            producer.resend(dao.toMessage())
                              .flatMap(newTpo -> MessageDao.setResent(dao))))
                          .collect().with(Collectors.counting()))
              .until(count -> count == 0)
              .collect().with(Collectors.summingLong(count -> count))
              .invoke(count -> Log.info(String.format("Resent %d messages", count)))
              .invoke(() -> semaphore.release())
              .replaceWithVoid();
  }
}
