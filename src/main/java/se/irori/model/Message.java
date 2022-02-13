package se.irori.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Message {
  private String id;
  private String sourceId;
  private Integer partition;
  private Long offset;
  private byte[] payload;
  private String payloadString;
  private String classification;
  private List<MetaData> metaDataList;
}
