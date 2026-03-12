package com.fd.cnav.feature.history

import com.fd.cnav.data.model.Order

object HistoryContract {

    data class State(
        val inProgressOrders: List<Order> = emptyList(),
        val completedOrders: List<Order> = emptyList(),
        val isLoading: Boolean = false,
        val selectedTab: HistoryTab = HistoryTab.IN_PROGRESS
    )

    sealed class Intent {
        data class OnTabSelected(val tab: HistoryTab) : Intent()
        data class OnOrderClicked(val order: Order) : Intent()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class NavigateToDetail(val product: com.fd.cnav.data.model.Product) : Effect()
    }

    enum class HistoryTab(val label: String) {
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed")
    }
}
