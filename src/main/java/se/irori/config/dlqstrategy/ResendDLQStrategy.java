package se.irori.config.dlqstrategy;


public interface ResendDLQStrategy extends DLQStrategy {
  String CONFIG_DURATION = "nextWaitDuration";
  String CONFIG_MAX_RETRIES = "maxTries";
  boolean isFingerprinted();
  boolean maxTriesExceeded(int noTries);
  Long nextWaitDuration(Long durationMillis);

  @Override
  default String defaultStatusString() {
    return "RESEND";
  }
}
