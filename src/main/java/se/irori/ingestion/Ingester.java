package se.irori.ingestion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.Cancellable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.irori.config.Rule;
import se.irori.config.SharedContext;
import se.irori.config.Source;
import se.irori.config.matchers.MatcherHolder;
import se.irori.ingestion.manager.IngesterState;
import se.irori.model.Message;
import se.irori.model.MessageStatus;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * The responsibility of the ingester instance is to read and filter the data stream from one source.
 */
@Builder(access = AccessLevel.PRIVATE)
@Getter
@Slf4j
public class Ingester {
  private UUID id;
  @JsonIgnore
  private Cancellable callback;
  private IngesterState ingesterState;
  private final AtomicInteger processedMessages = new AtomicInteger();

  @JsonIgnore
  private Consumer consumer;

  @JsonIgnore
  private Source source;

  private MatcherHolder matcherHolder;

  List<Rule> sortedRules;

  public static Ingester create(Source source, Consumer consumer, SharedContext ctx) {
    return Ingester.builder()
      .id(UUID.randomUUID())
      .consumer(consumer)
      .matcherHolder(ctx.getMatchers())
      .source(source)
      .sortedRules(source.matchRules().stream().sorted(Comparator.comparing(Rule::priority)).collect(Collectors.toList()))
      .ingesterState(IngesterState.CREATED)
      .build();
  }

  public void changeIngesterState(IngesterState ingesterState) {
    this.ingesterState = ingesterState;
  }

  public void setCallback(Cancellable callback) {
    this.callback = callback;
  }

  public Multi<Message> consume() {
    return consumer.consume().map( message -> {
      message.setStatus(MessageStatus.NEW);
      applyRules(message);
      return message;
    });
  }

  public void applyRules(Message message) {
    for (Rule rule : sortedRules) {
      if (matcherHolder.getMatcher(rule.matcher()).match(message)) {
        message.setMatchedRule(rule);
        log.debug("Rule matched: {}", rule.name());
        if (rule.resendTopicOverride().isPresent()) {
          message.setDestinationTopic(rule.resendTopicOverride().get());
        }
        return;
      } else {
        log.debug("Rule did not match: {}", rule.name());
      }
    }
    log.debug("No rules matched message {}", message.getId());
  }
}
