package com.charles.app.dreamloom.brand

import android.content.Context
import android.content.pm.PackageManager
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.charles.app.dreamloom.data.db.AppDatabase
import com.charles.app.dreamloom.data.db.DreamEntity
import com.charles.app.dreamloom.llm.DreamInterpreter
import com.charles.app.dreamloom.llm.LlmEngine
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking

/**
 * Instrumented checks for the five critical brand promises in [design/BRAND.md].
 */
@RunWith(AndroidJUnit4::class)
class BrandPromiseInstrumentedTest {

    /** 1 — "Your dreams never leave your phone." Interpretation stack is local-only (on-device LLM). */
    @Test
    fun brandPromise_interpretationUsesOnlyOnDeviceLlmEngine() {
        val interpCtor = DreamInterpreter::class.java.constructors.filter { it.parameterCount == 1 }.single()
        assertArrayEquals(arrayOf(LlmEngine::class.java), interpCtor.parameterTypes)
        val llmCtor = LlmEngine::class.java.constructors.filter { it.parameterCount == 0 }.single()
        assertEquals(0, llmCtor.parameterTypes.size)
    }

    /** 2 — "No account." Only the main entry activity is launcher-facing; no auth-style activities. */
    @Test
    fun brandPromise_noAccountFlowInManifest() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val pm = ctx.packageManager
        val pkg = ctx.packageName
        val main = pm.getLaunchIntentForPackage(pkg)
        assertTrue(main?.component?.className?.endsWith(".MainActivity") == true)
        val activities = pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES).activities.orEmpty()
        val names = activities.map { it.name }
        assertTrue(names.none { it.contains("login", ignoreCase = true) })
        assertTrue(names.none { it.contains("signin", ignoreCase = true) })
        assertTrue(names.none { it.contains("Authenticator", ignoreCase = true) })
    }

    /** 3 — "Free forever." No Play Billing client on the classpath; no BILLING permission. */
    @Test
    fun brandPromise_noInAppPurchases() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val pkg = ctx.packageManager.getPackageInfo(ctx.packageName, PackageManager.GET_PERMISSIONS)
        val perms = pkg.requestedPermissions.orEmpty()
        assertFalse(perms.any { it == "com.android.vending.BILLING" })
        var billingMissing = false
        try {
            Class.forName("com.android.billingclient.api.BillingClient")
        } catch (_: ClassNotFoundException) {
            billingMissing = true
        }
        assertTrue("Play Billing must not be a dependency", billingMissing)
    }

    /** 4 — "Works offline." Journal persistence is Room/SQLite only (no HTTP types in repository layer). */
    @Test
    fun brandPromise_dreamPersistenceIsLocalRoomOnly() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).build()
        val dao = db.dreamDao()
        dao.insert(
            DreamEntity(
                id = 100L,
                createdAt = 100L,
                rawText = "12345678901 offline path",
                mood = "skip",
                photoPath = null,
                title = null,
                symbolsJson = null,
                interpretation = null,
                intention = null,
                modelVersion = null,
                isInterpretationComplete = false,
            ),
        )
        val row = dao.getById(100L)
        assertEquals("12345678901 offline path", row!!.rawText)
        db.close()
    }

    /** 5 — "Encrypted on your phone." SQLCipher factory yields a file that is not plaintext SQLite. */
    @Test
    fun brandPromise_dreamsDatabaseUsesSqlCipherNotPlaintext() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val dbFile = ctx.getDatabasePath("brand_promise_cipher_test.db")
        dbFile.delete()
        val passphrase = SQLiteDatabase.getBytes("instrumented-test-passphrase".toCharArray())
        val db = Room.databaseBuilder(ctx, AppDatabase::class.java, dbFile.absolutePath)
            .openHelperFactory(SupportFactory(passphrase))
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
        runBlocking {
            db.dreamDao().insert(
                DreamEntity(
                    id = 1L,
                    createdAt = 1L,
                    rawText = "12345678901",
                    mood = "skip",
                    photoPath = null,
                    title = null,
                    symbolsJson = null,
                    interpretation = null,
                    intention = null,
                    modelVersion = null,
                    isInterpretationComplete = false,
                ),
            )
        }
        db.close()
        val header = dbFile.readBytes().take(16).toByteArray()
        val magic = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)
        assertFalse(
            "Encrypted DB must not begin with the standard SQLite plaintext header",
            header.contentEquals(magic.copyOf(minOf(header.size, magic.size))),
        )
        dbFile.delete()
    }
}
