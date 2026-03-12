package com.fd.cnav.feature.productlist

import com.fd.cnav.data.model.Product

object ProductListContract {

    data class State(
        val products: List<Product> = emptyList(),
        val isLoading: Boolean = false
    )

    sealed class Intent {
        data class OnProductClicked(val product: Product) : Intent()
        data object OnHistoryClicked : Intent()
    }

    sealed class Effect {
        data class NavigateToDetail(val productId: Int) : Effect()
        data object NavigateToHistory : Effect()
    }
}
