package com.spatel.scansign.core.ui.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Renders a composable in both light and dark themes in the preview pane.
 *
 * Usage:
 * ```
 * @ThemePreviews
 * @Composable
 * fun MyComposablePreview() {
 *     ScanSignTheme { MyComposable() }
 * }
 * ```
 */
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ThemePreviews
