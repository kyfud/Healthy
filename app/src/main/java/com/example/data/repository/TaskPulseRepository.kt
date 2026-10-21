package com.example.data.repository

import com.example.data.local.HabitDao
import com.example.data.local.TaskDao
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.Task
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TaskPulseRepository(
    private val taskDao: TaskDao,
    private val habitDao: HabitDao
) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allHabitLogs: Flow<List<HabitLog>> = habitDao.getAllLogs()

    // Task Operations
    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun toggleTaskCompletion(task: Task) {
        val newStatus = !task.isCompleted
        val completedAt = if (newStatus) System.currentTimeMillis() else null
        taskDao.setTaskCompleted(task.id, newStatus, completedAt)
    }

    // Habit Operations
    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteAllLogsForHabit(habit.id)
        habitDao.deleteHabit(habit)
    }

    suspend fun deleteHabitById(id: Long) {
        habitDao.deleteAllLogsForHabit(id)
        habitDao.deleteHabitById(id)
    }

    fun getLogsForDate(dateString: String): Flow<List<HabitLog>> =
        habitDao.getLogsForDate(dateString)

    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> =
        habitDao.getLogsForHabit(habitId)

    suspend fun toggleHabitCompletion(habitId: Long, dateString: String) {
        val isLogged = habitDao.isHabitLogged(habitId, dateString) > 0
        if (isLogged) {
            habitDao.deleteHabitLog(habitId, dateString)
        } else {
            habitDao.insertHabitLog(HabitLog(habitId = habitId, dateString = dateString))
        }
        recalculateStreaks(habitId)
    }

    private suspend fun recalculateStreaks(habitId: Long) {
        val habit = habitDao.getHabitById(habitId) ?: return
        val logs = habitDao.getLogsForHabitSync(habitId)
        val completedDates = logs.map { it.dateString }.toSet()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()
        val todayStr = dateFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(calendar.time)

        var currentStreak = 0
        // Calculate current streak
        val checkCalendar = Calendar.getInstance()
        // If today is completed, streak starts today; if not, check if yesterday was completed
        var checkStr = todayStr
        if (!completedDates.contains(todayStr)) {
            checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
            checkStr = yesterdayStr
        }

        while (completedDates.contains(checkStr)) {
            currentStreak++
            checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
            checkStr = dateFormat.format(checkCalendar.time)
        }

        // Calculate best streak across all logged dates
        var bestStreak = maxOf(habit.bestStreak, currentStreak)
        val sortedDates = completedDates.sorted()
        if (sortedDates.isNotEmpty()) {
            var tempStreak = 0
            var prevCal: Calendar? = null
            for (dateStr in sortedDates) {
                val cal = Calendar.getInstance()
                val parsed = dateFormat.parse(dateStr)
                if (parsed != null) {
                    cal.time = parsed
                    if (prevCal != null) {
                        val diffDays = (cal.timeInMillis - prevCal.timeInMillis) / (1000 * 60 * 60 * 24)
                        if (diffDays == 1L) {
                            tempStreak++
                        } else {
                            tempStreak = 1
                        }
                    } else {
                        tempStreak = 1
                    }
                    if (tempStreak > bestStreak) {
                        bestStreak = tempStreak
                    }
                    prevCal = cal
                }
            }
        }

        habitDao.updateHabit(habit.copy(currentStreak = currentStreak, bestStreak = bestStreak))
    }

    suspend fun seedInitialDataIfNeeded() {
        val tasks = allTasks.first()
        val habits = allHabits.first()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val todayStr = dateFormat.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, -1)
        val twoDaysAgoStr = dateFormat.format(cal.time)

        if (tasks.isEmpty()) {
            val initialTasks = listOf(
                Task(
                    title = "Review product roadmap and sprint goals",
                    description = "Outline key feature deliverables for the upcoming release.",
                    priority = TaskPriority.HIGH,
                    category = TaskCategory.WORK,
                    dueDate = System.currentTimeMillis(),
                    dueTime = "10:30 AM",
                    isCompleted = false,
                    reminderEnabled = true
                ),
                Task(
                    title = "Complete 30-minute cardio & stretch",
                    description = "Zone 2 cardio on treadmill or outdoor jog.",
                    priority = TaskPriority.HIGH,
                    category = TaskCategory.HEALTH,
                    dueDate = System.currentTimeMillis(),
                    dueTime = "07:30 AM",
                    isCompleted = true,
                    completedAt = System.currentTimeMillis() - 3600000,
                    reminderEnabled = false
                ),
                Task(
                    title = "Read 2 chapters of Atomic Habits",
                    description = "Take notes on habit stacking principles.",
                    priority = TaskPriority.MEDIUM,
                    category = TaskCategory.STUDY,
                    dueDate = System.currentTimeMillis(),
                    dueTime = "08:30 PM",
                    isCompleted = false,
                    reminderEnabled = true
                ),
                Task(
                    title = "Buy fresh groceries & meal prep",
                    description = "Greens, chicken breast, oats, fruits for the week.",
                    priority = TaskPriority.LOW,
                    category = TaskCategory.PERSONAL,
                    dueDate = System.currentTimeMillis() + 86400000,
                    dueTime = "05:00 PM",
                    isCompleted = false,
                    reminderEnabled = false
                )
            )
            for (t in initialTasks) {
                taskDao.insertTask(t)
            }
        }

        if (habits.isEmpty()) {
            val initialHabits = listOf(
                Habit(
                    name = "Drink 2.5L Water",
                    description = "Stay hydrated throughout the workday.",
                    category = TaskCategory.HEALTH,
                    targetDaysPerWeek = 7,
                    colorHex = "#06B6D4",
                    iconKey = "water",
                    currentStreak = 4,
                    bestStreak = 7,
                    reminderTime = "09:00 AM"
                ),
                Habit(
                    name = "Morning Exercise",
                    description = "At least 20 minutes of movement or workout.",
                    category = TaskCategory.HEALTH,
                    targetDaysPerWeek = 5,
                    colorHex = "#10B981",
                    iconKey = "fitness",
                    currentStreak = 3,
                    bestStreak = 12,
                    reminderTime = "07:00 AM"
                ),
                Habit(
                    name = "Read 20 Pages",
                    description = "Non-fiction or technical literature.",
                    category = TaskCategory.STUDY,
                    targetDaysPerWeek = 7,
                    colorHex = "#8B5CF6",
                    iconKey = "book",
                    currentStreak = 5,
                    bestStreak = 14,
                    reminderTime = "09:30 PM"
                ),
                Habit(
                    name = "Evening Meditation",
                    description = "10 minutes of box breathing and mindfulness.",
                    category = TaskCategory.PERSONAL,
                    targetDaysPerWeek = 7,
                    colorHex = "#F59E0B",
                    iconKey = "meditation",
                    currentStreak = 2,
                    bestStreak = 6,
                    reminderTime = "10:00 PM"
                )
            )

            for (h in initialHabits) {
                val habitId = habitDao.insertHabit(h)
                // Seed logs for streaks
                habitDao.insertHabitLog(HabitLog(habitId = habitId, dateString = todayStr))
                habitDao.insertHabitLog(HabitLog(habitId = habitId, dateString = yesterdayStr))
                habitDao.insertHabitLog(HabitLog(habitId = habitId, dateString = twoDaysAgoStr))
            }
        }
    }
}
