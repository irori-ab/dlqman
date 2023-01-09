package se.irori.persistence.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.irori.model.MetaDataType;
import se.irori.model.Metadata;

import javax.persistence.*;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "MetadataDao")
@Table(name="METADATA")
public class MetadataDao extends PanacheEntityBase {

  @Id
  public UUID id;
  private MetaDataType type;
  private String key;
  private String value;

  @ManyToOne(fetch = FetchType.LAZY, targetEntity = MessageDao.class)
  public MessageDao message;

  public static MetadataDao from(Metadata metadata) {
    return MetadataDao.builder()
        .id(metadata.getId())
        .type(metadata.getType())
        .key(metadata.getKey())
        .value(metadata.getValue())
        .build();
  }

  public static Metadata to(MetadataDao dao) {
    return Metadata.builder()
      .id(dao.getId())
      .type(dao.getType())
      .key(dao.getKey())
      .value(dao.getValue())
      .build();
  }
}
