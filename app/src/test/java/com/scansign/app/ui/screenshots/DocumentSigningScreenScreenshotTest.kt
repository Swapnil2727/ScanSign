package com.scansign.app.ui.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.scansign.app.ui.documents.DocumentSigningInProgressPreview
import com.scansign.app.ui.documents.DocumentSigningNoSignaturesPreview
import com.scansign.app.ui.documents.DocumentSigningWithSignaturesPreview
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class DocumentSigningScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun document_signing_no_signatures() {
        composeTestRule.setContent {
            DocumentSigningNoSignaturesPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun document_signing_with_signatures() {
        composeTestRule.setContent {
            DocumentSigningWithSignaturesPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun document_signing_in_progress() {
        composeTestRule.setContent {
            DocumentSigningInProgressPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
