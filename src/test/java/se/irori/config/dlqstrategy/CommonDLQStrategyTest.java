package se.irori.config.dlqstrategy;

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

public class CommonDLQStrategyTest {
  @Mock
  AppConfiguration config;

  StrategyHolder sh;

  AutoCloseable cl;

  @BeforeEach
  public void setup() {
    cl = MockitoAnnotations.openMocks(this);
    sh = new StrategyHolder(config);
  }

  @AfterEach
  public void teardown() throws Exception {
    cl.close();
  }

  @Test
  public void unknownStrategyClassNameProducesErrorTest() {
    when(config.dlqStrategies()).thenReturn(List.of(DefImpl.builder()
      .name("test")
      .className("NotAnyKnownClass")
      .build()));
    Assertions.assertThrows(ConfigurationException.class,
      () -> sh.initialize(null));

  }

  @Test
  public void canFindBuiltinStrategiesTest() {
    when(config.dlqStrategies()).thenReturn(Collections.EMPTY_LIST);
    sh.initialize(null);
    Assertions.assertNotNull(sh);
    Assertions.assertNotNull(sh.getStrategy("dismiss"), "Could not find 'dismiss' DLQStrategy");
    Assertions.assertNotNull(sh.getStrategy("void"), "Could not find 'void' DLQStrategy");
  }
  @Test
  public void canFindSimpleResendStrategyTest() {
    when(config.dlqStrategies()).thenReturn(List.of(
      DefImpl.builder()
        .name("simpleresend")
        .className("SimpleResendDLQStrategy")
        .config(Map.of("nextWaitDuration", "1")).build()
    ));
    sh.initialize(null);
    Assertions.assertNotNull(sh);
    Assertions.assertNotNull(sh.getStrategy("simpleresend"), "Could not find 'simpleresend' DLQStrategy");
  }
  @Test
  public void canMaxRetriesStrategyTest() {
    when(config.dlqStrategies()).thenReturn(List.of(
      DefImpl.builder()
        .name("maxretries")
        .className("MaxRetriesDLQStrategy")
        .config(Map.of("nextWaitDuration", "1",
          "maxTries","1")).build()
      ));
    sh.initialize(null);
    Assertions.assertNotNull(sh);
    Assertions.assertNotNull(sh.getStrategy("maxretries"), "Could not find 'maxretries' DLQStrategy");
  }
}
