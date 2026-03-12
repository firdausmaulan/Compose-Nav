package com.fd.cnav.navigation

import com.fd.cnav.feature.productdetail.ProductDetailArgs
import kotlinx.serialization.Serializable

sealed class AppKey {

    @Serializable
    data object ProductList : AppKey()

    @Serializable
    data object History : AppKey()

    @Serializable
    data class ProductDetail(val args: ProductDetailArgs) : AppKey()
}
