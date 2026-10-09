package com.intellipaat.learndash.data.remote

import kotlinx.serialization.Serializable

/** One course object exactly as the backend (or bundled JSON) returns it. */
@Serializable
data class RemoteCourse(
    val id: Int,
    val title: String,
    val instructor: String,
    val progress: Int,
    val lessons: Int,
    val lessonTitles: List<String> = emptyList()
)
