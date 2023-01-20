package se.irori.ingestion;

import io.smallrye.mutiny.Multi;
import se.irori.model.Message;

public interface Consumer {
  Multi<Message> consume();

  void closeConsumer();
}
