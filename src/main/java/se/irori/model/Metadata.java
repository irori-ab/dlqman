package se.irori.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.common.constraint.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Metadata extends PanacheEntityBase {

  @NotNull
  private UUID id;
  private MetaDataType type;
  private String key;
  private String value;
}
