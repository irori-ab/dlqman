package se.irori.utils;

import org.junit.jupiter.api.Assertions;
import se.irori.model.Message;
import se.irori.model.MetaDataType;
import se.irori.model.Metadata;

public class AssertHelper {

  public static void headerTypeValue(String key, MetaDataType type, String value, Message message) {
    Metadata h1 = message.getMetadataList().stream().filter(h -> h.getKey().equals(key)).findFirst().get();
    Assertions.assertEquals(value, h1.getValue());
    Assertions.assertEquals(type, h1.getType());
  }

  public static void headerValue(String key, String value, Message message) {
    Metadata h1 = message.getMetadataList().stream().filter(h -> h.getKey().equals(key)).findFirst().get();
    Assertions.assertEquals(value, h1.getValue());
  }
}
