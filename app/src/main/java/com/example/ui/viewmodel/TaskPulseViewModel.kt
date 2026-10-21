package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.TaskPulseApplication
import com.example.ai.DailyQuote
import com.example.ai.GeminiAiService
import com.example.ai.AiPrioritizationResult
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.Task
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.data.repository.TaskPulseRepository
import com.example.reminder.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class NavTab(val title: String) {
    TASKS("Tasks"),
    HABITS("Habits"),
    AI_INSIGHTS("AI Coach"),
    ANALYTICS("Analytics")
}

enum class TaskStatusFilter {
    ALL, PENDING, COMPLETED
}

data class AiMessage(
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class TaskPulseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskPulseRepository =
        (application as TaskPulseApplication).repository

    val allTasks: StateFlow<List<Task>> = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allHabits: StateFlow<List<Habit>> = repository.allHabits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allHabitLogs: StateFlow<List<HabitLog>> = repository.allHabitLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Navigation state
    private val _currentTab = MutableStateFlow(NavTab.TASKS)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    fun selectTab(tab: NavTab) {
        _currentTab.value = tab
    }

    // Filter states
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<TaskCategory?>(null)
    val selectedPriorityFilter = MutableStateFlow<TaskPriority?>(null)
    val taskStatusFilter = MutableStateFlow(TaskStatusFilter.ALL)

    // Filtered tasks flow
    val filteredTasks: StateFlow<List<Task>> = combine(
        allTasks,
        searchQuery,
        selectedCategoryFilter,
        selectedPriorityFilter,
        taskStatusFilter
    ) { tasks, query, catFilter, priorityFilter, statusFilter ->
        tasks.filter { task ->
            val matchesQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)
            val matchesCategory = catFilter == null || task.category == catFilter
            val matchesPriority = priorityFilter == null || task.priority == priorityFilter
            val matchesStatus = when (statusFilter) {
                TaskStatusFilter.ALL -> true
                TaskStatusFilter.PENDING -> !task.isCompleted
                TaskStatusFilter.COMPLETED -> task.isCompleted
            }
            matchesQuery && matchesCategory && matchesPriority && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Progress metrics
    val taskProgress: StateFlow<Pair<Int, Int>> = allTasks.combine(MutableStateFlow(Unit)) { tasks, _ ->
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        Pair(completed, total)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0, 0))

    // Calendar state for habit tracker
    private val _calendarMonth = MutableStateFlow(Calendar.getInstance())
    val calendarMonth: StateFlow<Calendar> = _calendarMonth.asStateFlow()

    private val _selectedCalendarDate = MutableStateFlow(todayDateString())
    val selectedCalendarDate: StateFlow<String> = _selectedCalendarDate.asStateFlow()

    // Dialog & bottom sheet states
    val isTaskDialogOpen = MutableStateFlow(false)
    val editingTask = MutableStateFlow<Task?>(null)

    val isHabitDialogOpen = MutableStateFlow(false)
    val editingHabit = MutableStateFlow<Habit?>(null)

    // AI Section states
    private val _dailyQuote = MutableStateFlow(GeminiAiService.getRandomQuote())
    val dailyQuote: StateFlow<DailyQuote> = _dailyQuote.asStateFlow()

    private val _aiPrioritization = MutableStateFlow<AiPrioritizationResult?>(null)
    val aiPrioritization: StateFlow<AiPrioritizationResult?> = _aiPrioritization.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _chatMessages = MutableStateFlow(
        listOf(
            AiMessage(
                sender = "ai",
                text = "Hello! I am your TaskPulse AI coach. Ask me for prioritization strategies, focus rhythms, or advice on overcoming procrastination."
            )
        )
    )
    val chatMessages: StateFlow<List<AiMessage>> = _chatMessages.asStateFlow()

    init {
        loadAiPrioritization()
    }

    fun todayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    // Task Actions
    fun openCreateTaskDialog() {
        editingTask.value = null
        isTaskDialogOpen.value = true
    }

    fun openEditTaskDialog(task: Task) {
        editingTask.value = task
        isTaskDialogOpen.value = true
    }

    fun dismissTaskDialog() {
        isTaskDialogOpen.value = false
        editingTask.value = null
    }

    fun saveTask(
        title: String,
        description: String,
        priority: TaskPriority,
        category: TaskCategory,
        dueTime: String?,
        reminderEnabled: Boolean
    ) {
        viewModelScope.launch {
            val current = editingTask.value
            if (current != null) {
                val updated = current.copy(
                    title = title,
                    description = description,
                    priority = priority,
                    category = category,
                    dueTime = dueTime,
                    reminderEnabled = reminderEnabled
                )
                repository.updateTask(updated)
                if (reminderEnabled) {
                    scheduleTaskReminder(updated)
                } else {
                    NotificationHelper.cancelReminder(getApplication(), updated.id.toInt())
                }
            } else {
                val newTask = Task(
                    title = title,
                    description = description,
                    priority = priority,
                    category = category,
                    dueDate = System.currentTimeMillis(),
                    dueTime = dueTime,
                    reminderEnabled = reminderEnabled
                )
                val id = repository.insertTask(newTask)
                if (reminderEnabled) {
                    scheduleTaskReminder(newTask.copy(id = id))
                }
            }
            dismissTaskDialog()
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            NotificationHelper.cancelReminder(getApplication(), task.id.toInt())
            repository.deleteTask(task)
        }
    }

    private fun scheduleTaskReminder(task: Task) {
        // Schedule notification for the task
        val context = getApplication<Application>()
        // Default trigger 1 hour from now or based on priority
        val triggerTime = System.currentTimeMillis() + when (task.priority) {
            TaskPriority.HIGH -> 15 * 60 * 1000L // 15 mins
            TaskPriority.MEDIUM -> 60 * 60 * 1000L // 1 hour
            TaskPriority.LOW -> 120 * 60 * 1000L // 2 hours
        }

        NotificationHelper.scheduleReminder(
            context = context,
            title = "Reminder: ${task.title}",
            message = "Priority: ${task.priority.displayName} • ${task.category.displayName}",
            triggerAtMillis = triggerTime,
            notificationId = task.id.toInt(),
            channelId = NotificationHelper.CHANNEL_TASKS_ID
        )
    }

    // Habit Actions
    fun openCreateHabitDialog() {
        editingHabit.value = null
        isHabitDialogOpen.value = true
    }

    fun openEditHabitDialog(habit: Habit) {
        editingHabit.value = habit
        isHabitDialogOpen.value = true
    }

    fun dismissHabitDialog() {
        isHabitDialogOpen.value = false
        editingHabit.value = null
    }

    fun saveHabit(
        name: String,
        description: String,
        category: TaskCategory,
        targetDays: Int,
        colorHex: String,
        iconKey: String,
        reminderTime: String?
    ) {
        viewModelScope.launch {
            val current = editingHabit.value
            if (current != null) {
                repository.updateHabit(
                    current.copy(
                        name = name,
                        description = description,
                        category = category,
                        targetDaysPerWeek = targetDays,
                        colorHex = colorHex,
                        iconKey = iconKey,
                        reminderTime = reminderTime
                    )
                )
            } else {
                repository.insertHabit(
                    Habit(
                        name = name,
                        description = description,
                        category = category,
                        targetDaysPerWeek = targetDays,
                        colorHex = colorHex,
                        iconKey = iconKey,
                        reminderTime = reminderTime
                    )
                )
            }
            dismissHabitDialog()
        }
    }

    fun toggleHabitCompletion(habitId: Long, dateString: String = todayDateString()) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, dateString)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // Calendar navigation
    fun changeCalendarMonth(monthDelta: Int) {
        val newCal = Calendar.getInstance().apply {
            time = _calendarMonth.value.time
            add(Calendar.MONTH, monthDelta)
        }
        _calendarMonth.value = newCal
    }

    fun selectCalendarDate(dateString: String) {
        _selectedCalendarDate.value = dateString
    }

    // AI & Quotes
    fun refreshDailyQuote() {
        _dailyQuote.value = GeminiAiService.getRandomQuote()
    }

    fun loadAiPrioritization() {
        viewModelScope.launch {
            _isAiLoading.value = true
            val tasks = allTasks.value
            val habits = allHabits.value
            _aiPrioritization.value = GeminiAiService.getSmartPrioritization(tasks, habits)
            _isAiLoading.value = false
        }
    }

    fun sendAiQuestion(question: String) {
        if (question.isBlank()) return
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(AiMessage(sender = "user", text = question))
        _chatMessages.value = currentList

        viewModelScope.launch {
            _isAiLoading.value = true
            val tasks = allTasks.value
            val habits = allHabits.value
            val answer = GeminiAiService.askProductivityAssistant(question, tasks, habits)
            val updated = _chatMessages.value.toMutableList()
            updated.add(AiMessage(sender = "ai", text = answer))
            _chatMessages.value = updated
            _isAiLoading.value = false
        }
    }

    fun triggerTestNotification() {
        val context = getApplication<Application>()
        NotificationHelper.showImmediateNotification(
            context = context,
            title = "TaskPulse: Focus Sprint!",
            message = "High-priority task reminder: Time to knock out your top goal today! 🔥",
            notificationId = (1000..9999).random(),
            channelId = NotificationHelper.CHANNEL_TASKS_ID
        )
    }
}
