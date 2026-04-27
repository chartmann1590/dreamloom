# Cursor — Read This First

You are building **Dreamloom**, an Android app. The whole specification is in this folder. Before writing a single line of code, read in this order:

1. `../README.md` — the pitch and stack
2. `../spec/MVP.md` — what to build
3. `../spec/SCREENS.md` — every screen
4. `../tech/STACK.md` — the dependency list
5. `../tech/ARCHITECTURE.md` — how it fits together
6. `../tech/GEMMA_INTEGRATION.md` — the LLM — most error-prone integration; read carefully
7. `../tech/MODEL_DOWNLOAD.md` — the 2.6 GB model dance
8. `../spec/PROMPTS.md` — copy these prompts verbatim into Kotlin
9. `../spec/ADS.md` — AdMob placement and rules
10. `../design/SYSTEM.md` and `../design/BRAND.md` — visual + copy
11. `BUILD_ORDER.md` — the ordered task list

---

## Hard rules

These are non-negotiable. Violating any of them breaks the product:

1. **No network call ever sends dream content anywhere.** The only outbound HTTP calls allowed: model download (GitHub Releases), AdMob impressions, Firebase Analytics events (only metadata, never content), Crashlytics (only stack traces, no user data).
2. **Single LlmEngine instance per process.** Hilt singleton, owned by Application. Never recreate on rotation.
3. **The 5 brand promises in `design/BRAND.md` are invariants.** Add an instrumented test for each.
4. **No banner ads in the main flow.** Settings → About only.
5. **No interstitial during streaming, recording, or onboarding.** See `spec/ADS.md` for the full list.
6. **No IAP or subscription billing.** The product uses ads only; do not add in-app purchases or subscriptions.
7. **Encrypt the local DB.** SQLCipher with a key stored in EncryptedSharedPreferences.
8. **Don't bundle the model file in the AAB.** Always download via `ModelDownloadWorker`.
9. **Match the visual style.** Cormorant + Inter. Indigo + Aurora + Moonglow. No other fonts, no other primary colors.
10. **Use the prompts verbatim.** They've been tuned. Treat them as configuration, not source code to "improve".

---

## Things to clarify with the human BEFORE building

- The GitHub username / org for hosting model releases (placeholder in `tech/MODEL_DOWNLOAD.md`).
- The actual AdMob app ID + ad unit IDs (placeholders below).
- The Firebase project name + `google-services.json` location.
- The package name (suggest: `app.dreamloom`).
- The signing keystore (release builds need it before the first Play Store upload).
- The contact email for the Play Store listing.

If any of those is missing, use a clearly-labeled placeholder (`TODO_REPLACE_WITH_REAL_ID`) and surface them all in a `SETUP.md` you generate in the project root.

---

## Placeholders to replace before release

| Placeholder | Where it appears | What to put |
| --- | --- | --- |
| `<user>` (GitHub) | `tech/MODEL_DOWNLOAD.md`, code constants | actual GitHub user/org |
| `REPLACE_AT_BUILD_TIME` (SHA-256) | `ModelConfig.SHA256` | computed from final hosted model |
| `ca-app-pub-XXXXXXXXX/XXXXXXXXX` | AdMob ad unit IDs | from AdMob console |
| `TODO_REPLACE_WITH_REAL_ID` | various | as needed |
| `hello@dreamloom.app` | Play listing, About | a real or forwarded address |

For development, use Google's official AdMob test ad unit IDs. They're listed in the AdMob docs and never change. **Never push real ad IDs to a debug build** — Google can ban the AdMob account.

```kotlin
object AdUnits {
    const val APP_OPEN_TEST     = "ca-app-pub-3940256099942544/9257395921"
    const val INTERSTITIAL_TEST = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED_TEST     = "ca-app-pub-3940256099942544/5224354917"
    const val BANNER_TEST       = "ca-app-pub-3940256099942544/9214589741"

    val APP_OPEN     get() = if (BuildConfig.DEBUG) APP_OPEN_TEST     else /* real */ "TODO_REPLACE"
    val INTERSTITIAL get() = if (BuildConfig.DEBUG) INTERSTITIAL_TEST else /* real */ "TODO_REPLACE"
    val REWARDED     get() = if (BuildConfig.DEBUG) REWARDED_TEST     else /* real */ "TODO_REPLACE"
    val BANNER       get() = if (BuildConfig.DEBUG) BANNER_TEST       else /* real */ "TODO_REPLACE"
}
```

---

## How to test what you can't run locally

You don't have an Android device with 6 GB RAM and a Gemma model handy. So:

- For UI work, use Compose Previews + the emulator.
- For LLM-dependent code paths, **provide a `FakeLlmEngine`** that returns a canned, parseable string for each prompt type. Wire it via Hilt qualifier in `debug` source set.
- Add a debug-only "Run model self-test" button that lights up the real engine when a developer is on a real device. Document it in About → Debug.

A representative `FakeLlmEngine.generateStream` for the interpretation prompt:

```kotlin
class FakeLlmEngine : LlmEngine {
    override fun generateStream(prompt: String, config: GenerationConfig, imageFile: File?) =
        flow {
            val text = """
                TITLE: Falling through warm water
                SYMBOLS: water, falling, light
                INTERPRETATION: You moved downward, but slowly, as if held by something that did not want to drop you. The water was warm — that is the part to listen to. Falling, in dreams, often names a release; warm water names a being-held. Together they suggest you are letting go of something you have been carrying alone, and noticing for the first time that the letting go has become safe.
                INTENTION: Today, let one thing happen without your hand on it.
            """.trimIndent()
            text.chunked(8).forEach { emit(it); delay(40) }
        }
}
```

---

## Code style

- Kotlin official style guide (`kotlin.code.style=official`).
- One Composable per file when it's a screen; helpers can stack in the same file when small.
- ViewModels expose a single `StateFlow<UiState>` per screen. Events go through a single `onAction(SomeIntent)` method.
- No callbacks-for-the-sake-of-callbacks; use Flows.
- All `runCatching` errors are logged via `Crashlytics.recordException` UNLESS they may contain dream content (in which case log only the exception type).

---

## Definition of "done" for the MVP

- [ ] Cold install on a Pixel 7 with 6 GB RAM works through onboarding to first dream interpretation in under 10 minutes (download dominates time).
- [ ] In airplane mode, after model download, every screen works.
- [ ] All five brand-promise instrumented tests pass.
- [ ] No Crashlytics non-fatals in a 30-minute manual smoke test.
- [ ] Play Console pre-launch report shows zero crashes across the device matrix.
- [ ] AdMob test ads render in all 4 placements.
- [ ] Wipe-everything truly erases data (verify by inspecting `filesDir` after).
- [ ] APK/AAB size under 25 MB (no bundled model).
- [ ] First-token latency on Pixel 7 GPU backend < 1 second on warm engine.
- [ ] All copy in `design/BRAND.md` "sample copy" section appears verbatim where stated.

When all of the above are true, push to the Play Console internal testing track and let the human verify.

---

## When you don't know

If a LiteRT-LM API method differs from what `tech/GEMMA_INTEGRATION.md` describes, **don't guess**. The integration shape is right but the exact symbol names may have changed in the final SDK. Open Android Studio's "Find Symbol", inspect `GenAI` and `GenAIModelOptions`, and adapt.

If the design system doesn't specify a behavior, **err toward simplicity and silence**. We are a calm app. When in doubt, do less.

If the brand voice is unclear for a piece of copy, **prefer fewer words**.
