package org.ecommerce.service

import org.ecommerce.dto.ShopifyOrderWebhookDTO
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

class OrderService {

    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    /**
     * Process Shopify order webhook
     * - Idempotency check
     * - Validate stock (later with WMS)
     * - Persist order
     * - Emit async event for inventory sync
     */
    @Transactional
    fun processShopifyOrder(request: ShopifyOrderWebhookDTO, webhookId: String?) {
        logger.info("Processing Shopify order id=${request.id}, webhookId=$webhookId")

        // Idempotency: ensure order not already processed
        if (orderRepository.existsByExternalId(request.id.toString())) {
            logger.warn("Order ${request.id} already processed, skipping.")
            return
        }

        try {
            // Map DTO → Entity
            val orderEntity = request.toEntity()
            orderRepository.save(orderEntity)

            // TODO: publish async event for inventory sync + WMS fulfillment

            logger.info("Order ${request.id} saved successfully")
        } catch (e: Exception) {
            logger.error("Error saving order ${request.id}", e)
            throw OrderProcessingException("Failed to process Shopify order ${request.id}", e)
        }
    }
}