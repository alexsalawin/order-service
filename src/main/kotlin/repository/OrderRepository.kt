package org.ecommerce.repository

import org.ecommerce.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<OrderEntity, Long> {

    /**
     * Check if an order already exists by its external Shopify order ID
     */
    fun existsByExternalId(externalId: String): Boolean

    /**
     * Optional: find order by external ID
     */
    fun findByExternalId(externalId: String): OrderEntity?
}
