# Build order (source of truth: Atlas & detail)

Aligned with `spec/SCREENS.md`, `spec/MVP.md`, and `spec/ADS.md`. Implement in this order for Atlas + dream detail workstreams.

1. **Atlas — timeline** — vertical list of dream cards (date, mood dot, title, 2-line interpretation preview, symbol chips). Newest first.
2. **Atlas — filters** — time: All / This week / This month. Symbol: bottom sheet with all symbols the user has logged (with counts), optional. Mood: picker + clear. Filters compose with the list/FTS.
3. **Atlas — FTS** — top search; Room `dreams_fts` on `rawText` only. Search query is debounced; escape/build safe `MATCH` tokens.
4. **Atlas — long-press** — quick delete (confirm) and share as image.
5. **Detail — full** — back + date, title, chips, your dream, interpretation, photo, intention card, overflow menu.
6. **Detail — re-interpret** — rerun Gemma on the same `rawText` (and photo if any), then persist interpretation.
7. **Detail — share** — render share card to PNG, `ACTION_SEND` via `FileProvider`.
8. **Detail / back — interstitial** — pre/post detail `AdGate` (lifetime dream count, 90s in session, 1/session, 60s since last) + `InterstitialManager`; preloads on screen; on back, show when allowed, then `popBackStack`.

P1: rewarded gates on detail (**extended interp** only for MVP sequencing) are separate work.

### Ignore for MVP

Do **not** implement or schedule until after core MVP ship (`spec/MVP.md` — *Explicitly post-MVP* and matching `[P2]` sections):

- **iOS** or cross-platform work
- **Heavy localization** (non–English UI; MVP is English-only strings)
- **Dream image generation** (rewarded `rew_dream_image`, SD / LiteRT / any remote image path)
- **Sleep / Health Connect** (dream–sleep correlation)
- Other **P2-only** roadmap items (lucid trainer, monthly/yearly shareable reports, etc.) unless explicitly promoted into a milestone

### Phase 14 — Quality gate

- **Brand promises** (`design/BRAND.md`): five instrumented checks in `BrandPromiseInstrumentedTest` (interpretation stack local-only, no account flow, no Play Billing, Room persistence without HTTP, SQLCipher file not plaintext SQLite).
- **Room / repository**: JVM tests under `app/src/test/.../DreamDaoTest`, `DreamRepositoryTest`.
- **Analytics**: event names and parameters in repo-root `INSTRUCTIONS.md`; implementation `DreamloomAnalytics` + call sites (save, interpret, re-interpret, delete, model download, weekly insight, oracle).
