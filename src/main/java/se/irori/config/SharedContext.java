package se.irori.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.vertx.mutiny.core.Vertx;
import lombok.Builder;
import lombok.Getter;
import se.irori.config.matchers.MatcherHolder;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Used to collect managed beans for injection into manually created instances.
 */
@Singleton
@Getter
@Builder
public class SharedContext {
  @Inject
  Vertx vertx;

  @Inject
  MeterRegistry metrics;

  @Inject
  AppConfiguration config;

  @Inject
  MatcherHolder matchers;
}
