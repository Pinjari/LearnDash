package com.intellipaat.learndash.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intellipaat.learndash.domain.model.Course
import com.intellipaat.learndash.domain.model.Lesson
import com.intellipaat.learndash.domain.repo.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUi(
    val course: Course? = null,
    val lessons: List<Lesson> = emptyList(),
    val loading: Boolean = true
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repo: CourseRepository
) : ViewModel() {

    private val courseId: Int = savedState.get<Int>("courseId") ?: 0

    /** Header (progress) + rows recompute together on every lesson toggle. */
    val ui: StateFlow<DetailUi> =
        combine(repo.observeCourses(), repo.observeLessons(courseId)) { all, lessons ->
            DetailUi(
                course = all.firstOrNull { it.id == courseId },
                lessons = lessons,
                loading = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUi())

    fun toggle(lesson: Lesson) {
        viewModelScope.launch {
            repo.toggleLesson(lesson.courseId, lesson.index, !lesson.completed)
        }
    }
}
