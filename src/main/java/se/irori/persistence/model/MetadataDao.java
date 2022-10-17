package se.irori.persistence.model;

import io.smallrye.common.constraint.NotNull;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.irori.model.MetaDataType;
import se.irori.model.Metadata;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="METADATA")
public class MetadataDao {

  @Id
  private UUID id;
  private MetaDataType type;
  private String key;
  private String value;

  @ManyToOne
  MessageDao message;

  public static MetadataDao from(Metadata metadata) {
    return MetadataDao.builder()
        .id(metadata.getId())
        .type(metadata.getType())
        .key(metadata.getKey())
        .value(metadata.getValue())
        .build();
  }

  public Metadata toMessage() {
    return Metadata.builder()
        .id(getId())
        .type(getType())
        .key(getKey())
        .value(getValue())
        .build();
  }
}
