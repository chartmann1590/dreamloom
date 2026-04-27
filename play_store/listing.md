# Google Play Store Listing — Dreamloom

## App title (max 30 chars)

```
Dreamloom: AI Dream Journal
```
(28 chars)

## Short description (max 80 chars)

```
Your dreams, decoded by AI — entirely on your phone. Private. Beautiful. Free.
```
(78 chars)

## Full description (max 4000 chars)

```
Wake up. Whisper your dream. Watch it bloom into meaning.

Dreamloom is the first dream journal where the AI lives entirely on your phone. No account. No cloud. No one — not even us — can read what you write. Just you, your dreams, and a private oracle that learns the shape of your subconscious.

★ ON-DEVICE AI — POWERED BY GEMMA 4 ★
Most "AI journal" apps quietly send everything you write to a server. Dreamloom is different. We use Google's Gemma 4 model running locally on your phone with LiteRT, so your most intimate words physically cannot leave your device. Open the app on a plane, in the woods, in airplane mode — it still works. Forever.

★ THE MORNING RITUAL ★
• Tap the moon. Speak or type your dream.
• Watch as Dreamloom decodes the symbols — water, falling, flying, faces — and weaves a poetic interpretation drawn from Jungian, Freudian, and folkloric traditions.
• Receive a daily intention spun from the emotional thread of your night.
• Save the entry to your private Dream Atlas.

★ THE DREAM ATLAS ★
A gorgeous timeline of every dream you've ever recorded. Filter by symbol, mood, or moon phase. Rediscover that dream from three months ago that felt important.

★ PATTERN INSIGHT ★
Every Sunday, Dreamloom quietly studies your week and shows you the recurring symbols, the colors of your moods, and the themes that have been weaving themselves through your sleep. (Optional. You'll never see this if you don't want it.)

★ DREAM ORACLE ★
Have a question? Ask the dream. Dreamloom draws on the symbols you've recorded over time and offers a single, calm answer.

★ DESIGN THAT FEELS LIKE A STARRY NIGHT ★
Cosmic dark mode by default. Hand-tuned typography. Soft particle motion. The kind of app you actually want to open at 6am.

★ FREE FOREVER ★
No subscription. No "premium" wall. Supported by occasional, respectful ads. The full app is yours from day one.

★ PRIVACY YOU CAN VERIFY ★
• No account required.
• No cloud sync (and we built it that way on purpose).
• No analytics on your dream content — only anonymous app-usage stats you can disable.
• Local database is encrypted at rest with SQLCipher.

You'll need about 2.6 GB of free storage for the AI model, downloaded on first launch over Wi-Fi. After that, everything runs offline. Forever.

Dream well.

— Dreamloom
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

Required by Play Store. Host a public page (e.g. GitHub Pages) using `tech/PRIVACY_POLICY_TEMPLATE.md` as a starting point. It should reflect Firebase Analytics / Crashlytics and AdMob as described in the template.

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

## ASO launch checklist

- [ ] Title contains primary keyword "Dream Journal"
- [ ] Short description repeats it within first 5 words
- [ ] Full description: keyword "dream" appears 8–12 times naturally, "AI" 4–6 times, "private/privacy" 4–6 times
- [ ] Feature graphic and first 2 screenshots tell the privacy story (text overlay)
- [ ] First review reply set up (template in Play Console)
- [ ] Pre-launch report run via Play Console internal testing track

## Monetization (Play Console)

The app is free to download, supported by advertising (AdMob). No in-app purchases or subscriptions in v1.
