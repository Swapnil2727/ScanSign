package com.scansign.app.ui.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.scansign.app.ui.documents.DocumentDetailContentPreview
import com.scansign.app.ui.documents.DocumentDetailLoadingPreview
import com.scansign.app.ui.documents.MetadataCardPreview
import com.scansign.app.ui.documents.RenameBottomSheetPreview
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class DocumentDetailScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun document_detail_content() {
        composeTestRule.setContent {
            DocumentDetailContentPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun document_detail_loading() {
        composeTestRule.setContent {
            DocumentDetailLoadingPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun document_detail_metadata_card() {
        composeTestRule.setContent {
            MetadataCardPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun document_detail_rename_bottom_sheet() {
        composeTestRule.setContent {
            RenameBottomSheetPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
