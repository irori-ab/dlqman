package se.irori.rest.model;

import lombok.Builder;
import lombok.Getter;
import se.irori.model.Message;

@Builder
@Getter
public class MessageDto {

  public static MessageDto from(Message Message) {
    return MessageDto.builder().build();
  }
}
