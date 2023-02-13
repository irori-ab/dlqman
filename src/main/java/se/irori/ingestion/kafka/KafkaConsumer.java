package se.irori.ingestion.kafka;

import io.smallrye.mutiny.Multi;
import io.vertx.kafka.client.common.TopicPartition;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumerRecord;
import lombok.extern.slf4j.Slf4j;
import se.irori.config.AppConfiguration;
import se.irori.config.Source;
import se.irori.ingestion.Consumer;
import se.irori.model.Message;
import se.irori.model.Metadata;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class KafkaConsumer implements Consumer {

  io.vertx.mutiny.kafka.client.consumer.KafkaConsumer<byte[], byte[]> kafkaConsumer;
  private final Source source;
  private final Set<TopicPartition> topicPartitionSet = new HashSet<>();

  private final String consumerGroup;

  public KafkaConsumer(AppConfiguration config, Source source, Vertx vertx) {
    Map<String, String> consumerConfig = new HashMap<>();
    consumerConfig.putAll(config.kafka().common());
    consumerConfig.putAll(config.kafka().consumer());
    consumerConfig.putAll(source.consumerPropertiesOverrides());
    this.source = source;
    this.consumerGroup = consumerConfig.get("group.id");
    this.kafkaConsumer = io.vertx.mutiny.kafka.client.consumer.KafkaConsumer.create(
        vertx,
        consumerConfig,
        byte[].class, byte[].class);
    init();
  }

  public void init() {
    kafkaConsumer.partitionsAssignedHandler(handler -> {
      handler.forEach(pi -> {
        log.info("[{}:{}] Assigned to partition {}:{}", source.name(), consumerGroup, pi.getTopic(), pi.getPartition());
        topicPartitionSet.add(pi);
      });
    });
    kafkaConsumer.partitionsRevokedHandler(handler -> {
      handler.forEach(pi -> {
        log.info("[{}:{}] Reassigned to partition {}:{}", source.name(), consumerGroup, pi.getTopic(), pi.getPartition());
        topicPartitionSet.add(pi);
      });
    });

  }

  public Multi<Message> consume() {
    kafkaConsumer.subscribeAndAwait(source.sourceTopic());
    kafkaConsumer.partitionsForAndAwait(source.sourceTopic()).stream()
      .forEach(pi -> log.info("[{}:{}] Subscribed to partition {}:{}", source.name(), consumerGroup, pi.getTopic(), pi.getPartition()));

    return kafkaConsumer.toMulti()
        .map(record -> mapRecord(record, source));
  }


  @Override
  public void closeConsumer() {
    this.kafkaConsumer.closeAndAwait();
  }

  private Message mapRecord(KafkaConsumerRecord<byte[], byte[]> record, Source source) {
    log.debug("Ingesting record from TPO[{}:{}:{}]", record.topic(), record.partition(), record.offset());
    HeaderExtractor headerExtractor = new HeaderExtractor(record.headers());

    return Message.builder()
        .id(UUID.randomUUID())
        .sourceId(source.name())
        .sourceOffset(record.offset())
        .sourcePartition(record.partition())
        .sourceTopic(record.topic())
        .destinationTopic(source.resendTopic())
        .metadataList(parseMetaDataFromHeaders(headerExtractor.getAllHeaders()))
        .build();
  }

  private List<Metadata> parseMetaDataFromHeaders(Map<String, String> headers) {
    return headers.entrySet()
        .stream()
        .flatMap(entry -> HeaderExtractor.convertHeader(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }


}
