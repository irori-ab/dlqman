package se.irori.process.kafka;

import io.quarkus.runtime.ShutdownEvent;
import io.vertx.kafka.client.common.TopicPartition;
import io.vertx.mutiny.kafka.admin.KafkaAdminClient;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import se.irori.config.SharedContext;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class KafkaConsumerPools {
  final io.vertx.mutiny.kafka.admin.KafkaAdminClient adminClient;

  final Map<String, String> consumerConfig;
  private final Map<String, KafkaConsumer<byte[],byte[]>> consumerMapFull = new HashMap<>();

  final SharedContext ctx;


  public KafkaConsumerPools(SharedContext ctx) {
    this.ctx = ctx;
    Map<String, String> adminConfig = new HashMap<>();
    adminConfig.putAll(ctx.getConfig().kafka().common());
    adminClient = KafkaAdminClient.create(ctx.getVertx(), adminConfig);

    consumerConfig = new HashMap<>();
    consumerConfig.putAll(ctx.getConfig().kafka().common());
    consumerConfig.putAll(ctx.getConfig().kafka().consumer());
    consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "dlqman-resend-poller");
    consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    consumerConfig.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");

  }

  public KafkaConsumer<byte[], byte[]> getConsumer(String topic, int partition) {
    if (!consumerMapFull.containsKey(String.format("%s:%s", topic, partition))) {
      consumerMapFull.put(String.format("%s:%d", topic, partition),
        KafkaConsumer.<byte[],byte[]>create(ctx.getVertx(), consumerConfig)
          .assignAndForget(new TopicPartition(topic, partition)));
    }
    return consumerMapFull.get(String.format("%s:%s", topic, partition));
  }

  public void teardown(@Observes ShutdownEvent evt) {
    consumerMapFull.values().forEach(KafkaConsumer::closeAndForget);
  }
}
