package com.spatel.scansign.ui.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.spatel.scansign.ui.signer.DrawTabPreview
import com.spatel.scansign.ui.signer.ImageTabEmptyPreview
import com.spatel.scansign.ui.signer.SignerScreenPreview
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class SignerScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun signer_screen_preview() {
        composeTestRule.setContent {
            SignerScreenPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun signer_draw_tab_preview() {
        composeTestRule.setContent {
            DrawTabPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun signer_image_tab_empty_preview() {
        composeTestRule.setContent {
            ImageTabEmptyPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
