# 🏗️ Order Management Service

This is a Spring Boot order-processing platform with PostgreSQL, Kafka, RabbitMQ, AWS SQS via LocalStack, and multiple consumer services.

The repo is organized as a single root workspace with these services:
- 🔌 `order-api`
- 📦 `inventory-service`
- 🧾 `invoice-woker`
- 📬 `notification-service`

All services now share the package root `com.ordermanagement`.

---

## 🏛️ System Architecture

```mermaid
graph TB
    subgraph Client["Client Layer"]
        REST["REST Client / Swagger / Postman"]
    end
    
    subgraph API["API Gateway"]
        OA["order-api<br/>Port 8080"]
    end
    
    subgraph Database["Data Layer"]
        PG["PostgreSQL<br/>Port 5432"]
    end
    
    subgraph MessageBrokers["Message Brokers"]
        K["Kafka<br/>Port 9092"]
        RMQ["RabbitMQ<br/>Broker 5672<br/>UI 15672"]
        SQS["LocalStack SQS<br/>Port 4566"]
    end
    
    subgraph KafkaConsumers["Kafka Consumers"]
        INV["inventory-service<br/>Port 8081"]
        NOTIF["notification-service<br/>Port 8082"]
    end
    
    subgraph QueueConsumers["Queue Consumers"]
        IW_RABBIT["invoice-woker<br/>rabbit profile<br/>Port 8083"]
        IW_SQS["invoice-woker<br/>sqs profile<br/>Port 8083"]
    end
    
    REST -->|POST /orders| OA
    REST -->|GET /orders| OA
    
    OA -->|Save/Query| PG
    OA -->|Publish OrderCreatedEvent| K
    OA -->|Publish InvoiceJob| RMQ
    OA -->|Publish InvoiceJob| SQS
    
    K -->|Subscribe orders.created| INV
    K -->|Subscribe orders.created| NOTIF
    RMQ -->|Consume invoice.jobs| IW_RABBIT
    SQS -->|Consume invoice-jobs| IW_SQS
    
    INV -->|Update stock<br/>Save events| PG
    NOTIF -->|Process notifications| PG
```

---

## 🔧 Services

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

## 🚀 Deployment Paths

```mermaid
graph LR
    subgraph Dev["Development Path"]
        OA_DEV["order-api<br/>Profile: dev"]
        INV_DEV["inventory-service<br/>Profile: dev"]
        NOTIF["notification-service<br/>Profile: dev"]
        IW_RABBIT["invoice-woker<br/>Profile: rabbit"]
        
        OA_DEV -->|Kafka| INV_DEV
        OA_DEV -->|Kafka| NOTIF
        OA_DEV -->|RabbitMQ| IW_RABBIT
    end
    
    subgraph SQS_Path["SQS Alternative Path"]
        OA_SQS["order-api<br/>Profile: sqs"]
        IW_SQS["invoice-woker<br/>Profile: sqs"]
        
        OA_SQS -->|LocalStack SQS| IW_SQS
    end
    
    subgraph Shared["Shared Infrastructure"]
        PG["PostgreSQL"]
        K["Kafka"]
        RMQ["RabbitMQ"]
        LS["LocalStack"]
    end
    
    Dev --> Shared
    SQS_Path --> Shared
```

## 📊 Message Flows

### Order Processing Flow (End-to-End)

```mermaid
sequenceDiagram
    participant C as Client
    participant O as order-api
    participant P as PostgreSQL
    participant K as Kafka
    participant R as RabbitMQ / SQS
    participant I as invoice-woker

    C->>O: POST /orders
    O->>P: Save order + items
    O->>K: Publish OrderCreatedEvent
    O->>R: Publish invoice job
    R->>I: Deliver invoice job
    I->>I: Generate invoice
    I->>P: Save invoice
```

### RabbitMQ Message Flow with DLT

```mermaid
sequenceDiagram
    participant OA as order-api
    participant RMQ as RabbitMQ<br/>invoice.exchange
    participant IW as invoice-woker
    participant DLQ as Dead Letter Queue<br/>invoice.jobs.dlq
    participant DB as PostgreSQL
    
    OA->>RMQ: Publish InvoiceJob<br/>to invoice.exchange
    activate RMQ
    RMQ->>RMQ: Route to queue<br/>invoice.jobs
    deactivate RMQ
    
    RMQ->>IW: Deliver Message
    activate IW
    alt Success
        IW->>DB: Generate & Save Invoice
        DB-->>IW: Success
        IW->>RMQ: ACK Message
    else Processing Error
        IW->>IW: Retry attempt
        alt Retries Exhausted
            IW->>DLQ: Send to DLQ
            IW->>RMQ: NACK & Move to DLQ
        end
    else Unrecoverable Error
        IW->>DLQ: Send directly to DLQ
        IW->>RMQ: NACK
    end
    deactivate IW
```

