package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ai.configureAppCheck
import com.example.ui.SehatSaathiApp
import com.example.ui.theme.SehatSaathiTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Configure Firebase App Check with launch intent before any UI or ViewModels
    configureAppCheck(this, intent)
    enableEdgeToEdge()
    setContent {
      SehatSaathiTheme {
        SehatSaathiApp()
      }
    }
  }
}
