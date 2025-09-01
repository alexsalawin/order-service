package org.ecommerce.mapper

import org.ecommerce.dto.ShopifyOrderWebhookDTO
import org.ecommerce.entity.OrderEntity
import org.ecommerce.entity.OrderLineItemEntity
import org.ecommerce.enum.OrderStatus

fun ShopifyOrderWebhookDTO.toEntity(): OrderEntity {
    // Convert Shopify's string totalPrice to Double safely
    val parsedTotalPrice = totalPrice.toDoubleOrNull() ?: 0.0

    val order = OrderEntity(
        externalId = id.toString(),
        name = name,
        totalPrice = parsedTotalPrice,
        currency = currency,
        status = when (fulfillmentStatus?.lowercase()) {
            "fulfilled" -> OrderStatus.COMPLETED
            "cancelled" -> OrderStatus.CANCELLED
            else -> OrderStatus.PENDING
        },
        createdAt = createdAt.toOffsetDateTime()
    )

    // Map line items and associate them back to the order
    val mappedLineItems = lineItems.map { lineItemDto ->
        OrderLineItemEntity(
            sku = lineItemDto.sku ?: "UNKNOWN", // handle null SKU gracefully
            quantity = lineItemDto.quantity,
            price = 0.0, // Shopify webhook doesn’t give per-line price in your DTO
            order = order
        )
    }

    // Attach line items to order
    return order.copy(
        lineItems = mappedLineItems
    )
}

