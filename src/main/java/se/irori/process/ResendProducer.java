package se.irori.process;

import io.smallrye.mutiny.Uni;
import se.irori.model.Message;

public interface ResendProducer {
  Uni<String> resend(Message message);
}
