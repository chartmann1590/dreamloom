# Dreamloom

**Your dreams, decoded. Privately.**

An on-device AI dream journal and daily oracle. Record or type your dream, and Gemma (via LiteRT-LM) runs entirely on your phone: symbols, themes over time, and a personal daily intention. No account. No cloud. Your dreams stay on the device.

This repository is the Android app source. For build steps, see [SETUP.md](SETUP.md) and [cursor/INSTRUCTIONS.md](cursor/INSTRUCTIONS.md).

---

## Features

- On-device LLM (Gemma) for interpretation; optional voice input and local storage
- Dream Atlas, weekly insight, daily oracle (per [spec/MVP.md](spec/MVP.md))
- Google AdMob for App Open, interstitial, and rewarded placements (see [spec/ADS.md](spec/ADS.md))

---

## Repository layout

```
Dreamloom/
├── README.md
├── SETUP.md
├── cursor/           INSTRUCTIONS.md, BUILD_ORDER.md
├── play_store/       listing and screenshot copy drafts
├── spec/             MVP, screens, prompts, ADS
├── tech/             stack, architecture, model download, etc.
└── design/           brand and UI system
```

---

## Stack (summary)

- **Android**: Kotlin, Jetpack Compose, Material 3, Hilt, Room + SQLCipher, WorkManager
- **LLM**: Gemma via LiteRT-LM; large model file downloaded on first launch (see [tech/MODEL_DOWNLOAD.md](tech/MODEL_DOWNLOAD.md))
- **Ads**: Google Mobile Ads (AdMob) with optional mediation adapters
- **Analytics / crashes**: Firebase Analytics and Crashlytics (see [tech/STACK.md](tech/STACK.md))

## Where to start (contributors)

Open [cursor/INSTRUCTIONS.md](cursor/INSTRUCTIONS.md) and follow [cursor/BUILD_ORDER.md](cursor/BUILD_ORDER.md).
