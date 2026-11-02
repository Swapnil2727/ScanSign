package com.spatel.scansign.ui.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.spatel.scansign.ui.settings.SettingsHighQualityDarkPreview
import com.spatel.scansign.ui.settings.SettingsPreview
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class SettingsScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settings_preview_light() {
        composeTestRule.setContent {
            SettingsPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun settings_preview_dark() {
        composeTestRule.setContent {
            SettingsHighQualityDarkPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
