# PDF Scanner & Signer - Implementation Plan

**Timeline**: 15 weeks (Android MVP)
**Team**: 1 developer with access to Claude Code Pro
**Distribution**: Google Play Store — commercial app, ad-supported via AdMob

---

## Strategy: Two-Phase Delivery

**Phase 1 (Weeks 1–8): PDF Scanner MVP**
Build a fully functional, shippable PDF scanner app. Use ML Kit Document Scanner for capture (edge detection, perspective correction, multi-page, PDF output — all built-in). Manage documents locally and share them. AdMob ads live from day one.

**Phase 2 (Weeks 9–15): Signing Capabilities**
Layer digital, drawn, and image signatures on top of the completed PDF scanner. Signing is additive — the core scanner app is not disrupted.

---

## Architecture

### Layers
- **Presentation**: Jetpack Compose + ViewModel + StateFlow
- **Domain**: Use Cases + Repository Interfaces
- **Data**: Repository Implementations + Local Data Sources

### Modularization
Feature-based modules with layer separation:
- `:feature:scanner`, `:feature:documents`, `:feature:settings` (Phase 1)
- `:feature:signer` (Phase 2)
- `:core:ui`, `:core:database`, `:core:pdf`, `:core:signing`, `:core:ads`, `:core:di`, `:core:model`
- No `:core:camera` — ML Kit Document Scanner replaces it entirely
- Features have no cross-dependencies; use navigation for communication

---

## Tech Stack

| Component | Choice | Licence | Reason |
|-----------|--------|---------|--------|
| DI | Koin 4.0+ | Apache 2.0 | KMP-ready, no code generation |
| UI | Jetpack Compose | Apache 2.0 | Modern, state management friendly |
| Navigation | Navigation 3 | Apache 2.0 | Back-stack first, type-safe `@Serializable` routes |
| Database | Room + SQLite | Apache 2.0 | Type-safe, migration support |
| Async | Coroutines + Flow | Apache 2.0 | Native Kotlin, KMP compatible |
| Document Scanning | ML Kit Document Scanner | Apache 2.0 / GMS | Edge detection, perspective correction, multi-page, direct PDF output |
| PDF Manipulation | **Apache PDFBox Android** | **Apache 2.0** | Open existing PDFs and embed signatures — free for commercial use |
| Signing | Android Keystore | Built-in | Native, secure private key storage |
| Serialization | Kotlin Serialization | Apache 2.0 | Route keys, DataStore preferences |
| Ads | Google AdMob | N/A | Banner + interstitial, primary revenue source |

> **PDF Library Decision — Apache PDFBox Android (final, not up for re-evaluation)**
>
> Three options were considered:
> - `Android PdfDocument` (built-in) — **rejected**: can only *create* PDFs from scratch, cannot open and modify an existing PDF. Useless for embedding signatures into scanned documents.
> - `iText 7` — **rejected**: AGPL licence. Distributing a closed-source commercial Play Store app requires a paid commercial licence. Not viable.
> - **`Apache PDFBox Android` (Apache 2.0)** — **chosen**: can open, read, modify, and save existing PDFs. Free for commercial use. Widely used in open-source PDF signing apps. This is the library we use for embedding signatures in Phase 2. `:core:pdf` is built around it from Week 3.

---

## Weekly Tickets

---

## Phase 1: PDF Scanner MVP

---

### Week 1: Foundation ✅

**1.1 Project Setup**
- Create modular project structure with type-safe project accessors
- Configure Gradle with version catalog (`libs.versions.toml`)

**1.2 Koin DI Framework**
- Initialize Koin in Application class
- Create `:core:di` module — AppModule, DatabaseModule (shell)

**1.3 Core Models**
- Define `Document`, `DocumentPage`, `Signature` domain models
- Set up Kotlin Serialization

---

### Week 2: Core Infrastructure ✅

**2.1 Core UI Module** ✅
- `:core:ui` library — Compose + Material3 exposed via `api()`
- `ScanSignSpacing` tokens, `@ThemePreviews` annotation

**2.2 Navigation 3** ✅
- `@Serializable` route objects: `DocumentsRoute`, `ScannerRoute`, `SignerRoute`, `SettingsRoute`, `DocumentDetailRoute`
- `AppNavigation`: single `Scaffold` + `NavDisplay` + `SnapshotStateList` back stack
- Bottom bar with FAB-style centre Scan button
- Placeholder screens for all destinations

**2.3 Room Database** ← deferred to Week 4
> Intentionally deferred — entities shaped by real Scanner usage, not assumptions.

---

### Week 3: ML Kit Document Scanner + core:pdf

