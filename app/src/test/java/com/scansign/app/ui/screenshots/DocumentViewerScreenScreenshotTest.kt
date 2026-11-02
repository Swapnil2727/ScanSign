package com.scansign.app.ui.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.scansign.app.ui.documents.DocumentViewerContentPreview
import com.scansign.app.ui.documents.DocumentViewerSinglePagePreview
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class DocumentViewerScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun document_viewer_content() {
        composeTestRule.setContent {
            DocumentViewerContentPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun document_viewer_single_page() {
        composeTestRule.setContent {
            DocumentViewerSinglePagePreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
