# Screens & Navigation

Every screen in the MVP, in build order. Use Jetpack Compose Navigation with type-safe routes (`androidx.navigation:navigation-compose:2.8.4` or later). Single-activity architecture.

---

## Navigation graph

```
NavHost(start = "splash")
├── splash                        (decides: onboarding vs home)
├── onboarding/
│   ├── welcome
│   ├── privacy
│   ├── modelDownload
│   └── permissions
├── home
├── newDream/
│   ├── recording
│   └── interpreting (intermediate, with streaming)
├── dreamDetail/{id}
├── atlas
├── insight
├── oracle
└── settings/
    ├── root
    ├── privacy
    ├── reminders
    └── about
```

---

## 1. Splash

Cosmic gradient, faint logo. 600ms minimum. Logic:
- Has user completed onboarding? → `home`
- No? → `onboarding/welcome`
- Is model file missing or corrupt? → `onboarding/modelDownload`

## 2. Onboarding — Welcome

- Full-screen aurora gradient (animated subtly)
- "Dreamloom" in 64sp Cormorant Garamond
- "Your dreams, decoded. Privately." in 18sp Inter
- Single CTA: "Begin" — large pill button

## 3. Onboarding — Privacy

- Three rows, fade in sequentially:
  - 🌙 No account
  - 🌙 No cloud
  - 🌙 Your dreams stay on your phone
- Below: "Continue"

## 4. Onboarding — Model Download

- Headline: "First, let's bring the dream-reader home."
- Body: "Dreamloom uses an AI brain that lives entirely on your phone. We download it once — about 2.6 GB — and then you'll never need internet again."
- Toggle: "Wi-Fi only" (default ON)
- Big CTA: "Download (2.6 GB)"
- After tap: progress bar, MB count, time estimate. Cancellable. If Wi-Fi only and on cellular → polite wait state.
- See `tech/MODEL_DOWNLOAD.md` for the WorkManager implementation.

## 5. Onboarding — Permissions

- Microphone request with rationale: "So you can whisper your dream while your eyes are still closed."
- Notifications request: "Optional — for a gentle morning nudge to capture last night."
- Either result → `home` (we never block on permissions; voice mode falls back to text if denied).

## 6. Home

```
┌──────────────────────────────┐
│           Dreamloom          │  small wordmark, top
│                              │
│                              │
│            ☾                 │  pulsing crescent button (~280dp)
│                              │
│      tap to record a dream   │
│                              │
│   Last night was waning      │  tiny moon-phase line
│       gibbous · day 4        │
│                              │
│   ─── Ask the dream ─── (P1) │  optional CTA
│                              │
├──────────────────────────────┤
│  ☾    📖    🧭    ⚙          │  bottom nav
└──────────────────────────────┘
```

Behavior:
- The moon button has a 2-second pulse (scale 1.0 → 1.04 → 1.0) and a soft glow.
- If a "Sunday Insight" is ready, show a card above the moon: "Your week, woven. →"
- Tap moon → animates upward as a fade transition to `newDream/recording`.

## 7. New Dream — Recording

```
┌──────────────────────────────┐
│  ←  New dream         📷  ⌨  │  back, photo, switch-to-text
│                              │
│   I dreamt of…               │  placeholder if not yet started
│                              │
│   [transcribed text appears  │  serif type, 22sp
│    here as you speak]        │
│                              │
│         ▁▃▆█▆▃▁              │  waveform
│           ●                  │  big red mic stop button
│                              │
│  serene · anxious · joyful   │  mood selector
│   · lost · fierce · skip     │
│                              │
│         [ Save dream ]       │
└──────────────────────────────┘
```

- Default: voice mode. Tap mic to start (request permission if needed). Tap again to stop. Auto-stop after 30s silence (configurable).
- Photo button: optional photo attachment. If attached, shown as a small thumbnail above the waveform.
- Text mode (⌨): replaces mic with a multi-line `TextField`.
- Mood: required, 5 chips + "skip".
- Save: validates min 10 characters of text, then navigates to `newDream/interpreting`.

## 8. New Dream — Interpreting (streaming)

A full-screen sheet with a "loom weaving" animation (slow horizontal lines criss-crossing, gold-on-indigo). Text streams in below as Gemma generates:

