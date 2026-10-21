package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.HabitCalendarView
import com.example.ui.components.HabitCard
import com.example.ui.components.HabitDialog
import com.example.ui.components.getHabitIcon
import com.example.ui.components.parseColorHex
import com.example.ui.viewmodel.TaskPulseViewModel

@Composable
fun HabitsScreen(
    viewModel: TaskPulseViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.allHabits.collectAsStateWithLifecycle()
    val habitLogs by viewModel.allHabitLogs.collectAsStateWithLifecycle()
    val calendarMonth by viewModel.calendarMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedCalendarDate.collectAsStateWithLifecycle()
    val isHabitDialogOpen by viewModel.isHabitDialogOpen.collectAsStateWithLifecycle()
    val editingHabit by viewModel.editingHabit.collectAsStateWithLifecycle()

    val todayStr = viewModel.todayDateString()
    val todayCompletedHabitIds = remember(habitLogs, todayStr) {
        habitLogs.filter { it.dateString == todayStr }.map { it.habitId }.toSet()
    }

    var selectedViewTab by remember { mutableIntStateOf(0) } // 0 = Streaks, 1 = Calendar

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateHabitDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("add_habit_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
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
            // Header
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Habit Streaks & Consistency",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Build positive habits daily and protect your streak.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Summary Stats Row
            item {
                val bestOverallStreak = habits.maxOfOrNull { it.bestStreak } ?: 0
                val activeStreaksCount = habits.count { it.currentStreak > 0 }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Active Streaks",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$activeStreaksCount Habits",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Record Streak",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$bestOverallStreak Days",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // View Tabs (Streaks vs Calendar)
            item {
                TabRow(
                    selectedTabIndex = selectedViewTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedViewTab == 0,
                        onClick = { selectedViewTab = 0 },
                        text = { Text("Daily Streaks") },
                        icon = { Icon(Icons.Default.Whatshot, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_daily_streaks")
                    )
                    Tab(
                        selected = selectedViewTab == 1,
                        onClick = { selectedViewTab = 1 },
                        text = { Text("Calendar Heatmap") },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_calendar_heatmap")
                    )
                }
            }

            if (selectedViewTab == 0) {
                // Streaks Habit List
                if (habits.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No habits added yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap the + button to create your first daily habit!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = habits,
                        key = { it.id }
                    ) { habit ->
                        val isDoneToday = todayCompletedHabitIds.contains(habit.id)
                        HabitCard(
                            habit = habit,
                            isCompletedToday = isDoneToday,
                            onToggleCompletion = { viewModel.toggleHabitCompletion(habit.id) },
                            onEdit = { viewModel.openEditHabitDialog(habit) },
                            onDelete = { viewModel.deleteHabit(habit) }
                        )
                    }
                }
            } else {
                // Calendar Heatmap View
                item {
                    HabitCalendarView(
                        calendarMonth = calendarMonth,
                        habits = habits,
                        habitLogs = habitLogs,
                        selectedDate = selectedDate,
                        onDateSelected = { viewModel.selectCalendarDate(it) },
                        onPrevMonth = { viewModel.changeCalendarMonth(-1) },
                        onNextMonth = { viewModel.changeCalendarMonth(1) }
                    )
                }

                // Selected Date Detail Section
                item {
                    val dateLogs = remember(habitLogs, selectedDate) {
                        habitLogs.filter { it.dateString == selectedDate }.map { it.habitId }.toSet()
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("selected_date_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status for $selectedDate",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${dateLogs.size} of ${habits.size} completed",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            habits.forEach { habit ->
                                val isLogged = dateLogs.contains(habit.id)
                                val habitColor = parseColorHex(habit.colorHex)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.toggleHabitCompletion(habit.id, selectedDate) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isLogged) habitColor else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isLogged) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Done",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = getHabitIcon(habit.iconKey),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = habit.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isLogged) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isLogged) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }

        // Add/Edit Habit Dialog
        if (isHabitDialogOpen) {
            HabitDialog(
                initialHabit = editingHabit,
                onDismiss = { viewModel.dismissHabitDialog() },
                onSave = { name, desc, category, targetDays, colorHex, iconKey, reminderTime ->
                    viewModel.saveHabit(name, desc, category, targetDays, colorHex, iconKey, reminderTime)
                }
            )
        }
    }
}
