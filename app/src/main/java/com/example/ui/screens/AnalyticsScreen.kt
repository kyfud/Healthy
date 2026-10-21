package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TaskCategory
import com.example.ui.viewmodel.TaskPulseViewModel

@Composable
fun AnalyticsScreen(
    viewModel: TaskPulseViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val habits by viewModel.allHabits.collectAsStateWithLifecycle()
    val habitLogs by viewModel.allHabitLogs.collectAsStateWithLifecycle()

    val completedTasks = tasks.count { it.isCompleted }
    val totalTasks = tasks.size
    val taskCompletionRate = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks.toFloat() * 100).toInt() else 0

    val maxStreak = habits.maxOfOrNull { it.bestStreak } ?: 0
    val totalHabitCompletions = habitLogs.size

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.triggerTestNotification()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Productivity & Insights",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Weekly completion metrics, category distribution, & reminders",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 4 Scorecard Badges
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScorecardItem(
                        title = "Tasks Done",
                        value = "$completedTasks / $totalTasks",
                        subtitle = "$taskCompletionRate% completion rate",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )

                    ScorecardItem(
                        title = "Habit Logs",
                        value = "$totalHabitCompletions",
                        subtitle = "Total logged check-ins",
                        icon = Icons.Default.LocalFireDepartment,
                        color = Color(0xFFF97316),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScorecardItem(
                        title = "Record Streak",
                        value = "$maxStreak Days",
                        subtitle = "Best continuous habit",
                        icon = Icons.Default.EmojiEvents,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )

                    ScorecardItem(
                        title = "Active Habits",
                        value = "${habits.size}",
                        subtitle = "Routines currently tracked",
                        icon = Icons.Default.BarChart,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Category Breakdown Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("category_breakdown_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tasks by Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TaskCategory.values().forEach { category ->
                        val catTasks = tasks.filter { it.category == category }
                        val catCount = catTasks.size
                        val catFraction = if (totalTasks > 0) catCount.toFloat() / totalTasks.toFloat() else 0f
                        val animatedFraction by animateFloatAsState(
                            targetValue = catFraction,
                            animationSpec = tween(durationMillis = 500),
                            label = "cat_fraction_${category.name}"
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(category.getColor())
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "$catCount tasks (${(catFraction * 100).toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = { animatedFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = category.getColor(),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Smart Reminders & Notifications Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notifications_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Smart Notifications & Alarms",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "TaskPulse schedules on-device local notifications using Android AlarmManager to notify you before task due dates and remind you to keep habit streaks alive.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.triggerTestNotification()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_notification_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Test Notification Now")
                    }
                }
            }
        }

        // Storage & Offline Architecture
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Room Database Persistent Storage",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Fast, encrypted, offline-first SQLite database. Zero latency, works completely without internet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun ScorecardItem(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
