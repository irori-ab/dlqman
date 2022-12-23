package se.irori.config;

import se.irori.config.dlqstrategy.DLQStrategy;
import se.irori.config.matchers.Matcher;

public class Rule {
  private String name;
  private int priority;
  private Matcher matcher;
  private DLQStrategy strategy;
  private String resendTopicOverride;
}
