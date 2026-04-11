package com.spatel.scansign

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spatel.scansign.core.model.Document
import com.spatel.scansign.core.model.DocumentStatus
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DOC_ID = "test-doc-001"
private const val TEST_DOC_TITLE = "Test Contract"

@RunWith(AndroidJUnit4::class)
class AppFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val testApp: TestScanSignApplication
        get() = composeTestRule.activity.application as TestScanSignApplication

    @Before
    fun seedData() {
        testApp.fakeDocumentRepository.setDocuments(
            listOf(
                Document(
                    id = TEST_DOC_ID,
                    title = TEST_DOC_TITLE,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    pageCount = 1,
                    fileSize = 512_000L,
                    status = DocumentStatus.SCANNED,
                    pdfPath = null,
                    thumbnailPath = null,
                )
            )
        )
    }

    @Test
    fun fullAppFlow_documentToSignerAndBack() {
        // STEP 1 — DocumentsRoute: document appears in list
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(TEST_DOC_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(TEST_DOC_TITLE).assertIsDisplayed()
        // Checkpoint: DocumentsScreen rendered with seeded document

        // STEP 2 — Navigate to DocumentDetailRoute
        composeTestRule.onNodeWithText(TEST_DOC_TITLE).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithContentDescription("Sign document").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Details").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Sign document").assertIsDisplayed()
        // Checkpoint: DocumentDetailScreen rendered with FAB

        // STEP 3 — Tap Sign FAB → DocumentSigningRoute
        composeTestRule.onNodeWithContentDescription("Sign document").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Place Signature").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Choose signature").assertIsDisplayed()
        composeTestRule.onNodeWithText("No signatures yet").assertIsDisplayed()
        // Checkpoint: DocumentSigningScreen with empty signature picker

        // STEP 4 — Tap "Sign tab" → clears back stack, opens SignerRoute
        composeTestRule.onNodeWithText("Sign tab").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Signatures").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Draw").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign here").assertIsDisplayed()
        // Checkpoint: SignerScreen, Draw tab visible

        // STEP 5 — Draw a stroke on the signature canvas
        composeTestRule.onNodeWithTag("signature_canvas").performTouchInput {
            swipe(
                start = Offset(100f, 120f),
                end = Offset(700f, 120f),
                durationMillis = 300,
            )
        }
        composeTestRule.mainClock.advanceTimeBy(500)
        composeTestRule.onNodeWithText("Save").assertIsEnabled()
        // Checkpoint: stroke drawn, Save button enabled

        // STEP 6 — Tap Save → enter name in dialog → confirm
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.onNodeWithText("Name this signature").assertIsDisplayed()
        composeTestRule.onNodeWithText("e.g. My Signature").performClick()
        composeTestRule.onNodeWithText("e.g. My Signature").performTextInput("My Test Signature")
        composeTestRule.onAllNodesWithText("Save")
            .filterToOne(hasAnyAncestor(hasText("Name this signature")))
            .assertIsEnabled()
            .performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Signature saved").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            composeTestRule.onAllNodesWithText("My Test Signature").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("My Test Signature").assertIsDisplayed()
        // Checkpoint: SignerScreen showing saved signature in the list

        // STEP 7 — Navigate back to Docs via bottom nav
        composeTestRule.onNodeWithText("Docs").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(TEST_DOC_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(TEST_DOC_TITLE).assertIsDisplayed()
        // Checkpoint: back on DocumentsScreen

        // STEP 8 — Tap document again → Detail → Sign FAB → DocumentSigning
        composeTestRule.onNodeWithText(TEST_DOC_TITLE).performClick()
        composeTestRule.onNodeWithContentDescription("Sign document").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Place Signature").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            composeTestRule.onAllNodesWithText("My Test Signature").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("No signatures yet").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("My Test Signature").assertIsDisplayed()
        // Checkpoint: DocumentSigningScreen with signature in picker
    }

    @Test
    fun bottomNav_settingsTabShowsSettings() {
        composeTestRule.onNodeWithText("ScanSign").assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Settings").fetchSemanticsNodes().size >= 2
        }
        composeTestRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Scanning").assertIsDisplayed()
        composeTestRule.onNodeWithText("Storage").assertIsDisplayed()
        composeTestRule.onNodeWithText("About").assertIsDisplayed()
        // Checkpoint: SettingsScreen rendered with all sections
    }

    @Test
    fun search_matchingQueryShowsDocument() {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(TEST_DOC_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            composeTestRule.onAllNodesWithText("Search documents…").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Search documents…").performTextInput("Test")
        composeTestRule.onNodeWithText(TEST_DOC_TITLE).assertIsDisplayed()
        // Checkpoint: search active, matching doc displayed

        composeTestRule.onNodeWithText("Search documents…").performTextClearance()
        composeTestRule.onNodeWithText("Search documents…").performTextInput("ZZZNOMATCH")
        composeTestRule.onNodeWithText("No results for \"ZZZNOMATCH\"").assertIsDisplayed()
        composeTestRule.onNodeWithText(TEST_DOC_TITLE).assertDoesNotExist()
        // Checkpoint: search no-results state verified
    }
}
