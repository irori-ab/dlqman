package se.irori.config.matchers;


import io.quarkus.runtime.configuration.ConfigurationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.irori.config.AppConfiguration;
import se.irori.utils.DefImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;


public class CommonMatcherTest {
  @Mock
  AppConfiguration config;

  MatcherHolder mh;

  AutoCloseable cl;

  @BeforeEach
  public void setup() {
    cl = MockitoAnnotations.openMocks(this);
    mh = new MatcherHolder(config);
  }

  @AfterEach
  public void teardown() throws Exception {
    cl.close();
  }

  @Test
  public void unknownMacherClassNameProducesError() {
    when(config.matchers()).thenReturn(List.of(DefImpl.builder()
      .name("test")
      .className("NotAnyKnownClass")
      .build()));
    Assertions.assertThrows(ConfigurationException.class,
      () -> mh.initialize(null));

  }

  @Test
  public void canFindHeaderRegexMatcher() {
    when(config.matchers()).thenReturn(List.of(DefImpl.builder()
      .name("regex")
      .className("HeaderRegexMatcher")
      .config(Map.of("headerName", "h", "regexPattern", ".")).build()));
    mh.initialize(null);
    Assertions.assertNotNull(mh.getMatcher("regex"));
  }

  @Test
  public void canFindBuiltinMatchers() {
    when(config.matchers()).thenReturn(Collections.EMPTY_LIST);
    mh.initialize(null);
    Assertions.assertNotNull(mh.getMatcher("all"), "Could not find all-mapper");
  }



}
