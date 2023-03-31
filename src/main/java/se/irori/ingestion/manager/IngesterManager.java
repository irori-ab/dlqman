package se.irori.ingestion.manager;

import io.smallrye.mutiny.Uni;
import se.irori.ingestion.Ingester;

import java.util.List;
import java.util.UUID;

/**
 * A process-manager is responsible to manage source -> sink processes.
 * It exposes functionality to create, fetch and cancel processes on demand.
 */
public interface IngesterManager {

  /**
   * Register and start an ingester.

   * @param ingester to start.
   */
  void registerIngester(Ingester ingester);

  Uni<Ingester> getIngester(UUID id);

  List<Ingester> listIngester();

  void cancelIngester(UUID id);
}
