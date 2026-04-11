# ScanSign — Play Store Release Checklist

**Version**: 1.0 · **Min SDK**: 29 (Android 10) · **Target SDK**: 36

---

## Part 1 — Manual QA Test Plan

Test on at least **two physical devices** (one older/mid-range, one newer flagship) and **one emulator**.
Check each box only after you have verified it yourself on a real device.

---

### 1.1 First Launch & Onboarding

- [ ] App launches in under 2 seconds (cold start)
- [ ] Documents screen shows correct empty state ("No documents yet" copy, not a crash or blank screen)
- [ ] Bottom navigation bar is visible with all 4 tabs: Docs · Scan · Sign · Settings
- [ ] Correct tab is highlighted on first open (Docs)
- [ ] App does not request unnecessary permissions on first launch

---

### 1.2 Camera Scan (ML Kit)

- [ ] Tapping "Scan Document" on the Docs screen opens the ML Kit scanner
- [ ] Camera viewfinder opens without ANR or crash
- [ ] Document edge detection highlights the page correctly
- [ ] User can capture multiple pages in a single session
- [ ] Retake / add page works correctly
- [ ] Scanning completes and navigates to the Review Scan screen
- [ ] Back arrow from scanner returns to Docs screen (not a blank stack)
- [ ] Denying camera permission shows an appropriate error / rationale, not a crash

---

### 1.3 Gallery Import

- [ ] Tapping "Scan from Gallery" opens the system photo picker
- [ ] Selecting multiple images navigates to the Review Scan screen
- [ ] Progress indicator is shown while images are being converted to PDF
- [ ] Cancelling the picker (selecting nothing) returns to Docs screen
- [ ] Selecting a corrupt / very large image shows an error dialog, not a crash

---

### 1.4 Review Scan Screen (ScanConfirmScreen)

- [ ] Page count label shows correct number ("X pages scanned")
- [ ] Thumbnail strip scrolls horizontally through all scanned pages
- [ ] Default document title is pre-filled ("Scan – MMM d, yyyy")
- [ ] Title field is editable; Save button is disabled when title is blank
- [ ] Save button shows a spinner while saving
- [ ] Successful save navigates to Docs screen and the new document appears in the list
- [ ] Error during save shows an error message below the title field
- [ ] Discard button (X icon and bottom bar) returns to Docs without saving
- [ ] Back navigation while saving is disabled (X icon is greyed out)

---

### 1.5 Documents Screen

- [ ] Saved documents appear in the list sorted by most recent first
- [ ] Each card shows: title, page count, file size, and creation date
- [ ] "View All" / "Show Less" toggle works when more than the default count of documents exist
- [ ] Search bar filters the list live as you type
- [ ] Clearing the search restores the full list
- [ ] Empty search returns "No results" state (not a blank screen)
- [ ] Swipe-to-delete removes the document and shows an undo snackbar
- [ ] Undo restores the document
- [ ] Tapping a document opens Document Detail

---

### 1.6 Document Detail Screen

- [ ] Title, page count, file size, and date are displayed correctly
- [ ] Page thumbnails are rendered and scrollable
- [ ] Tapping a page thumbnail opens the full-screen viewer
- [ ] Rename: bottom sheet appears, existing title is pre-filled, Save updates it, Cancel discards
- [ ] Share: system share sheet opens with the PDF file
- [ ] Delete: confirmation dialog appears; confirming removes document and navigates back
- [ ] Sign FAB is visible on SCANNED documents
- [ ] Sign FAB is also visible on SIGNED documents (for re-signing / countersigning)
- [ ] Tapping Sign FAB navigates to the Document Signing screen

---

### 1.7 Document Viewer (Full-Screen)

- [ ] Tapping a page in Document Detail opens the viewer at the correct page
- [ ] HorizontalPager allows swiping left / right between pages
- [ ] Page counter in the top bar updates as you swipe (e.g. "1 / 3")
- [ ] Thumbnail strip at the bottom scrolls and highlights the current page
- [ ] Tapping a thumbnail jumps to that page
- [ ] Sign button (pencil icon) in the top bar navigates to signing
- [ ] Back arrow returns to Document Detail
- [ ] Bottom navigation bar is hidden in this screen

