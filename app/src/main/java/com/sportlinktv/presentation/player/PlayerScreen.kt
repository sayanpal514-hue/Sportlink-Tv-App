package com.sportlinktv.presentation.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    channelUrl: String,
    channelName: String,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = remember { context.findActivity() }
    val qualities by viewModel.qualities.collectAsState()
    val selectedLabel by viewModel.selectedQualityLabel.collectAsState()
    var showQualitySheet by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) }

    LaunchedEffect(channelUrl) {
        viewModel.play(channelUrl, channelName)
    }

    DisposableEffect(Unit) {
        onDispose { 
            viewModel.player.pause() 
            // Restore orientation when leaving
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.let { window ->
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
    ) {
        // ── Video Player ──────────────────────────────────────
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = viewModel.player
                    useController = true
                    keepScreenOn = true
                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── Quality + Refresh Buttons (top-right overlay) ─────
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Refresh tracks button
            if (qualities.isEmpty()) {
                IconButton(
                    onClick = { viewModel.refreshTracks() },
                    modifier = Modifier
                        .background(Color(0x88000000), RoundedCornerShape(8.dp))
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Tracks", tint = Color.White)
                }
            }

            // Quality picker button
            Surface(
                onClick = {
                    viewModel.refreshTracks()
                    showQualitySheet = true
                },
                color = Color(0x88000000),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.HighQuality, contentDescription = "Quality", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(selectedLabel, color = Color.White, fontSize = 12.sp)
                }
            }

            // Fullscreen toggle button
            IconButton(
                onClick = {
                    if (activity != null) {
                        isFullscreen = !isFullscreen
                        if (isFullscreen) {
                            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            activity.window?.let { window ->
                                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            }
                        } else {
                            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            activity.window?.let { window ->
                                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                                insetsController.show(WindowInsetsCompat.Type.systemBars())
                            }
                        }
                    }
                },
                modifier = Modifier
                    .background(Color(0x88000000), RoundedCornerShape(8.dp))
                    .size(40.dp)
            ) {
                Icon(
                    if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = "Toggle Fullscreen",
                    tint = Color.White
                )
            }
        }
    }

    // ── Quality Bottom Sheet ───────────────────────────────────
    if (showQualitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showQualitySheet = false },
            containerColor = Color(0xFF1A1A1A),
            tonalElevation = 0.dp
        ) {
            Text(
                "Video Quality",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
            )

            // Auto option
            QualityItem(
                label = "Auto",
                isSelected = selectedLabel == "Auto",
                onClick = {
                    viewModel.setQuality(null)
                    showQualitySheet = false
                }
            )

            if (qualities.isEmpty()) {
                Text(
                    "No quality options available for this stream.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            } else {
                LazyColumn {
                    items(qualities) { q ->
                        QualityItem(
                            label = q.label,
                            isSelected = selectedLabel == q.label,
                            onClick = {
                                viewModel.setQuality(q)
                                showQualitySheet = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QualityItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
