package com.spatel.scansign.core.datastore

data class UserPreferences(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val scanQuality: ScanQuality = ScanQuality.STANDARD,
)