---

### 1.8 Signer Screen — Draw Tab

- [ ] Draw tab is selected by default on first open
- [ ] Canvas allows free-form drawing with smooth Bézier curves
- [ ] "Sign here" hint is visible on empty canvas and disappears once drawing starts
- [ ] Undo removes the most recent stroke
- [ ] Undo is disabled when canvas is empty
- [ ] Clear removes all strokes
- [ ] "White background" chip is selected by default
- [ ] Switching to "No background" chip and saving produces a transparent-background PNG
- [ ] Save button is disabled when canvas is empty
- [ ] Tapping Save opens the name dialog
- [ ] Blank name disables the confirm button in the dialog
- [ ] After confirming, a success snackbar appears and the signature appears in the Saved list
- [ ] Save error shows an error snackbar

---

### 1.9 Signer Screen — Image Tab

- [ ] Switching to Image tab shows the empty state placeholder
- [ ] "Pick from gallery" opens the system photo picker (single image)
- [ ] Selected image is previewed in the card
- [ ] "Change" button allows picking a different image
- [ ] Save opens the name dialog and saves the signature
- [ ] Saved image signature appears in the Saved list under the Image tab

---

### 1.10 Signer Screen — Saved Signatures

- [ ] Saved list is filtered to show only signatures matching the current tab type (Drawn / Image)
- [ ] Each item shows: thumbnail, name, type, and creation date
- [ ] Delete icon opens a confirmation dialog
- [ ] Confirming delete removes the signature from the list
- [ ] Cancelling delete leaves the signature intact

---

### 1.11 Document Signing Flow

- [ ] Opening the signing screen shows the first page of the PDF rendered at full width
- [ ] Page counter shows "Page 1 / N"
- [ ] Next / Previous page buttons work and are disabled at boundaries
- [ ] "Choose signature" section shows all saved signatures as horizontal chips
- [ ] When no signatures exist, the empty-state CTA card is shown with a "Sign tab →" button
- [ ] "Sign tab →" button navigates to the Signer screen
- [ ] Tapping a signature chip selects it and shows the drag overlay on the page
- [ ] Dragging the overlay repositions the signature correctly
- [ ] Resize handle (bottom-right dot) resizes the signature when dragged
- [ ] Signature is clamped within the page bounds (cannot be dragged off-screen)
- [ ] "Apply Signature" button is disabled when no signature is selected
- [ ] Applying shows a progress indicator
- [ ] On success, navigates back to Document Detail and the page thumbnail updates to show the embedded signature
- [ ] On error, a snackbar shows the error message
- [ ] Re-signing an already-signed document works (additional signature is added)
- [ ] Back button returns to Document Detail without signing

---

### 1.12 Settings Screen

- [ ] System Default / Light / Dark theme options work and take effect immediately
- [ ] Theme preference persists across app restarts
- [ ] Scan Quality selector (Standard / High) is displayed and saves correctly
- [ ] Storage info shows correct document count and approximate disk usage
- [ ] App version number is displayed correctly

---

### 1.13 Navigation & Back Stack

- [ ] Back gesture / system back button works on every screen
- [ ] Pressing back from Docs, Sign, or Settings returns to the previous tab, not out of the app unexpectedly
- [ ] No screen ever shows a blank white page when navigating
- [ ] Rotating the device on every screen does not crash or lose state
- [ ] Putting the app in the background and returning restores the correct screen
- [ ] Process death (kill from recents then reopen) lands back on Docs, not a crash

---

### 1.14 Edge Cases & Stability

- [ ] Signing a document with a very large drawn signature completes within 5 seconds
- [ ] Scanning a 10-page document saves without memory error
- [ ] Low-storage condition: saving a large scan shows an error, not a crash
- [ ] No memory leaks detectable via Android Studio Profiler on the signing screen after 3 sign operations

---

## Part 2 — Pre-Build Release Tasks

These are code/config changes to make **before** generating the AAB.

### 2.1 Enable R8 / Minification

Currently `isMinifyEnabled = false` in `app/build.gradle.kts`. Before release:

