package com.example.gazdik_vamz.ui

import androidx.compose.runtime.Composable
import com.example.gazdik_vamz.ui.navigation.AppNavigation

/** Koreňový Composable — spúšťa navigačný graf aplikácie. */
// https://www.youtube.com/watch?v=4gUeyNkGE3g - Jetpack Compose Navigation for Beginners - Android Studio Tutorial - Philipp Lackner
@Composable
fun VamzApp() {
    AppNavigation()
}