package com.jeeprep.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.jeeprep.app.ui.navigation.JeePrepNavHost
import com.jeeprep.app.ui.navigation.JeePrepBottomBar
import com.jeeprep.app.ui.theme.JeePrepTheme
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val pendingRoute = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            JeePrepTheme {
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    pendingRoute.collect { route ->
                        if (route != null) {
                            navController.navigate(route)
                            pendingRoute.value = null
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { JeePrepBottomBar(navController) }
                ) { innerPadding ->
                    JeePrepNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val route = intent?.getStringExtra("navigate_to")
        Log.d("JeePrep", "handleIntent: navigate_to=$route")
        if (route != null) {
            pendingRoute.value = route
        }
    }
}
