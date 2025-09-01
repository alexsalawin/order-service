package org.ecommerce.entity

import jakarta.persistence.*
import org.ecommerce.enum.OrderStatus
import java.time.OffsetDateTime

@Entity
@Table(
    name = "orders",
    uniqueConstraints = [UniqueConstraint(name = "uk_orders_external_id", columnNames = ["external_id"])]
)
data class OrderEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "external_id", nullable = false, unique = true)
    val externalId: String,

    @Column(name = "order_name", nullable = false)
    val name: String,

    @Column(name = "customer_email")
    val customerEmail: String? = null,

    @Column(name = "total_price", nullable = false)
    val totalPrice: Double,

    @Column(name = "currency", nullable = false)
    val currency: String,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val lineItems: List<OrderLineItemEntity> = emptyList(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = OffsetDateTime.now()
    }
}

