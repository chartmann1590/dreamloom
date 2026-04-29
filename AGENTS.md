# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project

Dreamloom is a single-module Android app: a private dream journal where on-device Gemma (via LiteRT-LM) interprets entries. Everything stays on-device — Room (SQLCipher), DataStore, internal file storage. Network is only used for first-time model download, ads, and Firebase analytics/Crashlytics (both opt-out).

## Build / run / test

JDK 17 is required. Toolchain is AGP 8.7, Kotlin 2.1, Gradle 8.10, `compileSdk` 35, `minSdk` 28.

```bash
./gradlew :app:assembleDebug              # debug APK (applicationId suffix .debug)
./gradlew :app:installDebug               # install on connected device/emulator
./gradlew :app:assembleRelease            # requires dreamloom.modelSha256 (see SETUP.md)
./gradlew :app:bundleRelease              # AAB for Play

./gradlew :app:testDebugUnitTest          # JVM/Robolectric tests under app/src/test
./gradlew :app:test --tests "com.charles.app.dreamloom.data.db.DreamDaoTest"   # single test
./gradlew :app:connectedAndroidTest       # instrumented tests on a device (BrandPromiseInstrumentedTest)
./gradlew :app:lintDebug                  # Android lint
```

`local.properties` must define `sdk.dir`. Release builds **fail** in `gradle.taskGraph.whenReady` unless `dreamloom.modelSha256` is set in `local.properties` or via `-Pdreamloom.modelSha256=...` (debug uses the `REPLACE_AT_BUILD_TIME` placeholder and skips verification). See `SETUP.md` for the full release checklist (Firebase `google-services.json`, AdMob ids in `app/src/main/res/values/ads.xml`, signing).

## Architecture

Single-activity Compose app with Hilt DI and MVVM. Layers: `ui` (Compose) → `ViewModel` (`StateFlow<UiState>`) → `Repository` → datasource (Room / `LlmEngine` / DataStore / Ads / FileSystem). Package-by-feature under `app/src/main/java/com/charles/app/dreamloom/`:

- `DreamloomApplication` (`@HiltAndroidApp`) — initializes Firebase, AdMob, applies `Telemetry` flags from `AppPreferences`, and schedules `WeeklyInsightWorkScheduler` and reminders. Implements `Configuration.Provider` for `HiltWorkerFactory`.
- `MainActivity` — only sets `setContent { DreamloomApp(...) }`. Reads `EXTRA_OPEN_ROUTE` for notification deep-links.
- `ui/` — `DreamloomApp` + `AppNavHost`. `navigation/Routes.kt` is the canonical route table.
- `feature/` — `onboarding`, `home`, `newdream` (recording → interpreting), `detail`, `atlas`, `insight`, `oracle`, `settings`. One screen ≈ one VM, no business logic in composables.
- `data/db` — Room `AppDatabase` (entities: `DreamEntity`, `DreamFts` FTS4, `InsightEntity`, `OracleEntity`). Encrypted with SQLCipher via `SupportFactory`; passphrase from `core/crypto/PassphraseProvider` (EncryptedSharedPreferences → Keystore).
- `data/repo/DreamRepository` — orchestrates DAO + interpretation persistence (raw → partial → full).
- `data/prefs/AppPreferences` — DataStore-backed settings (theme, opt-ins, reminders, ad-gate state).
- `data/WipeDataUseCase` — closes engine + DB, deletes model file, clears prefs/keystore, restarts process.
- `llm/` — see "LLM lifecycle" below.
- `ads/` — `AdGate` (frequency caps) + `InterstitialManager`. Test ad unit ids are used when `BuildConfig.DEBUG`.
- `work/` — WorkManager workers: `ModelDownloadWorker` (resumable OkHttp download with SHA-256 verify, multi-mirror from `ModelConfig.SOURCES`), `WeeklyInsightWorker`, `ReminderWorker` + scheduler.
- `telemetry/` — `Telemetry.apply` toggles Firebase Analytics + Crashlytics collection. `DreamloomAnalytics` logs the events listed in `INSTRUCTIONS.md`. **Never include dream text, titles, symbols, or user identifiers** — only counts and coarse flags.
- `di/` — `AppModule` (Clock, OkHttp), `DatabaseModule` (encrypted Room).

### LLM lifecycle (most load-bearing detail)

`llm/LlmEngine` is a Hilt `@Singleton` wrapper around the LiteRT-LM `Engine` (`com.google.ai.edge.litertlm:litertlm-android`). Hold rules:

1. **One engine per process.** Owned by the application, never by an activity. Survives configuration changes. The model file is ~2.6 GB on disk and ~1.7 GB resident — do not load twice.
2. **All generation runs on a single-threaded dispatcher** (`dreamloom-llm` thread). Concurrent generation breaks LiteRT-LM. `ensureLoaded()` is idempotent and synchronizes engine creation.
3. **Backend selection** is automatic via `llm/backend/QnnDetector` + `GpuDetector`: NPU (Hexagon) → GPU (OpenCL) → CPU.
4. State is a `StateFlow<LlmEngineState>` (`NotLoaded` / `Loading(progress)` / `Ready` / `Error`). UI observes this.
5. `generateStream` returns a `Flow<String>` of **delta** chunks (only the new text since last emission). `DreamInterpreter` accumulates into a `StringBuilder` and parses sections (TITLE / SYMBOLS / INTERPRETATION / INTENTION) via `llm/Parsers.kt`.
6. `DreamloomApplication.onTrimMemory` deliberately does **not** release the engine on `TRIM_MEMORY_UI_HIDDEN` — reload would cost several seconds. Only `WipeDataUseCase` and explicit settings actions release it.

`llm/ModelConfig` holds the version, filename (`gemma-4-E2B-it-int4.litertlm`), expected size, and download mirrors. `BuildConfig.MODEL_SHA256` (set from `dreamloom.modelSha256`) is what `ModelDownloadWorker` verifies against.

### Privacy invariants

- All dream content stays in the encrypted Room DB or app-internal file storage. Never log it, never send it to any network destination, never include it in analytics parameters.
- `INSTRUCTIONS.md` defines the exact analytics event names + parameters; stick to that schema.
- Firebase collection state is re-applied at process start from `AppPreferences` (`Telemetry.apply`) — keep that path in sync with any new opt-out UI.
- `BrandPromiseInstrumentedTest` (`app/src/androidTest`) enforces five invariants (interpretation local-only, no account flow, no Play Billing, Room without HTTP, SQLCipher file not plaintext SQLite) — keep it green.

## Conventions

- Compose-only UI; no XML layouts. Material 3 + the fixed Dreamloom palette in `ui/theme`.
- One Gradle module. Don't split unless the codebase grows past ~30 KLOC.
- Tests: Robolectric + Room in-memory for DAO/repository under `app/src/test`. Don't try to load Gemma in unit tests — fake `LlmEngine` with a canned `Flow<String>`.
- Internal Cursor docs live in `cursor/`, `spec/`, `tech/`, and `design/`. `cursor/BUILD_ORDER.md`, `tech/ARCHITECTURE.md`, `tech/GEMMA_INTEGRATION.md`, and `tech/MODEL_DOWNLOAD.md` are the most useful when planning changes.
