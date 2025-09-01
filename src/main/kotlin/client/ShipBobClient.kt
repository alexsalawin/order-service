package org.ecommerce.client

import org.ecommerce.dto.ShipBobInventoryListResponse
import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.CircuitBreaker
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


@Component
class ShipBobClient(
    private val webClient: WebClient
) {
    private val log = LoggerFactory.getLogger(ShipBobClient::class.java)

    @Retry(name = "shipbobRetry", fallbackMethod = "inventoryFallback")
    @CircuitBreaker(name = "shipbobCircuit", fallbackMethod = "inventoryFallback")
    fun fetchInventoryForSku(sku: String): Mono<ShipBobInventoryListResponse> {
        log.info("Fetching inventory from ShipBob for SKU: {}", sku)
        return webClient
            .get()
            .uri { builder ->
                builder.path("/2.0/inventory")
                    .queryParam("search", sku)
                    .build()
            }
            .retrieve()
            .bodyToMono(ShipBobInventoryListResponse::class.java)
    }

    fun inventoryFallback(sku: String, ex: Throwable): Mono<ShipBobInventoryListResponse> {
        log.error("ShipBob API unreachable. Fallback for SKU: {}. Error: {}", sku, ex.message)
        return Mono.empty()
    }

}