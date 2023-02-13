package se.irori.process.kafka;

import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.kafka.client.common.TopicPartition;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.kafka.client.producer.KafkaHeader;
import io.vertx.mutiny.kafka.client.producer.KafkaProducerRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import se.irori.config.AppConfiguration;
import se.irori.model.Message;
import se.irori.model.MetaDataType;
import se.irori.model.Metadata;

import javax.inject.Singleton;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class KafkaProducer {

  final io.vertx.mutiny.kafka.client.producer.KafkaProducer<byte[], byte[]> kafkaProducer;
  final io.vertx.mutiny.kafka.client.consumer.KafkaConsumer<byte[], byte[]> kafkaConsumer;


  public KafkaProducer(AppConfiguration config, Vertx vertx) {

    Map<String, String> producerConfig = new HashMap<>();
    producerConfig.putAll(config.kafka().common());
    producerConfig.putAll(config.kafka().producer());

    Map<String, String> consumerConfig = new HashMap<>();
    consumerConfig.putAll(config.kafka().common());
    consumerConfig.putAll(config.kafka().consumer());
    consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "dlqman-resender");
    consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    this.kafkaProducer = io.vertx.mutiny.kafka.client.producer.KafkaProducer.create(
      vertx,
      producerConfig,
      byte[].class, byte[].class);
    this.kafkaConsumer = io.vertx.mutiny.kafka.client.consumer.KafkaConsumer.create(
      vertx,
      consumerConfig,
      byte[].class, byte[].class);
  }

  @ConsumeEvent("resend")
  public Uni<String> sendToKafka(Message message) {
    return mapMessage(message).onFailure().retry().withBackOff(Duration.ofMillis(200)).atMost(5)
      .flatMap(kafkaProducer::send)
      .map(metadata -> String.format("%s:%s:%s", metadata.getTopic(), metadata.getPartition(), metadata.getOffset()));
  }

  private Uni<KafkaProducerRecord<byte[],byte[]>> mapMessage(Message message) {
    return kafkaConsumer
      .assignAndForget(new TopicPartition(message.getSourceTopic(), message.getSourcePartition()))
      .seek(new TopicPartition(message.getSourceTopic(), message.getSourcePartition()), message.getSourceOffset())
      .flatMap(nil -> kafkaConsumer.poll(Duration.ofMillis(2000))
        .flatMap(records -> Uni.createFrom().item(records.recordAt(0))
          .onItem().ifNull().failWith(
            new RuntimeException(String.format("Record not found with tpo [%s]", message.getTPO())))
          .flatMap(record -> Uni.createFrom().item(KafkaProducerRecord.<byte[], byte[]>create(
            message.getDestinationTopic(), record.key(), record.value())
              .addHeaders(record.headers())
              .addHeaders(convertMessageHeaders(message))))
      ));
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
