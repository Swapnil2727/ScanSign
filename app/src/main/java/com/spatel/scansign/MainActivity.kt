package com.spatel.scansign

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.spatel.scansign.core.ui.theme.ScanSignTheme
import com.spatel.scansign.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScanSignTheme {
                AppNavigation()
            }
        }
    }
}
