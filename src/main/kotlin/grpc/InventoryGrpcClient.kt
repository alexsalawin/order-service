package org.ecommerce.grpc

import inventory.Inventory
import inventory.InventoryServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.ecommerce.dto.ShopifyLineItemDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class InventoryGrpcClient(
    private val grpcHost: String = "localhost",
    private val grpcPort: Int = 6565
) {
    private val logger = LoggerFactory.getLogger(InventoryGrpcClient::class.java)

    private val channel: ManagedChannel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
        .usePlaintext()
        .build()

    private val stub: InventoryServiceGrpc.InventoryServiceBlockingStub =
        InventoryServiceGrpc.newBlockingStub(channel)

    /**
     * Allocates stock for the given Shopify line items.
     * Only uses SKU and quantity; variants are out of scope.
     */
    fun allocateStock(lineItems: List<ShopifyLineItemDTO>): Inventory.AllocateStockResponse {
        val request = Inventory.AllocateStockRequest.newBuilder()
            .addAllLineItems(
                lineItems.map { item ->
                    Inventory.LineItem.newBuilder()
                        .setSku(item.sku ?: item.productId.toString())
                        .setQuantity(item.quantity)
                        .build()
                }
            )
            .build()

        repeat(3) { attempt ->
            try {
                return stub.allocateStock(request)
            } catch (ex: Exception) {
                logger.warn("gRPC attempt ${attempt + 1} failed: ${ex.message}")
                Thread.sleep(1000L * (attempt + 1)) // simple exponential backoff
            }
        }

        throw RuntimeException("Failed to allocate stock via InventoryService after 3 attempts")
    }

    fun shutdown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}
