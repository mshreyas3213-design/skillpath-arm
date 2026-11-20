package com.bharatai.learningcompanion

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.goalDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "goal_settings"
)

object GoalKeys {
    val EXAM = stringPreferencesKey("exam")
    val SUBJECT = stringPreferencesKey("subject")
    val LEVEL = stringPreferencesKey("level")
}

class GoalPreferences(private val context: Context) {

    val goalFlow: Flow<GoalData?> = context.goalDataStore.data.map { preferences ->
        GoalData(
            exam = preferences[GoalKeys.EXAM],
            subject = preferences[GoalKeys.SUBJECT],
            level = preferences[GoalKeys.LEVEL]
        )
    }

    suspend fun saveGoal(exam: String, subject: String, level: String) {
        context.goalDataStore.edit { preferences ->
            preferences[GoalKeys.EXAM] = exam
            preferences[GoalKeys.SUBJECT] = subject
            preferences[GoalKeys.LEVEL] = level
        }
    }

    suspend fun clearGoals() {
        context.goalDataStore.edit { it.clear() }
    }
}

data class GoalData(
    val exam: String? = null,
    val subject: String? = null,
    val level: String? = null
)