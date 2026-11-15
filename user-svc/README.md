# User Service

Spring Boot service for customer registration, authentication, profile data,
and delivery addresses. MySQL stores users and addresses; passwords are stored
as BCrypt hashes. Protected endpoints accept an HS256 JWT in the
`Authorization: Bearer <token>` header. Configure the API Gateway with the same
JWT secret and issuer so it can validate the service's tokens.

## Run locally

Requirements: Java 25 and Docker Compose.

1. Copy `.env.example` to `.env`.
2. Set `DB_PASSWORD` and `MYSQL_ROOT_PASSWORD`, then set `JWT_SECRET` to a
   Base64-encoded random secret containing at least 32 bytes (for example,
   generate one with `openssl rand -base64 32`).
3. Start MySQL with `docker compose up -d`.
4. Start the API with `./mvnw spring-boot:run`.

The API listens on `http://localhost:9001`. Database schema updates are
managed by Hibernate for this starter service.

## API

| Method | Path | Purpose | Authentication |
| --- | --- | --- | --- |
| `POST` | `/users/signup` | Register a user | Public |
| `POST` | `/users/login` | Validate credentials and return an access token | Public |
| `GET` | `/users/profile` | Get the authenticated user's profile | Bearer token |
| `PUT` | `/users/profile` | Update the authenticated user's name and email | Bearer token |
| `POST` | `/users/addresses` | Add an address to the authenticated user | Bearer token |
| `GET` | `/users/addresses` | List the authenticated user's addresses | Bearer token |
| `PUT` | `/users/addresses/{id}` | Update an address owned by the authenticated user | Bearer token |
| `DELETE` | `/users/addresses/{id}` | Delete an address owned by the authenticated user | Bearer token |

Signup request:

```json
{
  "name": "Ada Lovelace",
  "email": "ada@example.com",
  "password": "a-long-unique-password"
}
```

Passwords must contain at least 12 characters; BCrypt's 72-byte input limit
also applies.

Login returns `accessToken`, `tokenType`, and `expiresIn` (seconds). The token
subject is the user ID; profile and address ownership are always derived from
the validated token, never from a client-supplied user ID. Email addresses are
stored in lowercase, and duplicate registration returns `409 Conflict`.

## Tests

Run `./mvnw test`. Tests use an in-memory H2 database and do not require MySQL.
