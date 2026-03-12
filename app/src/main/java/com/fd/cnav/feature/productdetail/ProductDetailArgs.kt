package com.fd.cnav.feature.productdetail

import com.fd.cnav.data.model.Product
import kotlinx.serialization.Serializable

@Serializable
data class ProductDetailArgs(
    val product: Product,
    val source: ProductSource = ProductSource.LIST
)

@Serializable
enum class ProductSource {
    LIST,
    HISTORY
}
