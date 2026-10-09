package com.intellipaat.learndash.data.remote

import com.intellipaat.learndash.domain.model.Lesson

/**
 * Course backend. The repository programs against this interface; production
 * serves it from bundled JSON ([CourseApi]), tests serve it from fakes.
 * Swapping in Retrofit later means adding one implementation, nothing else.
 */
interface CourseRemote {
    suspend fun fetchCourses(): List<RemoteCourse>
    fun seedCompleted(course: RemoteCourse): List<Lesson>
}
