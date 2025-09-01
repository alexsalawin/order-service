package org.ecommerce.service

import inventory.Inventory.AllocateStockResponse
import org.ecommerce.dto.ShopifyOrderWebhookDTO
import org.ecommerce.entity.OrderEntity
import org.ecommerce.enum.OrderStatus
import org.ecommerce.event.InventoryUpdatedEvent
import org.ecommerce.exception.OrderProcessingException
import org.ecommerce.mapper.toEntity
import org.ecommerce.repository.OrderRepository
import org.ecommerce.grpc.InventoryGrpcClient
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val inventoryGrpcClient: InventoryGrpcClient,
    private val kafkaTemplate: KafkaTemplate<String, InventoryUpdatedEvent>

) {
    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    @Transactional
    fun processShopifyOrder(request: ShopifyOrderWebhookDTO, webhookId: String?) {
        logger.info("Processing Shopify order id=${request.id}, webhookId=$webhookId")

        if (orderRepository.existsByExternalId(request.id.toString())) {
            logger.warn("Order ${request.id} already processed, skipping.")
            return
        }

        validateOrder(request)
        val orderEntity: OrderEntity = request.toEntity()

        try {
            // --- gRPC call to InventoryService ---
            val inventoryResponse: AllocateStockResponse = inventoryGrpcClient.allocateStock(request.lineItems)

            if (!inventoryResponse.success) {
                orderEntity.status = OrderStatus.CANCELLED
                orderRepository.save(orderEntity)
                logger.warn("Order ${request.id} cannot be allocated: ${inventoryResponse.message}")
                return
            }

            orderEntity.status = OrderStatus.ALLOCATED
            orderRepository.save(orderEntity)

            // --- publish inventory update event ---
            orderEntity.lineItems.forEach { item ->
                val event = InventoryUpdatedEvent(
                    orderId = orderEntity.externalId,
                    sku = item.sku,
                    availableQuantity = inventoryResponse.availableQuantityMap[item.sku] ?: 0,
                    locationId = request.locationId
                )
                kafkaTemplate.send("inventory-updates", event)
                logger.info("Published inventory update event for SKU ${item.sku}")
            }

        } catch (e: Exception) {
            logger.error("Error processing order ${request.id}", e)
            throw OrderProcessingException("Failed to process Shopify order ${request.id}", e)
        }
    }

    private fun validateOrder(request: ShopifyOrderWebhookDTO) {
        require(request.lineItems.isNotEmpty()) { "Order must have at least one line item" }
        require(request.totalPrice.toDoubleOrNull() != null) { "total_price must be a valid number" }
        require(request.currency.isNotBlank()) { "currency cannot be blank" }
    }
}
