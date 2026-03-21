package com.spatel.scansign.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.spatel.scansign.ui.documents.DocumentsScreen
import com.spatel.scansign.ui.scanner.ScanConfirmScreen
import com.spatel.scansign.ui.scanner.ScannerScreen
import com.spatel.scansign.ui.scanner.ScannerViewModel
import com.spatel.scansign.ui.settings.SettingsScreen
import com.spatel.scansign.ui.signer.SignerScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavigation() {
    val backStack = remember { mutableStateListOf<Any>(DocumentsRoute) }
    val currentRoute = backStack.last()
    val scannerViewModel: ScannerViewModel = koinViewModel()

    val showBottomBar = currentRoute !is ScannerRoute && currentRoute !is ScanConfirmRoute

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onNavigateTo = { destination ->
                        // Clicking a tab replaces the entire back stack with that root destination.
                        // Detail screens (e.g. DocumentDetailRoute) push on top separately via
                        // callbacks passed into each screen.
                        backStack.clear()
                        backStack.add(destination)
                    },
                )
            }
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
            entryProvider = entryProvider {
                entry<DocumentsRoute> {
                    DocumentsScreen(
                        onScanClick = { backStack.add(ScannerRoute) },
                        onSignClick = { backStack.add(SignerRoute) },
                    )
                }
                entry<ScannerRoute> {
                    ScannerScreen(
                        onBack = { backStack.removeAt(backStack.lastIndex) },
                        onScanComplete = { backStack.add(ScanConfirmRoute) },
                        viewModel = scannerViewModel,
                    )
                }
                entry<ScanConfirmRoute> {
                    ScanConfirmScreen(
                        onDiscard = {
                            backStack.removeAt(backStack.lastIndex) // ScanConfirmRoute
                            backStack.removeAt(backStack.lastIndex) // ScannerRoute
                        },
                        onSaveConfirmed = {
                            backStack.clear()
                            backStack.add(DocumentsRoute)
                        },
                        viewModel = scannerViewModel,
                    )
                }
                entry<SignerRoute> {
                    SignerScreen()
                }
                entry<SettingsRoute> {
                    SettingsScreen()
                }
                entry<DocumentDetailRoute> { key ->
                    // Placeholder — wired to real screen in Week 6
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Document ${key.documentId}")
                    }
                }
            },
        )
    }
}

// ── Bottom bar ────────────────────────────────────────────────────────────────

private data class BottomNavItem(
    val route: Any,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(DocumentsRoute, "Docs", Icons.Filled.FolderOpen, Icons.Outlined.FolderOpen),
    BottomNavItem(SignerRoute, "Sign", Icons.Filled.Draw, Icons.Outlined.Draw),
    BottomNavItem(SettingsRoute, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
private fun AppBottomBar(
    currentRoute: Any,
    onNavigateTo: (Any) -> Unit,
) {
    NavigationBar {
        // Docs
        bottomNavItems.take(1).forEach { item ->
            NavigationBarItem(
                selected = currentRoute::class == item.route::class,
                onClick = { onNavigateTo(item.route) },
                icon = {
                    Icon(
                        if (currentRoute::class == item.route::class) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = { Text(item.label) },
            )
        }

        // Scan — centre FAB-style button
        NavigationBarItem(
            selected = currentRoute is ScannerRoute,
            onClick = { onNavigateTo(ScannerRoute) },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = if (currentRoute is ScannerRoute)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            if (currentRoute is ScannerRoute) Icons.Filled.CameraAlt else Icons.Outlined.CameraAlt,
                            contentDescription = "Scan",
                            tint = if (currentRoute is ScannerRoute)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            },
            label = { Text("Scan") },
        )

        // Sign + Settings
        bottomNavItems.drop(1).forEach { item ->
            NavigationBarItem(
                selected = currentRoute::class == item.route::class,
                onClick = { onNavigateTo(item.route) },
                icon = {
                    Icon(
                        if (currentRoute::class == item.route::class) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = { Text(item.label) },
            )
        }
    }
}
