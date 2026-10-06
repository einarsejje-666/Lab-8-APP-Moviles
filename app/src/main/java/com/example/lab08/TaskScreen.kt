package com.example.lab08

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Colores inspirados en Microsoft To Do
private val ToDoAzul = Color(0xFF2564CF)
private val ToDoAzulOscuro = Color(0xFF1A4BA8)
private val ToDoGrisTexto = Color(0xFF767676)

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    var newTaskDescription by remember { mutableStateOf("") }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    val pendientes = tasks.filter { !it.isCompleted }
    val completadas = tasks.filter { it.isCompleted }
    val fecha = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-PE"))
            .format(Date()).replaceFirstChar { it.uppercase() }
    }

    fun agregar() {
        if (newTaskDescription.isNotBlank()) {
            viewModel.addTask(newTaskDescription.trim())
            newTaskDescription = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ToDoAzul, ToDoAzulOscuro)))
            .safeDrawingPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Encabezado: título, fecha y botón para borrar todo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tareas",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (tasks.isNotEmpty()) {
                TextButton(onClick = { viewModel.deleteAllTasks() }) {
                    Text("Borrar todo", color = Color.White)
                }
            }
        }
        Text(text = fecha, color = Color.White.copy(alpha = 0.85f), fontSize = 15.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de tareas
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (tasks.isEmpty()) {
                item {
                    Text(
                        text = "No tienes tareas. Agrega una abajo.",
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }
            items(pendientes, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    onToggle = { viewModel.toggleTaskCompletion(task) },
                    onEdit = { taskToEdit = task },
                    onDelete = { viewModel.deleteTask(task) }
                )
            }
            if (completadas.isNotEmpty()) {
                item {
                    Text(
                        text = "Completadas  ${completadas.size}",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                items(completadas, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        onToggle = { viewModel.toggleTaskCompletion(task) },
                        onEdit = { taskToEdit = task },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }

        // Barra inferior para agregar una tarea
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.95f))
                .padding(start = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { agregar() }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar tarea", tint = ToDoAzul)
            }
            TextField(
                value = newTaskDescription,
                onValueChange = { newTaskDescription = it },
                placeholder = { Text("Agregar una tarea", color = ToDoAzul) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { agregar() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Diálogo para editar una tarea
    taskToEdit?.let { task ->
        EditTaskDialog(
            task = task,
            onDismiss = { taskToEdit = null },
            onSave = { nuevoTexto ->
                viewModel.editTask(task, nuevoTexto)
                taskToEdit = null
            }
        )
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Círculo para marcar como completada
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (task.isCompleted) ToDoAzul else Color.Transparent)
                .border(2.dp, if (task.isCompleted) ToDoAzul else ToDoGrisTexto, CircleShape)
                .clickable { onToggle() }
        ) {
            if (task.isCompleted) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Completada",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = task.description,
                color = if (task.isCompleted) ToDoGrisTexto else Color(0xFF252423),
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                fontSize = 16.sp
            )
            Text(
                text = if (task.isCompleted) "Completada" else "Pendiente",
                color = ToDoGrisTexto,
                fontSize = 12.sp
            )
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = ToDoAzul)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Color(0xFFC50F1F))
        }
    }
}

@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var texto by remember { mutableStateOf(task.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar tarea") },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Descripción") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(texto.trim()) },
                enabled = texto.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
