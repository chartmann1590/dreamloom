# Build Order — MVP

The exact sequence to ship Dreamloom to Play Store internal testing. Each step has a clear "done" line. Don't proceed to the next step until the current one is done.

Estimated total: **~6–10 working days** of focused work for one engineer.

---

## Phase 0 — Project skeleton (Day 0, half a day)

1. Create new Android Studio project, **Empty Activity**, Kotlin + Compose.
2. Set package to `app.dreamloom`. Set minSdk = 28, targetSdk = 35.
3. Apply the dependencies and plugins exactly as in `tech/STACK.md`.
4. Create the package layout from `tech/ARCHITECTURE.md`.
5. Add a Hilt `Application` class (`DreamloomApplication`).
6. Replace `MainActivity` with a single-activity Compose host that just renders "Dreamloom" centered (placeholder).
7. Initialize Firebase in the Application (placeholder `google-services.json` from a fresh Firebase project).

**Done when**: app builds, installs on emulator, shows "Dreamloom" centered, no crashes.

## Phase 1 — Design system (Day 0.5)

1. Add Cormorant Garamond and Inter as `app/src/main/res/font/` assets.
2. Implement `DreamColors`, `DreamShapes`, `DreamloomTypography`, `DreamloomTheme` from `design/SYSTEM.md`.
3. Create a `Preview` activity that demos all components: MoonButton, DreamCard, SymbolChip, section label, bottom nav, streaming loom animation.

**Done when**: Compose previews show every component matching `design/SYSTEM.md` specs, on dark.

## Phase 2 — Navigation + screens scaffold (Day 1)

1. Set up `androidx.navigation:navigation-compose` with the routes in `spec/SCREENS.md`.
2. Create empty Composables for every screen (Home, NewDream, DreamDetail, Atlas, Insight, Oracle, Onboarding screens, Settings screens). Each just renders its title.
3. Wire bottom nav with the 4 destinations. Splash logic stubbed (always go to Home).

**Done when**: tapping the bottom nav switches screens, all routes are reachable.

## Phase 3 — Onboarding (Day 1.5)

1. Implement Welcome, Privacy, ModelDownload, Permissions screens per `spec/SCREENS.md`.
2. Use DataStore to persist `onboarding_complete = true` after permissions screen.
3. Wire splash logic: `onboarding_complete` ? Home : Welcome.
4. Model download screen calls a stubbed worker that fakes progress over 5 seconds (real worker comes in Phase 5).

**Done when**: a fresh install runs the full onboarding, persists completion, and never re-shows onboarding.

## Phase 4 — Room + repositories (Day 2)

1. Add Room + SQLCipher per `tech/ARCHITECTURE.md`. Implement `DreamEntity`, `InsightEntity`, `DreamFts`.
2. Add a `PassphraseProvider` using `EncryptedSharedPreferences` (generates a 32-byte random key on first run).
3. Implement `DreamRepository` with: `create`, `attachInterpretation`, `delete`, `wipeAll`, `observeAll(): Flow<List<Dream>>`, `byId(id): Flow<Dream?>`.
4. Implement `SettingsRepository` for theme, reminders, analytics opt-out.
5. Wire Hilt modules.

**Done when**: a unit test inserts a dream, observes it, deletes it, wipes all — passes against an in-memory Room DB.

## Phase 5 — Real model download (Day 2.5)

1. Implement `ModelDownloadWorker` per `tech/MODEL_DOWNLOAD.md`.
2. Host a placeholder model file (any file ~50 MB) on a GitHub Release for testing the download path.
3. Wire the onboarding ModelDownload screen to enqueue the worker and observe progress.
4. Add `BackupRules` XML to exclude the model dir from auto-backup.

**Done when**: onboarding download succeeds with the placeholder file on a real device, file lands at `filesDir/models/`, SHA verification skipped (constant placeholder), onboarding completes.

## Phase 6 — LlmEngine + DreamInterpreter wiring (Day 3)

1. Implement `LlmEngine` per `tech/GEMMA_INTEGRATION.md` against the real LiteRT-LM SDK. Add a debug self-test screen.
2. Implement `FakeLlmEngine` for `debug` source set (returns the canned interpretation).
3. Implement `DreamInterpreter` and `parseInterpretation`.
4. Add Hilt `@Qualifier`/`@Named` to swap real vs fake based on a build config flag.
5. Run the self-test on a real device with the actual Gemma 4 E2B model file. Confirm parseable output.

**Done when**: tapping "Run model self-test" on a real device with the real model returns a structured Interpretation with all four fields populated.

## Phase 7 — New Dream flow (Day 3.5)

1. Build the Recording screen: voice + text modes, mood selector, photo attach.
2. Wire `VoiceTranscriber` to Android `SpeechRecognizer`. Show partial transcripts.
3. Build the Interpreting screen: streaming UI with the loom animation.
4. On Save: insert dream → stream Gemma → on completion, attachInterpretation → navigate to Detail.

**Done when**: voice-record a dream, see streamed interpretation, land on Detail screen, find the dream in Atlas.

## Phase 8 — Atlas + Detail (Day 4)

