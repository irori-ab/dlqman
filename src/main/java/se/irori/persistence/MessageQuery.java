package se.irori.persistence;

import io.quarkus.panache.common.Parameters;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import se.irori.model.MessageStatus;

@Getter
public class MessageQuery {

  private final List<String> constraints = new ArrayList<>();
  private final Parameters params = new Parameters();

  public MessageQuery(LocalDateTime startTime, LocalDateTime endTime,
      UUID sourceId, MessageStatus messageStatus) {
    if (startTime!= null) {
      this.getConstraints().add("indexTime >= :startTime");
      this.addParam("startTime", startTime);
    }

    if (endTime!= null) {
      this.getConstraints().add("indexTime <= :endTime");
      this.addParam("endTime", endTime);
    }

    if(sourceId != null) {
      this.getConstraints().add("sourceId = :sourceId");
      this.addParam("sourceId", sourceId);
    }

    if(messageStatus != null) {
      this.getConstraints().add("messageStatus = :messageStatus");
      this.addParam("messageStatus", messageStatus);
    }
  }

  protected void addParam(String name, Object value) {
    params.and(name, value);
  }
  public String getQuery() {
    return String.join(" and ", constraints);
  }
}
