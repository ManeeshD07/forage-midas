# Midas Core

Midas Core is a Spring Boot 3 service that powers the JPMC Advanced Software Engineering (ASE) Forage program exercise. The application listens for transaction events on Kafka, persists them to an in-memory H2 database, and exposes a REST endpoint for querying user balances. A companion incentive API is bundled in `services/` to simulate an upstream bonus-calculation service.

## Project layout
- `src/main/java/com/jpmc/midascore` – application code (Spring Boot configuration, REST controller, Kafka listener, repositories, entities, and domain models).
- `src/test/java/com/jpmc/midascore` – workflow helpers and task-oriented integration tests used by the Forage program.
- `src/test/resources/test_data` – CSV-like fixtures for seeding users and emitting synthetic transactions.
- `application.yml` – runtime configuration (Kafka bootstrap servers, topic, H2 datasource, server port).
- `services/transaction-incentive-api.jar` – standalone mock service that responds to incentive lookups.

## Requirements
- Java 17 (matches the `pom.xml` toolchain).
- Maven 3.9+ (the repo ships with the Maven Wrapper `./mvnw`).
- Kafka broker reachable at `${KAFKA_BOOTSTRAP_SERVERS}` (defaults to `localhost:9092`).
- Optional: a shell with `java` available to launch the incentive API jar.

### Quick Kafka setup (local)
You can spin up Kafka with Docker for local experimentation.
```bash
docker run --rm -it -p 9092:9092 -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092 \
  -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_CFG_ZOOKEEPER_CONNECT=localhost:2181 \
  -e ALLOW_PLAINTEXT_LISTENER=yes bitnami/kafka:3
```
Pair it with a lightweight Zookeeper (required for Kafka < 3.5) if you do not already have one running.

## Configuration
`application.yml` exposes the knobs you will typically touch:
- `general.kafka-topic` (default `test-topic`) – topic consumed by `KafkaTransactionListener`.
- `spring.kafka.bootstrap-servers` – override with `KAFKA_BOOTSTRAP_SERVERS` env var.
- `spring.datasource.*` – H2 in-memory datasource used for user and transaction tables.
- `server.port` (default `33400`) – port for the REST API.

The service expects JSON transactions shaped as:
```json
{
  "senderId": 1,
  "recipientId": 2,
  "amount": 42.75
}
```

## Running the stack
1. **Start dependencies**
   - Launch Kafka (see snippet above or your preferred setup).
   - (Optional) Start the incentive API mock: `java -jar services/transaction-incentive-api.jar` (listens on `http://localhost:33433`).
2. **Start Midas Core**
   ```bash
   ./mvnw spring-boot:run
   ```
   The application will create the H2 schema automatically. You can also produce an executable jar via `./mvnw clean package` and run it with `java -jar target/midas-core-1.0.0.jar`.

### Kafka processing flow
`KafkaTransactionListener` consumes the configured topic and:
- loads the sender and recipient from `UserRepository`;
- validates balances (insufficient funds short-circuit the transaction);
- adjusts balances, persists the transaction (`TransactionRecord`), and logs results;
- logs the latest integer balance for users named `waldorf` or `wilbur` when they participate.

Seed users programmatically via `UserPopulator` (see tests) or insert `UserRecord` rows through any JPA-aware mechanism. Transactions produced in the tests originate from the CSV fixtures under `src/test/resources/test_data/`.

## REST API
- `GET /balance?userId=<id>` → returns `{"amount": <float>}`. Non-existent users respond with a zero balance. The controller lives in `BalanceController` and is packaged with the main application.

## Incentive lookups
`IncentiveService` demonstrates how to call an external service to fetch additional rewards for each transaction. By default it targets `http://localhost:8080/incentive`, but the included mock jar (`services/transaction-incentive-api.jar`) responds on port `33433`. Adjust the URL or proxy requests as needed when extending the exercises.

## Testing & Forage tasks
The `TaskOneTests`–`TaskFiveTests` classes guide the ASE virtual experience:
- Task 1 validates the application boots.
- Tasks 2–4 populate users, send Kafka traffic, and ask you to inspect balances for specific users.
- Task 5 exercises the REST API and expects serialized balance output for user IDs `0–12`.

Tests rely on `@EmbeddedKafka` and intentionally block at the end so you can attach a debugger. Abort them manually after gathering the required evidence.

Run the full suite with:
```bash
./mvnw test
```
Be aware that Task 2–4 never exit on their own; stop the run once you have collected the information you need.

## Useful utilities
- `src/test/java/com/jpmc/midascore/FileLoader` – loads resources from `/test_data`.
- `KafkaProducer` – publishes CSV rows as transaction events.
- `BalanceQuerier` – queries the running `/balance` endpoint.
- `DatabaseConduit` – thin wrapper for persisting new users.

Feel free to extend the service with additional endpoints, persistence, or Kafka producers/consumers to explore more Spring Boot patterns during the virtual internship.
