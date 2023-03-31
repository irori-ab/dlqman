package se.irori.config;

/**
 * Thrown when there is an error in the application configuration
 */
public class ConfigurationException extends RuntimeException {
  public ConfigurationException(String message) {
    super(message);
  }
}
