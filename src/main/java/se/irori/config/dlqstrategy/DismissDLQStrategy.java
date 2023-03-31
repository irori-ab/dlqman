package se.irori.config.dlqstrategy;

/**
 * Strategy for persisting messages, but dismissing them from further processing in the database.
 */
public class DismissDLQStrategy implements DLQStrategy {
  @Override
  public String defaultStatusString() {
    return "DISMISSED";
  }
}
