package com.intellipaat.learndash.data.repo

import com.intellipaat.learndash.data.local.dao.CourseDao
import com.intellipaat.learndash.data.local.dao.LessonDao
import com.intellipaat.learndash.data.local.entity.CourseEntity
import com.intellipaat.learndash.data.local.entity.LessonEntity
import com.intellipaat.learndash.data.remote.Connectivity
import com.intellipaat.learndash.data.remote.CourseRemote
import com.intellipaat.learndash.domain.model.Course
import com.intellipaat.learndash.domain.model.Lesson
import com.intellipaat.learndash.domain.repo.CourseRepository
import com.intellipaat.learndash.domain.repo.RefreshResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Offline-first: Room is the source of truth, the mock API only refreshes it.
 * Completed lessons are never overwritten by a refresh (IGNORE on insert +
 * refresh only inserts missing rows) so user progress survives reloads.
 */
class CourseRepositoryImpl @Inject constructor(
    private val api: CourseRemote,
    private val courses: CourseDao,
    private val lessons: LessonDao,
    private val connectivity: Connectivity
) : CourseRepository {

    override fun observeCourses(): Flow<List<Course>> =
        courses.observeCourses().map { rows ->
            rows.map { Course(it.id, it.title, it.instructor, it.totalLessons, it.completedLessons) }
        }

    override fun observeLessons(courseId: Int): Flow<List<Lesson>> =
        lessons.observeForCourse(courseId).map { rows ->
            rows.map { Lesson(it.courseId, it.lessonIndex, it.title, it.completed) }
        }

    override suspend fun course(courseId: Int): Course? =
        courses.courseById(courseId)?.let {
            Course(it.id, it.title, it.instructor, it.totalLessons, it.completedLessons)
        }

    override suspend fun refresh(): RefreshResult {
        // Fail fast when we know the radio is off — but only if the cache can
        // actually cover the screen, otherwise let the fetch attempt run so a
        // captive portal / flaky VALIDATED flag doesn't fake an offline state.
        val hasCache = courses.count() > 0
        if (!connectivity.isOnline() && hasCache) return RefreshResult.ServedFromCache
        return try {
            val remote = api.fetchCourses()
            courses.upsertAll(
                remote.map { CourseEntity(it.id, it.title, it.instructor, it.lessons) }
            )
            remote.forEach { course ->
                // IGNORE keeps existing rows (and completed flags) untouched
                // while appending anything the backend added since first seed.
                lessons.insertAll(
                    api.seedCompleted(course).map {
                        LessonEntity(it.courseId, it.index, it.title, it.completed)
                    }
                )
            }
            RefreshResult.Updated
        } catch (e: Exception) {
            if (courses.count() > 0) RefreshResult.ServedFromCache
            else RefreshResult.Failed(e.friendlyMessage())
        }
    }

    override suspend fun toggleLesson(courseId: Int, index: Int, completed: Boolean) {
        lessons.setCompleted(courseId, index, completed)
    }

    private fun Exception.friendlyMessage(): String =
        if (message?.contains("internet", ignoreCase = true) == true ||
            this is java.io.IOException
        ) {
            "Couldn't reach the server. Check your connection and retry."
        } else {
            "Something went wrong loading courses. Please retry."
        }
}
