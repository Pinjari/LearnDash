package com.intellipaat.learndash

import com.intellipaat.learndash.domain.model.progressFor
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The one test that matters here: progress maths. Every dashboard card and
 * the detail header derive from [progressFor], so a regression would show
 * wrong percentages across the app.
 */
class ProgressCalculationTest {

    @Test
    fun `matches the seeded dashboard values`() {
        assertEquals(65, progressFor(13, 20))
        assertEquals(25, progressFor(7, 28))
    }

    @Test
    fun `integer division truncates`() {
        assertEquals(37, progressFor(6, 16)) // 37.5 -> 37, consistent whole numbers
        assertEquals(0, progressFor(0, 20))
        assertEquals(100, progressFor(20, 20))
    }

    @Test
    fun `guards divide-by-zero and overflow`() {
        assertEquals(0, progressFor(5, 0))
        assertEquals(0, progressFor(-3, 20))
        assertEquals(100, progressFor(99, 20)) // over-completion clamps
    }

    @Test
    fun `completing one lesson nudges progress`() {
        // 13/20 (65%) -> 14/20 (70%): the detail-screen toggle path.
        val before = progressFor(13, 20)
        val after = progressFor(14, 20)
        assertEquals(65, before)
        assertEquals(70, after)
    }
}
