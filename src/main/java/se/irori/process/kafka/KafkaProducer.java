package se.irori.process.kafka;

import io.quarkus.logging.Log;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.kafka.client.common.TopicPartition;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumer;
import io.vertx.mutiny.kafka.client.producer.KafkaHeader;
import io.vertx.mutiny.kafka.client.producer.KafkaProducerRecord;
import se.irori.config.SharedContext;
import se.irori.model.Message;
import se.irori.model.MetaDataType;
import se.irori.model.Metadata;
import se.irori.process.ResendProducer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Singleton
public class KafkaProducer implements ResendProducer {

  final io.vertx.mutiny.kafka.client.producer.KafkaProducer<byte[], byte[]> kafkaProducer;

  long maxWaitPollExisting;

  final SharedContext ctx;

  final AtomicInteger noClients;

  @Inject
  KafkaConsumerPools pools;


  public KafkaProducer(SharedContext ctx) {
    maxWaitPollExisting = ctx.getConfig().kafka().pollTimeout();

    Map<String, String> producerConfig = new HashMap<>();
    producerConfig.putAll(ctx.getConfig().kafka().common());
    producerConfig.putAll(ctx.getConfig().kafka().producer());

    this.kafkaProducer = io.vertx.mutiny.kafka.client.producer.KafkaProducer.create(
      ctx.getVertx(),
      producerConfig,
      byte[].class, byte[].class);
    this.ctx = ctx;
    noClients = ctx.getMetrics().gauge("noClients", new AtomicInteger(0));
  }

  @ConsumeEvent("resend")
  public Uni<String> resend(Message message) {
    return sourceMessage(message)
      .onFailure().retry().withBackOff(Duration.ofMillis(500)).atMost(5)
      .flatMap(kafkaProducer::send)
      .map(metadata -> String.format("%s:%s:%s", metadata.getTopic(), metadata.getPartition(), metadata.getOffset()))
      .invoke(tpo -> Log.debug(String.format("Resent message with [oldtpo -> newtpo] [%s -> %s], ts: ",
        message.getTPO(), tpo)));
  }


  /**
   * Fetch a kafka-record from its source-topic and convert it into a new producer-record.
   * @param message The message representing the database state of the message to fetch.
   * @return A Uni with the producer record or failure.
   */
  private Uni<KafkaProducerRecord<byte[],byte[]>> sourceMessage(Message message) {
    KafkaConsumer<byte[],byte[]> consumer = pools.getConsumer(message.getSourceTopic(),
      message.getSourcePartition());
    return
      consumer
      .seek(new TopicPartition(message.getSourceTopic(), message.getSourcePartition()), message.getSourceOffset())
      .flatMap(nil -> consumer.poll(Duration.ofMillis(100))
        .flatMap(records -> {
          if (records.isEmpty()){
            return Uni.createFrom().failure(
              new RuntimeException(String.format("Record not found with tpo [%s]", message.getTPO())));
          }
          return Uni.createFrom().item(records.recordAt(0));
        })
          .flatMap(record -> Uni.createFrom().item(KafkaProducerRecord.<byte[], byte[]>create(
            message.getDestinationTopic(), record.key(), record.value())
              .addHeaders(record.headers())
              .addHeaders(convertMessageHeaders(message))))
      );
  }

  private List<KafkaHeader> convertMessageHeaders(Message message) {
    return message.getMetadataList().stream()
      .filter(metadata -> MetaDataType.OVERRIDE_HEADER.equals(metadata.getType()))
      .map(metadata -> KafkaHeader.header(metadata.getKey(), metadata.getValue()))
      .collect(Collectors.toList());
  }

  private List<KafkaHeader> getOverrideHeaders(List<Metadata> meta) {
    return meta.stream()
      .filter(metadata -> metadata.getType().equals(MetaDataType.OVERRIDE_HEADER))
      .map(metadata -> KafkaHeader.header(metadata.getKey(), metadata.getValue()))
      .collect(Collectors.toList());
  }
}
