package se.irori.config.dlqstrategy;

public class SimpleResendDLQStrategy implements ResendDLQStrategy {
  private final Long waitDuration;

  public SimpleResendDLQStrategy(Long waitDurationMillis) {
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
