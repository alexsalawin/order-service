package org.ecommerce.entity

import jakarta.persistence.*
import org.springframework.data.annotation.Id

@Entity
@Table(name = "order_line_items")
data class OrderLineItemEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val sku: String,
    val quantity: Int,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    val order: OrderEntity
)
