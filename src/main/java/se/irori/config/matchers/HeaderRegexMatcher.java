package se.irori.config.matchers;

import se.irori.model.Message;
import se.irori.model.MetaDataType;
import se.irori.model.Metadata;

import java.util.Optional;
import java.util.regex.Pattern;


public class HeaderRegexMatcher implements Matcher {
  String headerName;
  Pattern regex;

  /**
   * Matches a message if the specified header matches the patern.
   * @param headerName
   * @param regexPattern
   */
  public HeaderRegexMatcher(String headerName, String regexPattern) {
    this.headerName = headerName;
    this.regex = Pattern.compile(regexPattern);
  }

  @Override
  public boolean match(Message message) {
    if (message != null && message.getMetadataList() != null) {
      Optional<Metadata> metadata = message.getMetadataList().stream().filter(md ->
        MetaDataType.SOURCE_HEADER.equals(md.getType()) && headerName.equals(md.getKey())).findFirst();
      if (metadata.isPresent()) {
        return regex.matcher(metadata.get().getValue()).matches();
      }
    }
    return false;
  }
}
