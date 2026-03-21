package com.spatel.scansign

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spatel.scansign.core.datastore.AppTheme
import com.spatel.scansign.core.ui.theme.ScanSignTheme
import com.spatel.scansign.navigation.AppNavigation
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = koinViewModel()
            val userPreferences by mainViewModel.userPreferences.collectAsStateWithLifecycle()

            val darkTheme = when (userPreferences.appTheme) {
                AppTheme.SYSTEM -> isSystemInDarkTheme()
                AppTheme.LIGHT  -> false
                AppTheme.DARK   -> true
            }

            ScanSignTheme(darkTheme = darkTheme) {
                AppNavigation()
            }
        }
    }
}
