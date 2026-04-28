package com.charles.app.dreamloom.data.repo

import com.charles.app.dreamloom.data.db.DreamDao
import com.charles.app.dreamloom.data.db.DreamEntity
import com.charles.app.dreamloom.llm.Interpretation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
class DreamRepositoryTest {

    @Test
    fun create_insertsExpectedEntity() = runTest {
        val dao = mockk<DreamDao>(relaxed = true)
        coEvery { dao.insert(any()) } returns Unit
        val repo = DreamRepository(dao)
        repo.create(
            id = 42L,
            rawText = "12345678901",
            mood = "joyful",
            photoPath = "/p.jpg",
            audioPath = null,
        )
        val slot = slot<DreamEntity>()
        coVerify(exactly = 1) { dao.insert(capture(slot)) }
        assertEquals(42L, slot.captured.id)
        assertEquals("joyful", slot.captured.mood)
        assertEquals("/p.jpg", slot.captured.photoPath)
        assertEquals(false, slot.captured.isInterpretationComplete)
    }

    @Test
    fun attachInterpretation_noopWhenDreamMissing() = runTest {
        val dao = mockk<DreamDao>(relaxed = true)
        coEvery { dao.getById(1L) } returns null
        val repo = DreamRepository(dao)
        repo.attachInterpretation(
            1L,
            Interpretation("t", listOf("a"), "i", "in"),
            "mv",
        )
        coVerify(exactly = 0) { dao.update(any()) }
    }

    @Test
    fun attachInterpretation_updatesWhenPresent() = runTest {
        val base = DreamEntity(
            id = 1L,
            createdAt = 1L,
            rawText = "r",
            mood = "skip",
            photoPath = null,
            title = null,
            symbolsJson = null,
            interpretation = null,
            intention = null,
            modelVersion = null,
            isInterpretationComplete = false,
        )
        val dao = mockk<DreamDao>(relaxed = true)
        coEvery { dao.getById(1L) } returns base
        val updated = mutableListOf<DreamEntity>()
        coEvery { dao.update(any()) } answers {
            updated.add(invocation.args[0] as DreamEntity)
            Unit
        }
        val repo = DreamRepository(dao)
        repo.attachInterpretation(
            1L,
            Interpretation("T", listOf("x", "y"), "interp", "inten"),
            "v2",
        )
        coVerify(exactly = 1) { dao.update(any()) }
        assertEquals(1, updated.size)
        assertEquals("T", updated[0].title)
        assertTrue(updated[0].isInterpretationComplete)
        assertEquals("v2", updated[0].modelVersion)
        assertTrue(updated[0].symbolsJson!!.contains("x"))
    }

    @Test
    fun delete_delegatesToDao() = runTest {
        val dao = mockk<DreamDao>(relaxed = true)
        coEvery { dao.deleteById(3L) } returns Unit
        val repo = DreamRepository(dao)
        repo.delete(3L)
        coVerify(exactly = 1) { dao.deleteById(3L) }
    }

    @Test
    fun symbolsJsonForStorage_matchesJsonArrayShape() {
        val dao = mockk<DreamDao>()
        val repo = DreamRepository(dao)
        assertEquals("""["x","y"]""", repo.symbolsJsonForStorage(listOf("x", "y")))
        assertEquals("[]", repo.symbolsJsonForStorage(emptyList()))
    }

    @Test
    fun searchFts_delegatesToDao() = runTest {
        val dao = mockk<DreamDao>(relaxed = true)
        every { dao.searchFts("a*") } returns flowOf(emptyList())
        val repo = DreamRepository(dao)
        repo.searchFts("a*")
        verify(exactly = 1) { dao.searchFts("a*") }
    }
}
