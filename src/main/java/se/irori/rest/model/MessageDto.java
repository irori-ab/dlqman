package se.irori.rest.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import se.irori.model.Message;
import se.irori.model.MessageStatus;
import se.irori.model.Metadata;
import se.irori.model.TimestampType;

@Builder
@Getter
public class MessageDto {

  private UUID id;

  private LocalDateTime timeStamp;
  private TimestampType timeStampType;

  private LocalDateTime indexTime;

  private UUID sourceId;
  private Integer partition;

  private Long offset;
  private byte[] payload;
  private String payloadString;
  private MessageStatus messageStatus;

  private List<Metadata> metadataList;

  public static MessageDto from(Message message) {
    return MessageDto.builder()
        .id(message.getId())
        .timeStamp(message.getTimeStamp())
        .timeStampType(message.getTimeStampType())
        .indexTime(message.getIndexTime())
        .sourceId(message.getSourceId())
        .offset(message.getOffset())
        .partition(message.getPartition())
        .payload(message.getPayload())
        .payloadString(message.getPayloadString())
        .messageStatus(message.getStatus())
        .metadataList(message.getMetadataList())
        .build();
  }
}
