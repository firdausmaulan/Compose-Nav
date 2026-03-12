package com.fd.cnav.data.repository

import com.fd.cnav.data.model.Order
import com.fd.cnav.data.model.OrderStatus

class OrderRepository(private val productRepository: ProductRepository = ProductRepository()) {

    fun getOrders(): List<Order> {
        val products = productRepository.getProducts()
        return listOf(
            Order(101, products[0], 1, OrderStatus.IN_PROGRESS, "2025-03-10"),
            Order(102, products[3], 2, OrderStatus.IN_PROGRESS, "2025-03-11"),
            Order(103, products[5], 1, OrderStatus.IN_PROGRESS, "2025-03-12"),
            Order(104, products[1], 1, OrderStatus.COMPLETED, "2025-02-20"),
            Order(105, products[2], 3, OrderStatus.COMPLETED, "2025-02-25"),
            Order(106, products[6], 1, OrderStatus.COMPLETED, "2025-03-01"),
            Order(107, products[7], 1, OrderStatus.COMPLETED, "2025-03-05"),
            Order(108, products[9], 2, OrderStatus.IN_PROGRESS, "2025-03-12")
        )
    }

    fun getOrdersByStatus(status: OrderStatus): List<Order> =
        getOrders().filter { it.status == status }
}
