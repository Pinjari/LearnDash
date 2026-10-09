package com.intellipaat.learndash.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.intellipaat.learndash.domain.repo.AuthRepository
import com.intellipaat.learndash.ui.dashboard.DashboardScreen
import com.intellipaat.learndash.ui.detail.DetailScreen
import com.intellipaat.learndash.ui.login.LoginScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val DETAIL = "detail/{courseId}"
    fun detail(id: Int) = "detail/$id"
}

@HiltViewModel
class SessionViewModel @Inject constructor(auth: AuthRepository) : ViewModel() {
    val loggedIn = auth.loggedInEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
fun LearnNav() {
    val nav = rememberNavController()
    val session: SessionViewModel = hiltViewModel()
    val email by session.loggedIn.collectAsState()
    val loggedIn = !email.isNullOrBlank()

    // Session drives the back stack: logging out anywhere drops the whole
    // stack back to login so there's no stranded, signed-out dashboard. We pop
    // up to the graph root (not the start destination) because the login route
    // was already popped on the way in — popUpTo(start) would find nothing and
    // leave dashboard sitting underneath login, so back-press would fall
    // straight back into a signed-out dashboard.
    LaunchedEffect(loggedIn) {
        if (!loggedIn && nav.currentDestination?.route != Routes.LOGIN) {
            nav.navigate(Routes.LOGIN) {
                popUpTo(nav.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = nav,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(onLoggedIn = {
                nav.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            })
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onOpenCourse = { id -> nav.navigate(Routes.detail(id)) }
            )
        }
        composable(
            Routes.DETAIL,
            arguments = listOf(navArgument("courseId") { type = NavType.IntType })
        ) {
            DetailScreen(onBack = { nav.popBackStack() })
        }
    }
}