**3.1 ML Kit Document Scanner Integration**
- Add `play-services-mlkit-document-scanner` dependency
- Configure `GmsDocumentScannerOptions` (multi-page, JPEG + PDF output, gallery import)
- Replace placeholder `ScannerScreen` with ML Kit launcher + result handling
- Extract PDF `Uri` and page image `Uri` list from scanner result
- Navigate to confirm screen on success

**3.2 Core PDF Module (`:core:pdf`)**
- Add `Apache PDFBox Android` dependency (Apache 2.0 — commercially safe)
- `PdfCopier`: copy ML Kit's content-`Uri` PDF into app-internal storage as a permanent file
- `PdfMetadata`: read page count and file size from a PDF using PDFBox
- `PdfPageRenderer`: render a PDF page to a `Bitmap` for thumbnail generation
- Keep this module thin in Phase 1 — it grows in Phase 2 when `PdfSigner` is added

---

### Week 4: Scanner Feature — Domain + Data (incl. Room)

**4.1 Room Database (`:core:database`)**
- `ScanSignDatabase` Room DB version 1
- `DocumentEntity` (id, title, pdfPath, thumbnailPath, pageCount, fileSize, status, createdAt, updatedAt)
- `DocumentPageEntity` (id, documentId, pageNumber, imagePath, rotation)
- `DocumentDao`: insert, getAll (Flow), getById, delete, updateTitle
- No `SignatureEntity` yet — added in Phase 2 as migration version 2

**4.2 Scanner Domain**
- `DocumentRepository` interface
- `SaveScannedDocumentUseCase`: ML Kit result → copy files to internal storage → generate thumbnail → persist to Room
- `DeleteDocumentUseCase`: delete Room row + files from disk
- Pure Kotlin domain — no Android or Koin imports

**4.3 Scanner Data Layer**
- `DocumentRepositoryImpl` backed by Room + `FileManager`
- Wire `DatabaseModule` in `:core:di`

---

### Week 5: Documents Feature

**5.1 Documents Domain**
- `GetDocumentsUseCase`: returns `Flow<List<Document>>` — reactive, auto-updates on change
- `SearchDocumentsUseCase`: filter by title
- `ShareDocumentUseCase`: via Android `FileProvider` intent
- `RenameDocumentUseCase`

**5.2 Documents UI**
- Replace placeholder `DocumentsScreen` — real list from Room `Flow` via ViewModel
- Search bar with live filtering
- Document detail screen: PDF viewer (PDFBox page renderer) + metadata
- Swipe-to-delete with undo snackbar, share action, rename via bottom sheet

---

### Week 6: Settings

**6.1 Settings Feature**
- Replace placeholder `SettingsScreen`
- DataStore preferences: app theme override, default scan quality
- Storage usage display (total documents, disk used)
- App info (version, open-source licences, privacy policy)

---

### Week 7: Phase 1 Polish + Testing

**7.1 Polish**
- Edge-to-edge on all screens
- Empty states: no documents yet, first-time user onboarding
- Error states: ML Kit unavailable, storage full, permission denied
- Loading indicators and screen transitions

**7.2 Unit Tests**
- All Phase 1 use cases with fake repositories (no mocks)
- `PdfCopier`, `PdfMetadata`, `PdfPageRenderer`
- Target: 100% domain layer coverage

**7.3 ViewModel + Integration Tests**
- `ScannerViewModel`, `DocumentsViewModel` with fake use cases
- Integration test: scan result → save → list → delete with in-memory Room
- Navigation flow tests

**7.4 Performance**
- Cold start target: < 2 seconds
- Room indices on `createdAt`, `title`
- Thumbnail lazy-loading in document list

---

### Week 8: Phase 1 Release

**8.1 Play Store Preparation**
- Generate signed AAB with release keystore
- Store listing: title, description, screenshots (scanner-focused)
- Privacy policy (local storage only, no data leaves device)
- AdMob app ID registered, production ad unit IDs configured
- Google Play Console setup, content rating questionnaire

**8.2 Beta → Public**
- Internal testing track on 3+ real devices
- Firebase Crashlytics (crash reporting only, no analytics sync)
- Fix critical issues, promote to production

---

## Phase 2: Signing Capabilities

---

### Week 9: Core Signing Module (`:core:signing`) + PDF Signing Infrastructure

**9.1 Android Keystore Wrapper**
- `KeystoreManager`: generate RSA key pair, retrieve, delete
- Self-signed X.509 certificate generation
- `SignatureEntity` added to Room — migration version 2
- `SignatureDao`: insert, getAll, delete

