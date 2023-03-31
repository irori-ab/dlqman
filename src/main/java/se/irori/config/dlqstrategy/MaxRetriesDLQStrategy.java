package se.irori.config.dlqstrategy;

public class MaxRetriesDLQStrategy implements ResendDLQStrategy {
  private final Long waitDuration;
  private final int maxTries;

  public MaxRetriesDLQStrategy(Long waitDurationMillis, int maxTries) {
    this.waitDuration = waitDurationMillis;
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
  public Long nextWaitDuration(Long duration) {
    return waitDuration;
  }
}
