package com.intellipaat.learndash.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intellipaat.learndash.domain.model.Course
import com.intellipaat.learndash.domain.repo.AuthRepository
import com.intellipaat.learndash.domain.repo.CourseRepository
import com.intellipaat.learndash.domain.repo.RefreshResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One-shot banner state for the toolbar area (offline notice / error). */
data class DashboardUi(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val notice: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val courses: CourseRepository,
    private val auth: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(DashboardUi())
    val ui: StateFlow<DashboardUi> = _ui.asStateFlow()

    // Re-entry guard, deliberately separate from the display `loading` flag.
    // The initial state is loading=true so the spinner shows immediately, which
    // means we can't use that flag to gate the first load — it's already true.
    private var inFlight = false

    val email = auth.loggedInEmail

    val courseList: StateFlow<List<Course>> = courses.observeCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Any rows at all — drives empty vs failure copy on first load. */
    val hasAnything: StateFlow<Boolean> = courseList
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init { load(first = true) }

    fun load(first: Boolean = false) {
        // Tapping retry twice fast used to fire two refreshes that raced each
        // other. Gate on a dedicated flag instead of the display state — the
        // initial `loading=true` would otherwise block the very first load.
        if (inFlight) return
        inFlight = true
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(
                    loading = first, refreshing = !first, notice = null, isError = false
                )
                when (val res = courses.refresh()) {
                    RefreshResult.Updated -> Unit
                    RefreshResult.ServedFromCache ->
                        _ui.value = _ui.value.copy(
                            notice = "You're offline — showing your saved courses."
                        )
                    is RefreshResult.Failed ->
                        _ui.value = _ui.value.copy(notice = res.message, isError = true)
                }
                _ui.value = _ui.value.copy(loading = false, refreshing = false)
            } finally {
                inFlight = false
            }
        }
    }

    fun dismissNotice() { _ui.value = _ui.value.copy(notice = null) }

    fun logout() {
        viewModelScope.launch { auth.logout() }
    }
}
