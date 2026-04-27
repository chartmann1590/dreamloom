# Architecture

Single-activity Compose app, Hilt DI, MVVM with a thin layer of repositories. Coroutines + Flows everywhere. The defining constraint is the LLM: it owns ~1.7 GB RAM and one CPU/GPU resource — we want one and only one engine instance, lazy-loaded, kept alive across screens.

---

## Layers

```
ui (Compose screens) ──> ViewModel ──> Repository ──> Datasource (Room | Llm | Ads | DataStore | FileSystem)
```

- **ui**: Compose composables, no business logic, observes `StateFlow` from VM.
- **ViewModel**: per screen, holds `StateFlow<UiState>`, delegates to repos.
- **Repository**: orchestrates multiple datasources, returns Flows.
- **Datasource**: leaf — talks to one thing.

## Module map (single module, package-by-feature)

```
app/
├── DreamloomApplication.kt        // Hilt entry, ad SDK init, gemma warm-up
├── MainActivity.kt                // single activity, NavHost
├── di/                            // Hilt modules
├── core/
│   ├── time/Clock.kt
│   ├── crypto/PassphraseProvider.kt   // EncryptedSharedPrefs → SQLCipher key
│   └── ext/...
├── data/
│   ├── db/                        // Room entities, DAOs, RoomDatabase
│   ├── prefs/                     // DataStore wrapper
│   └── repo/
│       ├── DreamRepository.kt
│       ├── InsightRepository.kt
│       └── SettingsRepository.kt
├── llm/
│   ├── LlmEngine.kt               // singleton wrapper around LiteRT-LM
│   ├── LlmEngineState.kt          // sealed class: NotLoaded, Loading, Ready, Error
│   ├── DreamInterpreter.kt        // builds prompts, calls engine, parses output
│   └── prompts/                   // prompt templates as Kotlin objects
├── ads/
│   ├── AdGate.kt                  // frequency caps
│   ├── AppOpenAdManager.kt
│   ├── InterstitialAdManager.kt
│   └── RewardedAdManager.kt
├── audio/
│   └── VoiceTranscriber.kt        // wraps SpeechRecognizer
├── work/
│   ├── ModelDownloadWorker.kt
│   └── ReminderWorker.kt
└── feature/
    ├── onboarding/
    ├── home/
    ├── newdream/
    ├── detail/
    ├── atlas/
    ├── insight/
    ├── oracle/
    └── settings/
```

---

## The LLM lifecycle (most important detail)

The Gemma engine is a **process-scoped singleton**, owned by `LlmEngine`. It's created once per process (lazily, on first dream entry attempt) and never re-created across configuration changes. Loading takes ~3–8 seconds on a Pixel 7 — so we warm it up on app start IF the model file is present, and surface state via a `StateFlow<LlmEngineState>`:

```kotlin
sealed class LlmEngineState {
    data object NotLoaded : LlmEngineState()
    data class Loading(val progress: Float) : LlmEngineState()
    data object Ready : LlmEngineState()
    data class Error(val cause: Throwable) : LlmEngineState()
}
```

**Critical rules:**

1. The engine instance is held by `LlmEngine` (Hilt `@Singleton`). It MUST be held by the application object, not any activity, so screen rotations don't unload it.
2. Calls to `generate()` are dispatched on a single-threaded `CoroutineDispatcher` (we wrap `Executors.newSingleThreadExecutor().asCoroutineDispatcher()`). Concurrent generation breaks LiteRT-LM.
3. On `onLowMemory()` from Application, we **do not** unload the engine — that would just trigger a 5-second reload on next use. Instead we trim caches and request a GC.
4. On `onTrimMemory(LEVEL_BACKGROUND)` we keep it alive too. Only on `LEVEL_COMPLETE` do we release it.
5. The engine is released and recreated if Settings → "Re-load model" is tapped (rare, only for debugging).
6. Streaming results are exposed as a `Flow<String>` — partial token chunks. The repository layer accumulates into a `StringBuilder` for parsing.

See `tech/GEMMA_INTEGRATION.md` for the actual `LlmEngine` implementation.

---

## Threading model

