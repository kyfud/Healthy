package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Task
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.TaskStatusFilter

@Composable
fun DailyProgressCard(
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "progress_anim"
    )
    val percentage = (progress * 100).toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_progress_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Progress",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Today's Checklist",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$completedCount of $totalCount completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val motivationalMsg = when {
                totalCount == 0 -> "Add your first task below to start crushing your goals!"
                percentage == 100 -> "Incredible job! You cleared all your tasks today! 🎉"
                percentage >= 60 -> "Strong momentum! You're well on track to finish everything."
                percentage > 0 -> "Off to a solid start. Stay focused on your top priority!"
                else -> "Ready to take action? Check off your first task!"
            }

            Text(
                text = motivationalMsg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun TaskSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_search_bar"),
        placeholder = { Text("Search tasks or categories...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskFiltersRow(
    selectedCategory: TaskCategory?,
    onCategorySelected: (TaskCategory?) -> Unit,
    selectedPriority: TaskPriority?,
    onPrioritySelected: (TaskPriority?) -> Unit,
    statusFilter: TaskStatusFilter,
    onStatusSelected: (TaskStatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Status row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskStatusFilter.values().forEach { status ->
                val isSelected = statusFilter == status
                FilterChip(
                    selected = isSelected,
                    onClick = { onStatusSelected(status) },
                    label = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    modifier = Modifier.testTag("status_filter_${status.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Categories & Priority chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null && selectedPriority == null,
                onClick = {
                    onCategorySelected(null)
                    onPrioritySelected(null)
                },
                label = { Text("All Filters") },
                modifier = Modifier.testTag("filter_all")
            )

            TaskPriority.values().forEach { priority ->
                val isSelected = selectedPriority == priority
                FilterChip(
                    selected = isSelected,
                    onClick = { onPrioritySelected(if (isSelected) null else priority) },
                    label = { Text("${priority.displayName} Priority") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = priority.getColor().copy(alpha = 0.2f),
                        selectedLabelColor = priority.getColor()
                    ),
                    modifier = Modifier.testTag("filter_priority_${priority.name}")
                )
            }

            TaskCategory.values().forEach { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(if (isSelected) null else category) },
                    label = { Text(category.displayName) },
                    modifier = Modifier.testTag("filter_category_${category.name}")
                )
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: Task,
    onToggleCompletion: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleCompletion() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(task.category.getColor().copy(alpha = 0.15f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = task.category.getColor()
                        )
                    }

                    // Priority Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(task.priority.getColor().copy(alpha = 0.15f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.priority.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = task.priority.getColor()
                        )
                    }

                    if (task.reminderEnabled) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Reminder Active",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!task.dueTime.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Due time",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = task.dueTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Action Buttons
            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("task_edit_btn_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Task",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("task_delete_btn_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Task",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskDialog(
    initialTask: Task?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        priority: TaskPriority,
        category: TaskCategory,
        dueTime: String?,
        reminderEnabled: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var selectedPriority by remember { mutableStateOf(initialTask?.priority ?: TaskPriority.MEDIUM) }
    var selectedCategory by remember { mutableStateOf(initialTask?.category ?: TaskCategory.WORK) }
    var dueTime by remember { mutableStateOf(initialTask?.dueTime ?: "12:00 PM") }
    var reminderEnabled by remember { mutableStateOf(initialTask?.reminderEnabled ?: true) }
    var hasError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var speechFeedback by remember { mutableStateOf<String?>(null) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                val trimmed = spoken.trim()
                val lower = trimmed.lowercase()

                // Smart auto-priority detection from spoken text
                if (lower.contains("urgent") || lower.contains("high priority") || lower.contains("asap")) {
                    selectedPriority = TaskPriority.HIGH
                } else if (lower.contains("low priority")) {
                    selectedPriority = TaskPriority.LOW
                } else if (lower.contains("medium priority")) {
                    selectedPriority = TaskPriority.MEDIUM
                }

                // Smart auto-category detection from spoken text
                if (lower.contains("work") || lower.contains("meeting") || lower.contains("project") || lower.contains("report")) {
                    selectedCategory = TaskCategory.WORK
                } else if (lower.contains("health") || lower.contains("workout") || lower.contains("exercise") || lower.contains("water") || lower.contains("doctor")) {
                    selectedCategory = TaskCategory.HEALTH
                } else if (lower.contains("study") || lower.contains("reading") || lower.contains("learn") || lower.contains("book")) {
                    selectedCategory = TaskCategory.STUDY
                } else if (lower.contains("personal") || lower.contains("grocery") || lower.contains("home") || lower.contains("buy")) {
                    selectedCategory = TaskCategory.PERSONAL
                }

                // Smart due time detection
                val timeRegex = Regex("(?:at|by)\\s+(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm|AM|PM))")
                val timeMatch = timeRegex.find(trimmed)
                if (timeMatch != null) {
                    dueTime = timeMatch.groupValues[1].uppercase()
                }

                if (title.isBlank()) {
                    title = trimmed.replaceFirstChar { it.uppercase() }
                } else {
                    title = "$title $trimmed"
                }
                hasError = false
                speechFeedback = "Dictated: \"$trimmed\""
            }
        }
    }

    val launchDictation: () -> Unit = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak task (e.g. 'Finish sprint report by 3 PM, high priority')")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Voice dictation not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("task_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTask != null) "Edit Task" else "Create New Task",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FilledTonalButton(
                        onClick = launchDictation,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("voice_to_text_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Dictate",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dictate", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) hasError = false
                    },
                    label = { Text("Task Title *") },
                    trailingIcon = {
                        IconButton(
                            onClick = launchDictation,
                            modifier = Modifier.testTag("task_title_mic_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Dictate Task Title",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    isError = hasError,
                    supportingText = {
                        if (hasError) Text("Title cannot be empty")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                AnimatedVisibility(visible = speechFeedback != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = speechFeedback ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_desc_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.values().forEach { priority ->
                        val isSelected = selectedPriority == priority
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = priority.getColor().copy(alpha = 0.2f),
                                selectedLabelColor = priority.getColor()
                            ),
                            modifier = Modifier.testTag("dialog_priority_${priority.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TaskCategory.values().forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.displayName) },
                            modifier = Modifier.testTag("dialog_category_${cat.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = dueTime,
                    onValueChange = { dueTime = it },
                    label = { Text("Due Time (e.g. 10:30 AM)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_due_time_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Smart Reminder Notification",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Alert before due time",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                        modifier = Modifier.testTag("reminder_switch")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_task_button")
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                hasError = true
                            } else {
                                onSave(
                                    title.trim(),
                                    description.trim(),
                                    selectedPriority,
                                    selectedCategory,
                                    dueTime.trim().ifEmpty { null },
                                    reminderEnabled
                                )
                            }
                        },
                        modifier = Modifier.testTag("submit_task_button")
                    ) {
                        Text(if (initialTask != null) "Save Changes" else "Add Task")
                    }
                }
            }
        }
    }
}
