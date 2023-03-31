package se.irori.model;

/**
 * Describes the processing status of a specific message.
 */
public enum MessageStatus {
  /**
   * The message is new and not yet evaluated.
   */
  NEW,
  /**
   * The message is eligible for resending.
   */
  RESEND,
  /**
   * The message has been resent to an output.
   */
  RESENT,
  /**
   * The message has only been ingested but never processed further.
   */
  DISMISSED
}
