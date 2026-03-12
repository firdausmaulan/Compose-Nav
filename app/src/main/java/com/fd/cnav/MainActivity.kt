package com.fd.cnav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.fd.cnav.feature.history.HistoryScreen
import com.fd.cnav.feature.productdetail.ProductDetailArgs
import com.fd.cnav.feature.productdetail.ProductDetailScreen
import com.fd.cnav.feature.productdetail.ProductSource
import com.fd.cnav.feature.productlist.ProductListScreen
import com.fd.cnav.navigation.AppKey
import com.fd.cnav.ui.theme.ComposeNavTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeNavTheme {
                val backStack = remember { mutableStateListOf<AppKey>(AppKey.ProductList) }

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entry<AppKey.ProductList> {
                            ProductListScreen(
                                onNavigateToDetail = { product ->
                                    backStack.add(
                                        AppKey.ProductDetail(
                                            ProductDetailArgs(product, ProductSource.LIST)
                                        )
                                    )
                                },
                                onNavigateToHistory = {
                                    backStack.add(AppKey.History)
                                }
                            )
                        }
                        entry<AppKey.History> {
                            HistoryScreen(
                                onNavigateBack = { backStack.removeLastOrNull() },
                                onNavigateToDetail = { product ->
                                    backStack.add(
                                        AppKey.ProductDetail(
                                            ProductDetailArgs(product, ProductSource.HISTORY)
                                        )
                                    )
                                }
                            )
                        }
                        entry<AppKey.ProductDetail> { key ->
                            ProductDetailScreen(
                                args = key.args,
                                onNavigateBack = { backStack.removeLastOrNull() },
                                onNavigateToHistory = {
                                    backStack.add(AppKey.History)
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}