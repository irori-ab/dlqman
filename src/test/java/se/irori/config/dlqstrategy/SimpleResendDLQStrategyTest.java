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
    SimpleResendDLQStrategy sr = new SimpleResendDLQStrategy(100l);
    Assertions.assertEquals(Long.valueOf(100l), sr.nextWaitDuration(null), "Output should equal init-value");
    Assertions.assertEquals(Long.valueOf(100l), sr.nextWaitDuration(99l), "Input should not affect output");
  }
}
