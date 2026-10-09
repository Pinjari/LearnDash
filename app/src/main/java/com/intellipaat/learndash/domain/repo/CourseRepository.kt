package com.intellipaat.learndash.domain.repo

import com.intellipaat.learndash.domain.model.Course
import com.intellipaat.learndash.domain.model.Lesson
import kotlinx.coroutines.flow.Flow

/** What the dashboard + detail screens need. Room is the source of truth. */
interface CourseRepository {
    fun observeCourses(): Flow<List<Course>>
    fun observeLessons(courseId: Int): Flow<List<Lesson>>
    suspend fun course(courseId: Int): Course?
    suspend fun refresh(): RefreshResult
    suspend fun toggleLesson(courseId: Int, index: Int, completed: Boolean)
}

sealed interface RefreshResult {
    /** Fresh data from the mock API (also saved to cache). */
    data object Updated : RefreshResult
    /** API/asset failed but we have cached rows to show. */
    data object ServedFromCache : RefreshResult
    /** Nothing anywhere — show the failure/empty state. */
    data class Failed(val message: String) : RefreshResult
}
