# order-service

High Level Diagram
<img width="806" height="601" alt="AnyX_HighLevel drawio" src="https://github.com/user-attachments/assets/5d7b29ea-2519-48ad-b423-3b268cd9bbe2" />

a. Service Boundaries & Communication

Microservice decomposition:

Order Service

Responsibility: Receives orders, validates them, orchestrates allocation and fulfillment.

Boundaries: Does not manage inventory directly but coordinates with inventory-service.

Communication:

Synchronous: Calls inventory-service to check stock (REST/gRPC depending on latency requirements).

Asynchronous: Publishes order events (Kafka/message queue) to trigger fulfillment and notifications.

Inventory Service

Responsibility: Manages stock levels (local DB and WMS integration), prevents overselling.

Boundaries: Only manages stock, no order or fulfillment logic.

Communication:

Synchronous: REST/gRPC for stock queries from order-service.

Asynchronous: Listens to order allocation events to decrement stock or trigger WMS checks.

Fulfillment Service

Responsibility: Sends orders to WMS for fulfillment.

Boundaries: Independent from order creation; only handles actual fulfillment.

Communication:

Synchronous: Call WMS API (REST) with retry/backoff.

Asynchronous: Listens to order-allocated events to initiate fulfillment.

Channel Sync Service

Responsibility: Syncs inventory/stock changes to Shopify or other sales channels.

Boundaries: Independent from core order/fulfillment logic.

Communication:

Asynchronous: Listens to stock updates (Kafka) and publishes updates to Shopify (REST API).

Justification:

Each service has a single responsibility and owns its data.

Async events decouple services and improve resiliency.

Sync calls are used where immediate feedback is required (e.g., stock validation before order confirmation).

Protocol choices:

REST: For external integration (Shopify, WMS) due to wide support.

gRPC: Optional internal high-performance calls between microservices if low latency is critical.

Message Queue (Kafka/RabbitMQ): For async events like stock updates, order allocation, fulfillment triggers.
