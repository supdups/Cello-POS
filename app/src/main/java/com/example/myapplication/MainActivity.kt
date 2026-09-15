package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.checkout.CheckoutScreen
import com.example.myapplication.ui.checkout.ReceiptLine
import com.example.myapplication.ui.items.ItemsScreen
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.order.OrderScreen
import com.example.myapplication.ui.order.OrderViewModel
import com.example.myapplication.ui.settings.PrinterSettingsScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.users.UsersScreen

/**
 * The whole app flow is driven by a single screen enum, kept in MainActivity.
 * No navigation library — this is intentionally the simplest thing that works
 * for a handful of screens. Revisit with Navigation-Compose if the flow grows a lot.
 *
 * Login itself isn't in this enum: it's gated separately at the top of CelloPosApp
 * based on LoginViewModel's session state, so logging out always drops you back to
 * the login screen no matter which of these you were on.
 */
enum class AppScreen { ORDER, CHECKOUT, USERS, ITEMS, PRINTER_SETTINGS }

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
    // IMPORTANT: LoginViewModel.Factory must be given the Application context, not the
    // Activity context — LocalContext.current inside a Composable IS the Activity context,
    // so we explicitly grab applicationContext here. This matters for session persistence:
    // using an Activity context anywhere Room/DataStore is built can subtly cause the
    // underlying files to behave inconsistently across Activity recreation.
    val appContext = LocalContext.current.applicationContext

    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.Factory(appContext)
    )
    val loginState by loginViewModel.uiState.collectAsState()

    var screen by rememberSaveable { mutableStateOf(AppScreen.ORDER) }
    var checkoutTotal by rememberSaveable { mutableDoubleStateOf(0.0) }
    var checkoutLines by remember { mutableStateOf(listOf<ReceiptLine>()) }
    val orderViewModel: OrderViewModel = viewModel(factory = OrderViewModel.Factory(appContext))

    LaunchedEffect(loginState.isLoggedIn) {
        if (!loginState.isLoggedIn) {
            screen = AppScreen.ORDER
        }
    }

    when {
        // Briefly shown on cold start while we check DataStore for a saved session.
        loginState.isCheckingSession -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        !loginState.isLoggedIn -> {
            LoginScreen(viewModel = loginViewModel)
        }

        else -> when (screen) {
            AppScreen.ORDER -> OrderScreen(
                viewModel = orderViewModel,
                isAdmin = loginState.isAdmin,
                onManageUsers = { screen = AppScreen.USERS },
                onManageItems = { screen = AppScreen.ITEMS },
                onPrinterSettings = { screen = AppScreen.PRINTER_SETTINGS },
                onLogout = { loginViewModel.logout() },
                onCheckout = { total, lines ->
                    checkoutTotal = total
                    checkoutLines = lines
                    screen = AppScreen.CHECKOUT
                }
            )

            AppScreen.CHECKOUT -> CheckoutScreen(
                total = checkoutTotal,
                lines = checkoutLines,
                onOrderComplete = {
                    orderViewModel.clearCart()
                    screen = AppScreen.ORDER
                }
            )

            AppScreen.USERS -> UsersScreen(
                onBack = { screen = AppScreen.ORDER }
            )

            AppScreen.ITEMS -> ItemsScreen(
                onBack = { screen = AppScreen.ORDER }
            )

            AppScreen.PRINTER_SETTINGS -> PrinterSettingsScreen(
                onBack = { screen = AppScreen.ORDER }
            )
        }
    }
}
