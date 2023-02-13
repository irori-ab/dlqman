package se.irori.persistence;

import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import se.irori.config.Rule;
import se.irori.config.dlqstrategy.DLQStrategy;
import se.irori.config.dlqstrategy.ResendDLQStrategy;
import se.irori.config.dlqstrategy.StrategyHolder;
import se.irori.model.Message;
import se.irori.model.MessageStatus;
import se.irori.persistence.model.MessageDao;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Receives messages on internal bus and persists them.
 */
@Slf4j
@ApplicationScoped
public class DatabaseScheduler implements Scheduler {

  @Inject
  StrategyHolder strategyHolder;


  @ConsumeEvent("message-stream")
  public Uni<String> persist(Message message) {
    log.debug("Persisting message with TPO [{}:{}:{}]", message.getSourceTopic(), message.getSourcePartition(),
      message.getSourceOffset());
    Rule rule = message.getMatchedRule();
    return Uni.createFrom().item(applyStrategy(MessageDao.from(message), rule))
      .chain(MessageDao::save)
      .chain(MessageDao::getTPO);
  }

  private MessageDao applyStrategy(MessageDao message, Rule rule) {
    if (rule != null) {
      DLQStrategy strategy = strategyHolder.getStrategy(rule.strategy());
      if (strategy instanceof ResendDLQStrategy) {
        ResendDLQStrategy rstrat = (ResendDLQStrategy) strategy;
        Long waitDuration = rstrat.nextWaitDuration(null);
        message.setProcessAt(Instant.now().plusMillis(waitDuration).atOffset(ZoneOffset.UTC));
        message.setWaitTime(waitDuration);
        message.setStatus(MessageStatus.RESEND);
        if (rule.resendTopicOverride().isPresent()) {
          message.setDestinationTopic(rule.resendTopicOverride().get());
        }
      }
    }
    return message;
  }
}
