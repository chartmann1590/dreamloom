package com.charles.app.dreamloom.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.charles.app.dreamloom.data.model.buildFtsMatchQuery
import com.charles.app.dreamloom.llm.Interpretation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DreamDaoTest {
    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_getById_roundTrip() = runBlocking {
        val dao = db.dreamDao()
        val e = dream(1L, "I walked beside a quiet river.")
        dao.insert(e)
        val got = dao.getById(1L)
        assertNotNull(got)
        assertEquals("I walked beside a quiet river.", got!!.rawText)
    }

    @Test
    fun latestInterpretedDreams_onlyComplete() = runBlocking {
        val dao = db.dreamDao()
        dao.insert(dream(1L, "a").copy(isInterpretationComplete = false))
        dao.insert(
            dream(2L, "b").copy(
                isInterpretationComplete = true,
                title = "t",
                symbolsJson = "[\"x\"]",
                interpretation = "i",
                intention = "n",
                modelVersion = "v",
            ),
        )
        val list = dao.latestInterpretedDreams(10)
        assertEquals(1, list.size)
        assertEquals(2L, list[0].id)
    }

    @Test
    fun searchFts_findsRawText() = runBlocking {
        val dao = db.dreamDao()
        dao.insert(dream(1L, "flying over mountains at dawn"))
        dao.insert(dream(2L, "sitting in a quiet room"))
        val match = buildFtsMatchQuery("mountains")!!
        val hits = dao.searchFts(match).first()
        assertEquals(1, hits.size)
        assertEquals(1L, hits[0].id)
    }

    @Test
    fun deleteById_removesRow() = runBlocking {
        val dao = db.dreamDao()
        dao.insert(dream(1L, "x"))
        dao.deleteById(1L)
        assertNull(dao.getById(1L))
    }

    @Test
    fun attachInterpretation_viaUpdate() = runBlocking {
        val dao = db.dreamDao()
        dao.insert(dream(9L, "raw"))
        val cur = dao.getById(9L)!!
        val interp = Interpretation("T", listOf("water"), "body", "intention line")
        dao.update(
            cur.copy(
                title = interp.title,
                symbolsJson = """["water"]""",
                interpretation = interp.interpretation,
                intention = interp.intention,
                modelVersion = "m",
                isInterpretationComplete = true,
            ),
        )
        val after = dao.getById(9L)!!
        assertTrue(after.isInterpretationComplete)
        assertEquals("T", after.title)
    }

    private fun dream(id: Long, raw: String) = DreamEntity(
        id = id,
        createdAt = id,
        rawText = raw,
        mood = "skip",
        photoPath = null,
        title = null,
        symbolsJson = null,
        interpretation = null,
        intention = null,
        modelVersion = null,
        isInterpretationComplete = false,
        audioPath = null,
    )
}
