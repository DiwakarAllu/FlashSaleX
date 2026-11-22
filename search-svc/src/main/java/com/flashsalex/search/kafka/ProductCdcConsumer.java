package com.flashsalex.search.kafka;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.flashsalex.search.model.ProductSearchDocument;
import com.flashsalex.search.repository.ProductSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCdcConsumer {

    private final ProductSearchRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.product-topic}")
    public void consume(ConsumerRecord<String, String> record) {
        String message = record.value();
        if (message == null || message.isBlank()) {
            return;
        }

        JsonNode event = objectMapper.readTree(message);
        String operation = event.path("op").asText();

        switch (operation) {
            case "c", "u", "r" -> {
                JsonNode after = event.path("after");
                if (!after.isObject()) {
                    throw new IllegalArgumentException(
                            "CDC event is missing its product payload"
                    );
                }

                ProductSearchDocument product = mapProduct(after);
                repository.save(product);
                log.info("Product indexed successfully: {}", product.getProductId());
            }
            case "d" -> {
                JsonNode beforeId = event.path("before").path("_id");
                String productId = beforeId.isMissingNode() || beforeId.isNull()
                        ? idFromKey(record.key())
                        : mongoId(beforeId);

                repository.deleteById(productId);
                log.info("Product removed from index: {}", productId);
            }
            default -> log.warn("Ignoring unsupported CDC operation: {}", operation);
        }
    }

    private ProductSearchDocument mapProduct(JsonNode node) {
        JsonNode priceNode = node.path("price");
        BigDecimal price = null;
        if (priceNode.isNumber()) {
            price = priceNode.decimalValue();
        } else if (priceNode.isTextual()) {
            price = new BigDecimal(priceNode.asText());
        } else if (priceNode.has("$numberDecimal")) {
            price = new BigDecimal(priceNode.path("$numberDecimal").asText());
        }

        JsonNode attributesNode = node.path("attributes");
        if (!attributesNode.isObject()) {
            attributesNode = node.path("specifications");
        }
        Map<String, Object> attributes = attributesNode.isObject()
                ? objectMapper.convertValue(
                        attributesNode,
                        new TypeReference<Map<String, Object>>() {}
                )
                : Map.of();

        JsonNode imageUrlsNode = node.path("imageUrls");
        if (!imageUrlsNode.isArray()) {
            imageUrlsNode = node.path("images");
        }
        List<String> imageUrls = imageUrlsNode.isArray()
                ? objectMapper.convertValue(
                        imageUrlsNode,
                        new TypeReference<List<String>>() {}
                )
                : List.of();

        return ProductSearchDocument.builder()
                .productId(mongoId(node.path("_id")))
                .name(node.path("name").isTextual()
                        ? node.path("name").asText()
                        : node.path("title").asText())
                .description(node.path("description").asText())
                .category(node.path("category").asText())
                .price(price)
                .currency(node.path("currency").asText())
                .attributes(attributes)
                .imageUrls(imageUrls)
                .build();
    }

    private String idFromKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Delete CDC event has no Kafka key");
        }

        JsonNode id = objectMapper.readTree(key).path("_id");
        return mongoId(id);
    }

    private String mongoId(JsonNode id) {
        JsonNode value = id.isObject() ? id.path("$oid") : id;
        if (value.isMissingNode() || value.isNull() || value.asText().isBlank()) {
            throw new IllegalArgumentException("CDC event has no product ID");
        }
        return value.asText();
    }
}
