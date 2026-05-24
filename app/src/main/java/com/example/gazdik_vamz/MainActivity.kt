package com.example.gazdik_vamz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.gazdik_vamz.ui.VamzApp
import com.example.gazdik_vamz.ui.theme.Gazdik_VAMZTheme

/** Vstupný bod celého UI.*/
// https://www.youtube.com/watch?v=8YPXv7xKh2w - Philipp Lackner
// https://www.youtube.com/watch?v=4gUeyNkGE3g - Jetpack Compose Navigation for Beginners - Android Studio Tutorial - Philipp Lackner


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Gazdik_VAMZTheme {
                VamzApp()
            }
        }
    }
}