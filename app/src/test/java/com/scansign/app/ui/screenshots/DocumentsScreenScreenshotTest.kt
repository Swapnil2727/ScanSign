package com.scansign.app.ui.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.scansign.app.ui.documents.DocumentsEmptyPreview
import com.scansign.app.ui.documents.DocumentsMorningPreview
import com.scansign.app.ui.documents.DocumentsSearchActivePreview
import com.scansign.app.ui.documents.DocumentsSearchNoResultsPreview
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class DocumentsScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun documents_morning_preview() {
        composeTestRule.setContent {
            DocumentsMorningPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun documents_empty_preview() {
        composeTestRule.setContent {
            DocumentsEmptyPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun documents_search_active_preview() {
        composeTestRule.setContent {
            DocumentsSearchActivePreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun documents_search_no_results_preview() {
        composeTestRule.setContent {
            DocumentsSearchNoResultsPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
