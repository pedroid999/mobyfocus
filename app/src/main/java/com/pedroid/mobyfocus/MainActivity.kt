package com.pedroid.mobyfocus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.pedroid.mobyfocus.presentation.navigation.MobyFocusNavHost
import com.pedroid.mobyfocus.ui.theme.MobyFocusTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobyFocusTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MobyFocusNavHost(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding)
                }
            }
        }
    }
}
