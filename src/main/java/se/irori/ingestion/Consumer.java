package se.irori.ingestion;

import io.smallrye.mutiny.Multi;
import se.irori.config.Source;
import se.irori.model.Message;

public interface Consumer {
  Multi<Message> consume(Source source);

  void closeConsumer();
}
