# PDF Scanner & Signer - Implementation Plan

**Timeline**: 15 weeks (Android MVP)
**Team**: 1 developer with access to Claude Code Pro

---

## Architecture

### Layers
- **Presentation**: Jetpack Compose + ViewModel + StateFlow
- **Domain**: Use Cases + Repository Interfaces
- **Data**: Repository Implementations + Local Data Sources

### Modularization
Feature-based modules with layer separation:
- :feature:scanner, :feature:signer, :feature:documents, :feature:settings
- :core:ui, :core:database, :core:camera, :core:pdf, :core:signing, :core:di, :core:model
- Features have no cross-dependencies; use navigation for communication

---

## Tech Stack

| Component | Choice | Reason |
|-----------|--------|--------|
| DI | Koin 4.0+ | KMP-ready, no code generation |
| UI | Jetpack Compose | Modern, state management friendly |
| Navigation | Jetpack Navigation Compose | Type-safe, Compose-native |
| Database | Room + SQLite | Type-safe, migration support |
| Async | Coroutines + Flow | Native Kotlin, KMP compatible |
| Camera | CameraX | Modern, handles device variations |
| PDF | Android PdfDocument / iText | PDF generation from images |
| Signing | Android Keystore | Native, secure private key storage |
| Serialization | Kotlin Serialization | KMP-ready, used for preferences |

---

## Weekly Tickets

### Week 1: Foundation ✅

**1.1 Project Setup**
- Create modular project structure
- Configure Gradle with version catalog
- Set up build conventions

**1.2 Koin DI Framework**
- Initialize Koin in Application class
- Create core:di module
- Define AppModule, DatabaseModule

**1.3 Core Models**
- Define Document, DocumentPage, Signature entities
- Set up Kotlin Serialization

---

### Week 2: Core Infrastructure

**2.1 Core UI Module**
- Create shared Compose components
- Extract theme, typography, spacing tokens
- Set up preview utilities

**2.2 Navigation**
- Define route objects for each feature
- Create NavGraph with nested graphs per feature
- Wire bottom navigation to routes

**2.3 Database Module**
- Create Room database with version 1
- Define DocumentDao, SignatureDao
- Set up repository interfaces

---

### Week 3: Core Features

**3.1 Camera Module**
- Wrap CameraX with a clean API
- Implement camera preview composable
- Handle permissions and lifecycle

**3.2 PDF Module**
- Evaluate and integrate PDF library
- Create PDF generation from captured images
- Handle compression and metadata

---

### Week 4-5: Feature 1 - Scanner

**4.1 Scanner Domain**
- Define DocumentRepository interface
- Create CaptureDocumentUseCase
- Add input validation

**4.2 Scanner Data**
- Implement DocumentRepository with Room
- Create LocalDocumentDataSource

**4.3 Scanner UI**
- Create ScannerScreen with CameraX preview
- Implement ScannerViewModel with state management
- Implement capture → review → save flow

**5.1 Document Editor**
- Add image rotation and crop
- Preview before saving to PDF
- Save final document locally

---

### Week 6-7: Feature 2 - Documents

**6.1 Documents Domain**
- Create GetDocumentsUseCase, DeleteDocumentUseCase
- Implement share via Android intent
- Handle document metadata updates

**6.2 Documents UI**
- Create document list screen with search
- Add document detail / PDF viewer
- Implement delete and share actions

---

### Week 8-10: Feature 3 - Signing

**8.1 Signing Module**
- Create Android Keystore wrapper
- Implement certificate management
- Create SignDocumentUseCase

**8.2 Signing Feature**
- Create signing screen with draw, image, and digital tabs
- Implement PDF embedding for drawn signatures
- Add signature verification

**9.1 Settings Feature**
- Create settings screen
- User preferences (theme, default save location)
- App info and storage usage

---

### Week 11-13: Testing & Optimization

**11.1 Unit Tests**
- Test all use cases and repository implementations
- Use fake data sources instead of mocks
- Target: 100% domain layer coverage

**11.2 Feature Tests**
- Test ViewModels with fake use cases
- Test UI state transitions
- Target: 80%+ total coverage

**12.1 Integration Tests**
- Test scanner → documents → signer end-to-end with real Room database
- Test navigation flows

**13.1 Performance**
- Profile app startup (target: <2 seconds cold start)
- Optimize Room queries with indices
- Implement lazy loading for document list
- Compress images before PDF generation

---

### Week 14-15: Release

**14.1 Play Store Preparation**
- Generate signed APK / AAB
- Write store listing copy and screenshots
- Create privacy policy (local storage only, no data leaves device)
- Set up Google Play Console

**14.2 Beta Release**
- Internal testing on 3+ real devices
- Monitor crashes with Firebase Crashlytics (crash reporting only, no sync)
- Fix critical issues

**15.1 Public Release**
- Final Play Store submission
- Release to production

---

## Optional: Ad Integration (Post-Launch)

**Setup**:
- Create :core:ads module
- Integrate Google Mobile Ads SDK
- Create AdManager injectable via Koin

**Ad Placement**:
- Banner ads in document list and settings screens
- Interstitial ads after scan or sign operations
- Rewarded interstitial for removing ads temporarily

**Implementation**:
- Use test ad unit IDs in debug builds
- Configure production unit IDs in release builds
- Handle user consent for GDPR/CCPA

**Effort**: 2-3 weeks post-MVP, no impact on core codebase

---

## Key Decisions

- **Local-first, no cloud sync**: All data stays on device; no Firebase Storage, no Retrofit
- **No Hilt**: Koin chosen for future KMP compatibility
- **No Room schema export**: Migrations tracked manually in a changelog
- **Domain layer stays pure**: No Android or Koin imports in use cases
- **No mocks in tests**: Fake repository implementations instead of Mockito
- **Kotlin Serialization**: Used for DataStore preferences, no JSON API needed

---

## Success Criteria (Week 15)

- ✅ Scan, sign, manage, and share documents — fully offline
- ✅ 70%+ test coverage
- ✅ Cold start under 2 seconds
- ✅ APK / AAB signed and submitted to Play Store

---

**Version**: 1.1
**Status**: In Progress — Week 1 Complete
