package se.irori.config.matchers;

import se.irori.model.Message;
import se.irori.model.Metadata;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 *
 */
public class HeaderRegexMatcher implements Matcher {
  public static final String CONFIG_HEADER_NAME = "headerName";
  public static final String CONFIG_REGEX_PATTERN = "regexPattern";
  String headerName;
  Pattern regex;

  /**
   * Matches a message if the specified header matches the regex pattern.
   * @param headerName The name of the message header to be matched.
   * @param regex The regex pattern in string format used for matching.
   */
  public HeaderRegexMatcher(String headerName, String regex) {
    this.headerName = headerName;
    this.regex = Pattern.compile(regex);
  }

  @Override
  public boolean match(Message message) {
    if (message != null && message.getMetadataList() != null) {
      Optional<Metadata> metadata = message.getMetadataList().stream().filter(md ->
        headerName.equals(md.getKey())).findFirst();
      if (metadata.isPresent()) {
        return regex.matcher(metadata.get().getValue()).matches();
      }
    }
    return false;
  }

}
