package com.anix.rx.ui.screens.moderator

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
import com.anix.rx.ui.viewmodel.ModeratorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorScreen(onBackClick: () -> Unit, viewModel: ModeratorViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Moderator Dashboard", fontWeight = FontWeight.Bold) },
        navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Back") } }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(state.message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
