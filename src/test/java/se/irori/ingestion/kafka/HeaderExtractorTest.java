package se.irori.ingestion.kafka;

import io.vertx.mutiny.kafka.client.producer.KafkaHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.irori.model.MetaDataType;
import se.irori.model.Metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class HeaderExtractorTest {

    private HeaderExtractor headerExtractor;
    private List<KafkaHeader> headerList;

    @BeforeEach
    void setUp() {
        headerList = new ArrayList<>();
        headerExtractor = new HeaderExtractor(headerList);
    }

    @Test
    void testExtract_WithEmptyHeaderList_NoHeadersExtracted() {
        headerExtractor.extract();

        assertNull(headerExtractor.getTopic());
        assertNull(headerExtractor.getPartition());
        assertNull(headerExtractor.getOffset());
        assertNull(headerExtractor.getTimestamp());
        assertNull(headerExtractor.getTimestampType());
        assertTrue(headerExtractor.getNonMatchedHeaders().isEmpty());
        assertTrue(headerExtractor.getMatchedHeaders().isEmpty());
        assertTrue(headerExtractor.getAllHeaders().isEmpty());
    }

    @Test
    void testExtract_WithTopicHeader_TopicExtracted() {
        String topic = "my-topic";
        headerList.add(KafkaHeader.header(HeaderExtractor.SPRING_CLOUD_TOPIC, topic));

        headerExtractor.extract();

        assertEquals(topic, headerExtractor.getTopic());
        assertNull(headerExtractor.getPartition());
        assertNull(headerExtractor.getOffset());
        assertNull(headerExtractor.getTimestamp());
        assertNull(headerExtractor.getTimestampType());
        assertTrue(headerExtractor.getNonMatchedHeaders().isEmpty());
        assertEquals(1, headerExtractor.getMatchedHeaders().size());
        assertTrue(headerExtractor.getAllHeaders().containsKey(HeaderExtractor.SPRING_CLOUD_TOPIC));
    }

    @Test
    void testExtract_WithPartitionHeader_PartitionExtracted() {
        int partition = 42;
        headerList.add(KafkaHeader.header(HeaderExtractor.SPRING_CLOUD_PARTITION, String.valueOf(partition)));

        headerExtractor.extract();

        assertNull(headerExtractor.getTopic());
        assertEquals(partition, headerExtractor.getPartition());
        assertNull(headerExtractor.getOffset());
        assertNull(headerExtractor.getTimestamp());
        assertNull(headerExtractor.getTimestampType());
        assertTrue(headerExtractor.getNonMatchedHeaders().isEmpty());
        assertEquals(1, headerExtractor.getMatchedHeaders().size());
        assertTrue(headerExtractor.getAllHeaders().containsKey(HeaderExtractor.SPRING_CLOUD_PARTITION));
    }

    @Test
    void testExtract_WithOffsetHeader_OffsetExtracted() {
        long offset = 123456L;
        headerList.add(KafkaHeader.header(HeaderExtractor.SPRING_CLOUD_OFFSET, String.valueOf(offset)));

        headerExtractor.extract();

        assertNull(headerExtractor.getTopic());
        assertNull(headerExtractor.getPartition());
        assertEquals(offset, headerExtractor.getOffset());
        assertNull(headerExtractor.getTimestamp());
        assertNull(headerExtractor.getTimestampType());
        assertTrue(headerExtractor.getNonMatchedHeaders().isEmpty());
        assertEquals(1, headerExtractor.getMatchedHeaders().size());
        assertTrue(headerExtractor.getAllHeaders().containsKey(HeaderExtractor.SPRING_CLOUD_OFFSET));
    }

    @Test
    void testExtract_WithTimestampHeader_TimestampExtracted() {
        String timestamp = "2023-06-28T12:34:56Z";
        headerList.add(KafkaHeader.header(HeaderExtractor.SPRING_CLOUD_TIMESTAMP, timestamp));

        headerExtractor.extract();

        assertNull(headerExtractor.getTopic());
        assertNull(headerExtractor.getPartition());
        assertNull(headerExtractor.getOffset());
        assertEquals(timestamp, headerExtractor.getTimestamp());
        assertNull(headerExtractor.getTimestampType());
        assertTrue(headerExtractor.getNonMatchedHeaders().isEmpty());
        assertEquals(1, headerExtractor.getMatchedHeaders().size());
        assertTrue(headerExtractor.getAllHeaders().containsKey(HeaderExtractor.SPRING_CLOUD_TIMESTAMP));
    }

    @Test
    void testExtract_WithTimestampTypeHeader_TimestampTypeExtracted() {
        String timestampType = "create_time";
        headerList.add(KafkaHeader.header(HeaderExtractor.SPRING_CLOUD_TIMESTAMP_TYPE, timestampType));

        headerExtractor.extract();

        assertNull(headerExtractor.getTopic());
        assertNull(headerExtractor.getPartition());
        assertNull(headerExtractor.getOffset());
        assertNull(headerExtractor.getTimestamp());
        assertEquals(timestampType, headerExtractor.getTimestampType());
        assertTrue(headerExtractor.getNonMatchedHeaders().isEmpty());
        assertEquals(1, headerExtractor.getMatchedHeaders().size());
        assertTrue(headerExtractor.getAllHeaders().containsKey(HeaderExtractor.SPRING_CLOUD_TIMESTAMP_TYPE));
    }

    @Test
    void testConvertHeader_WithExistingMapping_ReturnsTwoMetadataObjects() {
        String key = HeaderExtractor.SPRING_CLOUD_TOPIC;
        String value = "my-topic";

        List<Metadata> metadataList = HeaderExtractor.convertHeader(key, value).collect(Collectors.toList());

        assertEquals(2, metadataList.size());

        Metadata sourceMetadata = metadataList.get(0);
        assertEquals(MetaDataType.SOURCE_HEADER, sourceMetadata.getType());
        assertEquals(key, sourceMetadata.getKey());
        assertEquals(value, sourceMetadata.getValue());

        Metadata internalMetadata = metadataList.get(1);
        assertEquals(MetaDataType.INTERNAL, internalMetadata.getType());
        assertEquals(HeaderExtractor.INTERNAL_TOPIC, internalMetadata.getKey());
        assertEquals(value, internalMetadata.getValue());
    }

    @Test
    void testConvertHeader_WithNonExistingMapping_ReturnsOneMetadataObject() {
        String key = "custom-key";
        String value = "custom-value";

        List<Metadata> metadataList = HeaderExtractor.convertHeader(key, value).collect(Collectors.toList());

        assertEquals(1, metadataList.size());

        Metadata sourceMetadata = metadataList.get(0);
        assertEquals(MetaDataType.SOURCE_HEADER, sourceMetadata.getType());
        assertEquals(key, sourceMetadata.getKey());
        assertEquals(value, sourceMetadata.getValue());
    }
}
