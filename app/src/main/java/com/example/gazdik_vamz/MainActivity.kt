package com.example.gazdik_vamz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.gazdik_vamz.ui.VamzApp
import com.example.gazdik_vamz.ui.theme.Gazdik_VAMZTheme

/** Vstupný bod celého UI.*/
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