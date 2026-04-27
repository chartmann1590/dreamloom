# Play Store Screenshot Concepts

8 portrait screenshots, 1080×1920 minimum. First 3 are decisive — most users never scroll further.

Each frame: phone mockup (Pixel 9 silhouette) on aurora gradient background (#0f0a2e → #2d1b69 → #6b3fc4), big sans-serif headline above, app icon row below. Use Inter Bold 56pt for headlines.

---

## Frame 1 — The privacy hook (most important)

**Headline**: "The dream journal that doesn't read your dreams"
**Sub**: "100% on-device AI. Your words never leave your phone."
**Phone shows**: Home screen with the moon-tap button pulsing softly, faint aurora particles. Status bar shows airplane mode icon (visual proof).

## Frame 2 — The morning ritual

**Headline**: "Wake up. Whisper your dream."
**Sub**: "Gemma 4 turns it into meaning while you brush your teeth."
**Phone shows**: Voice-recording state — large microphone, waveform animating, "Listening…" caption. Soft sunrise gradient on the app background.

## Frame 3 — The interpretation

**Headline**: "Symbols, decoded."
**Sub**: "Jungian, Freudian, folkloric — drawn from a single gentle voice."
**Phone shows**: Interpretation screen. Headline "Falling through warm water" with three highlighted symbols (water, falling, light). One paragraph of poetic interpretation, then a "Daily Intention" card.

## Frame 4 — The Dream Atlas

**Headline**: "Every dream you've ever had. Beautifully kept."
**Sub**: "Your Atlas, encrypted, on your phone alone."
**Phone shows**: Vertical timeline of dream cards, each with date / mood color dot / first line / symbol icons. Scrollable feel.

## Frame 5 — Pattern Insight

**Headline**: "What your dreams have been trying to tell you."
**Sub**: "Weekly patterns, recurring symbols, mood arcs — generated locally."
**Phone shows**: Sunday Insight screen. Soft circular chart of recurring symbols (water, doors, falling), a sentence-long summary, mood color bands across the week.

## Frame 6 — Daily Oracle

**Headline**: "Ask the dream a question."
**Sub**: "A single, calm answer drawn from the symbols of your past nights."
**Phone shows**: Oracle screen. Question typed: "Why have I been dreaming of doors?" Below it, a short, lyrical reply written in serif type, framed by faint moonlight.

## Frame 7 — Privacy receipts

**Headline**: "Verify it yourself."
**Sub**: "No account. No cloud. Built so we couldn't read you if we tried."
**Phone shows**: Settings → Privacy panel. Three rows: "Data shared with us: None", "Account: None", "Cloud sync: Disabled (and impossible)". A "Wipe everything" button below.

## Frame 8 — Free forever

**Headline**: "No subscription. Ever."
**Sub**: "The full Dreamloom is free. Always. (Yes, really.)"
**Phone shows**: Pricing comparison card. "Dreamloom — Free." vs three blurred competitor names with "$5.99/mo", "$9.99/mo", "$14.99/mo".

---

## Production notes

- Use Figma. Import phone frame from official Pixel 9 mockups.
- All copy in Inter Bold (headline) + Inter Regular (sub).
- Keep margins generous — Play Store crops 5% on each side on some devices.
- Export as JPEG quality 95 (PNG is overkill and rejected by Play Console for being too large).
- Save Figma source in `design/screenshots.fig` so future updates take 10 minutes.

## A/B priority

Frame 1's headline is the single most important pixel in this app's marketing. A/B test these alternates after launch via Play Store experiments:

- "The dream journal that doesn't read your dreams"  ← default
- "Decode your dreams. Without giving them away."
- "Free AI dream journal. Runs entirely on your phone."
- "Your subconscious, kept private."
