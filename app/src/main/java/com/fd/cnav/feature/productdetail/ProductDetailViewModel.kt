package com.fd.cnav.feature.productdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fd.cnav.data.repository.ProductRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val args: ProductDetailArgs,
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(
        ProductDetailContract.State(product = args.product, isRefreshing = true)
    )
    val state: StateFlow<ProductDetailContract.State> = _state.asStateFlow()

    private val _effect = Channel<ProductDetailContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        fetchFreshData()
    }

    fun onIntent(intent: ProductDetailContract.Intent) {
        when (intent) {
            ProductDetailContract.Intent.OnBackClicked -> {
                viewModelScope.launch {
                    _effect.send(ProductDetailContract.Effect.NavigateBack)
                }
            }
            ProductDetailContract.Intent.OnAddToCartClicked -> {
                viewModelScope.launch {
                    _effect.send(ProductDetailContract.Effect.NavigateToHistory)
                }
            }
            ProductDetailContract.Intent.OnRetry -> fetchFreshData()
        }
    }

    private fun fetchFreshData() {
        _state.update { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            try {
                val fresh = repository.fetchProductById(args.product.id)
                _state.update { it.copy(product = fresh, isFreshData = true, isRefreshing = false) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isFreshData = false,
                        isRefreshing = false,
                        error = "Couldn't refresh. Showing cached data."
                    )
                }
            }
        }
    }

    class Factory(private val args: ProductDetailArgs) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProductDetailViewModel(args) as T
    }
}
