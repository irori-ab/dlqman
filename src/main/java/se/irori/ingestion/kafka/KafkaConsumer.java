package se.irori.ingestion.kafka;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumerRecord;
import lombok.extern.slf4j.Slf4j;
import se.irori.config.Source;
import se.irori.ingestion.Consumer;
import se.irori.model.Message;
import se.irori.model.Metadata;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class KafkaConsumer implements Consumer {

  io.vertx.mutiny.kafka.client.consumer.KafkaConsumer<byte[], byte[]> kafkaConsumer;

  public KafkaConsumer(Map<String, String> consumerConfig) {
//    Map<String, String> consumerConfiguration = Map.of(
//        "bootstrap.servers", configuration.bootstrapServers(),
//        "key.deserializer", new BytesSerde().deserializer().getClass().getName(),
//        "value.deserializer", new BytesSerde().serializer().getClass().getName(),
//        "group.id", configuration.groupId(),
//        "auto.offset.reset", "earliest",
//        "enable.auto.commit", "false");

    this.kafkaConsumer = io.vertx.mutiny.kafka.client.consumer.KafkaConsumer.create(
        Vertx.vertx(),
        consumerConfig,
        byte[].class, byte[].class);
  }

  public Multi<Message> consume(Source source) {
    kafkaConsumer.subscribeAndAwait(source.getSourceTopic());

    return kafkaConsumer.toMulti()
        .map(record -> mapRecord(record, source));
  }

  private Message mapRecord(KafkaConsumerRecord<byte[], byte[]> record, Source source) {
    log.info("Indexing record with key [{}] & value [{}]", record.key(), record.value());
    HeaderExtractor headerExtractor = new HeaderExtractor(record.headers());

    return Message.builder()
        .id(UUID.randomUUID())
        .sourceId(source.getName())
        .sourceOffset(record.offset())
        .sourcePartition(record.partition())
        .sourceTopic(record.topic())
        .metadataList(parseMetaDataFromHeaders(headerExtractor.getNonMatchedHeaders()))
        .build();
  }

  private List<Metadata> parseMetaDataFromHeaders(Map<String, String> headers) {
    return headers.entrySet()
        .stream()
        .flatMap(entry -> HeaderExtractor.convertHeader(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }


}
