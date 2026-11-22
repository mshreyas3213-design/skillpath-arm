package com.bharatai.learningcompanion.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Singleton profile
    val name: String,
    val goal: String,
    val currentSkills: String,
    val timestamp: Long = System.currentTimeMillis()
)