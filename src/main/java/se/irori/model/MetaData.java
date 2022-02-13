package se.irori.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MetaData {
  private MetaDataType type;
  private String key;
  private String value;
}
