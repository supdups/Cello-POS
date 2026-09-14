package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.checkout.CheckoutScreen
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.order.OrderScreen
import com.example.myapplication.ui.order.OrderViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * The whole app flow is driven by a single screen enum, kept in MainActivity.
 * No navigation library — this is intentionally the simplest thing that works
 * for three screens. Revisit with Navigation-Compose if the flow grows a lot.
 */
enum class AppScreen { LOGIN, ORDER, CHECKOUT }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CelloPosApp()
            }
        }
    }
}

@Composable
fun CelloPosApp() {
    var screen by rememberSaveable { mutableStateOf(AppScreen.LOGIN) }
    var checkoutTotal by rememberSaveable { mutableDoubleStateOf(0.0) }
    val orderViewModel: OrderViewModel = viewModel()

    when (screen) {
        AppScreen.LOGIN -> LoginScreen(
            onLoginSuccess = { screen = AppScreen.ORDER }
        )

        AppScreen.ORDER -> OrderScreen(
            viewModel = orderViewModel,
            onCheckout = { total ->
                checkoutTotal = total
                screen = AppScreen.CHECKOUT
            }
        )

        AppScreen.CHECKOUT -> CheckoutScreen(
            total = checkoutTotal,
            onOrderComplete = {
                orderViewModel.clearCart()
                screen = AppScreen.ORDER
            }
        )
    }
}
