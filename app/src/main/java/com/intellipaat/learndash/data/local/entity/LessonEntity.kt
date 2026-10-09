package com.intellipaat.learndash.data.local.entity

import androidx.room.Entity

/**
 * One lesson. Composite key (courseId + index) — each course owns its own
 * 0..N numbering, so a single auto id would just get in the way.
 */
@Entity(tableName = "lessons", primaryKeys = ["courseId", "lessonIndex"])
data class LessonEntity(
    val courseId: Int,
    val lessonIndex: Int,
    val title: String,
    val completed: Boolean
)
