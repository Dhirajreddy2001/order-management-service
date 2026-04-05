# Order Management Service

Monorepo for a Spring Boot order-processing system with Kafka, PostgreSQL, RabbitMQ, and AWS SQS via LocalStack.

The codebase is organized as four services:
- `order-api`
- `inventory-service`
- `invoice-woker`
- `notification-service`

All services now live in a single root repo and use the shared package root `com.ordermanagement`.

## Architecture

- `order-api` accepts order requests, persists orders in PostgreSQL, and publishes:
  - `OrderCreatedEvent` to Kafka
  - invoice jobs to RabbitMQ or SQS depending on profile
- `inventory-service` consumes Kafka order events, reserves stock, and tracks processed events for idempotency.
- `notification-service` consumes the same Kafka order stream independently.
- `invoice-woker` consumes invoice jobs from RabbitMQ or SQS and performs invoice generation work.

### Messaging Channels

- Kafka topics:
  - `orders.created`
  - `orders.rejected`
  - `orders.created.DLT`
- RabbitMQ:
  - exchange: `invoice.exchange`
  - queue: `invoice.jobs`
  - dead-letter queue: `invoice.jobs.dlq`
- SQS:
  - queue: `invoice-jobs`
  - dead-letter queue: `invoice-jobs-dlq`

## Services

### `order-api`
- REST API for creating and querying orders
- PostgreSQL persistence with Flyway migrations
- Kafka producer for order events
- RabbitMQ or SQS producer for invoice jobs
- Swagger/OpenAPI support

### `inventory-service`
- Kafka consumer for `OrderCreatedEvent`
- stock reservation and rejection handling
- idempotency with the `processed_events` table

### `invoice-woker`
- RabbitMQ consumer profile: `rabbit`
- SQS consumer profile: `sqs`
- invoice generation and DLQ handling

### `notification-service`
- Kafka consumer for order events
- separate consumer group for fan-out style processing

## Tech Stack

- Java 17
- Spring Boot 4
- Spring Web / Spring WebMVC
- Spring Data JPA
- Spring Kafka
- Spring AMQP
- Spring Cloud AWS 4
- PostgreSQL
- RabbitMQ
- AWS SQS via LocalStack
- Docker and Docker Compose
- JUnit 5, Mockito, Spring Boot Test

## Profiles

### `order-api`
- `dev`: PostgreSQL + Kafka + RabbitMQ
- `sqs`: PostgreSQL + Kafka + LocalStack SQS

### `invoice-woker`
- `rabbit`: RabbitMQ consumer
- `sqs`: SQS consumer

### `inventory-service`
- `dev`: Kafka + PostgreSQL

### `notification-service`
- `dev`: Kafka consumer

## Ports

- `order-api`: `8080`
- `inventory-service`: `8081`
- `notification-service`: `8082`
- `invoice-woker`: `8083`
- PostgreSQL: `5432`
- Kafka: `9092`
- RabbitMQ management UI: `15672`
- RabbitMQ broker: `5672`
- LocalStack edge: `4566`

## Local Run

Start infrastructure:

```bash
docker compose up -d postgres kafka rabbitmq localstack
```

Run the services in separate terminals.

RabbitMQ path:

```bash
cd order-api && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
cd ../invoice-woker && ./mvnw spring-boot:run -Dspring-boot.run.profiles=rabbit
cd ../inventory-service && ./mvnw spring-boot:run
cd ../notification-service && ./mvnw spring-boot:run
```

SQS path:

```bash
cd order-api && ./mvnw spring-boot:run -Dspring-boot.run.profiles=sqs
cd ../invoice-woker && ./mvnw spring-boot:run -Dspring-boot.run.profiles=sqs
```

## Testing

Run module tests from each service directory:

```bash
cd order-api && ./mvnw test
cd inventory-service && ./mvnw test
cd invoice-woker && ./mvnw test
cd notification-service && ./mvnw test
```

Useful checks:
- `order-api` tests cover order creation and publisher wiring
- `inventory-service` tests cover repository and idempotency behavior
- `invoice-woker` tests cover consumer startup and message handling

## Current Milestone

Completed roadmap work through Day 17:
- PostgreSQL and Flyway
- RabbitMQ invoice publishing
- RabbitMQ invoice worker
- LocalStack SQS profile switch
- monorepo cleanup and package rename

## Notes

- The project uses the package root `com.ordermanagement`.
- `notification-service` is now part of the root monorepo and no longer tracked as a submodule.
- `invoice-woker` keeps both RabbitMQ and SQS consumers, selected by profile.
