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
}
