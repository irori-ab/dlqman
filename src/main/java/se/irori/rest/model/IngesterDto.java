package se.irori.rest.model;

import lombok.Builder;
import lombok.Getter;
import se.irori.config.Source;
import se.irori.ingestion.Ingester;
import se.irori.ingestion.manager.IngesterState;

import java.util.UUID;

@Getter
@Builder
public class IngesterDto {

  private UUID id;
  private IngesterState state;
  private Source source;
  private Integer ingestedMessages;

  public static IngesterDto from(Ingester ingester) {
    return IngesterDto.builder()
        .id(ingester.getId())
        .source(ingester.getSource())
        .state(ingester.getIngesterState())
        .ingestedMessages(ingester.getProcessedMessages().get())
        .build();
  }
}
