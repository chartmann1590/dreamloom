# Ads (AdMob)

The app is supported with Google AdMob: App Open, interstitial, and rewarded placements. There is no in-app subscription or in-app purchase for removing ads. No banner ads in the main journaling flow. Mediation and frequency rules prioritize retention and a calm user experience over aggressive impressions.

---

## Ad units to create in AdMob console

| Unit name | Format | Where it shows |
| --- | --- | --- |
| `app_open_main` | App Open | Cold start with cooldown |
| `int_post_detail` | Interstitial | After closing a Dream Detail view |
| `rew_extended_interp` | Rewarded | Unlock extended interpretation |
| `rew_weekly_insight` | Rewarded | Unlock the Sunday weekly insight |
| `rew_dream_image` | Rewarded | Unlock dream image generation (**post-MVP** — unit may be omitted until shipped; see `spec/MVP.md`) |
| `banner_settings` | Banner (adaptive) | Settings → About only |

Use **AdMob mediation** with at least Meta Audience Network + AppLovin enabled in the AdMob console.

---

## Frequency caps and cooldowns

| Unit | Rule |
| --- | --- |
| App Open | Show on cold start only. **4-hour cooldown** between shows. Skip if user opened from a notification (poor UX). Skip on the very first session after install. |
| Interstitial | At most **1 per session**. Cooldown of **60 seconds** since app open. Never on the first 3 dream entries the user ever creates. Never if the user has been in the app < 90 seconds. |
| Rewarded | User-initiated only. No cooldown — they tapped the button. |
| Banner | Settings → About only. Never on Home, Recording, Detail, Atlas, Insight, Oracle. |

These rules are non-negotiable. Encode them in a single `AdGate` class (see below).

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

## Rewarded video copy

The wording matters. Test these on launch and iterate:

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

The "free, with a short ad" phrase reframes the ad as a clear trade, not a surprise.

---

## What we do *not* do (and why)

- **No banner on Home** — the moon is the focus; a banner harms the aesthetic and session quality.
- **No interstitial on app open** — the App Open placement already exists there; doubling up is hostile.
- **No interstitial right after dream save** — the user just wrote something intimate.
- **No video pre-roll on streaming interpretation** — the streaming animation is the experience.
- **No "remove ads" in-app purchase** — one ad policy across all users; avoids a split codebase.

---

## ATT / consent handling

- **EU/UK**: integrate Google's User Messaging Platform (UMP) SDK for GDPR consent — required for AdMob serving in the EU. Must show before first ad request.
- **US (CCPA)**: handled by AdMob's "Restricted Data Processing" mode — configure in the AdMob console.
- **App Tracking Transparency (iOS)**: not applicable — Android only.
- Implementation: see `tech/ARCHITECTURE.md` → Consent module.

---

## Critical do-not-break invariants (encode as instrumented tests)

1. App Open ad does not show during onboarding.
2. App Open ad does not show during model download.
3. Interstitial does not show after the user's 1st, 2nd, or 3rd dream entry ever.
4. No ads of any kind are visible while the user is recording or while interpretation is streaming.
5. Wiping data clears all ad-related caches and resets all cooldowns.
