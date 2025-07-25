package com.example.plantcarelite.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//import com.example.plantcarelite.screens.HomeScreen
import com.example.plantcarelite.screens.LoginScreen
import com.example.plantcarelite.screens.RegisterScreen
//import com.example.plantcarelite.screens.PlantListScreen
//import com.example.plantcarelite.screens.AddPlantScreen
//import com.example.plantcarelite.screens.SplashScreen

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController = navController)
        }

        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("register") {
            RegisterScreen(navController = navController)
        }

        composable("home") {
            HomeScreen(navController = navController)
        }

        composable("plantList") {
            PlantListScreen(navController = navController)
        }

        composable("addPlant") {
            AddPlantScreen(navController = navController)
        }
    }
}
