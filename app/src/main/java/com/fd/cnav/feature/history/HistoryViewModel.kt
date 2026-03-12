package com.fd.cnav.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fd.cnav.data.model.OrderStatus
import com.fd.cnav.data.repository.OrderRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: OrderRepository = OrderRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryContract.State(isLoading = true))
    val state: StateFlow<HistoryContract.State> = _state.asStateFlow()

    private val _effect = Channel<HistoryContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadOrders()
    }

    fun onIntent(intent: HistoryContract.Intent) {
        when (intent) {
            is HistoryContract.Intent.OnTabSelected -> {
                _state.update { it.copy(selectedTab = intent.tab) }
            }
            is HistoryContract.Intent.OnOrderClicked -> {
                viewModelScope.launch {
                    _effect.send(HistoryContract.Effect.NavigateToDetail(intent.order.product))
                }
            }
        }
    }

    private fun loadOrders() {
        viewModelScope.launch {
            val inProgress = repository.getOrdersByStatus(OrderStatus.IN_PROGRESS)
            val completed = repository.getOrdersByStatus(OrderStatus.COMPLETED)
            _state.update {
                it.copy(
                    inProgressOrders = inProgress,
                    completedOrders = completed,
                    isLoading = false
                )
            }
        }
    }
}
