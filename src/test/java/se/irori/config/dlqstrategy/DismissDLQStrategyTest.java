package se.irori.config.dlqstrategy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import se.irori.model.MessageStatus;

public class DismissDLQStrategyTest {

  @Test
  public void testThatStrategyIgnores() {
    DismissDLQStrategy strategy = new DismissDLQStrategy();
    Assertions.assertTrue(strategy.persist(), "Persist should be true");
    Assertions.assertEquals(MessageStatus.DISMISSED.name(), strategy.defaultStatusString(), "Default status string");
  }
}
