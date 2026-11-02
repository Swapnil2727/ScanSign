package com.scansign.app.ui.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.scansign.app.ui.scanner.ScannerErrorDialogPreview
import com.scansign.app.ui.scanner.ScannerScreenPreview
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class ScannerScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun scanner_screen_loading() {
        composeTestRule.setContent {
            ScannerScreenPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun scanner_error_dialog() {
        composeTestRule.setContent {
            ScannerErrorDialogPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
