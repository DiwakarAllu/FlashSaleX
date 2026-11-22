package com.flashsalex.search.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.flashsalex.search.model.ProductSearchDocument;
import com.flashsalex.search.repository.ProductSearchRepository;

import tools.jackson.databind.ObjectMapper;

class ProductCdcConsumerTests {

    private final ProductSearchRepository repository = mock(ProductSearchRepository.class);
    private final ProductCdcConsumer consumer =
            new ProductCdcConsumer(repository, new ObjectMapper());

    @Test
    void indexesProductServiceDocumentAndMapsOptionalFields() {
        String event = """
                {
                  "op": "c",
                  "after": {
                    "_id": {"$oid": "product-1"},
                    "title": "Phone",
                    "category": "Electronics",
                    "price": {"$numberDecimal": "12.50"},
                    "currency": "USD",
                    "specifications": {"storage": "128GB"},
                    "images": ["front.jpg"]
                  }
                }
                """;

        consumer.consume(new ConsumerRecord<>("products", 0, 0L, null, event));

        var product = ArgumentCaptor.forClass(ProductSearchDocument.class);
        verify(repository).save(product.capture());
        assertEquals("product-1", product.getValue().getProductId());
        assertEquals("Phone", product.getValue().getName());
        assertEquals("128GB", product.getValue().getAttributes().get("storage"));
        assertEquals("front.jpg", product.getValue().getImageUrls().getFirst());
        assertEquals("12.50", product.getValue().getPrice().toPlainString());
    }

    @Test
    void deletesUsingKafkaKeyWhenBeforeDocumentIsMissing() {
        String event = """
                {"op":"d","before":null}
                """;

        consumer.consume(new ConsumerRecord<>(
                "products",
                0,
                0L,
                "{\"_id\":{\"$oid\":\"product-1\"}}",
                event
        ));

        verify(repository).deleteById("product-1");
    }

    @Test
    void propagatesIndexingFailuresSoKafkaDoesNotAcknowledgeTheEvent() {
        doThrow(new IllegalStateException("Elasticsearch unavailable"))
                .when(repository)
                .save(any(ProductSearchDocument.class));
        String event = """
                {
                  "op": "c",
                  "after": {
                    "_id": "product-1",
                    "title": "Phone"
                  }
                }
                """;

        assertThrows(
                IllegalStateException.class,
                () -> consumer.consume(new ConsumerRecord<>("products", 0, 0L, null, event))
        );
    }
}
