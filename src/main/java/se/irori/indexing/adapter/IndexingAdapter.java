package se.irori.indexing.adapter;

import io.smallrye.mutiny.Multi;
import se.irori.model.Message;
import se.irori.model.Source;

public interface IndexingAdapter {
  Multi<Message> consumeSource(Source source);
}
