package com.charles.app.dreamloom.ads

import com.charles.app.dreamloom.core.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

@Singleton
class AdGate @Inject constructor(
    private val clock: Clock,
) {
    /**
     * Frequency rules from [spec/MONETIZATION.md]. Implement with DataStore reads in
     * call sites; this is the pure policy layer for tests and clarity.
     */
    fun canShowAppOpen(
        firstSessionCompleted: Boolean,
        lastAppOpenAdAtMs: Long,
    ): Boolean {
        if (!firstSessionCompleted) return false
        return clock.nowMs() - lastAppOpenAdAtMs >= 4.hours.inWholeMilliseconds
    }

    fun canShowInterstitial(
        dreamCountLifetime: Int,
        sessionStartMs: Long,
        alreadyThisSession: Boolean,
        lastInterstitialAtMs: Long,
    ): Boolean {
        if (dreamCountLifetime <= 3) return false
        if (clock.nowMs() - sessionStartMs < 90_000) return false
        if (alreadyThisSession) return false
        return clock.nowMs() - lastInterstitialAtMs >= 60.seconds.inWholeMilliseconds
    }
}
