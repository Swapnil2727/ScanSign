package com.spatel.scansign.ui.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.spatel.scansign.ui.scanner.GalleryImportErrorDialogPreview
import com.spatel.scansign.ui.scanner.GalleryImportLoadingPreview
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class GalleryImportScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gallery_import_loading() {
        composeTestRule.setContent {
            GalleryImportLoadingPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun gallery_import_error_dialog() {
        composeTestRule.setContent {
            GalleryImportErrorDialogPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
