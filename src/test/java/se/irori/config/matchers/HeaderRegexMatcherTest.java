package se.irori.config.matchers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import se.irori.model.Message;
import se.irori.model.Metadata;

import static se.irori.utils.MockData.getDefaultSpringMessage;

public class HeaderRegexMatcherTest {

  @Test
  public void testMatchingHeaderNameReturnsTrue() {
    Message m = getDefaultSpringMessage(Metadata.builder()
      .key("myheader").value("myvalue").build());

    Matcher matcher = new HeaderRegexMatcher("myheader", "myvalue");
    Assertions.assertTrue(matcher.match(m));
  }
  @Test
  public void testMatchingHeaderNameWithRegexReturnsTrue() {
    Message m = getDefaultSpringMessage(Metadata.builder()
      .key("myheader").value("myvalue").build());

    Matcher matcher = new HeaderRegexMatcher("myheader", "my.*");
    Assertions.assertTrue(matcher.match(m));
  }
  @Test
  public void testMatchingHyphenHeaderNameWithRegexReturnsTrue() {
    Message m = getDefaultSpringMessage(Metadata.builder()
      .key("x-special-header").value("myvalue").build());

    Matcher matcher = new HeaderRegexMatcher("x-special-header", "my.*");
    Assertions.assertTrue(matcher.match(m));
  }

  @Test
  public void testNotMatchingHeaderNameWithRegexReturnsFalse() {
    Message m = getDefaultSpringMessage(Metadata.builder()
      .key("myheader").value("myvalue").build());

    Matcher matcher = new HeaderRegexMatcher("myheader", "wrong.*");
    Assertions.assertFalse(matcher.match(m));
  }




}
