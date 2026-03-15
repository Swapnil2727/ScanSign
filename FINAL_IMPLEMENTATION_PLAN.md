# PDF Scanner & Signer - Implementation Plan

**Timeline**: 15 weeks (Android MVP)
**Team**: 1 developer with access to Claude Code Pro

---

## Strategy: Two-Phase Delivery

**Phase 1 (Weeks 1–9): PDF Scanner MVP**
Build a fully functional, shippable PDF scanner app. Capture documents with the camera, generate PDFs, manage them locally, and share them. This phase ships as a standalone useful app.

**Phase 2 (Weeks 10–15): Signing Capabilities**
Layer digital, drawn, and image signatures on top of the completed PDF scanner. Signing is additive — the core scanner app is not disrupted.

This order means:
- Real users can get value from Phase 1 before Phase 2 is built
- The signing module is built against a stable, tested document foundation
- Scope risk is isolated: if signing takes longer, Phase 1 is already shippable

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
- `:core:ui`, `:core:database`, `:core:camera`, `:core:pdf`, `:core:signing`, `:core:di`, `:core:model`
- Features have no cross-dependencies; use navigation for communication

---

## Tech Stack

| Component | Choice | Reason |
|-----------|--------|--------|
| DI | Koin 4.0+ | KMP-ready, no code generation |
| UI | Jetpack Compose | Modern, state management friendly |
| Navigation | Navigation 3 (`navigation3-runtime/ui`) | Back-stack first, type-safe `@Serializable` routes, no NavController |
| Database | Room + SQLite | Type-safe, migration support |
| Async | Coroutines + Flow | Native Kotlin, KMP compatible |
| Camera | CameraX | Modern, handles device variations |
| PDF | Android PdfDocument / iText | PDF generation from images |
| Signing | Android Keystore | Native, secure private key storage |
| Serialization | Kotlin Serialization | Route keys, DataStore preferences |

---

## Weekly Tickets

---

## Phase 1: PDF Scanner MVP

---

### Week 1: Foundation ✅

**1.1 Project Setup**
- Create modular project structure with type-safe project accessors
- Configure Gradle with version catalog (`libs.versions.toml`)
- Set up build conventions

**1.2 Koin DI Framework**
- Initialize Koin in Application class
- Create `:core:di` module
- Define AppModule, DatabaseModule (shell)

**1.3 Core Models**
- Define `Document`, `DocumentPage`, `Signature` domain models
- Set up Kotlin Serialization

---

### Week 2: Core Infrastructure ✅

**2.1 Core UI Module** ✅
- Create `:core:ui` library; expose Compose + Material3 transitively via `api()`
- Move theme (Color, Theme, Type) out of `:app`
- Add `ScanSignSpacing` tokens and `@ThemePreviews` annotation

**2.2 Navigation 3** ✅
- Add Navigation 3 dependencies
- Define `@Serializable` route objects: `DocumentsRoute`, `ScannerRoute`, `SignerRoute`, `SettingsRoute`, `DocumentDetailRoute`
- `AppNavigation`: single `Scaffold` + `NavDisplay` driven by `SnapshotStateList` back stack
- Bottom bar with FAB-style centre Scan button; selected state from `backStack.last()`
- Placeholder screens for all destinations

**2.3 Room Database** ← deferred to Week 4
> Intentionally deferred. Room will be introduced alongside the Scanner feature
> so entities and DAOs are shaped by real usage, not assumptions.

---

### Week 3: Core Modules — Camera + PDF

**3.1 Camera Module (`:core:camera`)**
- Wrap CameraX with a clean, lifecycle-safe API
- `CameraPreview` composable (no business logic)
- Camera permission handling utility
- Capture callback that returns an image file path

**3.2 PDF Module (`:core:pdf`)**
- Evaluate Android `PdfDocument` vs iText for image-to-PDF quality
- `PdfGenerator`: accepts a list of image paths, returns a PDF file
- Handle page size, compression, and basic metadata (title, creation date)

---

### Week 4: Scanner Feature — Domain + Data (incl. Room)

**4.1 Room Database (`:core:database`)**
- `ScanSignDatabase` Room DB version 1
- `DocumentEntity`, `DocumentPageEntity` (no `SignatureEntity` yet — Phase 2)
- `DocumentDao`: insert, getAll, getById, delete
- `LocalDocumentDataSource`

**4.2 Scanner Domain**
- `DocumentRepository` interface
- `CaptureDocumentUseCase`: image path → save pages → generate PDF → persist
- `GetDocumentsUseCase`, `DeleteDocumentUseCase`
- Input validation in use cases

**4.3 Scanner Data**
- `DocumentRepositoryImpl` backed by Room + file storage
- Wire `DatabaseModule` in `:core:di`

---

### Week 5: Scanner Feature — UI

**5.1 Scanner Screen**
- Replace placeholder with real CameraX preview from `:core:camera`
- `ScannerViewModel`: manage capture state with StateFlow
- Multi-page capture flow (capture → review page → add more or finish)

**5.2 Document Editor**
- Per-page review: rotation, retake
- Page reorder before saving
- "Save as PDF" triggers `CaptureDocumentUseCase`
- Navigate to Documents list on success

---

### Week 6: Documents Feature

**6.1 Documents Domain**
- `GetDocumentsUseCase` with search/filter support
- `ShareDocumentUseCase` via Android `FileProvider` intent
- `DeleteDocumentUseCase` (file + DB row)

**6.2 Documents UI**
- Replace placeholder `DocumentsScreen` with real list backed by Room Flow
- Search bar with live filtering
- Document detail screen: PDF viewer + metadata
- Swipe-to-delete and share action

