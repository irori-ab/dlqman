package se.irori.ingestion.manager;

/**
 * State defining lifecycle of a {@link se.irori.ingestion.Ingester}.
 */
public enum IngesterState {
  CREATED, RUNNING, CANCELLED, FAILED, COMPLETED
}
