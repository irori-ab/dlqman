package se.irori.model;

import io.smallrye.common.constraint.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Metadata {
  public static final String HEADER_REASON = "header-reason";
  public static final String HEADER_CAUSE = "header-cause";
  /**
   * Original topic of the record
   */
  public static final String HEADER_SMALLRYE_DLQ_TOPIC = "dead-letter-topic";

  @NotNull
  private UUID id;
  @Builder.Default
  private MetaDataType type = MetaDataType.SOURCE_HEADER;
  private String key;
  private String value;

  private Message message;
}