```
─── Title ───
Falling through warm water

─── Symbols ───
[water] [falling] [light]

─── Interpretation ───
You moved downward, but slowly, as if held…
[stream continues, ~120 words]

─── Daily Intention ───
Today, let one thing happen without your hand on it.
```

When streaming completes, "Save & view" pulses. On tap → `dreamDetail/{id}` (replaces this screen in the back stack so user can't go back to interpreting).

If the user backs out mid-stream, save partial text but don't claim the interpretation is finished. Show a "Re-interpret" button on the detail screen.

## 9. Dream Detail

```
┌──────────────────────────────┐
│  ←  Apr 12 · Mon       ⋯     │  date, more menu (delete, re-interp)
│                              │
│  Falling through warm water  │  big serif title
│                              │
│  [water] [falling] [light]   │  symbol chips
│                              │
│  Your dream                  │  small label
│  "I was sinking but it       │  monospace-ish, italic
│   wasn't scary…"             │
│                              │
│  Interpretation              │  small label
│  You moved downward…         │  full Gemma output, paragraph
│                              │
│  Today's intention           │
│  Today, let one thing…       │  card with gold border
│                              │
│  [Generate dream image] (post-MVP) │  rewarded video gate — omit until shipped
│  [Extended interpretation]   │  rewarded video gate
└──────────────────────────────┘
```

- More menu: Delete (confirm), Re-interpret (regenerates), Share as image (renders a card to image and offers system share — for TikTok/IG virality).
- After back press from this screen → trigger interstitial check (1 per session, 60s cooldown).

## 10. Atlas

Vertical scroll of dream cards. Top: filter chips (All / Week / Month / Symbol picker / Mood picker / Search). Empty state: "Your Atlas is empty. Tap the moon to add your first dream."

Each card: see `MVP.md`. Long-press for quick actions (delete, share). Tap → `dreamDetail/{id}`.

## 11. Insight

Empty until the first Sunday after the user has logged 3+ dreams. Then shows the latest weekly insight + a list of past weekly insights (read-only history). See `MVP.md` P1.

If insight is gated by rewarded video, show a tasteful CTA: "Tap to weave this week's insight" → rewarded ad → generation.

## 12. Oracle (P1)

Single-input screen. "Ask the dream." Big text field. Below: "The dream draws on the symbols of your past 30 nights." Submit → streamed Gemma response in a soft card. History of past oracles below.

## 13. Settings — Root

A simple list:
- Privacy → `settings/privacy`
- Reminders → `settings/reminders`
- Theme: Dark / Light / System
- Voice: Auto-stop toggle
- About → `settings/about`
- Send feedback (opens mailto)

## 14. Settings — Privacy

- "What we collect" section listing nothing for journal data.
- Toggle: "Share anonymous app-usage analytics" (Firebase Analytics — defaults ON, can opt out).
- Toggle: "Crash reports" (Firebase Crashlytics — defaults ON, can opt out).
- "Wipe everything" — destructive button, double-confirm dialog. On confirm: drops Room DB, deletes model file, clears DataStore, restarts to onboarding.

## 15. Settings — Reminders

- Toggle: "Morning nudge"
- Time picker (default 7:30am)
- Free-text "What it should say" (default: "Tap the moon. While the dream is still warm.")

## 16. Settings — About

- Version / build number
- Model version (e.g., "Gemma 4 E2B-IT, int4, May 2026")
- Open-source licenses (auto-generated by `com.google.android.gms:oss-licenses-plugin`)
- Short paragraph that the app is supported by advertising (AdMob), with a link to the privacy policy.
- A subtle credit line: "Made with care."

---

## Transitions

- All forward navigation: 250ms fade + 50ms slide-up (16dp) — feels weightless.
- Back navigation: instant (Compose default).
- The moon button → Recording transition: shared-element transition where the moon shrinks to the top-right corner.

## Loading & error states

- Model not loaded yet (rare): show a quick "The dream-reader is waking up…" with a soft animation. Block dream entry until ready.
- Out of memory / Gemma fails to load: graceful card "We need a bit more memory to wake the dream-reader. Try closing other apps." Offer "Try again."
- Storage full during model download: clear error with "Free up at least 3 GB and try again."
