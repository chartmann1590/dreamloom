# Dreamloom — MVP Specification

The MVP is what ships to Play Store on day one. Everything in `[P0]` must work. `[P1]` is "ship within 30 days". `[P2]` is roadmap.

---

## [P0] Onboarding

A 4-screen flow, no signup, no email:

1. **Welcome** — full-bleed cosmic gradient, "Dreamloom" in Cormorant Garamond, "Your dreams, decoded. Privately." Tap to continue.
2. **Privacy promise** — three short rows: "No account.", "No cloud.", "Your dreams stay on your phone." A faint moon icon. Tap to continue.
3. **Model download** — "Dreamloom uses an AI brain that lives entirely on your phone. We'll download it once (about 2.6 GB). It's free, and you'll never need internet again after this." Buttons: "Download on Wi-Fi only" (default) / "Download now". A clean progress bar with MB downloaded + estimated time. (See `tech/MODEL_DOWNLOAD.md`.)
4. **Permissions ask** — microphone permission with friendly explainer ("So you can whisper your dream while your eyes are still closed."). Notification permission with "Optional — for a gentle morning nudge."

After this: land on the Home screen. **Never re-show onboarding.**

## [P0] Home screen — the moon

Centered: a softly pulsing crescent moon button, ~40% of the screen. Tap to start a new dream entry. Below: "Last night's moon was waning gibbous." (computed locally, no API.)

A small bottom navigation: Home (moon icon), Atlas (book icon), Insight (compass icon), Settings (gear icon). 4 tabs only.

## [P0] New Dream Entry flow

1. Tap moon → fades into a recording sheet.
2. **Voice mode (default)**: large mic button. Press and hold OR tap to start/stop. Live waveform. Real-time transcription via Android `SpeechRecognizer` shown in soft serif type.
3. **Text mode**: small "type instead" link. Multi-line text field. Same poetic placeholder: "I dreamt of…"
4. Optional: tap "+" to attach a photo of a dream sketch (gallery or camera). Stored locally, optionally analyzed by Gemma 4 multimodal.
5. Mood selector (5 emojis: serene, anxious, joyful, lost, fierce — plus a "skip"). Adds a single mood color tag.
6. Save → triggers Gemma interpretation pipeline.

## [P0] Interpretation pipeline

Streamed Gemma response. UI shows a "loom weaving" animation while text streams in. Output structure (enforced by prompt — see `spec/PROMPTS.md`):

1. **Title** — short, evocative, generated from content (e.g., "Falling through warm water")
2. **Symbols** — 3–6 symbols extracted, each as a chip
3. **Interpretation** — one paragraph, ~120 words, lyrical but grounded
4. **Daily Intention** — one sentence, action-oriented, written for today

Save the entry to Room with: id, timestamp, raw text, transcript audio path (optional), photo path (optional), mood, list of symbols, full interpretation, intention, model version.

## [P0] Dream Atlas (timeline)

A vertical timeline of all entries, newest first. Each card:
- Date in soft gold, day-of-week in muted text
- Mood-color dot (5 colors)
- The interpretation **title** in serif
- 2-line preview of interpretation
- Symbol chips at the bottom

Tap a card → full Detail screen with the original text, full interpretation, photo if any, mood, intention. "Re-interpret" button (rerun Gemma on same text — useful for users who got a weak first pass).

Filter chips at top: All / This Week / Symbol picker (bottom sheet listing all symbols user has ever recorded with counts). Search by free text (Room FTS — but only on entry text, not interpretation, since users will mostly remember what they wrote).

## [P0] Settings

- **Privacy** — display the privacy receipts (see screenshot Frame 7). "Wipe everything" big red button (double-confirm dialog → drops Room DB + deletes model file).
- **Reminders** — toggle morning nudge. Time picker (default 7:30am).
- **Theme** — Dark (default), Light, System.
- **Voice** — toggle "Auto-stop after 30s of silence".
- **About** — version, model version, build number, an honest "How is this free?" explainer about AdMob.
- **Send feedback** — opens email client to `hello@dreamloom.app`. No in-app form (avoid backend cost).

## [P0] AdMob integration (see `spec/MONETIZATION.md` for full plan)

- App Open ad on cold start, 4-hour cooldown
- Interstitial after closing a dream Detail screen, 1 per session, 60s cooldown
- Rewarded video gates: "Generate dream image" (P1), "Extended interpretation" (P0), "Weekly Insight" (P0)

## [P1] Pattern Insight (weekly)

Every Sunday morning, a card appears on Home: "Your week, woven." Tap → Insight screen showing:
- Top 5 recurring symbols this week (with counts)
- Mood color band — one bar per night, blend of mood colors
- A 60-word AI-written summary of themes
- Tappable symbols → list of dreams that contained each

## [P1] Daily Oracle

A discoverable feature on the Home screen below the moon: "Ask the dream." Tap → small input → Gemma synthesizes from the user's recent symbols + the question, returns one short paragraph.

## [P1] Photo of dream sketch → multimodal interpretation

Use Gemma 4 E2B's vision capability. User attaches a photo, prompt becomes "{user_text}\n\nUser also drew this. Use the drawing as additional symbolic input." Vision pass adds 1–3 extra symbols.

## [P1] Dream image generation (rewarded)

After interpretation, a CTA: "Watch a short ad to see your dream illustrated." Tap → rewarded video → on completion, send the interpretation title + symbols to Gemma to generate a richly descriptive prompt, then to **Stable Diffusion XS or SDXL Turbo via LiteRT** (later — for v1.1 — start by using a remote free generator like Pollinations.ai with privacy disclosure for users who opt in).

## [P2] Lucid dreaming trainer

Reality checks: scheduled notifications throughout the day asking "Are you dreaming right now?" Optional symbol journal cross-references that build a "dream signs" personal dictionary.

## [P2] Sleep tracker integration

Read Health Connect. Correlate dream content with sleep stages.

## [P2] Pattern reports — monthly + yearly

Year-in-review with shareable card (designed for TikTok/Instagram aspect ratio). Strong viral loop.

## [P2] Localization

Spanish, Portuguese, German, French, Indonesian. Gemma is multilingual; UI strings via Android resources.

---

## Out of scope (deliberately)

- **Social / sharing within the app** — privacy story breaks. Users can screenshot.
- **Cloud sync** — privacy story breaks. Use Android's automatic backup of the local DB if user opts in (via standard Android backup, encrypted, Google's responsibility).
- **Account / login** — adds friction, breaks privacy story.
- **Subscription** — cannibalizes ads, breaks "free forever" hook.
- **Web app companion** — we are mobile, on-device, only.
