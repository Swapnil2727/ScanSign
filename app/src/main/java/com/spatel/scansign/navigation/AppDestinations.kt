package com.spatel.scansign.navigation

import kotlinx.serialization.Serializable

// Top-level bottom-nav destinations
@Serializable data object DocumentsRoute
@Serializable data object ScannerRoute
@Serializable data object SignerRoute
@Serializable data object SettingsRoute

// Detail destinations pushed on top of a root
@Serializable data class DocumentDetailRoute(val documentId: String)
