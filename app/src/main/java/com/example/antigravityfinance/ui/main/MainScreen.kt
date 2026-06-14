package com.example.antigravityfinance.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravityfinance.data.model.ThemeType
import com.example.antigravityfinance.theme.AntigravityFinanceTheme
import com.example.antigravityfinance.ui.components.PinKeyboardGate
import com.example.antigravityfinance.ui.screens.*
import com.example.antigravityfinance.ui.viewmodel.FinanceViewModel

enum class MainTab(val displayName: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Rounded.Dashboard),
    TRANSACTIONS("Wallet", Icons.Rounded.ReceiptLong),
    ASSISTANT("AI Chat", Icons.Rounded.AutoAwesome),
    FINANCIAL_TOOLS("Financial Tools", Icons.Rounded.Widgets),
    SETTINGS("Settings", Icons.Rounded.Settings)
}

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FinanceViewModel = viewModel()
) {
    val themeType by viewModel.themeType.collectAsState()
    val isDarkPref by viewModel.isDarkMode.collectAsState()
    val customAccent by viewModel.customAccent.collectAsState()

    // Render themed container
    AntigravityFinanceTheme(
        themeType = themeType,
        darkTheme = isDarkPref ?: androidx.compose.foundation.isSystemInDarkTheme(),
        customAccent = customAccent
    ) {
        val isRegistered by viewModel.isRegistered.collectAsState()
        val isPinSet by viewModel.isPinSet.collectAsState()
        val isAuthRequired by viewModel.isAuthRequired.collectAsState()
        val pinError by viewModel.pinError.collectAsState()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (!isRegistered) {
                OnboardingScreen(viewModel = viewModel)
            } else if (isPinSet && isAuthRequired) {
                // Secure Authentication Lock Gate
                PinKeyboardGate(
                    isPinSet = true,
                    pinError = pinError,
                    onPinSubmitted = { pin -> viewModel.submitPin(pin) },
                    onFingerprintClick = { viewModel.submitPin("1234") } // Mock biometric bypass
                )
            } else {
                // Main Application Workspace
                MainWorkspace(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWorkspace(viewModel: FinanceViewModel) {
    var activeTab by remember { mutableStateOf(MainTab.DASHBOARD) }
    val visibleTabs = remember { MainTab.values() }
    val language by viewModel.language.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = androidx.compose.ui.Modifier.drawBehind {
                    drawLine(
                        color = androidx.compose.ui.graphics.Color(0xFFE8EAED),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            ) {
                visibleTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.displayName) },
                        label = { Text(tab.displayName.translate(language), fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = activeTab,
            modifier = Modifier.padding(innerPadding),
            label = "tab_fade"
        ) { tab ->
            when (tab) {
                MainTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                MainTab.TRANSACTIONS -> TransactionsScreen(viewModel = viewModel)
                MainTab.ASSISTANT -> AssistantScreen(viewModel = viewModel)
                MainTab.FINANCIAL_TOOLS -> FinancialToolsScreen(viewModel = viewModel)
                MainTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
