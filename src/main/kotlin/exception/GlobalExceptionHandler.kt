package org.ecommerce.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    data class ErrorResponse(
        val error: String,
        val message: String?,
        val timestamp: Long = System.currentTimeMillis()
    )

    @ExceptionHandler(OrderProcessingException::class)
    fun handleOrderProcessingException(ex: OrderProcessingException): ResponseEntity<ErrorResponse> {
        logger.error("Order processing error: ${ex.message}", ex)
        val response = ErrorResponse(
            error = "ORDER_PROCESSING_FAILED",
            message = ex.message
        )
        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error: ${ex.message}", ex)
        val response = ErrorResponse(
            error = "INTERNAL_SERVER_ERROR",
            message = ex.message
        )
        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
