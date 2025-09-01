package service

import org.ecommerce.service.OrderService
import org.junit.jupiter.api.assertThrows

import inventory.Inventory
import org.ecommerce.dto.ShopifyLineItemDTO
import org.ecommerce.dto.ShopifyOrderWebhookDTO
import org.ecommerce.entity.OrderEntity
import org.ecommerce.enum.OrderStatus
import org.ecommerce.event.InventoryUpdatedEvent
import org.ecommerce.event.OrderAllocatedEvent
import org.ecommerce.exception.OrderProcessingException
import org.ecommerce.mapper.toEntity
import org.ecommerce.repository.OrderRepository
import org.ecommerce.grpc.InventoryGrpcClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.kafka.core.KafkaTemplate
import java.time.ZonedDateTime

import org.junit.jupiter.api.Assertions.assertEquals

class OrderServiceTest {

    private lateinit var orderRepository: OrderRepository
    private lateinit var inventoryGrpcClient: InventoryGrpcClient
    private lateinit var kafkaTemplate: KafkaTemplate<String, Any>
    private lateinit var orderService: OrderService

    @BeforeEach
    fun setup() {
        orderRepository = mock(OrderRepository::class.java)
        inventoryGrpcClient = mock(InventoryGrpcClient::class.java)
        kafkaTemplate = mock(KafkaTemplate::class.java) as KafkaTemplate<String, Any>
        orderService = OrderService(orderRepository, inventoryGrpcClient, kafkaTemplate)
    }

    private fun createMockOrderDto(): ShopifyOrderWebhookDTO {
        return ShopifyOrderWebhookDTO(
            id = 1001,
            name = "#1001",
            createdAt = ZonedDateTime.now(),
            currency = "THB",
            totalPrice = "1500.0",
            lineItems = listOf(
                ShopifyLineItemDTO(
                    id = 1,
                    productId = 101,
                    title = "T-shirt",
                    quantity = 2,
                    sku = "TSHIRT-001",
                    vendor = "BrandA",
                    fulfillmentStatus = null
                )
            ),
            fulfillmentStatus = null,
            financialStatus = "paid",
            locationId = 12345L
        )
    }

    @Test
    fun `processShopifyOrder should allocate stock and publish events successfully`() {
        val dto = createMockOrderDto()
        val entity = dto.toEntity()

        // Mock repository and gRPC responses
        `when`(orderRepository.existsByExternalId(dto.id.toString())).thenReturn(false)

        val grpcResponse = Inventory.AllocateStockResponse.newBuilder()
            .setSuccess(true)
            .setMessage("Allocated")
            .putAvailableQuantity("TSHIRT-001", 10)
            .build()

        `when`(inventoryGrpcClient.allocateStock(dto.lineItems)).thenReturn(grpcResponse)
        `when`(orderRepository.save(any(OrderEntity::class.java))).thenReturn(entity)

        // Call service
        orderService.processShopifyOrder(dto, webhookId = null)

        // Verify order status saved
        val savedCaptor = ArgumentCaptor.forClass(OrderEntity::class.java)
        verify(orderRepository, atLeastOnce()).save(savedCaptor.capture())
        assertEquals(OrderStatus.ALLOCATED, savedCaptor.value.status)

        // Verify inventory update event sent
        val inventoryCaptor = ArgumentCaptor.forClass(InventoryUpdatedEvent::class.java)
        verify(kafkaTemplate).send(eq("inventory-updates"), inventoryCaptor.capture())
        assertEquals("TSHIRT-001", inventoryCaptor.value.sku)
        assertEquals(10, inventoryCaptor.value.availableQuantity)

        // Verify order allocated event sent
        val orderAllocatedCaptor = ArgumentCaptor.forClass(OrderAllocatedEvent::class.java)
        verify(kafkaTemplate).send(eq("orders-allocated"), orderAllocatedCaptor.capture())
        assertEquals("1001", orderAllocatedCaptor.value.orderId)
        assertEquals(1, orderAllocatedCaptor.value.lineItems.size)
        assertEquals("TSHIRT-001", orderAllocatedCaptor.value.lineItems.first().sku)
    }

    @Test
    fun `processShopifyOrder should cancel order if inventory allocation fails`() {
        val dto = createMockOrderDto()
        `when`(orderRepository.existsByExternalId(dto.id.toString())).thenReturn(false)

        val grpcResponse = Inventory.AllocateStockResponse.newBuilder()
            .setSuccess(false)
            .setMessage("Insufficient stock")
            .build()

        `when`(inventoryGrpcClient.allocateStock(dto.lineItems)).thenReturn(grpcResponse)

        orderService.processShopifyOrder(dto, webhookId = null)

        val savedCaptor = ArgumentCaptor.forClass(OrderEntity::class.java)
        verify(orderRepository).save(savedCaptor.capture())
        assertEquals(OrderStatus.CANCELLED, savedCaptor.value.status)
    }

    @Test
    fun `processShopifyOrder should throw exception when gRPC fails`() {
        val dto = createMockOrderDto()
        `when`(orderRepository.existsByExternalId(dto.id.toString())).thenReturn(false)

        `when`(inventoryGrpcClient.allocateStock(dto.lineItems)).thenThrow(RuntimeException("gRPC down"))

        assertThrows<OrderProcessingException> {
            orderService.processShopifyOrder(dto, webhookId = null)
        }
    }

    @Test
    fun `processShopifyOrder should skip already processed order`() {
        val dto = createMockOrderDto()
        `when`(orderRepository.existsByExternalId(dto.id.toString())).thenReturn(true)

        orderService.processShopifyOrder(dto, webhookId = null)

        verify(orderRepository, never()).save(any(OrderEntity::class.java))
        verifyNoInteractions(inventoryGrpcClient)
    }
}
