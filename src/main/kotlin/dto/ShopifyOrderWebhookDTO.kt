package org.ecommerce.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

data class ShopifyOrderWebhookDTO(
    @JsonProperty("id")
    val id: Long, // Shopify order ID
    @JsonProperty("name")
    val name: String, // e.g. "#1001" (order name/number)
    @JsonProperty("created_at")
    val createdAt: ZonedDateTime, // Order creation timestamp
    @JsonProperty("currency")
    val currency: String, // ISO currency code
    @JsonProperty("total_price")
    val totalPrice: String, // Shopify sends as string for precision
    @JsonProperty("line_items")
    val lineItems: List<ShopifyLineItemDTO>, // Products in the order
    @JsonProperty("fulfillment_status")
    val fulfillmentStatus: String?, // "fulfilled", "unfulfilled", etc.
    @JsonProperty("financial_status")
    val financialStatus: String?, // "paid", "pending", etc.
    @JsonProperty("location_id")
    val locationId: Long? // Store location
)

data class ShopifyLineItemDTO(
    @JsonProperty("id")
    val id: Long, // Line item ID
    @JsonProperty("product_id")
    val productId: Long?, // Shopify product ID
    @JsonProperty("title")
    val title: String, // Product title
    @JsonProperty("quantity")
    val quantity: Int, // Quantity ordered
    @JsonProperty("sku")
    val sku: String?, // SKU (primary key for WMS sync)
    @JsonProperty("vendor")
    val vendor: String?, // Optional brand/vendor info
    @JsonProperty("fulfillment_status")
    val fulfillmentStatus: String? // Line item-level fulfillment status
)

