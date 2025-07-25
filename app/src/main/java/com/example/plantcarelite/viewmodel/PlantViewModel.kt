package com.example.plantcarelite.viewmodel

import androidx.lifecycle.ViewModel
import com.example.plantcarelite.model.Plant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlantViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _plants = MutableStateFlow<List<Plant>>(emptyList())
    val plants: StateFlow<List<Plant>> = _plants

    init {
        loadPlants()
    }

    fun addPlant(name: String, type: String, note: String) {
        val id = db.collection("plants").document().id
        val plant = Plant(id, name, type, note, userId)
        db.collection("plants").document(id).set(plant).addOnSuccessListener {
            loadPlants()
        }
    }

    fun deletePlant(id: String) {
        db.collection("plants").document(id).delete().addOnSuccessListener {
            loadPlants()
        }
    }

    private fun loadPlants() {
        db.collection("plants")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val plantList = result.map { it.toObject(Plant::class.java) }
                _plants.value = plantList
            }
    }
}
