package com.sportlinktv.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sportlinktv.R
import com.sportlinktv.data.local.entity.Channel

import androidx.compose.ui.platform.LocalContext
import com.sportlinktv.BuildConfig
import com.sportlinktv.util.UpdateInfo
import com.sportlinktv.util.UpdateManager
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToPlayer: (Channel) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        val info = UpdateManager.checkForUpdate(BuildConfig.VERSION_CODE)
        if (info != null) {
            updateInfo = info
        }
    }

    if (updateInfo != null) {
        AlertDialog(
            onDismissRequest = { if (!isDownloading) updateInfo = null },
            title = { Text("Update Available (v${updateInfo?.versionName})") },
            text = {
                Column {
                    Text(updateInfo?.changelog ?: "A new version of Sportlink Tv is available.")
                    if (isDownloading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Downloading: ${(downloadProgress * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isDownloading && updateInfo != null) {
                            isDownloading = true
                            coroutineScope.launch {
                                val success = UpdateManager.downloadAndInstallApk(
                                    context = context,
                                    apkUrl = updateInfo!!.apkUrl,
                                    onProgress = { downloadProgress = it }
                                )
                                isDownloading = false
                                if (!success) {
                                    updateInfo = null
                                }
                            }
                        }
                    },
                    enabled = !isDownloading
                ) {
                    Text(if (isDownloading) "Downloading..." else "Update Now")
                }
            },
            dismissButton = {
                if (!isDownloading) {
                    TextButton(onClick = { updateInfo = null }) {
                        Text("Later")
                    }
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // ── Header with logo ──────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(40.dp).padding(end = 8.dp)
            )
            Text(
                text = "Sportlink Tv",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        if (uiState.error != null) {
            Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
        }

        if (uiState.channels.isEmpty()) {
            // ── Playlist Grid ─────────────────────────────────
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.playlists) { playlist ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clickable { viewModel.selectPlaylist(playlist) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = playlist.icon),
                                contentDescription = playlist.name,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = playlist.name, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        } else {
            // ── Channel List ──────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { viewModel.clearChannels() }) {
                    Text("Back to Groups")
                }
            }
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                placeholder = { Text("Search channels...") },
                leadingIcon = { Icon(androidx.compose.material.icons.Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(androidx.compose.material.icons.Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.filteredChannels) { channel ->
                    ChannelCard(channel = channel, onClick = { onNavigateToPlayer(channel) })
                }
            }
        }
    }
}

@Composable
fun ChannelCard(channel: Channel, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            // Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                if (channel.logo.isNotBlank()) {
                    AsyncImage(
                        model = channel.logo,
                        contentDescription = channel.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Badge
                Surface(
                    color = Color(0xFFE53935),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "LIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                
                // DRM Badge
                if (!channel.drmKeyId.isNullOrEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "DRM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            // Body
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = channel.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (channel.group.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = channel.group,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
