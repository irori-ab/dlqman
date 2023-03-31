package se.irori.config.dlqstrategy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VoidDLQStrategyTest {

  @Test
  public void testThatStrategyDiscards() {
    VoidDLQStrategy strategy = new VoidDLQStrategy();
    Assertions.assertFalse(strategy.persist());
  }
}
