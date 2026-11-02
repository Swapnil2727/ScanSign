package com.scansign.app.ui.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.scansign.app.ui.onboarding.NameEntryStepInvalidPreview
import com.scansign.app.ui.onboarding.NameEntryStepPreview
import com.scansign.app.ui.onboarding.TrustCardsStepPreview
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class OnboardingScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun onboarding_name_entry_step() {
        composeTestRule.setContent {
            NameEntryStepPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun onboarding_name_entry_step_invalid() {
        composeTestRule.setContent {
            NameEntryStepInvalidPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun onboarding_trust_cards_step() {
        composeTestRule.setContent {
            TrustCardsStepPreview()
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