1. Build the Atlas timeline with filter chips and search (Room FTS).
2. Build Dream Detail with all fields and the more menu (delete, re-interpret, share-as-image).
3. Implement "share as image" using a `Bitmap` rendered from a Compose layout (`ComposeView.drawToBitmap` or capture with `GraphicsLayer`).

**Done when**: scroll through Atlas, tap any card, see Detail, share as image, delete, see Atlas update.

## Phase 9 — Settings (Day 4.5)

1. Build all Settings sub-screens per `spec/SCREENS.md`.
2. Implement "Wipe everything": drop Room DB, delete model file, clear DataStore + EncryptedSharedPreferences, restart to onboarding.
3. Implement OSS licenses screen via the Play services plugin.

**Done when**: every Settings toggle persists; "Wipe everything" returns the app to a fresh-install state.

## Phase 10 — Ads (Day 5)

1. Initialize AdMob in `DreamloomApplication.onCreate()`.
2. Implement `AppOpenAdManager` registered on `ProcessLifecycleOwner`.
3. Implement `InterstitialAdManager`, hook into `DreamDetailViewModel.onBackPressed()`.
4. Implement `RewardedAdManager` with two gates: extended interpretation (Detail), weekly insight (Insight).
5. Implement `AdGate` with all frequency caps from `spec/MONETIZATION.md`.
6. Integrate UMP for EU consent.
7. Use **test ad unit IDs in debug**, real IDs in release.

**Done when**: app open ad shows on cold start with cooldown respected; interstitial shows on Detail back-press with caps; rewarded ads gate the two features.

## Phase 11 — Pattern Insight (Day 5.5)

1. Build Insight screen.
2. Add a `WeeklyInsightWorker` (WorkManager periodic, every Sunday 9am local).
3. Worker generates an insight via `LlmEngine` from the past 7 dreams, persists `InsightEntity`.
4. Insight screen shows latest + history.

**Done when**: on a debug device, manually trigger the worker, see a weekly insight appear.

## Phase 12 — Daily Oracle (Day 6)

1. Build the Oracle screen.
2. Implement Oracle prompt + flow that pulls user's top symbols from the past 30 days.
3. Stream response, persist as a small `OracleEntity`.

**Done when**: ask a question, get a streamed response that references actual recent symbols.

## Phase 13 — Reminders (Day 6.5)

1. Implement `ReminderWorker` scheduled by user time setting.
2. Notification channel: "Morning nudge", low-importance.
3. Tapping the notification opens Home with the moon already focused.

**Done when**: scheduled reminder fires, opens Home, doesn't double-fire.

## Phase 14 — Brand-promise tests (Day 7)

Add instrumented tests under `androidTest/`:

1. `DreamContentNeverLeavesDeviceTest` — set up a `MockWebServer`, intercept all OkHttp/Cronet calls; create a dream; assert the server saw zero requests containing the dream text.
2. `NoLoginScreenExistsTest` — search the app for any "Sign in" / "Log in" / "Email" UI. Asserts none.
3. `OfflineModeWorksTest` — toggle airplane mode in the test, verify Home → Recording → Interpretation works end-to-end with the real engine.
4. `EncryptedAtRestTest` — open the SQLite file directly, assert it does not contain plaintext "dreamt" or any other dream-typical word.
5. `NoIapExistsTest` — assert `BillingClient` not in dependencies; assert no `IInAppBillingService` references.

**Done when**: all five tests pass on the emulator + a real device.

## Phase 15 — Play Store assets (Day 7.5)

1. Generate the 8 screenshots per `play_store/screenshot_concepts.md` in Figma; export 1080×1920.
2. Generate feature graphic and app icon.
3. Write privacy policy from `tech/PRIVACY_POLICY_TEMPLATE.md` (generate this from `play_store/listing.md` if it doesn't exist) and host on a GitHub Pages site.
4. Fill out Data Safety form — minimal as documented.
5. Push to internal testing track.

**Done when**: the Play Console pre-launch report comes back clean. Open the listing on a phone — looks beautiful.

## Phase 16 — Beta + iterate (Days 8–10)

1. Invite 20–50 beta testers (Reddit r/Dreams + personal network).
2. Collect feedback for 5–7 days.
3. Fix top 3 reported issues.
4. Promote to closed → open testing → production.

---

## What to ignore for MVP

- iOS port. (Out of scope.)
- Localization beyond English. (P1.)
- Dream image generation. (P1, gated behind rewarded ad.)
- Sleep tracker integration. (P2.)
- Lucid dreaming trainer. (P2.)
- Year-in-review. (P2, but design now since it's the strongest viral loop later.)

---

## What to instrument from day one

- Anonymous Firebase Analytics events (only metadata):
  - `dream_saved` (with mood as a property, NOT the text)
  - `interpretation_complete` (with `tok_per_sec` bucket: `<10`, `10–30`, `>30`)
  - `model_download_started`, `model_download_succeeded`, `model_download_failed` (with reason)
  - `ad_app_open_shown`, `ad_interstitial_shown`, `ad_rewarded_completed` (per-unit)
  - `setting_wipe_everything_confirmed`
- Crashlytics: enabled by default, opt-out in Settings.
- Never log dream text to anything other than the local DB.
