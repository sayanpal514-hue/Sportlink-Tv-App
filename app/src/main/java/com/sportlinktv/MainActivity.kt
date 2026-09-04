package com.sportlinktv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sportlinktv.player.PendingChannelHolder
import com.sportlinktv.presentation.home.HomeScreen
import com.sportlinktv.presentation.player.PlayerScreen
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var pendingHolder: PendingChannelHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(onNavigateToPlayer = { channel ->
                                // Store full channel object (with DRM keys) before navigating
                                pendingHolder.pendingChannel = channel
                                val encodedUrl  = URLEncoder.encode(channel.url,  StandardCharsets.UTF_8.toString())
                                val encodedName = URLEncoder.encode(channel.name, StandardCharsets.UTF_8.toString())
                                navController.navigate("player/$encodedUrl/$encodedName")
                            })
                        }
                        composable("player/{url}/{name}") { backStackEntry ->
                            val url  = backStackEntry.arguments?.getString("url")  ?: ""
                            val name = backStackEntry.arguments?.getString("name") ?: ""
                            PlayerScreen(channelUrl = url, channelName = name)
                        }
                    }
                }
            }
        }
    }
}
