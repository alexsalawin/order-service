package org.ecommerce.event

import java.time.OffsetDateTime

data class OrderAllocatedEvent(
    val orderId: String,
    val lineItems: List<OrderLineItemDto>,
    val locationId: Long?,
    val allocatedAt: OffsetDateTime = OffsetDateTime.now()
)

data class OrderLineItemDto(
    val sku: String,
    val quantity: Int
)
