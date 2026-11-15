# Product Service

Spring Boot REST service for product catalog data. MongoDB stores catalog
records; inventory quantity is owned by the separate Inventory Service.

## Run locally

Requirements: Java 25 and Docker Compose.

1. Copy `.env.example` to `.env` and set local MongoDB credentials.
2. Start MongoDB: `docker compose up -d`
3. Start the API: `./mvnw spring-boot:run`

The API listens on `http://localhost:9000`. The Compose file starts MongoDB
only; the Spring Boot application runs locally. Check application health at
`GET /actuator/health`.

## API

All endpoints use `/api/v1/products`.

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/` | Create a product |
| `GET` | `/{id}` | Get an active product |
| `GET` | `/` | List active products |
| `GET` | `/search?q={term}` | Search active products by title, description, category, or brand |
| `GET` | `/category/{category}` | List active products in a category |
| `PUT` | `/{id}` | Replace product catalog fields |
| `DELETE` | `/{id}` | Deactivate a product (soft delete) |

List and search endpoints accept Spring pagination parameters such as
`page=0&size=20`; requested page sizes are capped at 100.

Example create request:

```json
{
  "sku": "PHONE-001",
  "title": "Example Smartphone",
  "description": "Smartphone with 256GB storage.",
  "category": "electronics",
  "brand": "Example",
  "price": 799.99,
  "currency": "USD",
  "images": ["https://example.com/phone.jpg"],
  "specifications": {
    "storage": "256GB",
    "screenSizeInches": 6.2
  }
}
```

Search currently uses case-insensitive MongoDB text-field matching. It is a
starter implementation, not a replacement for a dedicated search index at
large catalog/search volumes.

## Tests

Run `./mvnw test`. The application context test uses the MongoDB instance
configured by `.env`.
