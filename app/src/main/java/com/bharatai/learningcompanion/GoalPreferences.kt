
package com.bharatai.learningcompanion

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.goalDataStore by preferencesDataStore(
    name = "goal_settings"
)

object GoalKeys {
    val EXAM = stringPreferencesKey("exam")
    val SUBJECT = stringPreferencesKey("subject")
    val HOURS = stringPreferencesKey("hours")
    val TARGET_DATE = stringPreferencesKey("target_date")
}
