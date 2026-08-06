# ScanSign — Play Store Release Checklist

**Version**: 1.0 · **Min SDK**: 29 (Android 10) · **Target SDK**: 36

---

## Part 1 — Manual QA Test Plan

**Moved to [`docs/MANUAL_TEST_PLAN.md`](docs/MANUAL_TEST_PLAN.md).**

That file has the same 14 coverage areas as this section used to, rewritten as
numbered, step-by-step test cases with explicit expected results and a pass/fail
column per case, plus a device matrix, a 🔥 smoke subset for hotfixes, a
release-build-specific pass (R8/minification), a bug report template, and a sign-off
block. Run it in full before every submission to a Play Store track.

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
