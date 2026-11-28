# Inventory Service

Inventory Service owns authoritative product quantities. PostgreSQL row locks
serialize reservations for the same product; failed multi-product reservations
roll back without holding a partial set of stock.

## Run locally

Start PostgreSQL with `docker compose up -d`, then run `./mvnw spring-boot:run`.
The service listens on port `8085`. Database connection settings can be
overridden with `INVENTORY_DB_URL`, `INVENTORY_DB_USERNAME`, and
`INVENTORY_DB_PASSWORD`.

## API

All paths are under `/api/v1/inventory`.

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/{productId}` | Read current and available stock |
| `POST` | `/{productId}/stock` | Add stock |
| `POST` | `/reservations` | Atomically reserve a list of items |
| `POST` | `/reservations/{reservationId}/confirm` | Consume a reservation after payment succeeds |
| `DELETE` | `/reservations/{reservationId}` | Release a reservation after payment fails |

Reservation, confirmation, and release requests require a trusted
`X-User-Id` header. The API gateway must remove client-supplied values and
inject the authenticated user ID. The stock-adjustment endpoint is operational
and must only be reachable through a trusted internal/admin channel.

Example reservation:

```http
POST /api/v1/inventory/reservations
X-User-Id: 1001
Content-Type: application/json
```

```json
{
  "items": [
    { "productId": "product-123", "quantity": 2 }
  ]
}
```

Reservations are idempotent for confirmation and release transitions.
Inventory remains reserved until one of those transitions succeeds; payment
orchestration is responsible for confirming or releasing it.

## Tests

Run `./mvnw test`. Tests use an in-memory H2 database and do not require
PostgreSQL.
