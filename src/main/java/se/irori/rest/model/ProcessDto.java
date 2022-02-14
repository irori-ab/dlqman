package se.irori.rest.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import se.irori.model.Process;
import se.irori.model.ProcessState;
import se.irori.model.Source;

@Getter
@Builder
public class ProcessDto {

  private UUID id;
  private ProcessState state;
  private Source source;
  private Integer processedMessages;

  public static ProcessDto from(Process process) {
    return ProcessDto.builder()
        .id(process.getId())
        .source(process.getSource())
        .state(process.getProcessState())
        .processedMessages(process.getProcessedMessages().get())
        .build();
  }
}
