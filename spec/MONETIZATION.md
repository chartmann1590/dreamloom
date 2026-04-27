# Monetization — AdMob

The whole revenue model. No subscription. No IAP. No banners in the main flow. The objective is **highest possible eCPM with zero damage to retention**, because retention compounds and ad damage compounds against it.

---

## Ad units to create in AdMob console

| Unit name | Format | Where it shows |
| --- | --- | --- |
| `app_open_main` | App Open | Cold start with cooldown |
| `int_post_detail` | Interstitial | After closing a Dream Detail view |
| `rew_extended_interp` | Rewarded | Unlock extended interpretation |
| `rew_weekly_insight` | Rewarded | Unlock the Sunday weekly insight |
| `rew_dream_image` | Rewarded | Unlock dream image generation (P1) |
| `banner_settings` | Banner (adaptive) | Settings → About only |

Use **AdMob mediation** with at least Meta Audience Network + AppLovin enabled — typically 20–50% lift in eCPM versus AdMob alone.

---

## Frequency caps & cooldowns (the rules that keep retention)

| Unit | Rule |
| --- | --- |
| App Open | Show on cold start only. **4-hour cooldown** between shows. Skip if user opened from a notification (poor UX). Skip on the very first session after install. |
| Interstitial | At most **1 per session**. Cooldown of **60 seconds** since app open. Never on the first 3 dream entries the user ever creates (let them taste the magic). Never if the user has been in the app < 90 seconds. |
| Rewarded | User-initiated only. No cooldown — they tapped the button. |
| Banner | Settings → About only. Never on Home, Recording, Detail, Atlas, Insight, Oracle. |

These rules are non-negotiable. Encode them as a single `AdGate` class (see below).

---

## Implementation sketch — `AdGate`

```kotlin
@Singleton
class AdGate @Inject constructor(
    private val prefs: DataStore<Preferences>,
    private val clock: Clock,
) {
    suspend fun shouldShowAppOpen(): Boolean {
        val last = prefs.data.first()[LAST_APP_OPEN_AT] ?: 0
        return clock.nowMs() - last >= 4.hours.inWholeMilliseconds
    }

    suspend fun shouldShowInterstitial(sessionStart: Long, dreamCount: Int): Boolean {
        if (dreamCount <= 3) return false
        if (clock.nowMs() - sessionStart < 90_000) return false
        val shown = prefs.data.first()[INTERSTITIAL_SHOWN_THIS_SESSION] ?: false
        if (shown) return false
        val lastShownMs = prefs.data.first()[LAST_INTERSTITIAL_AT] ?: 0
        return clock.nowMs() - lastShownMs >= 60_000
    }

    suspend fun markAppOpenShown() { /* update LAST_APP_OPEN_AT */ }
    suspend fun markInterstitialShown() { /* update both keys */ }
}
```

`dreamCount` is the lifetime count from Room. `sessionStart` is reset on cold start.

---

## Rewarded video copy (the part that drives revenue)

The wording matters more than people realize. Test these on launch and iterate:

**Extended interpretation gate** (on Detail screen):
```
[ ✦ Look deeper ]
A longer reading + two reflective questions —
free, with a short ad.
```

**Weekly insight gate** (on Insight screen):
```
[ ✦ Weave this week ]
Watch a short ad to reveal your week's pattern.
```

**Dream image gate** (P1, on Detail):
```
[ ✦ See your dream ]
Generate a dreamlike illustration —
free, with a short ad.
```

The "free, with a short ad" phrase is essential — it reframes the ad as a fair trade rather than a tax.

---

## What we *don't* do (and why)

- **No banner on Home** — the moon is sacred. Banner kills the aesthetic and tanks D7.
- **No interstitial on app open** — App Open ad already lives there; doubling up is hostile.
- **No interstitial after dream save** — the user just wrote something intimate. Showing a dating-app ad there is brand-incinerating.
- **No video pre-roll on streaming interpretation** — the streaming animation IS the experience. Can't break it.
- **No "remove ads" IAP** — keeping ads makes the free-vs-paid story honest. We don't want a divided codebase.

---

## eCPM expectations & projections

Conservative AdMob eCPM in lifestyle category, US-skewed audience, mediation enabled:

| Format | eCPM |
| --- | --- |
| App Open | $8–15 |
| Interstitial | $6–12 |
| Rewarded | $15–30 |
| Banner | $0.50–1.50 |

Per-user ad impressions per day (target):
- App Open: ~1.0
- Interstitial: ~0.5
- Rewarded: ~0.2

Daily ARPDAU estimate (US-heavy): **$0.04–0.10**.

| MAU | DAU (assume 25%) | Monthly revenue |
| --- | --- | --- |
| 10K | 2.5K | $300–750 |
| 100K | 25K | $3K–7.5K |
| 500K | 125K | $15K–37K |
| 1M | 250K | $30K–75K |

These are conservative. If TikTok virality hits and weekly Insight + Dream Image are sticky rewarded gates, ARPDAU can climb 2–3×.

---

## ATT / consent handling

- **EU/UK**: integrate Google's User Messaging Platform (UMP) SDK for GDPR consent — required for AdMob serving in EU. Must show before first ad request.
- **US (CCPA)**: handled by AdMob's "Restricted Data Processing" mode — flip the toggle in AdMob console.
- **App Tracking Transparency (iOS)**: not relevant — we are Android only.
- Implementation: see `tech/ARCHITECTURE.md` → Consent module.

---

## Critical do-not-break invariants (encode as instrumented tests)

1. App Open ad does not show during onboarding.
2. App Open ad does not show during model download.
3. Interstitial does not show after the user's 1st, 2nd, or 3rd dream entry ever.
4. No ads of any kind are visible while the user is recording or while interpretation is streaming.
5. Wiping data clears all ad-related caches and resets all cooldowns.
