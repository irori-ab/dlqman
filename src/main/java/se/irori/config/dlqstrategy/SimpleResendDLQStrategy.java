package se.irori.config.dlqstrategy;

import java.time.Duration;

public class SimpleResendDLQStrategy implements ResendDLQStrategy {
  private final Duration waitDuration;

  public SimpleResendDLQStrategy(Duration waitDuration) {
    this.waitDuration = waitDuration;
  }

  @Override
  public boolean isFingerprinted() {
    return false;
  }

  @Override
  public boolean maxTriesExceeded(int noTries) {
    return false;
  }

  @Override
  public Duration nextWaitDuration(Duration duration) {
    return waitDuration;
  }
}
