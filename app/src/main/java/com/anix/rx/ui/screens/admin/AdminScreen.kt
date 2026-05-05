package com.anix.rx.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anix.rx.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onBackClick: () -> Unit, viewModel: AdminViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
        navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Back") } }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.isLoading) { CircularProgressIndicator() }
            else {
                state.stats?.let {
                    Text("Anime: ${it.totalAnime}"); Text("Episodes: ${it.totalEpisodes}")
                    Text("Users: ${it.totalUsers}"); Text("Comments: ${it.totalComments}"); Text("Views: ${it.totalViews}")
                }
                state.error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
