package se.irori.config.dlqstrategy;

import java.time.Duration;

public class MaxRetriesDLQStrategy implements ResendDLQStrategy {
  private final Duration waitDuration;
  private final int maxTries;

  public MaxRetriesDLQStrategy(Duration waitDuration, int maxTries) {
    this.waitDuration = waitDuration;
    this.maxTries = maxTries;
  }

  @Override
  public boolean isFingerprinted() {
    return false;
  }

  @Override
  public boolean maxTriesExceeded(int noTries) {
    return noTries > maxTries;
  }

  @Override
  public Duration nextWaitDuration(Duration duration) {
    return waitDuration;
  }
}
