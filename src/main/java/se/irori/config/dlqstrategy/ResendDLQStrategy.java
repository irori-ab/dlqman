package se.irori.config.dlqstrategy;

import java.time.Duration;

public interface ResendDLQStrategy extends DLQStrategy {
  boolean isFingerprinted();
  boolean maxTriesExceeded(int noTries);
  Duration nextWaitDuration(Duration duration);
}
