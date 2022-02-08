package se.irori.indexing.adapter.kafka;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumer;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumerRecord;
import io.vertx.mutiny.kafka.client.producer.KafkaHeader;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes.BytesSerde;
import se.irori.indexing.adapter.IndexingAdapter;
import se.irori.model.Message;
import se.irori.model.MetaData;
import se.irori.model.Source;

@Slf4j
public class KafkaAdapter implements IndexingAdapter {

  KafkaConsumer<byte[], byte[]> kafkaConsumer;

  public KafkaAdapter(KafkaAdapterConfiguration configuration) {
    Map<String, String> consumerConfiguration = Map.of(
        "bootstrap.servers", configuration.getBootstrapServers(),
        "key.deserializer", new BytesSerde().deserializer().getClass().getName(),
        "value.deserializer", new BytesSerde().serializer().getClass().getName(),
        "group.id", configuration.getGroupId(),
        "auto.offset.reset", "earliest",
        "enable.auto.commit", "false");

    this.kafkaConsumer = KafkaConsumer.create(
        Vertx.vertx(),
        consumerConfiguration,
        byte[].class, byte[].class);
  }

  public Multi<Message> consumeSource(Source topic) {
    kafkaConsumer.subscribeAndAwait(topic.getName());
    return kafkaConsumer.toMulti()
        .map(this::indexRecord);
  }

  private Message indexRecord(KafkaConsumerRecord<byte[], byte[]> record) {
    log.debug("Indexing record with key [{}] & value [{}]", record.key(), record.value());
    return Message.builder()
        .id(UUID.randomUUID().toString())
        //TODO Should be a reference to a "topic". Not the topic name.
        .topicId(record.topic())
        .offset(record.offset())
        .partition(record.partition())
        .payload(record.value())
        .payloadString(new String(record.value()))
        .metaDataList(parseMetaData(record.headers()))
        .build();
  }

  private List<MetaData> parseMetaData(List<KafkaHeader> headers) {
    return null;
  }
}
