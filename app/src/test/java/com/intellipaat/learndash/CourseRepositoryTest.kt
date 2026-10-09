package com.intellipaat.learndash.data.repo

import com.intellipaat.learndash.data.local.dao.CourseDao
import com.intellipaat.learndash.data.local.dao.LessonDao
import com.intellipaat.learndash.data.local.entity.CourseEntity
import com.intellipaat.learndash.data.local.entity.LessonEntity
import com.intellipaat.learndash.data.remote.Connectivity
import com.intellipaat.learndash.data.remote.CourseRemote
import com.intellipaat.learndash.data.remote.RemoteCourse
import com.intellipaat.learndash.domain.model.Lesson
import com.intellipaat.learndash.domain.model.progressFor
import com.intellipaat.learndash.domain.repo.RefreshResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Repository offline behaviour: cached rows keep the app usable when the
 * backend dies, and lesson progress is never clobbered by a later refresh.
 * Fakes only — no Room, no network, pure JVM.
 */
class CourseRepositoryTest {

    @Test
    fun `failed refresh still serves cache`() = runTest {
        val repo = CourseRepositoryImpl(
            api = FakeRemote(fail = true),
            courses = FakeCourses(
                listOf(CourseDao.CourseWithCounts(1, "Python Programming", "John Smith", 20, 13))
            ),
            lessons = FakeLessons(),
            connectivity = FakeConnectivity(online = true)
        )

        assertEquals(RefreshResult.ServedFromCache, repo.refresh())
        assertEquals(1, repo.observeCourses().first().size)
    }

    @Test
    fun `failed refresh with empty cache surfaces failure`() = runTest {
        val repo = CourseRepositoryImpl(
            api = FakeRemote(fail = true),
            courses = FakeCourses(emptyList()),
            lessons = FakeLessons(),
            connectivity = FakeConnectivity(online = true)
        )

        val result = repo.refresh()

        assertTrue(result is RefreshResult.Failed)
        assertTrue(repo.observeCourses().first().isEmpty())
    }

    @Test
    fun `refresh seeds lessons once and never overwrites progress`() = runTest {
        val lessons = FakeLessons()
        val courses = FakeCourses(emptyList())
        courses.doneLookup = { lessons.doneCount(it) }
        val repo = CourseRepositoryImpl(
            api = FakeRemote(fail = false),
            courses = courses,
            lessons = lessons,
            connectivity = FakeConnectivity(online = true)
        )

        assertEquals(RefreshResult.Updated, repo.refresh())
        assertEquals(13, lessons.doneCount(courseId = 1))
        assertEquals(65, repo.observeCourses().first().first { it.id == 1 }.let {
            progressFor(it.completedLessons, 20)
        })

        // user finishes another lesson, then a second refresh rolls in
        repo.toggleLesson(1, 13, true)
        assertEquals(RefreshResult.Updated, repo.refresh())

        assertEquals(14, lessons.doneCount(courseId = 1))
    }

    @Test
    fun `known-offline with cache skips the fetch entirely`() = runTest {
        var fetches = 0
        val lessons = FakeLessons()
        val repo = CourseRepositoryImpl(
            api = object : FakeRemote(fail = false) {
                override suspend fun fetchCourses(): List<RemoteCourse> {
                    fetches++
                    return super.fetchCourses()
                }
            },
            courses = FakeCourses(
                listOf(CourseDao.CourseWithCounts(1, "Python Programming", "John Smith", 20, 13))
            ),
            lessons = lessons,
            connectivity = FakeConnectivity(online = false)
        )

        assertEquals(RefreshResult.ServedFromCache, repo.refresh())
        assertEquals(0, fetches)
    }

    // -- fakes ------------------------------------------------------------

    private class FakeConnectivity(val online: Boolean) : Connectivity {
        override fun isOnline() = online
    }

    private open class FakeRemote(val fail: Boolean) : CourseRemote {
        override suspend fun fetchCourses(): List<RemoteCourse> {
            if (fail) throw IOException("No internet connection")
            return listOf(
                RemoteCourse(1, "Python Programming", "John Smith", 65, 20, List(20) { "L$it" }),
                RemoteCourse(2, "Generative AI", "Sarah Williams", 40, 16, List(16) { "L$it" }),
                RemoteCourse(3, "Full Stack Development", "David Brown", 25, 28, List(28) { "L$it" })
            )
        }

        override fun seedCompleted(course: RemoteCourse): List<Lesson> {
            val done = ((course.progress / 100.0) * course.lessonTitles.size).toInt()
            return course.lessonTitles.mapIndexed { i, title ->
                Lesson(course.id, i, title, completed = i < done)
            }
        }
    }

    private class FakeCourses(seed: List<CourseDao.CourseWithCounts>) : CourseDao {
        private val rows = MutableStateFlow(seed)

        /** Wired by the test so counts mirror lesson state, like the real query. */
        var doneLookup: (Int) -> Int =
            { id -> rows.value.firstOrNull { it.id == id }?.completedLessons ?: 0 }

        override fun observeCourses(): Flow<List<CourseDao.CourseWithCounts>> =
            rows.map { list -> list.map { it.copy(completedLessons = doneLookup(it.id)) } }

        override suspend fun courseById(id: Int) =
            rows.value.firstOrNull { it.id == id }?.let { it.copy(completedLessons = doneLookup(id)) }

        override suspend fun count() = rows.value.size

        override suspend fun upsertAll(courses: List<CourseEntity>) {
            val prev = rows.value.associateBy { it.id }
            rows.value = courses.map {
                CourseDao.CourseWithCounts(
                    it.id, it.title, it.instructor, it.totalLessons,
                    prev[it.id]?.completedLessons ?: 0
                )
            }
        }
    }

    private class FakeLessons : LessonDao {
        private val store = MutableStateFlow(emptyList<LessonEntity>())

        fun doneCount(courseId: Int) =
            store.value.count { it.courseId == courseId && it.completed }

        override fun observeForCourse(courseId: Int): Flow<List<LessonEntity>> =
            store.map { list -> list.filter { it.courseId == courseId } }

        override suspend fun countForCourse(courseId: Int) =
            store.value.count { it.courseId == courseId }

        override suspend fun setCompleted(courseId: Int, index: Int, completed: Boolean) {
            store.value = store.value.map {
                if (it.courseId == courseId && it.lessonIndex == index) it.copy(completed = completed)
                else it
            }
        }

        override suspend fun insertAll(lessons: List<LessonEntity>) {
            val known = store.value.map { it.courseId to it.lessonIndex }.toSet()
            store.value = store.value + lessons.filter { (it.courseId to it.lessonIndex) !in known }
        }
    }
}
