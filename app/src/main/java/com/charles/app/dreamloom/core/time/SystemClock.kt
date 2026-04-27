package com.charles.app.dreamloom.core.time

import javax.inject.Inject
import javax.inject.Singleton

interface Clock {
    fun nowMs(): Long
}

@Singleton
class SystemClock @Inject constructor() : Clock {
    override fun nowMs(): Long = System.currentTimeMillis()
}
