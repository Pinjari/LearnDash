package com.intellipaat.learndash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.intellipaat.learndash.ui.LearnNav
import com.intellipaat.learndash.ui.theme.LearnDashTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearnDashTheme {
                LearnNav()
            }
        }
    }
}
