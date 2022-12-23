package se.irori.model;

import io.smallrye.common.constraint.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.UUID;

import static javax.persistence.CascadeType.ALL;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {

  @Id
  @NotNull
  private UUID id;

  private String sourceId;
  private String sourceTopic;
  private Integer sourcePartition;
  @Column(name = "topic_offset")
  private Long sourceOffset;

  private String fingerprint;

  private MessageStatus status;
  private String matchedRule;


  @OneToMany(mappedBy = "message", cascade = ALL, fetch = FetchType.EAGER)
  private List<Metadata> metadataList;
}
