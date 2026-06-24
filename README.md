# DineOps

DineOps is a modular restaurant management platform built as a set of Spring Boot microservices. It is designed to support core restaurant operations including authentication, order management, menu management, kitchen processing, and billing.

## Overview

DineOps is organized as multiple independent services, each responsible for a specific domain:

- `Auth-service` — handles user authentication, registration, and security.
- `order-service` — manages customer orders, order creation, and order lifecycle.
- `menu-service` — maintains menu items, availability, and menu-related operations.
- `kitchen-service` — processes kitchen tickets, order preparation status, and kitchen events.
- `bill-service` — generates bills, processes payments, and tracks billing events.

The project uses a microservice architecture and follows separation of concerns so that each service can evolve independently.

## How it works

DineOps is built around the following principles:

- **Service isolation**: each service owns its own domain logic, data model, and API.
- **Event-driven communication**: services exchange status updates and integration events through Kafka topics.
- **REST APIs**: services expose HTTP endpoints for client interaction and internal coordination.
- **Security**: authentication and authorization are handled in a dedicated service, with JWT-based mechanisms used across the ecosystem.

### Typical flow

1. A customer order is created in the `order-service`.
2. The `kitchen-service` receives the order and updates kitchen status while preparing items.
3. When the order is ready, kitchen events are published and the `bill-service` generates a bill.
4. The `bill-service` manages payment processing and finalizes the transaction.
5. The `Auth-service` secures access and manages user credentials.

## Technology stack

- Java with Spring Boot
- Apache Kafka for asynchronous event communication
- Spring Security for authentication and authorization
- REST APIs for service-to-service and client interactions
- Lombok for boilerplate reduction

## Repository structure

The repository contains one folder per service:

- `Auth-service`
- `bill-service`
- `kitchen-service`
- `menu-service`
- `order-service`

Each service includes its own `pom.xml`, `src/main/java`, and `src/main/resources`.

## Notes

- This README is intentionally high level. Service-specific README files can be added later for deeper setup and configuration details.
- Some services currently depend on local or external dependencies and may require Maven and Kafka setup to run end-to-end.

## Next steps

When expanding this repository, consider adding:

- a root-level architecture diagram
- service-specific README files
- deployment instructions
- development and testing conventions
