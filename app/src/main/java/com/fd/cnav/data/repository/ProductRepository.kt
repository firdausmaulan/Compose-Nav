package com.fd.cnav.data.repository

import com.fd.cnav.data.model.Product
import kotlinx.coroutines.delay

class ProductRepository {

    fun getProducts(): List<Product> = listOf(
        Product(1, "Wireless Headphones", 89.99, "Electronics", "Premium noise-cancelling over-ear headphones with 30h battery life.", 4.5f),
        Product(2, "Running Shoes", 129.99, "Sports", "Lightweight and breathable shoes for everyday runners.", 4.7f),
        Product(3, "Stainless Water Bottle", 24.99, "Lifestyle", "Double-walled insulated bottle keeps drinks cold 24h or hot 12h.", 4.8f),
        Product(4, "Mechanical Keyboard", 149.99, "Electronics", "Compact TKL keyboard with tactile brown switches and RGB backlight.", 4.6f),
        Product(5, "Yoga Mat", 39.99, "Sports", "Non-slip eco-friendly mat with alignment lines, 6mm thick.", 4.4f),
        Product(6, "Smart Watch", 199.99, "Electronics", "Tracks fitness, sleep, and notifications. GPS + heart rate included.", 4.3f),
        Product(7, "Coffee Grinder", 59.99, "Kitchen", "Burr grinder with 18 grind settings for espresso to French press.", 4.5f),
        Product(8, "Backpack 30L", 79.99, "Travel", "Durable weatherproof backpack with laptop compartment and USB port.", 4.6f),
        Product(9, "Desk Lamp LED", 34.99, "Home", "Touch-dimming lamp with 3 color modes and USB charging port.", 4.7f),
        Product(10, "Wireless Charger", 29.99, "Electronics", "Fast 15W Qi wireless charging pad compatible with all Qi devices.", 4.2f)
    )

    fun getProductById(id: Int): Product? = getProducts().find { it.id == id }

    /** Simulates a network fetch. Throws [Exception] to demonstrate the fallback strategy. */
    suspend fun fetchProductById(id: Int): Product {
        delay(800) // simulate network latency
        // Toggle the line below to simulate a network error:
        // throw Exception("Network unavailable")
        return getProducts().find { it.id == id }
            ?: throw NoSuchElementException("Product $id not found on server")
    }
}
