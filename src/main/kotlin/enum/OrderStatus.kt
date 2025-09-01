package org.ecommerce.enum

enum class OrderStatus(val value: String) {
    PENDING("PENDING"),
    CONFIRMED("CONFIRMED"),
    ALLOCATED("ALLOCATED"),
    SHIPPED("SHIPPED"),
    CANCELLED("CANCELLED")
}