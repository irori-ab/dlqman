package se.irori.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.common.constraint.NotNull;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="METADATA")
public class Metadata extends PanacheEntityBase {

  @Id
  @NotNull
  private UUID id;
  private MetaDataType type;
  private String key;
  private String value;

  @ManyToOne(fetch = FetchType.LAZY)
  private Message message;
}
