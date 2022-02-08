package se.irori.model;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Message {
  private String id;
  private String topicId;
  private Integer partition;
  private Long offset;
  private byte[] payload;
  private String payloadString;
  private String classification;
  private List<MetaData> metaDataList;
}
