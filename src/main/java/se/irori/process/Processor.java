package se.irori.process;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import org.hibernate.reactive.mutiny.Mutiny;
import se.irori.persistence.model.MessageDao;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

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

  @Scheduled(every = "5s")
  public Uni<Void> scheduler() {
    return pollDB()
      .collect().asList()
      .invoke(tpomap -> {
        if (tpomap.size() > 0) {
          Log.info(String.format("Resent %d messages", tpomap.size()));
        } else {
          Log.debug("Resent no messages");
        }
      })
      .replaceWithVoid();
  }

  @Inject
  Mutiny.SessionFactory factory;



  public Multi<String> pollDB() {
    Log.debug("Initializing DB-poll");
//    Log.info(String.format("OpenSessions: %d", factory.getStatistics().getSessionOpenCount()));
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
