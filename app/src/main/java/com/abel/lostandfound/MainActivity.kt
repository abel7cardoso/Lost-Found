package com.abel.lostandfound

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.abel.lostandfound.ui.navigation.AppNavHost
import com.abel.lostandfound.ui.theme.LostFoundTheme // <-- Mudança aqui!

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LostFoundTheme { // <-- E aqui!
                AppNavHost()
            }
        }
    }
}