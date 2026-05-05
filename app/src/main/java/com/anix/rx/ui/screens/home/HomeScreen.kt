package com.anix.rx.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.input.nestedscroll.rememberNestedScrollConnection
import com.anix.rx.ui.components.*
import com.anix.rx.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAnimeClick: (Long) -> Unit,
    onProfileClick: () -> Unit,
    onModeratorClick: () -> Unit,
    onAdminClick: () -> Unit,
    onBrowseClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Inisialisasi data navigasi
    val navItems = listOf(
        Triple("Home", Icons.Default.Home, 0),
        Triple("Browse", Icons.Default.Search, 1),
        Triple("Profile", Icons.Default.Person, 2)
    )
    var selectedItem by remember { mutableIntStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "ANIX",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    if (state.userRole == "moderator" || state.userRole == "admin") {
                        IconButton(onClick = onModeratorClick) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Moderator")
                        }
                    }
                    if (state.userRole == "admin") {
                        IconButton(onClick = onAdminClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Admin")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, (title, icon, _) ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            when (index) {
                                1 -> onBrowseClick()
                                2 -> onProfileClick()
                            }
                        },
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Trending Section
                item {
                    SectionHeader(title = "Trending 🎬")
                    
                    if (state.trending.isEmpty()) {
                        EmptyState(
                            title = "No trending anime",
                            message = "Check back later!",
                            modifier = Modifier.height(200.dp)
                        )
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.trending) { anime ->
                                AnimeCard(
                                    anime = anime,
                                    onClick = { onAnimeClick(anime.id) },
                                    modifier = Modifier.width(160.dp)
                                )
                            }
                        }
                    }
                }
                
                // Ongoing Section
                item {
                    if (state.ongoing.isNotEmpty()) {
                        SectionHeader(title = "Ongoing 📺")
                        
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.ongoing) { anime ->
                                AnimeCard(
                                    anime = anime,
                                    onClick = { onAnimeClick(anime.id) },
                                    modifier = Modifier.width(160.dp)
                                )
                            }
                        }
                    }
                }
                
                // Recently Added Section
                item {
                    if (state.recentlyAdded.isNotEmpty()) {
                        SectionHeader(title = "Recently Added 🆕")
                        
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.recentlyAdded) { anime ->
                                AnimeCard(
                                    anime = anime,
                                    onClick = { onAnimeClick(anime.id) },
                                    modifier = Modifier.width(160.dp)
                                )
                            }
                        }
                    }
                }
                
                // All Anime Grid Header
                item {
                    SectionHeader(title = "All Anime")
                }
                
                // Items Grid
                items(
                    (state.trending + state.ongoing + state.recentlyAdded).distinctBy { it.id }
                ) { anime ->
                    AnimeCard(
                        anime = anime,
                        onClick = { onAnimeClick(anime.id) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
