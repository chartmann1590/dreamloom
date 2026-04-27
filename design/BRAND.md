# Brand — Dreamloom

The brand is the moat. Functionally similar apps already exist; what we sell is a feeling.

---

## North star

> **A quiet, private companion for the inner life — modern, beautiful, and yours alone.**

If a copy decision, color decision, animation decision, or feature decision conflicts with that line — kill it.

---

## Voice

- **Lyrical, not flowery.** "Wake up. Whisper your dream." Not "Welcome to your AI-powered dream interpretation platform."
- **Second person.** Always *you*. Never *the user*.
- **Short sentences.** Two or three words is fine. Periods are louder than commas.
- **Reverent of dreams, not of the AI.** We never hype the model. We never name it on user-facing screens (it's "the dream-reader"). The user is the protagonist.
- **No emoji in app copy.** Subtle iconography only.
- **No exclamation marks.** Not even on success states.

### Vocabulary, allowed:

dream, whisper, weave, loom, atlas, oracle, symbol, intention, the moon, the night, the morning

### Vocabulary, banned:

unleash, unlock, journey, transformative, AI-powered, smart, intelligent, revolutionary, magical, awesome, delight, leverage

---

## Sample copy

**Onboarding privacy panel:**
> No account.
> No cloud.
> Your dreams stay on your phone.

**Empty state — Atlas:**
> Your Atlas is empty.
> Tap the moon to add your first dream.

**Streaming label:**
> Weaving…

**Save success toast:**
> Saved to your Atlas.

**Wipe confirmation dialog:**
> Erase everything?
> Every dream, every interpretation, every pattern — gone. The model file too.
> [Cancel] [Erase]

**Daily reminder notification:**
> Tap the moon. While the dream is still warm.

---

## What Dreamloom is NOT

- Not a therapy app. (Liability + tone.)
- Not a productivity tool. (Wrong feeling.)
- Not a social app. (Privacy story breaks.)
- Not a game. (No streaks framed as competition; "consistency" is fine, "leveling up" is not.)
- Not "AI" — we are a dream journal, that happens to use AI quietly.

---

## Logo

A crescent moon overlaid on a faint loom motif (three crossing threads). Monochromatic gold-on-indigo. The loom motif disappears at small sizes — only the crescent remains, which is the right hierarchy.

Provide three sizes:
- Adaptive icon (108dp foreground)
- Notification icon (24dp, single-color silhouette of just the crescent)
- Feature graphic mark (1024×500, crescent + wordmark)

---

## Color associations

| Color | Means | Use |
| --- | --- | --- |
| Indigo / Night | Inwardness, sleep | Backgrounds |
| Aurora / Violet | The subconscious | Accents, primary actions |
| Moonglow / Gold | Insight, the daily intention | Highlight moments |
| Mood colors | Emotion of a single night | Atlas dots, mood selector |

Resist the urge to use colors outside this palette. Even error states use the existing red.

---

## Typography emotion

- **Cormorant** = the dream itself. Reverent, literary, slow.
- **Inter** = the room around the dream. Functional, steady, calm.

Never use Cormorant for buttons or labels — it loses authority outside its register.

---

## Marketing channels (post-launch — passive-income loop)

1. **TikTok** — 30-second videos: a person waking up, whispering a dream, watching it weave into meaning. The aesthetic is the marketing. Hashtags: `#dreamjournal #aijournal #privacymatters #manifestation #spirituality`. Posting: 3×/week, no paid ad spend, cross-post to Reels and YouTube Shorts. Goal: organic loop.
2. **Reddit** — `r/Dreams`, `r/LucidDreaming`, `r/Privacy`, `r/Android`. Post the launch as a "I built this because no good dream journal runs on-device" story. Honest, not spammy.
3. **Product Hunt** — single launch day. Submission emphasizes the on-device privacy angle.
4. **Hacker News** — title: "Show HN: A dream journal that runs Gemma 4 entirely on your phone". Skews technical; the audience cares about the engineering and will share.
5. **Google Play organic** — winning ASO with the keywords listed in `play_store/listing.md`. The Play Store privacy badge ("No data collected") is itself a search advantage in the wellness category.

No paid acquisition. The economics of $0.04–0.10 ARPDAU only work with organic growth.

---

## Critical brand promises (encode into product)

These appear in marketing AND must be true in product. Engineering is responsible for keeping them true.

1. "Your dreams never leave your phone." → No network calls during interpretation. Period.
2. "No account." → No login screen. Anywhere.
3. "Free forever." → No paywalls. No "premium" tier. No "limited free trial." No IAP at all.
4. "Works offline." → After model download, every feature works in airplane mode.
5. "Encrypted on your phone." → SQLCipher on the dreams DB.

A single broken promise here destroys the brand. Treat them as invariants.
