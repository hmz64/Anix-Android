package com.anix.rx.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.anix.rx.data.model.*
import com.anix.rx.domain.repository.*
import com.anix.rx.ui.components.*
import androidx.compose.ui.platform.LocalContext
import com.anix.rx.ui.theme.Primary
import com.anix.rx.ui.theme.ThemePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.anix.rx.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Profile", "History", "Favorites")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var showLogoutDialog by remember { mutableStateOf(false) }
                    
                    if (showLogoutDialog) {
                        AlertDialog(
                            onDismissRequest = { showLogoutDialog = false },
                            title = { Text("Confirm Logout") },
                            text = { Text("Are you sure you want to logout?") },
                            confirmButton = {
                                Button(onClick = {
                                    showLogoutDialog = false
                                    viewModel.logout()
                                    onLogout()
                                }) {
                                    Text("Logout")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showLogoutDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                    
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Profile Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        AsyncImage(
                            model = state.user?.avatar,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(MaterialTheme.shapes.extraLarge),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            state.user?.username ?: "Username",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            state.user?.email ?: "email@example.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Surface(
                            color = Primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                state.user?.role?.replaceFirstChar { it.uppercase() } ?: "User",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                }
                
                // Stats
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCard("Watched", "${state.profile?.stats?.episodesWatched ?: 0}")
                        StatCard("Favorites", "${state.profile?.stats?.favoritesCount ?: 0}")
                        StatCard("Comments", "${state.profile?.stats?.commentsCount ?: 0}")
                    }
                }
                
                // Tabs
                item {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }
                }
                
                // Edit Profile Section (Tab 0)
                if (selectedTab == 0) {
                    item {
                        var bioText by remember { mutableStateOf(state.user?.bio ?: "") }
                        var avatarUrl by remember { mutableStateOf(state.user?.avatar ?: "") }
                        
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Edit Profile",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = bioText,
                                onValueChange = { bioText = it },
                                label = { Text("Bio") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = false,
                                maxLines = 3
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = avatarUrl,
                                onValueChange = { avatarUrl = it },
                                label = { Text("Avatar URL") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = {
                                    viewModel.updateProfile(bioText, avatarUrl.ifEmpty { null })
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Save")
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Theme Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Dark Mode",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                var isDark by remember { mutableStateOf(false) }
                                // Load saved theme
                                LaunchedEffect(Unit) {
                                    ThemePreferences.getThemeMode(LocalContext.current).collect { mode ->
                                        isDark = mode == "dark"
                                    }
                                }
                                Switch(
                                    checked = isDark,
                                    onCheckedChange = { checked ->
                                        isDark = checked
                                        val mode = if (checked) "dark" else "light"
                                        CoroutineScope(Dispatchers.IO).launch {
                                            ThemePreferences.saveThemeMode(LocalContext.current, mode)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Tab Content
                when (selectedTab) {
                    0 -> {
                        // Bio Edit
                        item {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Bio",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    state.user?.bio ?: "No bio yet",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    1 -> {
                        // Watch History
                        items(state.watchHistory) { item ->
                            ListItem(
                                headlineContent = { Text(item.title) },
                                supportingContent = { Text("Episode ${item.episodeNumber}") },
                                leadingContent = {
                                    AsyncImage(
                                        model = item.thumbnail,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(MaterialTheme.shapes.small)
                                    )
                                }
                            )
                        }
                    }
                    2 -> {
                        // Favorites
                        items(state.favorites) { item ->
                            ListItem(
                                headlineContent = { Text(item.title) },
                                supportingContent = { Text("Rating: ${item.rating}") },
                                leadingContent = {
                                    AsyncImage(
                                        model = item.thumbnail,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(MaterialTheme.shapes.small)
                                    )
                                }
                            )
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}