package com.spatel.scansign.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.spatel.scansign.ui.documents.DocumentDetailScreen
import com.spatel.scansign.ui.documents.DocumentDetailViewModel
import com.spatel.scansign.ui.documents.DocumentSigningScreen
import com.spatel.scansign.ui.documents.DocumentSigningViewModel
import com.spatel.scansign.ui.documents.DocumentsScreen
import com.spatel.scansign.ui.documents.DocumentsViewModel
import com.spatel.scansign.ui.scanner.GalleryImportScreen
import com.spatel.scansign.ui.scanner.ScanConfirmScreen
import com.spatel.scansign.ui.scanner.ScannerScreen
import com.spatel.scansign.ui.scanner.ScannerViewModel
import com.spatel.scansign.ui.settings.SettingsScreen
import com.spatel.scansign.ui.signer.SignerScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavigation() {
    val backStack = remember { mutableStateListOf<Any>(DocumentsRoute) }
    val currentRoute = backStack.last()
    val scannerViewModel: ScannerViewModel = koinViewModel()
    val documentsViewModel: DocumentsViewModel = koinViewModel()

    val showBottomBar = currentRoute !is ScannerRoute
        && currentRoute !is ScanConfirmRoute
        && currentRoute !is GalleryImportRoute
        && currentRoute !is DocumentSigningRoute

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onNavigateTo = { destination ->
                        backStack.clear()
                        // Transient screens always keep DocumentsRoute as their parent
                        // so pressing back returns to the document list.
                        if (destination is ScannerRoute || destination is GalleryImportRoute) {
                            backStack.add(DocumentsRoute)
                        }
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
                        onGalleryClick = { backStack.add(GalleryImportRoute) },
                        onDocumentClick = { id -> backStack.add(DocumentDetailRoute(id)) },
                        viewModel = documentsViewModel,
                    )
                }
                entry<ScannerRoute> {
                    ScannerScreen(
                        onBack = { backStack.removeAt(backStack.lastIndex) },
                        onScanComplete = { backStack.add(ScanConfirmRoute) },
                        viewModel = scannerViewModel,
                    )
                }
                entry<GalleryImportRoute> {
                    GalleryImportScreen(
                        onBack = { backStack.removeAt(backStack.lastIndex) },
                        onImportComplete = { backStack.add(ScanConfirmRoute) },
                        viewModel = scannerViewModel,
                    )
                }
                entry<ScanConfirmRoute> {
                    ScanConfirmScreen(
                        onDiscard = {
                            backStack.removeAt(backStack.lastIndex) // ScanConfirmRoute
                            backStack.removeAt(backStack.lastIndex) // ScannerRoute or GalleryImportRoute
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
                    val detailViewModel: DocumentDetailViewModel = koinViewModel(
                        key = key.documentId,
                        parameters = { parametersOf(key.documentId) },
                    )
                    DocumentDetailScreen(
                        onBack = { backStack.removeAt(backStack.lastIndex) },
                        onDocumentDeleted = { backStack.removeAt(backStack.lastIndex) },
                        onSignClick = { backStack.add(DocumentSigningRoute(key.documentId)) },
                        viewModel = detailViewModel,
                    )
                }
                entry<DocumentSigningRoute> { key ->
                    val signingViewModel: DocumentSigningViewModel = koinViewModel(
                        key = key.documentId,
                        parameters = { parametersOf(key.documentId) },
                    )
                    DocumentSigningScreen(
                        onBack   = { backStack.removeAt(backStack.lastIndex) },
                        onSigned = { backStack.removeAt(backStack.lastIndex) },
                        viewModel = signingViewModel,
                    )
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
    BottomNavItem(ScannerRoute, "Scan", Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt),
    BottomNavItem(SignerRoute, "Sign", Icons.Filled.Draw, Icons.Outlined.Draw),
    BottomNavItem(SettingsRoute, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
private fun AppBottomBar(
    currentRoute: Any,
    onNavigateTo: (Any) -> Unit,
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
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
