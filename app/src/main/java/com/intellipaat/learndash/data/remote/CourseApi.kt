package com.intellipaat.learndash.data.remote

import android.content.Context
import com.intellipaat.learndash.domain.model.Lesson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock course backend. Serves the bundled courses.json the way a real API
 * would — with latency, and an offline toggle for rehearsing the failure
 * states in manual QA.
 */
@Singleton
class CourseApi @Inject constructor(
    @ApplicationContext private val context: Context
) : CourseRemote {

    /** Flip in manual QA to rehearse the failure + offline states. */
    var simulateOffline = false
    var simulatedDelayMs = 900L

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchCourses(): List<RemoteCourse> {
        delay(simulatedDelayMs) // feel like a network round trip
        if (simulateOffline) throw IOException("No internet connection")
        val raw = context.assets.open("courses.json").bufferedReader().use { it.readText() }
        return json.decodeFromString(raw)
    }

    /**
     * Seeds which lessons start completed so progress matches the bundled
     * percentages (e.g. 65% of 20 = first 13 done). Deterministic per course.
     */
    override fun seedCompleted(course: RemoteCourse): List<Lesson> {
        val titles = course.lessonTitles.ifEmpty {
            List(course.lessons) { i -> "Lesson ${i + 1}" }
        }
        val doneCount = ((course.progress / 100.0) * titles.size).toInt()
        return titles.mapIndexed { i, title ->
            Lesson(course.id, i, title, completed = i < doneCount)
        }
    }
}
