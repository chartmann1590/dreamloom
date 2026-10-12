# Google Play Store Listing — Dreamloom

## App title (max 30 chars)

```
Dreamloom: AI Dream Journal
```
(28 chars)

## Short description (max 80 chars)

```
Private dream journal: record by voice or text, on-device AI reads the meaning.
```
(79 chars)

## Full description (max 4000 chars)

```
Dreamloom is a private dream journal with an AI dream reader that runs entirely on your phone. Record a dream by voice or text the moment you wake up, and the app writes a title, picks out the symbols, explains what the dream might mean, and suggests an intention for the day. Your journal never leaves your device.

🌙 Record your dreams
• Speak your dream and it is transcribed, or type it
• Tag the mood of the night: serene, anxious, joyful, lost or fierce
• Entries are saved to an encrypted journal on your phone

🔮 On-device dream reading
• A small AI language model (Gemma, through LiteRT-LM) runs on your phone and reads each dream
• For every dream you get a title, a list of symbols, an interpretation and an intention for the day
• Dream text is never uploaded to a server for interpretation
• One-time download of the AI model (about 2.5 GB) during setup; after that, journaling and interpretation work offline

🗺️ Dream Atlas
• Search the full text of every dream
• Filter by this week, this month, a symbol or a mood
• Reopen any dream to read its interpretation again

📈 Weekly Insight
• Once a week the app reads your last seven saved dreams and writes a card with your recurring symbols, the pattern it sees and a short summary
• A mood strip shows how recent nights felt
• Run a new insight on demand

🧙 Dream Oracle
• Ask follow-up questions about your dreams and get a short answer, generated on your phone

⏰ Morning reminder
• An optional notification at the time you choose, with your own reminder text

✨ More
• Share a dream as an image
• Dark, light or system theme
• Erase the whole journal from Settings at any time

🔒 Privacy
• No account and no cloud sync; your dreams are stored only on your device
• Free, supported by ads (Google AdMob); watching a rewarded ad unlocks an extended interpretation or an on-demand weekly insight
• Optional anonymous usage analytics and crash reports help improve the app and can be turned off in Settings
• Privacy policy: https://chartmann1590.github.io/dreamloom/privacy.html

Requirements: Android 9 or newer, about 3 GB of free storage for the AI model, and the microphone permission if you want to record by voice. Interpretation speed depends on your phone.

Dreamloom is for reflection and curiosity. It is not medical, psychological or sleep advice.
```

## Category

**Primary**: Lifestyle
**Secondary**: Health & Fitness

## Tags / Keywords (Play Console "tags" + ASO keywords)

Primary keywords (target):
- dream journal
- dream interpretation
- ai journal
- private journal
- offline ai
- dream meaning
- lucid dream
- daily affirmation
- mindfulness journal
- dream symbols

Long-tail (high intent, low competition):
- private ai journal no cloud
- offline dream interpreter
- on-device ai diary
- ai dream meaning analyzer
- dream symbol decoder
- subconscious journal app

## Content rating

**Teen** (13+) — discusses dreams, may surface emotional content. No sexual, violent, or drug content.

## Contact email

`hello@dreamloom.app`

## Privacy policy URL

**Canonical file to host:** [play_store/privacy.html](privacy.html) (static HTML aligned with Firebase / AdMob / model download). The app and listing should use the same public URL as [app/src/main/res/values/strings.xml](../app/src/main/res/values/strings.xml) (`https://dreamloom.app/privacy`). For longer legal copy, still start from `tech/PRIVACY_POLICY_TEMPLATE.md`.

## Data safety form (Play Console)

Critical to fill out **honestly and minimally** — this is the app's biggest marketing asset:

- Does this app collect or share user data? **No** (for dream content / journal entries)
- Diagnostic data (Firebase Analytics, Crashlytics): collected, **not linked to user**, optional
- "Data is encrypted in transit": yes (only the model download)
- "You can request data deletion": yes (uninstall = full deletion; built-in "wipe everything" button in Settings)

The "Data Safety" badge that says "No data shared with third parties, no data collected" is THE selling point. Protect it ruthlessly.

## Screenshots — 8 portrait at 1080×1920 (or higher 9:16)

See `screenshot_concepts.md` for the 8-frame story. Use vector mockups + cosmic gradient backgrounds.

## Feature graphic — 1024×500

Centered word "Dreamloom" in elegant serif (Cormorant Garamond) over an aurora gradient (deep indigo → violet → moonglow gold), with a single thin crescent moon to the right. Tagline below: "Your dreams, decoded. Privately."

## App icon — 512×512

Crescent moon overlaid on a thread/loom motif. Monochrome silver-gold on deep indigo. Must read at 48dp on the home screen — keep it simple.

## Localization (priority order for v1.1)

1. English (US) — launch
2. Spanish (ES + LATAM)
3. Portuguese (BR)
4. German
5. French
6. Indonesian

Use Gemma's multilingual capability to translate the system prompts; UI strings via `strings.xml` resource folders.

## Internal testing track (BUILD_ORDER 15–16)

Screens **15 — Reminders** and **16 — About** (`spec/SCREENS.md`) are required for a trustworthy Settings area before internal testers: morning nudge, time + copy, version/model/AdMob/privacy link, OSS licenses. Confirm on a release build after uploading the model GitHub asset and setting `dreamloom.modelSha256` (see [SETUP.md](../SETUP.md)).

## ASO launch checklist

- [ ] Title contains primary keyword "Dream Journal"
- [ ] Short description repeats it within first 5 words
- [ ] Full description: keyword "dream" appears 8–12 times naturally, "AI" 4–6 times, "private/privacy" 4–6 times
- [ ] Feature graphic and first 2 screenshots tell the privacy story (text overlay)
- [ ] First review reply set up (template in Play Console)
- [ ] Pre-launch report run via Play Console internal testing track

## Monetization (Play Console)

The app is free to download, supported by advertising (AdMob). No in-app purchases or subscriptions in v1.
