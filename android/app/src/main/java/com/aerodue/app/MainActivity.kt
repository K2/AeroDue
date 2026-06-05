package com.aerodue.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aerodue.app.ui.AeroDueApp
import com.aerodue.app.ui.theme.AeroDueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AeroDueTheme {
                AeroDueApp()
            }
        }
    }
}
