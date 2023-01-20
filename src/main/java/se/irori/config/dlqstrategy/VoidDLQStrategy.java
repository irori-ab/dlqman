package se.irori.config.dlqstrategy;

/**
 * Strategy to void messages not even persisting them in the database.
 */
public class VoidDLQStrategy implements DLQStrategy {

  @Override
  public boolean persist() {
    return false;
  }
}
