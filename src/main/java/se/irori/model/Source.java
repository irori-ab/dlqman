package se.irori.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Source {
  private String id;
  private String name;
  private String description;
}
