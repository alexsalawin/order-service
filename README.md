# order-service

High Level Diagram

%3CmxGraphModel%3E%3Croot%3E%3CmxCell%20id%3D%220%22%2F%3E%3CmxCell%20id%3D%221%22%20parent%3D%220%22%2F%3E%3CmxCell%20id%3D%222%22%20value%3D%22%26lt%3Bfont%20style%3D%26quot%3Bfont-size%3A%2010px%3B%26quot%3B%26gt%3Bdecrement%20stock%26lt%3B%2Ffont%26gt%3B%22%20style%3D%22text%3BstrokeColor%3Dnone%3Balign%3Dcenter%3BfillColor%3Dnone%3Bhtml%3D1%3BverticalAlign%3Dmiddle%3BwhiteSpace%3Dwrap%3Brounded%3D0%3B%22%20vertex%3D%221%22%20parent%3D%221%22%3E%3CmxGeometry%20x%3D%22100%22%20y%3D%22480%22%20width%3D%22100%22%20height%3D%2230%22%20as%3D%22geometry%22%2F%3E%3C%2FmxCell%3E%3C%2Froot%3E%3C%2FmxGraphModel%3E
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
