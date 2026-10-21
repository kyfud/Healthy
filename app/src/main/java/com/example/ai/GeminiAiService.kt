package com.example.ai

import com.example.BuildConfig
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class DailyQuote(
    val quote: String,
    val author: String,
    val actionableTip: String
)

data class AiPrioritizationResult(
    val summary: String,
    val topTaskSuggestion: String,
    val recommendedOrder: List<String>,
    val motivationalAdvice: String
)

object GeminiAiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    private val motivationalQuotes = listOf(
        DailyQuote(
            quote = "You do not rise to the level of your goals. You fall to the level of your systems.",
            author = "James Clear",
            actionableTip = "Focus on completing your top priority task before checking email or social feeds."
        ),
        DailyQuote(
            quote = "Action is the foundational key to all success.",
            author = "Pablo Picasso",
            actionableTip = "Break down your most intimidating task into a 5-minute micro-action."
        ),
        DailyQuote(
            quote = "We are what we repeatedly do. Excellence, then, is not an act, but a habit.",
            author = "Will Durant",
            actionableTip = "Protect your habit streak today; consistency beats perfection every single time."
        ),
        DailyQuote(
            quote = "Focus is a muscle. The more you practice saying no to distractions, the stronger it becomes.",
            author = "Marcus Aurelius",
            actionableTip = "Block 45 minutes of distraction-free deep work for your highest priority item."
        ),
        DailyQuote(
            quote = "The secret of getting ahead is getting started.",
            author = "Mark Twain",
            actionableTip = "Check off just one small item on your checklist to trigger momentum."
        ),
        DailyQuote(
            quote = "Small disciplines repeated with consistency every day lead to great achievements.",
            author = "John C. Maxwell",
            actionableTip = "Celebrate your streaks; even drinking your target water keeps momentum alive."
        )
    )

    fun getRandomQuote(): DailyQuote {
        return motivationalQuotes[Random.nextInt(motivationalQuotes.size)]
    }

    suspend fun getSmartPrioritization(tasks: List<Task>, habits: List<Habit>): AiPrioritizationResult =
        withContext(Dispatchers.IO) {
            val pendingTasks = tasks.filter { !it.isCompleted }
            if (pendingTasks.isEmpty()) {
                return@withContext AiPrioritizationResult(
                    summary = "You're all caught up! No pending tasks on your checklist.",
                    topTaskSuggestion = "Take time to review your long-term goals or plan for tomorrow.",
                    recommendedOrder = emptyList(),
                    motivationalAdvice = "Great job maintaining momentum today! Keep your habit streaks burning."
                )
            }

            val apiKey = BuildConfig.GEMINI_API_KEY
            val isValidKey = apiKey.isNotEmpty() && !apiKey.startsWith("MY_")

            if (isValidKey) {
                try {
                    val prompt = buildPrioritizationPrompt(pendingTasks, habits)
                    val response = callGeminiApi(apiKey, prompt)
                    if (response != null && response.isNotBlank()) {
                        return@withContext parseAiResponse(response, pendingTasks)
                    }
                } catch (e: Exception) {
                    // Fall back to heuristic prioritization
                }
            }

            // Intelligent heuristic fallback
            return@withContext generateHeuristicPrioritization(pendingTasks, habits)
        }

    suspend fun askProductivityAssistant(
        question: String,
        pendingTasks: List<Task>,
        habits: List<Habit>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isValidKey = apiKey.isNotEmpty() && !apiKey.startsWith("MY_")

        if (isValidKey) {
            try {
                val prompt = """
                    You are TaskPulse AI, an elite productivity and habit coach.
                    User question: "$question"
                    Current context:
                    - Pending tasks: ${pendingTasks.take(5).joinToString { "${it.title} (${it.priority.displayName})" }}
                    - Habits tracked: ${habits.joinToString { "${it.name} (Streak: ${it.currentStreak}d)" }}
                    
                    Provide concise, highly actionable, inspiring advice (maximum 3 short paragraphs or bullet points).
                """.trimIndent()

                val response = callGeminiApi(apiKey, prompt)
                if (!response.isNullOrBlank()) {
                    return@withContext response
                }
            } catch (e: Exception) {
                // fall through to smart offline answer
            }
        }

        // Offline smart answer
        return@withContext generateHeuristicAssistantResponse(question, pendingTasks, habits)
    }

    private fun buildPrioritizationPrompt(tasks: List<Task>, habits: List<Habit>): String {
        val taskList = tasks.joinToString("\n") {
            "- [${it.priority.displayName}] ${it.title} (Category: ${it.category.displayName}, Due: ${it.dueTime ?: "Today"})"
        }
        val habitList = habits.joinToString("\n") {
            "- ${it.name} (Current streak: ${it.currentStreak} days)"
        }

        return """
            Analyze these pending tasks and habits. Return your analysis in this exact format:
            SUMMARY: <1 sentence overall assessment of today's workload>
            TOP_TASK: <name of the single most impactful task to do FIRST and why>
            ORDER: <comma-separated list of the task names in suggested execution order>
            COACHING: <1-2 sentences of motivational coaching based on their habits and tasks>

            Tasks:
            $taskList

            Habits:
            $habitList
        """.trimIndent()
    }

    private fun callGeminiApi(apiKey: String, prompt: String): String? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val partObj = JSONObject().put("text", prompt)
        val contentObj = JSONObject().put("parts", JSONArray().put(partObj))
        val bodyObj = JSONObject().put("contents", JSONArray().put(contentObj))

        val request = Request.Builder()
            .url(url)
            .post(bodyObj.toString().toRequestBody(JSON_MEDIA))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val responseString = response.body?.string() ?: return null
            val json = JSONObject(responseString)
            val candidates = json.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            return parts.getJSONObject(0).optString("text")
        }
    }

    private fun parseAiResponse(rawText: String, pendingTasks: List<Task>): AiPrioritizationResult {
        var summary = "Your day is optimized for high impact."
        var topTask = pendingTasks.firstOrNull()?.title ?: "Review tasks"
        var recommended = pendingTasks.map { it.title }
        var coaching = "Maintain your focus and tackle high priority goals early."

        val lines = rawText.lines()
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("SUMMARY:", ignoreCase = true) ->
                    summary = trimmed.substringAfter(":").trim()
                trimmed.startsWith("TOP_TASK:", ignoreCase = true) ->
                    topTask = trimmed.substringAfter(":").trim()
                trimmed.startsWith("ORDER:", ignoreCase = true) -> {
                    val orderStr = trimmed.substringAfter(":").trim()
                    val items = orderStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    if (items.isNotEmpty()) recommended = items
                }
                trimmed.startsWith("COACHING:", ignoreCase = true) ->
                    coaching = trimmed.substringAfter(":").trim()
            }
        }

        return AiPrioritizationResult(
            summary = summary,
            topTaskSuggestion = topTask,
            recommendedOrder = recommended,
            motivationalAdvice = coaching
        )
    }

    private fun generateHeuristicPrioritization(
        pendingTasks: List<Task>,
        habits: List<Habit>
    ): AiPrioritizationResult {
        val sortedTasks = pendingTasks.sortedWith(
            compareByDescending<Task> { it.priority.level }
                .thenBy { it.dueDate ?: Long.MAX_VALUE }
        )

        val highPriority = sortedTasks.filter { it.priority == TaskPriority.HIGH }
        val topTask = highPriority.firstOrNull() ?: sortedTasks.first()

        val summary = if (highPriority.isNotEmpty()) {
            "You have ${highPriority.size} high-impact task(s) demanding your peak morning energy."
        } else {
            "Balanced workload today. Tackle quick wins first to build unstoppable momentum."
        }

        val topTaskSuggestion = "Start with \"${topTask.title}\" (${topTask.category.displayName}) — high leverage task."
        val recommendedOrder = sortedTasks.map { it.title }

        val bestHabit = habits.maxByOrNull { it.currentStreak }
        val habitCoaching = if (bestHabit != null && bestHabit.currentStreak > 0) {
            "Keep your ${bestHabit.currentStreak}-day streak alive on \"${bestHabit.name}\"! Small daily victories fuel larger breakthroughs."
        } else {
            "Anchor your work blocks with a simple habit like hydration or stretching."
        }

        return AiPrioritizationResult(
            summary = summary,
            topTaskSuggestion = topTaskSuggestion,
            recommendedOrder = recommendedOrder,
            motivationalAdvice = habitCoaching
        )
    }

    private fun generateHeuristicAssistantResponse(
        question: String,
        pendingTasks: List<Task>,
        habits: List<Habit>
    ): String {
        val q = question.lowercase()
        return when {
            q.contains("procrastinat") || q.contains("start") -> {
                "Procrastination often happens when tasks feel too large. Apply the '2-Minute Rule': pick your task \"${pendingTasks.firstOrNull()?.title ?: "current task"}\" and commit to working on it for only 120 seconds. Once started, inertia shifts in your favor!"
            }
            q.contains("habit") || q.contains("streak") -> {
                val topHabit = habits.maxByOrNull { it.currentStreak }
                "To cement habits, use 'Habit Stacking': Anchor your new habit to an existing routine (e.g., 'Right after my morning coffee, I will do ${topHabit?.name ?: "my habit"}'). Never miss twice in a row!"
            }
            q.contains("focus") || q.contains("deep work") || q.contains("time") -> {
                "Use the 50/10 Pomodoro Rhythm: 50 minutes of deep focus with zero notifications, followed by 10 minutes of movement or hydration. Today, dedicate your first 50-minute block to your high priority task."
            }
            else -> {
                "Focus on the 'Rule of 3': Identify just 3 essential outcomes for today. Knock out \"${pendingTasks.firstOrNull()?.title ?: "your main task"}\" first, then celebrate with a 5-minute break."
            }
        }
    }
}
