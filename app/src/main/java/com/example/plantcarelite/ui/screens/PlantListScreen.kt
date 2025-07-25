package com.example.plantcarelite.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.plantcarelite.model.Plant
import com.example.plantcarelite.viewmodel.PlantViewModel

@Composable
fun PlantListScreen(navController: NavController, vm: PlantViewModel = viewModel()) {
    val plants by vm.plants.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addPlant") }) {
                Text("+")
            }
        }
    ) { padding ->
        if (plants.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No plants yet. Tap + to add one!")
            }
        } else {
            LazyColumn(contentPadding = padding) {
                items(plants) { plant ->
                    PlantCard(plant, onDelete = { vm.deletePlant(plant.id) })
                }
            }
        }
    }
}

@Composable
fun PlantCard(plant: Plant, onDelete: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .clickable { /* could edit here */ }) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = plant.name, style = MaterialTheme.typography.titleMedium)
                Text(text = plant.type, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}


