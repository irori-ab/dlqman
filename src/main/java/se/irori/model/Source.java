package se.irori.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Source {
  private UUID id;
  private String name;
  private String description;
}
