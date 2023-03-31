package se.irori.config.dlqstrategy;

public interface DLQStrategy {
  default boolean persist() {
    return true;
  }

  default String defaultStatusString() {
    return "NEW";
  }
}