---

### Week 7: Settings + Phase 1 Polish

**7.1 Settings Feature**
- Replace placeholder `SettingsScreen`
- User preferences via DataStore: default PDF quality, app theme override
- Storage usage display (total PDFs, disk used)
- App info (version, privacy policy link)

**7.2 Phase 1 Polish**
- Edge-to-edge polish across all screens
- Empty states (no documents yet)
- Error states (camera permission denied, storage full)
- Loading indicators

---

### Week 8: Testing — Phase 1

**8.1 Unit Tests**
- Test all Phase 1 use cases with fake repositories
- Test `PdfGenerator` with sample images
- Target: 100% domain layer coverage

**8.2 ViewModel + Integration Tests**
- Test `ScannerViewModel` and `DocumentsViewModel` with fake use cases
- Integration test: full scan → save → list → delete flow with real Room (in-memory)
- Test navigation flows

**8.3 Performance**
- Profile cold start (target: < 2 seconds)
- Optimize Room queries with indices on `createdAt`, `title`
- Lazy-load thumbnails in document list
- Benchmark image compression settings

---

### Week 9: Phase 1 Release

**9.1 Play Store Preparation**
- Generate signed AAB
- Write store listing copy and screenshots (scanner-focused)
- Create privacy policy (local storage only, no data leaves device)
- Set up Google Play Console

**9.2 Beta Release**
- Internal testing on 3+ real devices
- Monitor crashes with Firebase Crashlytics (crash reporting only)
- Fix critical issues before promoting to production

---

## Phase 2: Signing Capabilities

---

### Week 10: Core Signing Module (`:core:signing`)

**10.1 Android Keystore Wrapper**
- `KeystoreManager`: generate RSA key pair, retrieve, delete
- Certificate self-signed generation (for digital signatures)
- `SignatureEntity` added to Room DB (migration version 2)
- `SignatureDao`: insert, getAll, delete

**10.2 Sign Document Use Case**
- `SignDocumentUseCase`: embed signature into existing PDF
- Support three signature types: drawn bitmap, image file, digital certificate
- `VerifySignatureUseCase`: validate digital signature on a PDF

---

### Week 11: Signing Feature UI

**11.1 Signing Screen**
- Replace placeholder `SignerScreen`
- Three tabs: Draw, Image, Digital
- Draw tab: canvas with pressure-sensitive stroke, clear + save
- Image tab: photo picker or gallery pick, crop to signature shape
- Digital tab: select or create a key pair, show certificate details

**11.2 PDF Signing Flow**
- Entry point: long-press a document → "Sign" action
- Signature placement overlay on PDF preview (drag to position)
- Confirm → `SignDocumentUseCase` → update document status to `SIGNED`

---

### Week 12: Signing Tests + Integration

**12.1 Signing Tests**
- Unit test `SignDocumentUseCase` and `VerifySignatureUseCase`
- Test Keystore operations on emulator and real device
- Integration test: scan → sign → verify end-to-end

**12.2 Full App Integration**
- End-to-end test: scan → documents list → sign → verify → share
- Navigation regression test (all routes)

---

### Week 13: Performance + Final Polish

**13.1 Performance**
- Profile signing operation (target: < 3 seconds for a 10-page PDF)
- Memory profiling during multi-page capture
- Final Room query optimisation pass

**13.2 Final Polish**
- Accessibility audit (content descriptions, touch targets)
- Dark/light theme audit on all screens
- Tablet layout check (Navigation 3 adaptive layout if needed)

---

### Week 14–15: Phase 2 Release

**14.1 Play Store Update**
- Update store listing to highlight signing capabilities
- New screenshots showing signing flow
- Bump `versionCode` and `versionName`

**14.2 Beta + Public Release**
- Beta on existing Phase 1 users
- Address feedback
- Promote to production

---

## Optional: Ad Integration (Post-Launch)

**Setup**: `:core:ads` module, Google Mobile Ads SDK, `AdManager` via Koin
**Placement**: Banner in document list + settings; interstitial after scan or sign
**Effort**: 2–3 weeks post-launch, zero impact on core codebase

---

## Key Decisions

- **Two-phase delivery**: PDF Scanner ships first; Signing is additive in Phase 2
- **Room deferred to Week 4**: Built alongside the first feature that uses it — entities shaped by real queries, not assumptions
- **Navigation 3**: Type-safe `@Serializable` routes + `NavDisplay` + `SnapshotStateList` back stack; no `NavController`
- **Local-first, no cloud sync**: All data stays on device; no Firebase Storage, no Retrofit
- **No Hilt**: Koin chosen for future KMP compatibility
- **No Room schema export**: Migrations tracked manually in a changelog
- **Domain layer stays pure**: No Android or Koin imports in use cases
- **No mocks in tests**: Fake repository implementations instead of Mockito
- **Kotlin Serialization**: Route keys + DataStore preferences; no JSON API needed

---

## Success Criteria

**Phase 1 (Week 9)**
- ✅ Scan multi-page documents, generate PDFs, manage and share — fully offline
- ✅ 80%+ test coverage on domain + data layers
- ✅ Cold start < 2 seconds
- ✅ AAB submitted to Play Store

**Phase 2 (Week 15)**
- ✅ Draw, image, and digital signatures embedded in PDFs
- ✅ Signature verification working
- ✅ 70%+ total test coverage
- ✅ Updated Play Store listing live

---

**Version**: 2.0
**Status**: In Progress — Week 2 Complete (2.1 Core UI ✅, 2.2 Navigation 3 ✅)
