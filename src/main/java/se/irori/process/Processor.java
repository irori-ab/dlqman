package se.irori.process;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import se.irori.persistence.model.MessageDao;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@ActivateRequestContext
@Path("")
public class Processor {

  @Inject
  Poller poller;

  @Inject
  EventBus eventBus;

  @GET
  @Path("/poll")
  public Uni<Void> poller() {
    return scheduler();
  }

  @Scheduled(every = "5s", delay = 10, delayUnit = TimeUnit.SECONDS)
  public Uni<Void> scheduler() {
    return pollDB()
      .collect().asList()
      .invoke(tpomap -> {
        if (tpomap.size() > 0) {
          Log.info(String.format("Resent %d messages", tpomap.size()));
        } else {
          Log.trace("Resent no messages");
        }
      })
      .replaceWithVoid();
  }

  public Multi<String> pollDB() {
    Log.trace("Initializing DB-poll");
    return poller.poll("")
      .onItem().transformToMulti(list ->
        Multi.createFrom().iterable(list)
            .onItem().transformToUniAndMerge(msg ->
              eventBus.<String>request("resend", msg.toMessage())
                .map(Message::body)
                .flatMap(newTpo -> MessageDao.setResent(msg)
                    .map(oldtpo -> String.format("%s -> %s", oldtpo, newTpo)))
                .invoke(tpo -> Log.debug(String.format("Resent message with [oldtpo -> newtpo] [%s]", tpo)))
                ));
  }
}
