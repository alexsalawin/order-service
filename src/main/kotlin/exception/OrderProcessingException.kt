package org.ecommerce.exception

class OrderProcessingException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)