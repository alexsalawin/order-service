package org.ecommerce.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ShipBobInventoryListResponse(
    val items: List<ShipBobInventoryItem>
)

data class ShipBobInventoryItem(
    @JsonProperty("inventory_id")
    val inventoryId: Long,
    val sku: String,
    @JsonProperty("total_sellable_quantity")
    val totalSellableQuantity: Int
)
