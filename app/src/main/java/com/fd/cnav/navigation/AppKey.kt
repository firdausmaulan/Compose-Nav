package com.fd.cnav.navigation

import kotlinx.serialization.Serializable

sealed class AppKey {

    @Serializable
    data object ProductList : AppKey()

    @Serializable
    data object History : AppKey()

    @Serializable
    data class ProductDetail(val productId: Int, val fromHistory: Boolean = false) : AppKey()
}
