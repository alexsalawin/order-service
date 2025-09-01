package org.ecommerce.controller

import jakarta.validation.Valid
import org.ecommerce.dto.ShopifyOrderWebhookDTO
import org.ecommerce.exception.OrderProcessingException
import org.ecommerce.service.OrderService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService
) {
    private val logger = LoggerFactory.getLogger(OrderController::class.java)

    /**
     * Shopify webhook endpoint for new orders
     * - Shopify will retry if not acknowledged with 200
     * - We must process idempotently
     */
    @PostMapping("/shopify/webhook")
    fun handleShopifyWebhook(
        @Valid @RequestBody request: ShopifyOrderWebhookDTO,
        @RequestHeader("X-Shopify-Topic") topic: String?,
        @RequestHeader("X-Shopify-Webhook-Id") webhookId: String?
    ): ResponseEntity<String> {
        logger.info("Received Shopify webhook [topic=$topic, webhookId=$webhookId, orderId=${request.id}]")

        return try {
            orderService.processShopifyOrder(request, webhookId)
            ResponseEntity.ok("Webhook processed successfully")
        } catch (ex: OrderProcessingException) {
            logger.error("Failed to process Shopify order: ${request.id}", ex)
            throw ex
        }
    }
}