package com.bharatai.learningcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bharatai.learningcompanion.ui.theme.AILearningCompanionTheme
import com.bharatal.learningcompanion.AppNav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AILearningCompanionTheme {
                AppNav()
            }
        }
    }
}