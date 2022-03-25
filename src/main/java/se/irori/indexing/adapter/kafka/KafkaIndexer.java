package se.irori.indexing.adapter.kafka;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumer;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumerRecord;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes.BytesSerde;
import org.eclipse.microprofile.context.ManagedExecutor;
import se.irori.indexing.adapter.Indexer;
import se.irori.indexing.adapter.configuration.SourceConfiguration.KafkaSourceConfiguration;
import se.irori.model.Message;
import se.irori.model.MetaDataType;
import se.irori.model.Metadata;
import se.irori.model.Source;
import se.irori.model.TimestampType;

@Slf4j
public class KafkaIndexer implements Indexer {

  KafkaConsumer<byte[], byte[]> kafkaConsumer;

  public KafkaIndexer(KafkaSourceConfiguration configuration) {
    Map<String, String> consumerConfiguration = Map.of(
        "bootstrap.servers", configuration.bootstrapServers(),
        "key.deserializer", new BytesSerde().deserializer().getClass().getName(),
        "value.deserializer", new BytesSerde().serializer().getClass().getName(),
        "group.id", configuration.groupId(),
        "auto.offset.reset", "earliest",
        "enable.auto.commit", "false");

    this.kafkaConsumer = KafkaConsumer.create(
        Vertx.vertx(),
        consumerConfiguration,
        byte[].class, byte[].class);
  }

  public Multi<Message> consume(Source source) {
    kafkaConsumer.subscribeAndAwait(source.getName());

    return kafkaConsumer.toMulti()
        .map(record -> indexRecord(record, source));
  }

  private Message indexRecord(KafkaConsumerRecord<byte[], byte[]> record, Source source) {
    log.debug("Indexing record with key [{}] & value [{}]", record.key(), record.value());
    HeaderExtractor headerExtractor = new HeaderExtractor(record.headers());

    return Message.builder()
        .id(UUID.randomUUID())
        .timeStamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(record.timestamp()),
            TimeZone.getDefault().toZoneId()))
        .timeStampType(handleTimeStampType(record.timestampType()))
        .indexTime(LocalDateTime.now())
        .sourceId(source.getId())
        .offset(headerExtractor.getOffset())
        .partition(headerExtractor.getPartition())
        .payload(record.value())
        .payloadString(new String(record.value()))
        .metadataList(parseMetaDataFromHeaders(headerExtractor.getNonMatchedHeaders()))
        .build();
  }

  private TimestampType handleTimeStampType(
      org.apache.kafka.common.record.TimestampType timestampType) {
    if (timestampType.equals(org.apache.kafka.common.record.TimestampType.CREATE_TIME)) {
      return TimestampType.CREATE_TIME;
    } else if (timestampType.equals(org.apache.kafka.common.record.TimestampType.LOG_APPEND_TIME)) {
      return TimestampType.LOG_APPEND;
    }

    return null;
  }

  private List<Metadata> parseMetaDataFromHeaders(Map<String, String> headers) {
    return headers.entrySet()
        .stream()
        .map(entry -> Metadata.builder()
            .id(UUID.randomUUID())
            .type(MetaDataType.HEADER)
            .key(entry.getKey())
            .value(entry.getValue())
            .build())
        .collect(Collectors.toList());
  }
}
