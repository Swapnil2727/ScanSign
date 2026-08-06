# ScanSign — Manual Test Plan (Pre-Release)

**App version under test:** _fill in versionName/versionCode before each run_
**Build type:** _debug / release (AAB installed via Play Store Internal Testing)_
**Min SDK:** 29 (Android 10) · **Target SDK:** 36

This replaces the bullet checklist that used to live in `RELEASE_CHECKLIST.md` Part 1.
Same coverage, restructured so a tester (including a future you, six months from now,
with zero context) can execute it without guessing what "works correctly" means for
each line.

## How to use this document

- Run the **full plan** before every Play Store submission (internal, closed, or
  production track). Run the **smoke subset** (marked 🔥) after any hotfix.
- One row per test case: **Steps** are numbered and literal — do exactly that, in that
  order. **Expected** is what you should see. If what you see doesn't match, that's a
  fail — file it (see [Bug Report Template](#bug-report-template)) and keep going;
  don't stop the run for one failure.
- Fill in **Result** as you go: ✅ Pass / ❌ Fail / ⚠️ Pass with notes / ➖ Blocked
  (couldn't test because an earlier step failed).
- **Never mark something Pass from memory or from reading the code.** If you didn't
  personally tap through it on a device this run, it's untested, not passing.

## Device matrix

Run the full plan on at least these three targets. Fill in actual models before each
release — this is a placeholder shape, not real hardware yet.

| # | Device class | Target | Android version | Notes |
|---|---|---|---|---|
| 1 | Older / mid-range physical | _e.g. Redmi Note / Moto G series_ | API 29–31 | Catches low-RAM ML Kit issues (§1.2), slow storage (§1.14) |
| 2 | Newer flagship physical | _e.g. Pixel 8/9, Galaxy S-series_ | API 34–36 | Catches edge-to-edge / gesture-nav regressions |
| 3 | Emulator | Any AVD | API 29 (minSdk floor) | Catches the actual minSdk floor, which your physical devices may not hit |

Run each section below on **at least one physical device**; the R8/minified release
build (§ Release-Build-Specific below) must be tested on a physical device, not just
the emulator.

---

## 🔥 Smoke subset (run after every hotfix, ~10 minutes)

Cherry-pick these IDs and nothing else when you don't have time for the full plan:
`TC-ONB-01`, `TC-SCAN-01`, `TC-GAL-01`, `TC-CONF-01`, `TC-DOC-01`, `TC-DET-08` (Sign FAB),
`TC-SIGNFLOW-05` (apply signature), `TC-NAV-06` (process death).

---

## 1. First Launch & Onboarding

**Precondition:** Fresh install — uninstall any existing copy first (`adb uninstall com.spatel.scansign`), or use a fresh emulator.

| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-ONB-01 🔥 | 1. Cold-launch the app (tap icon, device was not already running it in background). Time it with a stopwatch. | App reaches an interactive screen in **under 2 seconds**. | |
| TC-ONB-02 | 1. Observe the first screen shown. | Onboarding flow appears (name entry / trust carousel) — not the Documents screen directly, not a crash, not a blank white screen. | |
| TC-ONB-03 | 1. Complete the onboarding flow through to the end. | Lands on the Documents screen with the correct empty-state copy ("No documents yet" or equivalent) — not a crash. | |
| TC-ONB-04 | 1. After onboarding completes, observe the bottom navigation bar. | All 4 tabs visible: **Docs · Scan · Sign · Settings**. | |
| TC-ONB-05 | 1. Check which tab is highlighted/selected. | **Docs** is selected by default. | |
| TC-ONB-06 | 1. Watch for any permission dialogs during first launch, before you've tapped Scan. | **No** permission dialog appears unprompted — ML Kit Document Scanner needs no camera permission from the host app; nothing should be requested until an action needs it. | |
| TC-ONB-07 | 1. Force-close the app. 2. Relaunch it. | Onboarding does **not** show again — app remembers it was completed and goes straight to Docs. | |

---

## 2. Camera Scan (ML Kit)

| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-SCAN-01 🔥 | 1. From Docs screen, tap "Scan Document" (or the Scan tab). | ML Kit's native scanner UI opens (camera viewfinder) within ~1 second, no ANR, no crash. | |
| TC-SCAN-02 | 1. Point the camera at a physical document (any printed page). | The scanner highlights/outlines the detected page edges in real time. | |
| TC-SCAN-03 | 1. Capture the page. 2. Without exiting, capture a second page (a different document/page). | Both pages are captured into the same session — a page counter or thumbnail strip shows 2 pages. | |
| TC-SCAN-04 | 1. After capturing a page, look for a retake option. 2. Use it. | The page is retaken/replaced; the page count doesn't increment incorrectly. | |
| TC-SCAN-05 🔥 | 1. Capture at least 1 page. 2. Confirm/finish scanning in the ML Kit UI. | App navigates to the Review Scan screen (`ScanConfirmScreen`) with the captured page(s) shown. | |
| TC-SCAN-06 | 1. Open the scanner (TC-SCAN-01). 2. Immediately tap the system/toolbar back arrow before capturing anything. | Returns cleanly to the Docs screen — not a blank screen, not stuck on the scanner. | |
| TC-SCAN-07 | 1. In device Settings, revoke/deny camera permission for ScanSign (if the OS prompts one — some ML Kit flows still trigger a system camera permission dialog on first use). 2. Try to scan. | A clear in-app error/rationale is shown — **not** a crash, **not** a silent failure. | |

---

## 3. Gallery Import

| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-GAL-01 🔥 | 1. From Docs screen, tap "Scan from Gallery". | System photo picker opens. | |
| TC-GAL-02 | 1. Select 3 images from the picker. 2. Confirm the selection. | A progress indicator shows while images convert to PDF, then navigates to Review Scan with 3 pages. | |
| TC-GAL-03 | 1. Open the picker (TC-GAL-01). 2. Select nothing, back out / cancel. | Returns to Docs screen — no crash, no stuck loading state. | |
| TC-GAL-04 | 1. Select a very large image (>10MB, e.g. a modern phone camera photo) or a deliberately corrupted image file. | An error dialog is shown — **not** a crash, **not** a silently truncated document. | |
| TC-GAL-05 | 1. Select an image known to have EXIF rotation (e.g. a photo taken in portrait on a phone camera) via gallery import. 2. Check the resulting page in Review Scan. | Page appears in the correct orientation (not sideways) — confirms `ImagesToPdfConverter`'s EXIF-rotation handling works. | |

---

## 4. Review Scan Screen (`ScanConfirmScreen`)

**Precondition:** Arrived here via either TC-SCAN-05 or TC-GAL-02.

| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-CONF-01 🔥 | 1. Observe the page count label. | Shows the correct count, e.g. "2 pages scanned". | |
| TC-CONF-02 | 1. Swipe the thumbnail strip left/right. | Scrolls smoothly through all scanned pages. | |
| TC-CONF-03 | 1. Observe the title field on screen load. | Pre-filled with a default like "Scan – Aug 6, 2026". | |
| TC-CONF-04 | 1. Clear the title field completely. | Save button becomes disabled. | |
| TC-CONF-05 | 1. Type a title. 2. Tap Save. | Save button shows a spinner while saving. | |
| TC-CONF-06 🔥 | 1. Complete a save with a valid title. | Navigates to Docs screen; the new document appears in the list immediately. | |
| TC-CONF-07 | 1. Fill available device storage to near-zero (or simulate via a low-storage test device). 2. Attempt to save. | An error message appears below the title field — not a crash. | |
| TC-CONF-08 | 1. Tap the discard action (X icon, top or bottom bar). | Returns to Docs screen without saving — document does not appear in the list. | |
| TC-CONF-09 | 1. Tap Save. 2. While the spinner is showing, immediately tap the back button/X icon. | Back/discard is disabled (greyed out) while saving is in progress — can't interrupt a save mid-flight. | |

---

## 5. Documents Screen

**Precondition:** At least 3 saved documents exist (repeat TC-SCAN or TC-GAL flow to seed data).

| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-DOC-01 🔥 | 1. Open Docs screen. | Documents are sorted most-recent-first. | |
| TC-DOC-02 | 1. Inspect any document card. | Shows title, page count, file size, and creation date. | |
| TC-DOC-03 | 1. Save enough documents to exceed the default visible count. 2. Tap "View All". 3. Tap "Show Less". | List expands to show all, then collapses back — no crash, no duplicate entries. | |
| TC-DOC-04 | 1. Type a partial title into the search bar that matches one document. | List filters live, as you type (no need to hit enter/search button). | |
| TC-DOC-05 | 1. Type a search query matching zero documents. | "No results" state is shown — not a blank screen. | |
| TC-DOC-06 | 1. Clear the search field. | Full list is restored. | |
| TC-DOC-07 🔥 | 1. Swipe a document card to delete. | Document is removed from the list, an undo snackbar appears. | |
| TC-DOC-08 | 1. Immediately after TC-DOC-07, tap Undo. | Document reappears in its original position in the list. | |
| TC-DOC-09 | 1. Tap a document card. | Navigates to Document Detail for that document. | |

---

## 6. Document Detail Screen

**Precondition:** Open a saved document's detail screen.

| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-DET-01 | 1. Observe the header/metadata area. | Title, page count, file size, and date are all displayed and correct. | |
| TC-DET-02 | 1. Scroll through page thumbnails. | All pages are rendered and scrollable. | |
| TC-DET-03 | 1. Tap any page thumbnail. | Opens the full-screen Document Viewer at that page. | |
| TC-DET-04 | 1. Tap Rename. 2. Observe the bottom sheet. | Bottom sheet appears with the existing title pre-filled. | |
| TC-DET-05 | 1. In the rename sheet, change the title, tap Save. | Title updates on the detail screen and in the Docs list. | |
| TC-DET-05b | 1. In the rename sheet, change the title, tap Cancel instead. | Title is unchanged. | |
| TC-DET-06 | 1. Tap Share. | System share sheet opens with the PDF file attached (verify by sharing to e.g. Gmail draft or Files app — file should actually be attached, not a broken/empty attachment). | |
| TC-DET-07 | 1. Tap Delete. 2. Observe the dialog. 3. Confirm. | Confirmation dialog appears first; confirming removes the document and navigates back to Docs. | |
| TC-DET-08 🔥 | 1. Open a document with status SCANNED (not yet signed). | Sign FAB is visible. | |
| TC-DET-09 | 1. Open a document with status SIGNED (repeat TC-SIGNFLOW-05 first, then return here). | Sign FAB is **still** visible (re-signing/countersigning is supported, not blocked). | |
| TC-DET-10 | 1. Tap the Sign FAB. | Navigates to the Document Signing screen. | |

---

## 7. Document Viewer (Full-Screen)

**Precondition:** Opened via TC-DET-03, on a document with 2+ pages.

| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-VIEW-01 | 1. Open the viewer from a specific thumbnail (e.g. page 2 of 3). | Viewer opens directly on page 2, not page 1. | |
| TC-VIEW-02 | 1. Swipe left/right on the page. | Pager navigates between pages smoothly. | |
| TC-VIEW-03 | 1. Swipe to a different page. 2. Check the top bar. | Page counter updates correctly (e.g. "2 / 3"). | |
| TC-VIEW-04 | 1. Observe the bottom thumbnail strip while swiping. | Strip scrolls and highlights the current page in sync with the main pager. | |
| TC-VIEW-05 | 1. Tap a thumbnail in the bottom strip for a different page. | Jumps the main pager to that page. | |
| TC-VIEW-06 | 1. Tap the Share action (top bar). | System share sheet opens with the PDF. | |
| TC-VIEW-07 | 1. Tap the Sign (pencil) icon in the top bar. | Navigates to the signing flow. | |
| TC-VIEW-08 | 1. Tap the back arrow. | Returns to Document Detail (not Docs list, not a blank screen). | |
| TC-VIEW-09 | 1. While in the viewer, check for the bottom navigation bar. | Bottom nav bar is hidden in this screen (full-screen viewing experience). | |

---

## 8. Signer Screen — Draw Tab

**Precondition:** Navigate to the Sign tab from the bottom bar.

| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-SIGN-DRAW-01 | 1. Open the Signer screen. | Draw tab is selected by default. | |
| TC-SIGN-DRAW-02 | 1. Observe the empty canvas. | "Sign here" hint is visible. | |
| TC-SIGN-DRAW-03 | 1. Draw a stroke on the canvas. | Stroke renders as a smooth curve (not jagged straight-line segments); the "Sign here" hint disappears. | |
| TC-SIGN-DRAW-04 | 1. Draw 2 separate strokes. 2. Tap Undo once. | Only the most recent stroke is removed; the first stroke remains. | |
| TC-SIGN-DRAW-05 | 1. On an empty canvas, check the Undo button state. | Undo is disabled. | |
| TC-SIGN-DRAW-06 | 1. Draw several strokes. 2. Tap Clear. | Canvas is fully cleared, all strokes gone. | |
| TC-SIGN-DRAW-07 | 1. Observe the background chip selector after drawing something. | "White background" chip is selected by default. | |
| TC-SIGN-DRAW-08 | 1. Switch to "No background" chip. 2. Draw a signature. 3. Save. 4. Later, place this signature over a dark-colored page region (or inspect the saved PNG file directly). | Signature has a transparent background — no white box around it. | |
| TC-SIGN-DRAW-09 | 1. On an empty canvas, check the Save button state. | Save is disabled. | |
| TC-SIGN-DRAW-10 🔥 | 1. Draw a signature. 2. Tap Save. | Name dialog opens. | |
| TC-SIGN-DRAW-11 | 1. In the name dialog, leave the name field blank. | Confirm button is disabled. | |
| TC-SIGN-DRAW-12 | 1. Enter a name. 2. Confirm. | Success snackbar appears; signature shows up in the Saved list under Draw. | |

---

## 9. Signer Screen — Image Tab

| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-SIGN-IMG-01 | 1. Switch to the Image tab (fresh, no image picked yet). | Empty-state placeholder is shown. | |
| TC-SIGN-IMG-02 | 1. Tap "Pick from gallery". | System photo picker opens in single-image-selection mode. | |
| TC-SIGN-IMG-03 | 1. Select an image. | Image previews in the card. | |
| TC-SIGN-IMG-04 | 1. Tap Change. 2. Select a different image. | Preview updates to the new image. | |
| TC-SIGN-IMG-05 | 1. With an image selected, tap Save, name it, confirm. | Saved image signature appears in the Saved list under the Image tab. | |

---

## 10. Signer Screen — Saved Signatures

**Precondition:** At least one Draw and one Image signature saved.

| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-SIGN-SAVED-01 | 1. On the Draw tab, view the Saved list. 2. Switch to Image tab, view its Saved list. | Each tab's Saved list only shows signatures of its own type (Draw list ≠ Image list mixed together). | |
| TC-SIGN-SAVED-02 | 1. Inspect a saved signature item. | Shows thumbnail, name, type, and creation date. | |
| TC-SIGN-SAVED-03 | 1. Tap the delete icon on a saved signature. | Confirmation dialog appears. | |
| TC-SIGN-SAVED-04 | 1. Confirm the delete. | Signature is removed from the list. | |
| TC-SIGN-SAVED-05 | 1. Tap delete on another signature, then Cancel in the dialog. | Signature remains, untouched. | |

---

## 11. Document Signing Flow

**Precondition:** Navigate here via a Sign FAB (TC-DET-10) or Sign icon (TC-VIEW-07), on a document with at least 1 saved signature.

| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-SIGNFLOW-01 | 1. Open the signing screen. | First page of the PDF renders at full width; page counter shows "Page 1 / N". | |
| TC-SIGNFLOW-02 | 1. Tap Next, then Previous. 2. Navigate to the last page and check Next; navigate to page 1 and check Previous. | Both buttons work and are disabled at their respective boundaries (no Next past the last page, no Previous before page 1). | |
| TC-SIGNFLOW-03 | 1. Observe "Choose signature" section. | Saved signatures shown as horizontal chips. | |
| TC-SIGNFLOW-03b | 1. Test on a document/account with **zero** saved signatures. | Empty-state CTA card shown instead, with a "Sign tab →" button. | |
| TC-SIGNFLOW-03c | 1. Tap "Sign tab →" from the empty state. | Navigates to the Signer screen. | |
| TC-SIGNFLOW-04 | 1. Tap a signature chip. | Chip is selected (visually indicated); a drag overlay of that signature appears on the page. | |
| TC-SIGNFLOW-04b | 1. Drag the signature overlay to a new position. | Overlay follows the drag and repositions correctly. | |
| TC-SIGNFLOW-04c | 1. Drag the resize handle (bottom-right dot). | Signature resizes proportionally as dragged. | |
| TC-SIGNFLOW-04d | 1. Attempt to drag the signature off the edge of the page. | Signature is clamped within the page bounds — cannot be dragged fully off-screen. | |
| TC-SIGNFLOW-05 🔥 | 1. With no signature selected, check the Apply Signature button. 2. Select a signature. 3. Check again. | Disabled with no selection; enabled once a signature is chosen. | |
| TC-SIGNFLOW-06 | 1. Tap Apply Signature. | Progress indicator shows during processing. | |
| TC-SIGNFLOW-07 | 1. Wait for success. | Navigates back to Document Detail; the page thumbnail now shows the embedded signature. | |
| TC-SIGNFLOW-08 | 1. Force an error condition if possible (e.g. airplane mode won't matter since this is local — try a corrupted/locked PDF file, or simulate low storage). | Error is shown via snackbar, not a silent failure or crash. | |
| TC-SIGNFLOW-09 | 1. On an already-signed document, repeat the full signing flow with a different signature. | A second signature is added successfully — re-signing works. | |
| TC-SIGNFLOW-10 | 1. Open the signing flow, then tap system back without applying. | Returns to Document Detail; document is **not** marked as signed. | |

---

## 12. Settings Screen

| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-SET-01 | 1. Select System Default theme. 2. Select Light. 3. Select Dark. | Each selection takes effect immediately, app-wide. | |
| TC-SET-02 | 1. Set theme to Dark. 2. Force-close and relaunch the app. | Dark theme persists across restart. | |
| TC-SET-03 | 1. Toggle Scan Quality between Standard and High. 2. Import a gallery scan under each setting (repeat TC-GAL-02 twice). 3. Compare resulting file sizes in Document Detail. | Setting saves correctly, and — as of the `ScanQuality` fix — **High produces a visibly larger file size than Standard** for the same source images (this specifically verifies the fix isn't just cosmetic). Camera scans via ML Kit are *not* expected to change size with this setting — that's a known, documented limitation, not a bug. | |
| TC-SET-04 | 1. Save several documents of known approximate size. 2. Check the Storage info display. | Document count and approximate disk usage roughly match what you actually saved. | |
| TC-SET-05 | 1. Check the About/version section. | App version number displayed matches the actual installed `versionName`. | |

---

## 13. Navigation & Back Stack

| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-NAV-01 | 1. On every top-level screen (Docs, Scan, Sign, Settings), press the system back button/gesture. | Returns to the previous tab or exits gracefully — never a blank white screen. | |
| TC-NAV-02 | 1. From Docs (the default/home tab), press back. | Standard Android behavior for a root screen (exits app or shows exit confirmation, per whatever the nav graph defines) — not a crash, not a stuck state. | |
| TC-NAV-03 | 1. Navigate several levels deep (Docs → Detail → Viewer → Sign flow). 2. Press back repeatedly. | Each back press unwinds one level — no skipped screens, no crash. | |
| TC-NAV-04 🔥 | 1. On any screen, rotate the device (portrait ↔ landscape). | No crash, no lost state (e.g. mid-draw signature isn't wiped, scroll position roughly preserved). | |
| TC-NAV-05 | 1. Put the app in the background (home button). 2. Wait ~30 seconds. 3. Return to the app (recents). | Restores to the exact screen you left, correct state. | |
| TC-NAV-06 🔥 | 1. Open the app to any screen. 2. Open Recents, swipe the app away to force process death (or use `adb shell am kill com.spatel.scansign` after backgrounding). 3. Reopen the app from the launcher. | Lands back on Docs screen — not a crash, not a blank screen. | |

---

## 14. Edge Cases & Stability

| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-EDGE-01 | 1. Draw a very large, detailed signature (fill the whole canvas with dense strokes). 2. Apply it to a document. 3. Time the operation. | Completes within 5 seconds. | |
| TC-EDGE-02 | 1. Scan a 10-page physical document via ML Kit (or gallery-import 10 images). 2. Save it. | Saves successfully, no OutOfMemoryError, no crash. | |
| TC-EDGE-03 | 1. Simulate low storage (fill device storage to near-zero, or use a test device/emulator with a small partition). 2. Attempt to save a large scan. | Clear error shown — not a crash, not silent data loss. | |
| TC-EDGE-04 | 1. Open the signing screen. 2. Apply a signature. 3. Repeat 3 times total (on the same or different documents). 4. Open Android Studio Profiler's Memory tab (debug build only) and check heap after each cycle, forcing GC between checks. | No sustained upward memory trend across the 3 cycles — no obvious leak. | |
| TC-EDGE-05 | 1. Deny/never-grant any runtime permission the app might request. 2. Use every feature (scan, gallery import, share, signing). | App degrades gracefully wherever a permission is missing — no crash. | |

---

## Release-Build-Specific (run once minification is enabled)

**Precondition:** `isMinifyEnabled = true` and `isShrinkResources = true` in `app/build.gradle.kts` (see `docs/superpowers/plans/2026-08-06-release-readiness.md` Task 4). Install the actual `app-release.apk` / bundletool-generated APK — **not** a debug build.

R8 silently breaks reflection-based code paths that debug builds never exercise. Run
the **entire plan above again** against this build specifically, plus:

| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-R8-01 | 1. Fresh-install the release build. 2. Launch it. | App launches without a `ClassNotFoundException` or Koin `NoBeanDefFoundException` — confirms Koin's DI graph (reflection-adjacent) survived minification. | |
| TC-R8-02 | 1. Run TC-SCAN-05 and TC-GAL-02 (produce a PDF via both paths) against the release build. | PDFBox operations succeed — confirms `-keep class com.tom_roush.pdfbox.**` rule is sufficient. | |
| TC-R8-03 | 1. Navigate through every screen in the app (Docs → Detail → Viewer → Sign → Settings and back). | No crash from a missing/renamed Kotlin Serialization route class — confirms the `$$serializer` keep rule covers all `@Serializable` route objects, not just the ones tested in debug. | |
| TC-R8-04 | 1. Run TC-SIGNFLOW-05 through TC-SIGNFLOW-07 (full apply-signature flow) against the release build. | Succeeds — this exercises PDFBox + Koin + Serialization together in one flow, the highest-risk combination for missed keep rules. | |

If any `TC-R8-*` case fails, read the R8 mapping/obfuscation log to find the missing
keep rule, add it to `app/proguard-rules.pro`, rebuild, and re-run the **entire**
release-build pass — not just the failing case, since one missing rule often masks
another.

---

## Bug Report Template

When a test case fails, file it with:

```
**Test ID:** TC-XXX-NN
**Device:** [model, Android version]
**Build:** [versionName/versionCode, debug or release]
**Steps to reproduce:** [copy from the test case, note any deviation]
**Expected:** [copy from the test case]
**Actual:** [what actually happened]
**Severity:** P0 (crash/data loss, blocks release) / P1 (broken feature) / P2 (cosmetic/minor)
**Screenshot or screen recording:** [attach]
```

---

## Sign-off

| Field | Value |
|---|---|
| Tester | |
| Date | |
| Build tested | |
| Total cases run | |
| Pass | |
| Fail | |
| Blocked | |
| P0 issues open | |
| **Release decision** | Go / No-Go |

A release is **not** Go while any P0 issue is open, or while any `🔥` smoke-subset
case has not been run and passed on this exact build.
