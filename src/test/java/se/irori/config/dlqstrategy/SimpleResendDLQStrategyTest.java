package se.irori.config.dlqstrategy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import se.irori.config.ConfigurationException;

public class SimpleResendDLQStrategyTest {

  @Test
  public void testThatNullDurationFailsStrategy() {
    Assertions.assertThrows(ConfigurationException.class,
      () -> new SimpleResendDLQStrategy(null));
  }

  @Test
  public void testThatCorrectDurationReturns() {
    SimpleResendDLQStrategy sr = new SimpleResendDLQStrategy(100L);
    Assertions.assertEquals(Long.valueOf(100L), sr.nextWaitDuration(null), "Output should equal init-value");
    Assertions.assertEquals(Long.valueOf(100L), sr.nextWaitDuration(99L), "Input should not affect output");
  }

  @Test
  public void testFingerprintAndMaxTries() {
    SimpleResendDLQStrategy sr = new SimpleResendDLQStrategy(100L);
    Assertions.assertFalse(sr.isFingerprinted(), "Hardcoded fingerprint flag is returned");
    Assertions.assertFalse(sr.maxTriesExceeded(10), "Always returns false as of now");
  }
}
