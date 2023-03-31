package se.irori.config.dlqstrategy;

import se.irori.config.ConfigurationException;

public class SimpleResendDLQStrategy implements ResendDLQStrategy {
  private final Long waitDuration;

  public SimpleResendDLQStrategy(Long waitDurationMillis) {
    if (waitDurationMillis == null) {
      throw new ConfigurationException("nextWaitDuration must be specified for SimpleResendDLQStrategy");
    }
    this.waitDuration = waitDurationMillis;
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
  public Long nextWaitDuration(Long duration) {
    return waitDuration;
  }
}
