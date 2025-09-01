package org.ecommerce.mapper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.ecommerce.dto.ShopifyOrderWebhookDTO
import org.ecommerce.entity.OrderEntity
import java.time.OffsetDateTime

private val objectMapper = jacksonObjectMapper()

fun ShopifyOrderWebhookDTO.toEntity(): OrderEntity {
    val totalPriceDouble = totalPrice.toDoubleOrNull() ?: 0.0

    return OrderEntity(
        externalId = id.toString(),
        name = name,
        customerEmail = null, // optional; add if you capture customer email elsewhere
        totalPrice = totalPriceDouble,
        currency = currency.ifBlank { "USD" },
        lineItemsJson = objectMapper.writeValueAsString(lineItems),
        status = "PENDING",
        createdAt = createdAt.toOffsetDateTime(),
        updatedAt = OffsetDateTime.now()
    )
}
