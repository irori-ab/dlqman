package se.irori.model;

/**
 * State defining lifecycle of a {@link Process}.
 */
public enum ProcessState {
  CREATED, RUNNING, CANCELLED, FAILED, COMPLETED;
}
