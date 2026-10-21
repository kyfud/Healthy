package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DailyProgressCard
import com.example.ui.components.TaskDialog
import com.example.ui.components.TaskFiltersRow
import com.example.ui.components.TaskItemCard
import com.example.ui.components.TaskSearchBar
import com.example.ui.viewmodel.TaskPulseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: TaskPulseViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val selectedPriority by viewModel.selectedPriorityFilter.collectAsStateWithLifecycle()
    val statusFilter by viewModel.taskStatusFilter.collectAsStateWithLifecycle()
    val (completedCount, totalCount) = viewModel.taskProgress.collectAsStateWithLifecycle().value

    val isTaskDialogOpen by viewModel.isTaskDialogOpen.collectAsStateWithLifecycle()
    val editingTask by viewModel.editingTask.collectAsStateWithLifecycle()

    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.US)
    val todayHeader = dateFormat.format(Date())

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { viewModel.openCreateTaskDialog() },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.testTag("voice_task_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Quick Voice Dictate Task",
                        modifier = Modifier.size(20.dp)
                    )
                }

                FloatingActionButton(
                    onClick = { viewModel.openCreateTaskDialog() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.testTag("add_task_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = todayHeader,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Task & Focus Flow",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Daily Progress Bar Card
            item {
                DailyProgressCard(
                    completedCount = completedCount,
                    totalCount = totalCount
                )
            }

            // Search Bar
            item {
                TaskSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.searchQuery.value = it }
                )
            }

            // Filter Chips
            item {
                TaskFiltersRow(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.selectedCategoryFilter.value = it },
                    selectedPriority = selectedPriority,
                    onPrioritySelected = { viewModel.selectedPriorityFilter.value = it },
                    statusFilter = statusFilter,
                    onStatusSelected = { viewModel.taskStatusFilter.value = it }
                )
            }

            // Tasks List
            if (tasks.isEmpty()) {
                item {
                    EmptyTasksState(
                        isFiltered = allTasks.isNotEmpty(),
                        onCreateTask = { viewModel.openCreateTaskDialog() }
                    )
                }
            } else {
                items(
                    items = tasks,
                    key = { it.id }
                ) { task ->
                    TaskItemCard(
                        task = task,
                        onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                        onEdit = { viewModel.openEditTaskDialog(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }

            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }

        // Add/Edit Task Dialog
        if (isTaskDialogOpen) {
            TaskDialog(
                initialTask = editingTask,
                onDismiss = { viewModel.dismissTaskDialog() },
                onSave = { title, desc, priority, category, dueTime, reminder ->
                    viewModel.saveTask(title, desc, priority, category, dueTime, reminder)
                }
            )
        }
    }
}

@Composable
fun EmptyTasksState(
    isFiltered: Boolean,
    onCreateTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .testTag("empty_tasks_state"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFiltered) Icons.Default.FilterList else Icons.Default.CheckCircleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isFiltered) "No matching tasks" else "No tasks scheduled",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isFiltered) {
                    "Try clearing your search query or adjusting the filters."
                } else {
                    "Tap the + button to add a new task and kickstart your day!"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