### AWS SQS Message Flow with DLT

```mermaid
sequenceDiagram
    participant OA as order-api
    participant SQS as LocalStack SQS<br/>invoice-jobs
    participant IW as invoice-woker
    participant DLQ as Dead Letter Queue<br/>invoice-jobs-dlq
    participant DB as PostgreSQL
    
    OA->>SQS: SendMessage<br/>InvoiceJob
    
    SQS->>IW: ReceiveMessage
    activate IW
    alt Success
        IW->>DB: Generate & Save Invoice
        DB-->>IW: Success
        IW->>SQS: DeleteMessage
    else Processing Error
        IW->>IW: Check retry count<br/>in message metadata
        alt Retries Exhausted or<br/>Unrecoverable Error
            IW->>SQS: SendMessage<br/>to invoice-jobs-dlq
            IW->>SQS: DeleteMessage<br/>from main queue
        else Retry Available
            IW->>SQS: ChangeMessageVisibility<br/>Retry later
        end
    end
    deactivate IW
```

### Kafka Event Flow with Retry Topics & DLT

```mermaid
sequenceDiagram
    participant OA as order-api
    participant K as Kafka Cluster
    participant INV as inventory-service
    participant NOTIF as notification-service
    participant DLT as DLT Topic<br/>orders.created.DLT
    
    OA->>K: Produce OrderCreatedEvent<br/>to orders.created
    
    par Kafka Processing
        K->>INV: Deliver to Inventory<br/>Consumer Group
        activate INV
        alt Success
            INV->>INV: Reserve Stock
            INV->>K: Commit Offset
        else Retryable Error
            INV->>K: Don't commit
            K->>INV: Redeliver (N times)
        else Exhausted Retries
            INV->>DLT: Send to DLT topic
        end
        deactivate INV
    and
        K->>NOTIF: Deliver to Notification<br/>Consumer Group
        activate NOTIF
        alt Success
            NOTIF->>NOTIF: Process Notification
            NOTIF->>K: Commit Offset
        else Error
            NOTIF->>DLT: Send to DLT
        end
        deactivate NOTIF
    end
```

## 📬 Messaging Channels

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

### Error Handling & DLT Flow

```mermaid
graph TD
    Start["Message Processing Starts"] --> Parse{Parse Message<br/>Successful?}
    
    Parse -->|Yes| Process["Execute Business Logic"]
    Parse -->|No| InvalidMsg["Invalid Message Error"]
    
    Process --> Execute{Processing<br/>Successful?}
    
    Execute -->|Yes| ACK["✓ ACK Message<br/>Commit Offset"]
    Execute -->|No - Retryable| CheckRetry{Retry Count<br/>< Max?}
    Execute -->|No - Fatal| Fatal["✗ Unrecoverable Error"]
    
    CheckRetry -->|Yes| Retry["⟳ Retry Message<br/>Increment Counter"]
    CheckRetry -->|No| Exhausted["✗ Retries Exhausted"]
    
    InvalidMsg --> DLT1["→ Send to DLT<br/>Log Error"]
    Fatal --> DLT2["→ Send to DLT<br/>Log Details"]
    Exhausted --> DLT3["→ Send to DLT<br/>Log Attempts"]
    Retry --> Wait["⏳ Wait Before Retry"]
    
    Wait --> Process
    DLT1 --> Monitor["🔔 Operator Reviews DLT"]
    DLT2 --> Monitor
    DLT3 --> Monitor
    ACK --> Success["✓ Message Processed"]
    
    Monitor -->|Fix Issue| Manual["Manual Reprocess"]
    Manual --> Process
```

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

## 🧪 Run with Make

The repo includes a root `Makefile` for common tasks.

Start infrastructure:

```bash
make up
```

Stop infrastructure:

```bash
make down
```

Restart infrastructure:

```bash
make restart
```

Run services:

```bash
make run-order-dev
make run-order-sqs
make run-inventory
make run-invoice-rabbit
make run-invoice-sqs
make run-notification
```

Run tests:

```bash
make test-order
make test-inventory
make test-invoice
make test-notification
```

Build modules:

```bash
make build-order
make build-inventory
make build-invoice
make build-notification
```

Show all targets:

```bash
make help
```

## 🖥️ Run Manually

If you prefer direct Maven commands, use these.

Start infrastructure:

```bash
docker compose up -d postgres kafka rabbitmq localstack
```

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

## ✅ Testing

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
