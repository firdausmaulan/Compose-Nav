package com.fd.cnav.data.model

data class Order(
    val id: Int,
    val product: Product,
    val quantity: Int,
    val status: OrderStatus,
    val orderDate: String
)

enum class OrderStatus {
    IN_PROGRESS,
    COMPLETED
}
