package com.sportlinktv.presentation.home

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sportlinktv.R
import com.sportlinktv.data.local.entity.Channel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToPlayer: (Channel) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.filteredChannels) { channel ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            onNavigateToPlayer(channel)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Channel logo
                            if (channel.logo.isNotBlank()) {
                                AsyncImage(
                                    model = channel.logo,
                                    contentDescription = channel.name,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(end = 12.dp)
                                )
                            } else {
                                Spacer(Modifier.width(60.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(channel.name, fontWeight = FontWeight.Medium)
                                if (channel.group.isNotBlank()) {
                                    Text(
                                        channel.group,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            // DRM badge
                            if (!channel.drmKeyId.isNullOrEmpty()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "DRM",
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
