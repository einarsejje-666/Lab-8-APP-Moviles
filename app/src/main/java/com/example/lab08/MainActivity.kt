package com.example.lab08

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.room.Room
import com.example.lab08.ui.theme.Lab08Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab08Theme {
                // Se crean una sola vez para no repetirlos en cada recomposición
                val db = remember {
                    Room.databaseBuilder(
                        applicationContext,
                        TaskDatabase::class.java,
                        "task_db"
                    ).build()
                }
                val viewModel = remember { TaskViewModel(db.taskDao()) }

                TaskScreen(viewModel)
            }
        }
    }
}