**9.2 PDF Signing via PDFBox**
- `PdfSigner` in `:core:pdf`: open existing PDF, embed signature (bitmap or digital cert) at given coordinates, save
- `PdfVerifier`: validate digital signature embedded in a PDF
- `SignDocumentUseCase`, `VerifySignatureUseCase`

---

### Week 10: Signing Feature UI

**10.1 Signing Screen**
- Replace placeholder `SignerScreen`
- Three tabs: Draw, Image, Digital
- **Draw**: canvas with stroke input, pressure simulation, undo, clear, save as bitmap
- **Image**: photo picker / gallery, crop to signature bounds
- **Digital**: create or select existing Keystore key pair, display certificate details

**10.2 PDF Signing Flow**
- Entry point: document detail → "Sign" action
- Signature placement overlay on PDF page preview (drag, scale, reposition)
- Confirm → `SignDocumentUseCase` → document status → `SIGNED`
- Interstitial ad shown on signing completion

---

### Week 11: Signing Tests + Integration

**11.1 Signing Tests**
- Unit test `SignDocumentUseCase`, `VerifySignatureUseCase` with fakes
- Keystore tests on emulator + real device
- PDFBox signing round-trip test (sign → verify → assert valid)

**11.2 Full App Regression**
- End-to-end: scan → list → sign → verify → share
- Navigation regression across all routes

---

### Week 12: AdMob Integration (`:core:ads`)

**12.1 AdMob Setup**
- Add Google Mobile Ads SDK dependency
- `AdManager` singleton injectable via Koin — single source of truth for ad state
- Banner ad composable (`AdBanner`) placed in document list and settings screens
- Interstitial ad shown after a successful scan and after a successful signing operation
- Use test ad unit IDs in debug builds, production IDs in release builds via `BuildConfig`
- GDPR/CCPA consent flow using UMP (User Messaging Platform) SDK — required for EU users and Play Store compliance

---

### Week 13: Performance + Final Polish

**13.1 Performance**
- Signing target: < 3 seconds for a 10-page PDF
- Memory profiling during PDF manipulation
- Final Room query pass

**12.2 Polish**
- Accessibility audit (content descriptions, 48dp touch targets)
- Dark/light theme audit on all screens
- Tablet layout review

---

### Week 14–15: Phase 2 Release

**13.1 Play Store Update**
- Update listing to highlight signing
- New screenshots of signing flow
- Bump `versionCode` + `versionName`

**13.2 Beta → Public**
- Beta on existing Phase 1 users
- Address feedback, promote to production

---

## Key Decisions

- **ML Kit Document Scanner over CameraX**: Eliminates 2–3 weeks of camera work. Google Drive-quality scanning in ~50 lines. ML Kit's job ends at the PDF file — signing is identical either way. Requires GMS (irrelevant for Play Store).
- **Apache PDFBox Android over iText 7**: iText 7 is AGPL — incompatible with a commercial closed-source Play Store app without a paid licence. PDFBox is Apache 2.0, free for commercial use, and fully capable of opening and modifying existing PDFs for signature embedding.
- **Ads are planned, not optional**: AdMob integration in Week 6, live at Phase 1 launch. All library choices verified as Apache 2.0 / MIT — no AGPL or GPL dependencies.
- **Two-phase delivery**: PDF Scanner ships first (Week 8); Signing is additive in Phase 2.
- **Room deferred to Week 4**: Built alongside the Scanner feature — entities shaped by real usage.
- **Navigation 3**: Type-safe `@Serializable` routes + `NavDisplay` + `SnapshotStateList` back stack.
- **Local-first, no cloud sync**: All data stays on device; no Firebase Storage, no Retrofit.
- **No Hilt**: Koin chosen for future KMP compatibility.
- **Domain layer stays pure**: No Android or Koin imports in use cases.
- **No mocks in tests**: Fake repository implementations instead of Mockito.

---

## Success Criteria

**Phase 1 (Week 8)**
- ✅ Scan multi-page documents, manage and share PDFs — fully offline
- ✅ 80%+ domain + data layer test coverage
- ✅ Cold start < 2 seconds
- ✅ AAB signed and live on Play Store

**Phase 2 (Week 15)**
- ✅ Draw, image, and digital signatures embedded in PDFs
- ✅ Signature verification working
- ✅ AdMob banner + interstitial ads live in production
- ✅ GDPR/CCPA consent handled
- ✅ 70%+ total test coverage
- ✅ Updated Play Store listing live

---

**Version**: 4.0
**Status**: In Progress — Week 2 Complete (2.1 Core UI ✅, 2.2 Navigation 3 ✅)
