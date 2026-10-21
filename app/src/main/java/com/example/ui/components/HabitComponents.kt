package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.TaskCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun getHabitIcon(iconKey: String): ImageVector {
    return when (iconKey.lowercase()) {
        "water" -> Icons.Default.WaterDrop
        "fitness" -> Icons.Default.FitnessCenter
        "book" -> Icons.Default.Book
        "meditation" -> Icons.Default.SelfImprovement
        "code" -> Icons.Default.Code
        "walk" -> Icons.AutoMirrored.Filled.DirectionsWalk
        else -> Icons.Default.FitnessCenter
    }
}

fun parseColorHex(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFF4F46E5)
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    isCompletedToday: Boolean,
    onToggleCompletion: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habitColor = parseColorHex(habit.colorHex)
    val checkBgColor by animateColorAsState(
        targetValue = if (isCompletedToday) habitColor else Color.Transparent,
        label = "check_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Habit Icon Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(habitColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getHabitIcon(habit.iconKey),
                    contentDescription = habit.name,
                    tint = habitColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (habit.description.isNotBlank()) {
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Current Streak Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (habit.currentStreak > 0) Color(0xFFF97316).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = if (habit.currentStreak > 0) Color(0xFFEA580C) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${habit.currentStreak} day streak",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (habit.currentStreak > 0) Color(0xFFEA580C) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Best streak badge
                    if (habit.bestStreak > 0) {
                        Text(
                            text = "Best: ${habit.bestStreak}d",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Checkoff Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(checkBgColor)
                    .border(
                        width = 2.dp,
                        color = if (isCompletedToday) habitColor else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .clickable { onToggleCompletion() }
                    .testTag("habit_check_${habit.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (isCompletedToday) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed Today",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Edit / Delete icons
            Column(
                modifier = Modifier.padding(start = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("habit_edit_btn_${habit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Habit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("habit_delete_btn_${habit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Habit",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

data class CalendarDay(
    val dayNumber: Int,
    val dateString: String,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val completedHabitCount: Int,
    val totalHabits: Int
)

@Composable
fun HabitCalendarView(
    calendarMonth: Calendar,
    habits: List<Habit>,
    habitLogs: List<HabitLog>,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    val monthTitle = monthFormat.format(calendarMonth.time)

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val todayStr = dateFormat.format(Date())

    // Group logs by dateString
    val logsByDate = remember(habitLogs) {
        habitLogs.groupBy { it.dateString }
    }

    // Build days of the current month grid
    val daysInMonth = remember(calendarMonth, habitLogs, habits) {
        val days = mutableListOf<CalendarDay>()
        val cal = calendarMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Previous month padding
        val prevCal = cal.clone() as Calendar
        prevCal.add(Calendar.MONTH, -1)
        val maxPrevDays = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in (firstDayOfWeek - 2) downTo 0) {
            val dayNum = maxPrevDays - i
            prevCal.set(Calendar.DAY_OF_MONTH, dayNum)
            val dStr = dateFormat.format(prevCal.time)
            days.add(
                CalendarDay(
                    dayNumber = dayNum,
                    dateString = dStr,
                    isCurrentMonth = false,
                    isToday = dStr == todayStr,
                    completedHabitCount = logsByDate[dStr]?.size ?: 0,
                    totalHabits = habits.size
                )
            )
        }

        // Current month days
        for (i in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, i)
            val dStr = dateFormat.format(cal.time)
            days.add(
                CalendarDay(
                    dayNumber = i,
                    dateString = dStr,
                    isCurrentMonth = true,
                    isToday = dStr == todayStr,
                    completedHabitCount = logsByDate[dStr]?.size ?: 0,
                    totalHabits = habits.size
                )
            )
        }

        // Next month padding to fill grid
        var nextDay = 1
        while (days.size % 7 != 0) {
            val nextCal = cal.clone() as Calendar
            nextCal.add(Calendar.MONTH, 1)
            nextCal.set(Calendar.DAY_OF_MONTH, nextDay)
            val dStr = dateFormat.format(nextCal.time)
            days.add(
                CalendarDay(
                    dayNumber = nextDay,
                    dateString = dStr,
                    isCurrentMonth = false,
                    isToday = dStr == todayStr,
                    completedHabitCount = logsByDate[dStr]?.size ?: 0,
                    totalHabits = habits.size
                )
            )
            nextDay++
        }
        days
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_calendar_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Month title & Nav buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevMonth,
                    modifier = Modifier.testTag("calendar_prev_month")
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                }

                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.testTag("calendar_next_month")
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Weekday labels
            val weekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekDays.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid
            val rows = daysInMonth.chunked(7)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                rows.forEach { rowDays ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rowDays.forEach { day ->
                            val isSelected = day.dateString == selectedDate
                            val completionFraction = if (day.totalHabits > 0) {
                                day.completedHabitCount.toFloat() / day.totalHabits.toFloat()
                            } else 0f

                            val cellBg = when {
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                completionFraction >= 0.75f -> Color(0xFF10B981).copy(alpha = 0.22f)
                                completionFraction > 0f -> Color(0xFF06B6D4).copy(alpha = 0.15f)
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cellBg)
                                    .border(
                                        width = if (day.isToday) 1.5.dp else 0.dp,
                                        color = if (day.isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onDateSelected(day.dateString) }
                                    .testTag("calendar_day_${day.dateString}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${day.dayNumber}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (day.isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            day.isToday -> MaterialTheme.colorScheme.primary
                                            !day.isCurrentMonth -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )

                                    if (day.completedHabitCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (completionFraction >= 0.75f) Color(0xFF10B981)
                                                    else Color(0xFF06B6D4)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Heatmap Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF06B6D4).copy(alpha = 0.2f))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Partial", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.35f))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("All Done", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitDialog(
    initialHabit: Habit?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        description: String,
        category: TaskCategory,
        targetDays: Int,
        colorHex: String,
        iconKey: String,
        reminderTime: String?
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialHabit?.name ?: "") }
    var description by remember { mutableStateOf(initialHabit?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(initialHabit?.category ?: TaskCategory.HEALTH) }
    var targetDays by remember { mutableIntStateOf(initialHabit?.targetDaysPerWeek ?: 7) }
    var selectedColor by remember { mutableStateOf(initialHabit?.colorHex ?: "#4F46E5") }
    var selectedIcon by remember { mutableStateOf(initialHabit?.iconKey ?: "fitness") }
    var reminderTime by remember { mutableStateOf(initialHabit?.reminderTime ?: "08:00 AM") }
    var hasError by remember { mutableStateOf(false) }

    val colorOptions = listOf(
        "#4F46E5", // Indigo
        "#06B6D4", // Cyan
        "#10B981", // Emerald
        "#F59E0B", // Amber
        "#EC4899", // Pink
        "#8B5CF6"  // Violet
    )

    val iconOptions = listOf("fitness", "water", "book", "meditation", "code", "walk")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("habit_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (initialHabit != null) "Edit Habit" else "Create New Habit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) hasError = false
                    },
                    label = { Text("Habit Name (e.g. Read 20 mins) *") },
                    isError = hasError,
                    supportingText = { if (hasError) Text("Name cannot be empty") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Motivation or cue (Optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_desc_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Habit Icon",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconOptions.forEach { iconKey ->
                        val isSelected = selectedIcon == iconKey
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedIcon = iconKey }
                                .testTag("icon_option_$iconKey"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getHabitIcon(iconKey),
                                contentDescription = iconKey,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Theme Accent Color",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorOptions.forEach { hex ->
                        val isSelected = selectedColor == hex
                        val c = parseColorHex(hex)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                                .testTag("color_option_$hex"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Weekly Target Frequency",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(3, 5, 7).forEach { days ->
                        FilterChip(
                            selected = targetDays == days,
                            onClick = { targetDays = days },
                            label = { Text(if (days == 7) "Everyday" else "$days days/week") },
                            modifier = Modifier.testTag("target_days_$days")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    label = { Text("Daily Reminder Time (e.g. 08:30 AM)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_reminder_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_habit_button")
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                hasError = true
                            } else {
                                onSave(
                                    name.trim(),
                                    description.trim(),
                                    selectedCategory,
                                    targetDays,
                                    selectedColor,
                                    selectedIcon,
                                    reminderTime.trim().ifEmpty { null }
                                )
                            }
                        },
                        modifier = Modifier.testTag("submit_habit_button")
                    ) {
                        Text(if (initialHabit != null) "Save Habit" else "Add Habit")
                    }
                }
            }
        }
    }
}
