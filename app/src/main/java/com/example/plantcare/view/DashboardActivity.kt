package com.example.plantcare.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantcare.R
import com.example.plantcare.repository.ProductRepositoryImpl
import com.example.plantcare.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ActionId enum
enum class ActionId {
    WATER_PLANTS,
    ADD_NEW_PLANT,
    CARE_SCHEDULE,
    PLANT_HEALTH
}

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get user details from intent
        val username = intent.getStringExtra("username") ?: "User"
        val email = intent.getStringExtra("email") ?: "user@plantcare.com"
        val memberSince = intent.getStringExtra("member_since") ?: "January 2024"
        val userLevel = intent.getStringExtra("user_level") ?: "Expert Gardener"

        setContent {
            DashboardBody(
                username = username,
                email = email,
                memberSince = memberSince,
                userLevel = userLevel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBody(
    username: String = "User",
    email: String = "user@plantcare.com",
    memberSince: String = "January 2024",
    userLevel: String = "Expert Gardener"
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val recentActivities = remember {
        mutableStateListOf<RecentActivity>().apply { addAll(getRecentActivities()) }
    }
    val repo = remember { ProductRepositoryImpl() }
    val viewModel = remember { ProductViewModel(repo) }
    var showProfileDialog by remember { mutableStateOf(false) }

    // Calculate dynamic stats
    val totalPlants = recentActivities.count { it.isPlant }
    val careStreak = 28 // This could be calculated from actual care data

    // Register launcher for AddProductActivity
    val addPlantLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val plantName = data.getStringExtra("plant_name") ?: "New Plant"
                val plantType = data.getStringExtra("plant_type") ?: ""
                val plantDescription = data.getStringExtra("plant_description") ?: ""
                val plantPrice = data.getStringExtra("plant_price") ?: ""
                val plantImageUrl = data.getStringExtra("plant_image_url") ?: ""
                val plantId = data.getStringExtra("plant_id") ?: System.currentTimeMillis().toString()
                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val isEdit = data.getBooleanExtra("is_edit", false)
                if (isEdit) {
                    val index = recentActivities.indexOfFirst { it.plantId == plantId && it.isPlant }
                    if (index != -1) {
                        recentActivities[index] = recentActivities[index].copy(
                            title = "Plant Updated",
                            description = "$plantName${if (plantType.isNotEmpty()) " • $plantType" else ""}",
                            time = "Just now"
                        )
                    }
                    Toast.makeText(context, "$plantName updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    recentActivities.add(
                        0,
                        RecentActivity(
                            title = "New Plant Added",
                            description = "$plantName${if (plantType.isNotEmpty()) " • $plantType" else ""}",
                            time = "Just now",
                            icon = Icons.Default.Add,
                            iconColor = Color(0xFF8BC34A),
                            isPlant = true,
                            plantId = plantId,
                            plantName = plantName,
                            plantType = plantType,
                            plantDescription = plantDescription,
                            plantPrice = plantPrice,
                            plantImageUrl = plantImageUrl
                        )
                    )
                    Toast.makeText(context, "$plantName added successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Register launcher for UpdateProductActivity
    val updatePlantLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val plantName = data.getStringExtra("plant_name") ?: "Updated Plant"
                val plantType = data.getStringExtra("plant_type") ?: ""
                val plantId = data.getStringExtra("plant_id") ?: ""
                val plantDescription = data.getStringExtra("plant_description") ?: ""
                val plantPrice = data.getStringExtra("plant_price") ?: ""
                val plantImageUrl = data.getStringExtra("plant_image_url") ?: ""
                val index = recentActivities.indexOfFirst { it.plantId == plantId && it.isPlant }
                if (index != -1) {
                    recentActivities[index] = recentActivities[index].copy(
                        title = "Plant Updated",
                        description = "$plantName${if (plantType.isNotEmpty()) " • $plantType" else ""}",
                        time = "Just now",
                        plantName = plantName,
                        plantType = plantType,
                        plantDescription = plantDescription,
                        plantPrice = plantPrice,
                        plantImageUrl = plantImageUrl
                    )
                }
                Toast.makeText(context, "$plantName updated successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Good morning, plant parent!",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = username,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Plant care reminders", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Care Reminders",
                            tint = Color.White
                        )
                    }
                    // ENHANCED PROFILE ICON BUTTON
                    IconButton(onClick = { showProfileDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Your Plant Journey",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(getStatsData(totalPlants, careStreak)) { stat ->
                        StatsCard(stat = stat)
                    }
                }
            }

            item {
                WeeklyProgressCard()
            }

            item {
                Text(
                    text = "Plant Care Actions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(320.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(getQuickActions()) { action ->
                        QuickActionCard(action = action) {
                            when (action.id) {
                                ActionId.ADD_NEW_PLANT -> {
                                    val intent = Intent(context, AddProductActivity::class.java)
                                    addPlantLauncher.launch(intent)
                                }
                                ActionId.WATER_PLANTS -> {
                                    recentActivities.add(
                                        0,
                                        RecentActivity(
                                            title = "Plants Watered",
                                            description = "Daily watering completed",
                                            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                                            icon = Icons.Default.Refresh,
                                            iconColor = Color(0xFF2196F3),
                                            isPlant = false
                                        )
                                    )
                                    Toast.makeText(context, "Plants watered successfully!", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    Toast.makeText(context, "${action.title} clicked", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Recent Plant Care & Additions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            items(recentActivities) { activity ->
                RecentActivityCard(
                    activity = activity,
                    onEdit = { plantActivity ->
                        val intent = Intent(context, UpdateProductActivity::class.java).apply {
                            putExtra("plant_id", plantActivity.plantId)
                            putExtra("plant_name", plantActivity.plantName)
                            putExtra("plant_type", plantActivity.plantType)
                            putExtra("plant_description", plantActivity.plantDescription)
                            putExtra("plant_price", plantActivity.plantPrice)
                            putExtra("plant_image_url", plantActivity.plantImageUrl)
                        }
                        updatePlantLauncher.launch(intent)
                    },
                    onDelete = { plantActivity ->
                        if (plantActivity.plantId.isNotEmpty()) {
                            viewModel.deleteProduct(plantActivity.plantId) { success, message ->
                                if (success) {
                                    recentActivities.remove(plantActivity)
                                    Toast.makeText(context, "Plant deleted successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to delete plant: $message", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            recentActivities.remove(plantActivity)
                            Toast.makeText(context, "Activity removed", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    // --- ENHANCED PROFILE DIALOG ---
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Text(
                    "Profile Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Color(0xFF4CAF50).copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F9FA)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Username
                            ProfileDetailRow(
                                icon = Icons.Default.Person,
                                label = "Username",
                                value = username
                            )

                            // Email
                            ProfileDetailRow(
                                icon = Icons.Default.Email,
                                label = "Email",
                                value = email
                            )

                            // Member Since
                            ProfileDetailRow(
                                icon = Icons.Default.DateRange,
                                label = "Member Since",
                                value = memberSince
                            )

                            // Total Plants
                            ProfileDetailRow(
                                icon = Icons.Default.Place,
                                label = "Total Plants",
                                value = "$totalPlants plants"
                            )

                            // Care Streak
                            ProfileDetailRow(
                                icon = Icons.Default.Star,
                                label = "Care Streak",
                                value = "$careStreak days"
                            )

                            // Plant Parent Level
                            ProfileDetailRow(
                                icon = Icons.Default.Star,
                                label = "Plant Parent Level",
                                value = userLevel
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Settings
                    Text(
                        text = "Quick Settings",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Notification Toggle
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Notifications toggled", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color(0xFF4CAF50)
                                )
                            }
                            Text("Reminders", fontSize = 10.sp)
                        }

                        // Settings
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Settings opened", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color(0xFF4CAF50)
                                )
                            }
                            Text("Settings", fontSize = 10.sp)
                        }

                        // Help
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Help & Support", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Help",
                                    tint = Color(0xFF4CAF50)
                                )
                            }
                            Text("Help", fontSize = 10.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            showProfileDialog = false
                            Toast.makeText(context, "Edit profile feature coming soon!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Edit Profile", color = Color(0xFF4CAF50))
                    }
                    TextButton(
                        onClick = { showProfileDialog = false }
                    ) {
                        Text("Close")
                    }
                }
            }
        )
    }
}

// ProfileDetailRow Composable
@Composable
fun ProfileDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun StatsCard(stat: StatData) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stat.value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = stat.color
            )
            Text(
                text = stat.label,
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WeeklyProgressCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Care Goal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "92% Complete",
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = 0.92f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE8F5E8)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "23 out of 25 care tasks completed this week",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun QuickActionCard(action: QuickAction, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = action.backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RecentActivityCard(
    activity: RecentActivity,
    onEdit: (RecentActivity) -> Unit = {},
    onDelete: (RecentActivity) -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(activity.iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = activity.icon,
                    contentDescription = null,
                    tint = activity.iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = activity.title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(text = activity.description, fontSize = 12.sp, color = Color.Gray)
            }
            Text(text = activity.time, fontSize = 12.sp, color = Color.Gray)
            if (activity.isPlant) {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.Gray
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        modifier = Modifier.size(18.dp),
                                        tint = Color(0xFF4CAF50)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Edit Plant")
                                }
                            },
                            onClick = {
                                expanded = false
                                onEdit(activity)
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        modifier = Modifier.size(18.dp),
                                        tint = Color(0xFFE53E3E)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Delete Plant")
                                }
                            },
                            onClick = {
                                expanded = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Plant") },
            text = { Text("Are you sure you want to delete this plant? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(activity)
                    }
                ) {
                    Text("Delete", color = Color(0xFFE53E3E))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Data classes and helpers

data class StatData(
    val value: String,
    val label: String,
    val color: Color
)

data class QuickAction(
    val id: ActionId,
    val title: String,
    val icon: ImageVector,
    val backgroundColor: Color
)

data class RecentActivity(
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector,
    val iconColor: Color,
    val isPlant: Boolean = false,
    val plantId: String = "",
    val plantName: String = "",
    val plantType: String = "",
    val plantDescription: String = "",
    val plantPrice: String = "",
    val plantImageUrl: String = ""
)

fun getStatsData(totalPlants: Int, careStreak: Int): List<StatData> = listOf(
    StatData("$totalPlants", "Plants Cared", Color(0xFF4CAF50)),
    StatData("$careStreak", "Days Streak", Color(0xFF2196F3)),
    StatData("${(totalPlants * 0.9).toInt()}", "Healthy Plants", Color(0xFF8BC34A)),
    StatData("4.8", "Care Rating", Color(0xFFFF9800))
)

fun getQuickActions(): List<QuickAction> = listOf(
    QuickAction(ActionId.WATER_PLANTS, "Water Plants", Icons.Default.Refresh, Color(0xFF2196F3)),
    QuickAction(ActionId.ADD_NEW_PLANT, "Add New Plant", Icons.Default.Add, Color(0xFF4CAF50)),
    QuickAction(ActionId.CARE_SCHEDULE, "Care Schedule", Icons.Default.Settings, Color(0xFF9C27B0)),
    QuickAction(ActionId.PLANT_HEALTH, "Plant Health", Icons.Default.Star, Color(0xFFE91E63))
)

fun getRecentActivities(): List<RecentActivity> = listOf(
    RecentActivity(
        "Watered Monstera",
        "Morning watering • Next due in 3 days",
        "2 hours ago",
        Icons.Default.Refresh,
        Color(0xFF2196F3),
        isPlant = false
    ),
    RecentActivity(
        "Added fertilizer",
        "Snake Plant • Nutrient boost applied",
        "1 day ago",
        Icons.Default.Settings,
        Color(0xFF4CAF50),
        isPlant = false
    ),
    RecentActivity(
        "Plant health check",
        "All plants looking healthy!",
        "2 days ago",
        Icons.Default.Check,
        Color(0xFFFF9800),
        isPlant = false
    )
)

@Preview
@Composable
fun DashboardPreview() {
    DashboardBody(username = "Plant Lover")
}
