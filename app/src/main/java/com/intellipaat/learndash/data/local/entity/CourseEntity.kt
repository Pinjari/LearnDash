package com.intellipaat.learndash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One cached course row. Lesson completion lives in [LessonEntity], counted on read. */
@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val instructor: String,
    val totalLessons: Int
)
