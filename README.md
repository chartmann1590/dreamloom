# Dreamloom

**Your dreams, decoded. Privately.**

An on-device AI dream journal and daily oracle. Open the app each morning, voice-record or type the dream you just had, and Gemma 4 (running entirely on your phone) interprets the symbols, tracks themes over time, and weaves a personal daily intention from your subconscious.

No account. No cloud. No subscription. Your dreams never leave your phone.

---

## The pitch in one paragraph

Every existing AI dream journal sends your most intimate thoughts to a server and charges $5–$15/month. Dreamloom is the first to run a real LLM (Gemma 4 E2B via LiteRT-LM) entirely on-device, so dreams stay sealed on the user's phone — and the app is free, monetized only by AdMob. The product wraps that privacy story in a beautiful cosmic aesthetic, daily-habit retention loops (morning ritual → oracle pull → evening reflection), and TikTok-friendly shareable artifacts (dream cards, weekly pattern reports). Target demo: women 18–35 in Tier-1 countries — high install volume, high ad eCPM, strong organic loop via spirituality / wellness creators.

## Why this concept wins

| Factor | Dreamloom | Cloud competitors (Lucidity, Oniri, Dreamly) |
| --- | --- | --- |
| Privacy | 100% on-device, no account | Account + cloud sync |
| Cost | Free + AdMob | $5–15/mo subscription |
| Offline | Works on a plane | Doesn't work without signal |
| AI quality | Gemma 4 E2B (32K ctx) | GPT-3.5 class wrappers |
| Multimodal | Voice + photo of dream sketch | Text only mostly |
| Marketing hook | "The journal that doesn't read your dreams" | Generic AI features |

## What's in this folder

```
Dreamloom/
├── README.md                       (this file — start here)
├── cursor/
│   ├── INSTRUCTIONS.md             read first if you are Cursor
│   └── BUILD_ORDER.md              ordered task list to ship MVP
├── play_store/
│   ├── listing.md                  title, descriptions, keywords, ASO
│   └── screenshot_concepts.md      what each store screenshot shows
├── spec/
│   ├── MVP.md                      feature list with priority
│   ├── SCREENS.md                  every screen + navigation
│   ├── PROMPTS.md                  Gemma system + few-shot prompts
│   └── MONETIZATION.md             AdMob placements + frequency caps
├── tech/
│   ├── STACK.md                    libs + versions
│   ├── ARCHITECTURE.md             modules, data flow, threading
│   ├── GEMMA_INTEGRATION.md        exact LiteRT-LM recipe
│   └── MODEL_DOWNLOAD.md           how to deliver the 2.58 GB model
└── design/
    ├── SYSTEM.md                   colors, type, spacing, components
    └── BRAND.md                    voice, tone, visual identity
```

## Stack at a glance

- **Android** Kotlin + Jetpack Compose + Material 3 (Material You)
- **Min SDK 28** (Android 9+, ~98% of active devices); target SDK latest
- **LLM**: Gemma 4 E2B IT via LiteRT-LM (`com.google.ai.edge.litert:litert-genai:1.0.0-beta01`), int4, ~2.58 GB model file streamed from GitHub Releases on first launch
- **Speech-to-text**: Android `SpeechRecognizer` (free, on-device on Pixel/Samsung; cloud fallback warned in UI)
- **Storage**: Room (SQLite) with SQLCipher; EncryptedSharedPreferences for keys; everything local
- **Ads**: Google Mobile Ads SDK (AdMob) — App Open + Interstitial + Rewarded
- **Analytics**: Firebase Analytics (Spark — free) + Crashlytics (free)
- **Hosting** for model file: GitHub Releases (free, generous bandwidth)
- **CI**: GitHub Actions (free for public repos)

## Free-tier accounting

| Service | Free tier | Where we land | Headroom |
| --- | --- | --- | --- |
| Firebase Analytics | unlimited | within | infinite |
| Firebase Crashlytics | unlimited | within | infinite |
| GitHub Releases | unlimited public bandwidth | model download | fine |
| AdMob | n/a (revenue side) | n/a | n/a |
| Play Console | $25 one-time | one-time | already paid |

**Total recurring cost to operate the app: $0.**

## Revenue model

AdMob only. No subscription, no IAP. See `spec/MONETIZATION.md` for placements, but the loop is:

1. **App Open ad** on cold start (4-hour cooldown) — biggest eCPM driver
2. **Interstitial** after closing a dream entry (1 per session cap)
3. **Rewarded video** to unlock: AI-generated dream image, extended interpretation, weekly pattern report
4. No banner ads in main flow (kills retention) — only on Settings/About

Conservative projection at 100K MAU with US-skewed audience: ~$3K–8K/month. At 1M MAU: ~$30K–80K/month.

## Where to start (Cursor)

Open `cursor/INSTRUCTIONS.md` and follow `cursor/BUILD_ORDER.md` step by step.
