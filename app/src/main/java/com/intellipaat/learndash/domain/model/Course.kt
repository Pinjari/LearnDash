package com.intellipaat.learndash.domain.model

/** A course on the dashboard. Progress is derived from lessons, not stored. */
data class Course(
    val id: Int,
    val title: String,
    val instructor: String,
    val totalLessons: Int,
    val completedLessons: Int
) {
    val progress: Int get() = progressFor(completedLessons, totalLessons)
}

data class Lesson(
    val courseId: Int,
    val index: Int,          // 0-based position inside the course
    val title: String,
    val completed: Boolean
)

/**
 * Single source of truth for "65%" maths. Integer division on purpose —
 * the brief shows whole percentages everywhere.
 */
fun progressFor(completed: Int, total: Int): Int {
    if (total <= 0) return 0
    return ((completed.coerceAtLeast(0) * 100) / total).coerceIn(0, 100)
}