- [ ] Set `isMinifyEnabled = true` in the `release` build type
- [ ] Set `isShrinkResources = true` in the `release` build type
- [ ] Build a release APK locally and run through the full test plan above
- [ ] Add any missing ProGuard rules for PDFBox, Koin, Coil, and ML Kit to `proguard-rules.pro`
  ```
  # PDFBox Android
  -keep class com.tom_roush.pdfbox.** { *; }
  # Koin
  -keep class org.koin.** { *; }
  # Kotlin Serialization (Navigation routes)
  -keepattributes *Annotation*, InnerClasses
  -dontnote kotlinx.serialization.AnnotationsKt
  -keep,includedescriptorclasses class com.spatel.scansign.**$$serializer { *; }
  -keepclassmembers class com.spatel.scansign.** {
      *** Companion;
  }
  ```

### 2.2 Version & Application ID

- [ ] Confirm `applicationId = "com.spatel.scansign"` is your intended Play Store ID (cannot change after publish)
- [ ] Set `versionCode = 1` and `versionName = "1.0.0"` (use semantic versioning)

### 2.3 Privacy Policy

- [ ] Host a privacy policy at a public URL (GitHub Pages or a free site like Carrd works)
- [ ] It must state: data is stored locally on device, no data is transmitted to third parties
- [ ] Add the URL to the Play Console listing and inside the app (Settings screen)

### 2.4 Remove Debug Artifacts

- [ ] Ensure no `Log.d` / `Log.v` statements exist in release paths (or wrap with `BuildConfig.DEBUG`)
- [ ] Remove any test/placeholder copy from strings
- [ ] Verify app icon looks correct at all densities (mdpi through xxxhdpi) and as an adaptive icon

---

## Part 3 — Keystore & Signed AAB

- [ ] **Generate a keystore** (keep this file and password safe forever — losing it means you can never update the app):
  ```bash
  keytool -genkey -v -keystore scansign-release.keystore \
    -alias scansign -keyalg RSA -keysize 2048 -validity 10000
  ```
- [ ] Store the keystore **outside** the repo (never commit it to Git)
- [ ] Add signing config to `app/build.gradle.kts`:
  ```kotlin
  signingConfigs {
      create("release") {
          storeFile = file("path/to/scansign-release.keystore")
          storePassword = System.getenv("KEYSTORE_PASSWORD")
          keyAlias = "scansign"
          keyPassword = System.getenv("KEY_PASSWORD")
      }
  }
  buildTypes {
      release {
          signingConfig = signingConfigs.getByName("release")
          isMinifyEnabled = true
          isShrinkResources = true
          ...
      }
  }
  ```
- [ ] Build the AAB:
  ```
  Build → Generate Signed Bundle / APK → Android App Bundle → release
  ```
- [ ] Verify the AAB with `bundletool`:
  ```bash
  bundletool build-apks --bundle=app-release.aab --output=app.apks --ks=scansign-release.keystore
  bundletool install-apks --apks=app.apks
  ```
- [ ] Install and run the full test plan above against the **release build** (not debug)

---

## Part 4 — Google Play Console Setup

### 4.1 Create the App

