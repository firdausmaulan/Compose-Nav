package com.fd.cnav.feature.productdetail

import com.fd.cnav.data.model.Product

object ProductDetailContract {

    data class State(
        val product: Product? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    sealed class Intent {
        data object OnBackClicked : Intent()
        data object OnAddToCartClicked : Intent()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data object NavigateToHistory : Effect()
    }
}
