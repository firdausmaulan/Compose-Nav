package com.fd.cnav.feature.productlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fd.cnav.data.repository.ProductRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductListViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ProductListContract.State(isLoading = true))
    val state: StateFlow<ProductListContract.State> = _state.asStateFlow()

    private val _effect = Channel<ProductListContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadProducts()
    }

    fun onIntent(intent: ProductListContract.Intent) {
        when (intent) {
            is ProductListContract.Intent.OnProductClicked -> {
                viewModelScope.launch {
                    _effect.send(ProductListContract.Effect.NavigateToDetail(intent.product))
                }
            }
            ProductListContract.Intent.OnHistoryClicked -> {
                viewModelScope.launch {
                    _effect.send(ProductListContract.Effect.NavigateToHistory)
                }
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            val products = repository.getProducts()
            _state.update { it.copy(products = products, isLoading = false) }
        }
    }
}
