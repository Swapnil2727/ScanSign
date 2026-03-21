package com.spatel.scansign.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Top-level bottom-nav destinations
@Serializable data object DocumentsRoute : NavKey
@Serializable data object ScannerRoute : NavKey
@Serializable data object SignerRoute : NavKey
@Serializable data object SettingsRoute : NavKey

// Detail destinations pushed on top of a root
@Serializable data object ScanConfirmRoute : NavKey
@Serializable data class DocumentDetailRoute(val documentId: String) : NavKey
