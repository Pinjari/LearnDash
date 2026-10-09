package com.intellipaat.learndash.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.intellipaat.learndash.ui.components.EmptyView
import com.intellipaat.learndash.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenCourse: (Int) -> Unit,
    vm: DashboardViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    val courses by vm.courseList.collectAsState()
    val hasAnything by vm.hasAnything.collectAsState()
    val email by vm.email.collectAsState(initial = null)
    val snacks = remember { SnackbarHostState() }

    // Offline banner: show once per notice, then clear it so rotation or
    // recomposition doesn't replay the snackbar.
    LaunchedEffect(ui.notice) {
        ui.notice?.let { message ->
            if (!ui.isError) snacks.showSnackbar(message)
            vm.dismissNotice()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Courses") },
                actions = {
                    IconButton(onClick = vm::logout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snacks) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (email != null) {
                Text(
                    "Signed in as $email",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            when {
                ui.loading -> LoadingView()
                // Error banner only when we have nothing cached to show.
                ui.isError && !hasAnything -> FailureView(
                    message = ui.notice ?: "Couldn't load courses.",
                    onRetry = { vm.load(first = true) }
                )
                courses.isEmpty() -> EmptyView("No courses assigned yet.\nCheck back soon.")
                else -> PullToRefreshBox(
                    isRefreshing = ui.refreshing,
                    onRefresh = { vm.load() }
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(courses, key = { it.id }) { course ->
                            CourseCard(course = course, onContinue = { onOpenCourse(course.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FailureView(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}