- [ ] Go to [play.google.com/console](https://play.google.com/console) → **Create app**
- [ ] App name: "ScanSign — PDF Scanner & Signer" (or your preferred title)
- [ ] Default language: English (US)
- [ ] App or game: **App**
- [ ] Free or paid: **Free** (required if using AdMob ads in Phase 2)
- [ ] Accept the declarations

### 4.2 Store Listing

- [ ] **Short description** (80 chars max): "Scan, manage, and sign PDF documents — fully offline."
- [ ] **Full description** (4000 chars): Describe scan, gallery import, draw/image signatures, offline-first, no account needed. Include keywords: PDF scanner, document scanner, PDF signer, e-signature, offline.
- [ ] **App icon**: 512×512 PNG, no alpha
- [ ] **Feature graphic**: 1024×500 PNG (shown at top of listing)
- [ ] **Screenshots**: minimum 2, recommended 4–8
  - Documents list (with documents present)
  - Camera scanning in progress
  - Signing flow (signature placed on page)
  - Signer screen (Draw tab)
  - Settings screen
  - Document viewer (multi-page swipe)
- [ ] Use a tool like **Previewed.app** or **Shotbot** to put screenshots in device frames — increases conversion rate significantly

### 4.3 App Content

- [ ] **Privacy policy URL**: paste the URL you hosted in Part 2
- [ ] **Content rating questionnaire**: answer honestly — this app has no violence, user-generated content shared with others, or sensitive APIs. Expected rating: **Everyone**
- [ ] **Target audience**: 18+ (default; simplest path — no child-directed treatment needed)
- [ ] **Data safety section**:
  - Data collected: None (all data stays on device)
  - Data shared: None
  - Security practices: Data is encrypted in transit: No / Data can be deleted by users: Yes (via delete document)

### 4.4 Release Track

- [ ] Upload the AAB to **Internal testing** track first
- [ ] Add your own Google account as an internal tester
- [ ] Install via the Play Store (not sideload) and run the full test plan one more time
- [ ] Once satisfied → **Promote to Production** (or Closed Testing if you want a small beta first)
- [ ] Submit for review — Google typically reviews within 1–3 days for new apps

---

## Part 5 — Expert Suggestions

### 5.1 Critical Before Release

| Issue | Impact | Fix |
|-------|--------|-----|
| **R8 / minification is off** | APK/AAB is 2–3× larger than necessary, slower install | Enable in release build type (see Part 2.1) |
| **No crash reporting** | You will be flying blind post-launch; no way to know what's crashing for real users | Add Firebase Crashlytics (free) — 2 hours of work |
| **No splash screen** | Blank white flash on cold start looks unpolished | Add `androidx.core:core-splashscreen` — 30 minutes |
| **Scan Quality setting is inert** | DataStore stores it but nothing reads it (documented in CLAUDE.md) | Wire it in `ImagesToPdfConverter` as JPEG quality parameter |
| **No FileProvider** | Share document feature will fail on Android 10+ if not already done | Verify `FileProvider` is configured in `AndroidManifest.xml` |

### 5.2 Quality of Life (Post-Release Polish)

- **Pinch-to-zoom in the document viewer** — HorizontalPager + `transformable` modifier. Users will immediately want this. High demand, medium effort.
- **Sort and filter on Documents screen** — Sort by: Newest / Oldest / Name / Size. Simple `DropdownMenu`, no backend change needed.
- **Rename from the document list** — Long-press to bring up a context menu. Saves navigating into detail just to rename.
- **Signature preview in the picker** — Show a small inline preview of the selected signature before placing it, so users don't have to place it blind.
- **Page rotation in the viewer** — Single button tap, saves to Room. Useful for photos taken sideways.

### 5.3 Small Wins (High ROI, Low Effort)

These are features that take under a day and have disproportionate user impact:

- **App shortcuts** (static `shortcuts.xml`): "Scan Document" and "Import from Gallery" directly from the home screen long-press. Takes 1 hour, makes the app feel native and polished. Users who scan frequently will love this.

- **Recently used signature first** — Sort the signing flow's signature picker so the most recently used signature appears first. One-liner in the ViewModel (`sortedByDescending { it.createdAt }`). Zero UI changes.

- **Document count on the Docs tab badge** — Small number badge on the Docs bottom-bar icon when the user has documents. Already supported by `NavigationBarItem.badge {}`. 20 minutes of work.

- **"Tap to copy" document file path in Settings / Detail** — Power users always want this for backups. One `Clipboard` call.

- **Shake to clear canvas in Draw tab** — Fun, discoverable, and easier than finding the Clear button. Uses `SensorManager`. About 2 hours.

- **PDF page reordering** — Drag-to-reorder pages in the Confirm screen before saving. `LazyColumn` with `dragContainer` modifier. Half a day of work, very visible in marketing.

---

## Part 6 — Launch Day Checklist

- [ ] Test the production build one final time on 2 physical devices
- [ ] Announce in any communities / networks you're part of (Reddit r/androiddev, LinkedIn, Twitter/X)
- [ ] Create a short demo video for the Play Store listing (optional but increases conversion 2–3×)
- [ ] Set up a way to receive user feedback (support email, GitHub Issues, or an in-app feedback link)
- [ ] Monitor Play Console → Android vitals the first 48 hours for crash spikes
- [ ] Plan your first update (fix the top 3 crash reports within 2 weeks — Google's algorithm rewards fast iteration)