- **UI**: `Dispatchers.Main`
- **DB**: Room's own dispatcher (it manages its threads)
- **LLM generation**: dedicated single-threaded dispatcher inside `LlmEngine`
- **Audio transcription**: dispatched as it runs (Android `SpeechRecognizer` is callback-based on Main)
- **Model download**: `WorkManager` foreground worker
- **Image decode**: Coil's pool
- **Insight generation (weekly)**: WorkManager periodic work, runs on `LlmEngine`'s dispatcher

---

## Data flow — the canonical "save dream" sequence

```
User taps Save on NewDream
   │
   ▼
NewDreamViewModel.saveDream()
   │
   ├─> DreamRepository.create(text, mood, photoUri?)        (Room insert, returns id)
   │
   ├─> emits state = Interpreting(id)
   │
   ├─> DreamInterpreter.interpret(id, text, mood, photoUri?)
   │       │
   │       ├─> builds prompt from prompts/InterpretationPrompt.kt
   │       ├─> LlmEngine.generateStream(prompt, params, photoUri?) : Flow<String>
   │       │       (engine handles its own dispatcher internally)
   │       │
   │       ├─> collect chunks, append to StringBuilder
   │       ├─> as each labeled section completes (TITLE, SYMBOLS, INTERPRETATION, INTENTION)
   │       │     emit InterpretationProgress(sectionsSoFar)
   │       │
   │       └─> on stream end: parse, return Interpretation
   │
   ├─> DreamRepository.attachInterpretation(id, parsedInterpretation)
   │
   └─> Navigate to DreamDetail(id), pop NewDream from back stack
         │
         └─> AdGate.shouldShowInterstitial(...) when user later backs out of Detail
```

---

## Ads lifecycle

- **App Open ad**: preloaded by `AppOpenAdManager`, registered as `LifecycleObserver` on `ProcessLifecycleOwner`. On `ON_START` (app resumes from background), check `AdGate.shouldShowAppOpen()` — if yes, show. Always preload the next one immediately after.
- **Interstitial**: preloaded by `InterstitialAdManager`. Show is requested by `DreamDetailViewModel.onBackPressed()` after consulting `AdGate`. Never blocks navigation — if not loaded, just skip.
- **Rewarded**: explicit user-initiated. Preload on entering Detail / Insight screens. Show is awaited as a coroutine; on success, run the gated action.

---

## Database schema (Room)

```kotlin
@Entity(tableName = "dreams")
data class DreamEntity(
    @PrimaryKey val id: Long,         // epoch ms
    val createdAt: Long,
    val rawText: String,
    val mood: String,                 // serene/anxious/joyful/lost/fierce/skip
    val photoPath: String?,           // app-internal storage
    val title: String?,
    val symbolsJson: String?,         // JSON array of strings
    val interpretation: String?,
    val intention: String?,
    val modelVersion: String?,        // "gemma-4-e2b-it-int4"
    val isInterpretationComplete: Boolean,
)

@Entity(tableName = "insights")
data class InsightEntity(
    @PrimaryKey val weekStartEpochDay: Long,
    val createdAt: Long,
    val pattern: String,
    val summary: String,
    val invitation: String,
    val topSymbolsJson: String,
)

// FTS for free-text search of dream rawText
@Entity(tableName = "dreams_fts")
@Fts4(contentEntity = DreamEntity::class)
data class DreamFts(val rawText: String)
```

DB is encrypted at rest with SQLCipher; passphrase generated on first run, stored in `EncryptedSharedPreferences` (which uses Android Keystore under the hood).

---

## Process startup sequence

```
DreamloomApplication.onCreate()
   ├─> Firebase init
   ├─> AdMob init  (MobileAds.initialize, async)
   ├─> Crashlytics enabled if user opted in
   ├─> Hilt graph ready
   ├─> ProcessLifecycleOwner observer (App Open ad, model warmup)
   └─> If model file present + verified: kick off LlmEngine warm-up coroutine on Default dispatcher
```

`MainActivity.onCreate()`: just sets `setContent { DreamloomApp() }`. No business logic.

---

## Things that are intentionally simple

- **No Compose performance optimization until measured.** Material 3 + recompose-friendly state usage is enough for our scale.
- **No DI in tests for the LLM**: replace `LlmEngine` with a fake interface that returns a canned stream. Don't try to load Gemma in unit tests.
- **No multi-process model**: AdMob does its own process work; we don't.
- **No custom theming engine**: Material 3 dynamic colors + a fixed `Dreamloom` palette overlay. Done.
