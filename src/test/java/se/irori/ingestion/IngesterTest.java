package se.irori.ingestion;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.irori.config.Rule;
import se.irori.config.SharedContext;
import se.irori.config.Source;
import se.irori.config.matchers.AllMatcher;
import se.irori.config.matchers.HeaderRegexMatcher;
import se.irori.config.matchers.MatcherHolder;
import se.irori.model.Message;
import se.irori.model.MessageStatus;
import se.irori.utils.MockHelper;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class IngesterTest {
  @Mock
  Consumer consumer;

  @Mock
  Source source;

  @Mock
  Rule regexp1;

  @Mock
  Rule regexp2;

  @Mock
  Rule allp100;

  @Mock
  MatcherHolder holder;

  Ingester ingester;


  @BeforeEach
  public void initMocks() {
    SharedContext ctx = SharedContext.builder()
      .matchers(holder).build();
    MockHelper.mockDefaultSource(source, allp100, regexp1, regexp2);
    MockHelper.mockDefaultRegexRule(regexp1, "rule1", 1, "regex1");
    MockHelper.mockDefaultRegexRule(regexp2, "rule2", 1, "regex2");
    MockHelper.mockDefaultAllRule(allp100);

    when(holder.getMatcher(eq("regex1"))).thenReturn(new HeaderRegexMatcher("myheader", "my.*"));
    when(holder.getMatcher(eq("regex2"))).thenReturn(new HeaderRegexMatcher("myheader", "other.*"));
    when(holder.getMatcher(eq("all"))).thenReturn(new AllMatcher());

    ingester = Ingester.create(source, consumer, ctx);
  }

  @Test
  public void testConsumeIngesterMatchingRegex() {
    when(consumer.consume()).thenReturn(Multi.createFrom().item(
      MockHelper.messageBuilder(Map.of("myheader","myvalue")).build())
    );
    //Ingester ingester = Ingester.create(MockHelper.source(), consumer, MockHelper.matcherHolder());
    Assertions.assertNotNull(ingester);
    AssertSubscriber<Message> sub = ingester.consume().subscribe().withSubscriber(AssertSubscriber.create(1l));
    Message m = sub.assertCompleted().getLastItem();
    Assertions.assertEquals("rule1",m.getMatchedRule().name());
    Assertions.assertEquals(MessageStatus.NEW, m.getStatus());
  }

  @Test
  public void testConsumeIngesterMatchingMiddleRuleRegex() {
    when(consumer.consume()).thenReturn(Multi.createFrom().item(
      MockHelper.messageBuilder(Map.of("myheader","othervalue")).build())
    );
    //Ingester ingester = Ingester.create(MockHelper.source(), consumer, MockHelper.matcherHolder());
    Assertions.assertNotNull(ingester);
    AssertSubscriber<Message> sub = ingester.consume().subscribe().withSubscriber(AssertSubscriber.create(1l));
    Message m = sub.assertCompleted().getLastItem();
    Assertions.assertEquals("rule2",m.getMatchedRule().name());
    Assertions.assertEquals(MessageStatus.NEW, m.getStatus());
  }
  @Test
  public void testConsumeIngesterMatchingFallback() {
    when(consumer.consume()).thenReturn(Multi.createFrom().item(
      MockHelper.messageBuilder(Map.of("myheader","novalue")).build())
    );
    //Ingester ingester = Ingester.create(MockHelper.source(), consumer, MockHelper.matcherHolder());
    Assertions.assertNotNull(ingester);
    AssertSubscriber<Message> sub = ingester.consume().subscribe().withSubscriber(AssertSubscriber.create(1l));
    Message m = sub.assertCompleted().getLastItem();
    Assertions.assertEquals("all",m.getMatchedRule().name());
    Assertions.assertEquals(MessageStatus.NEW, m.getStatus());
  }

}
