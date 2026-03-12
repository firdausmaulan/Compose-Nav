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
    private val productId: Int,
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ProductDetailContract.State(isLoading = true))
    val state: StateFlow<ProductDetailContract.State> = _state.asStateFlow()

    private val _effect = Channel<ProductDetailContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadProduct()
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
        }
    }

    private fun loadProduct() {
        viewModelScope.launch {
            val product = repository.getProductById(productId)
            _state.update {
                it.copy(
                    product = product,
                    isLoading = false,
                    error = if (product == null) "Product not found" else null
                )
            }
        }
    }

    class Factory(private val productId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProductDetailViewModel(productId) as T
    }
}
