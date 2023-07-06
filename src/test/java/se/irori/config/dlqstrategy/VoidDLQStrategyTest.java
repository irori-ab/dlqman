package se.irori.config.dlqstrategy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import se.irori.model.MessageStatus;

public class VoidDLQStrategyTest {

  @Test
  public void testThatStrategyDiscards() {
    VoidDLQStrategy strategy = new VoidDLQStrategy();
    Assertions.assertFalse(strategy.persist());
    Assertions.assertEquals(MessageStatus.NEW.name(), strategy.defaultStatusString(), "Default status string");
  }
}
