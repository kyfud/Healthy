package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Task
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TaskPulse", appName)
  }

  @Test
  fun `task model properties test`() {
    val task = Task(
      title = "Focus sprint",
      priority = TaskPriority.HIGH,
      category = TaskCategory.WORK
    )
    assertEquals("Focus sprint", task.title)
    assertEquals(TaskPriority.HIGH, task.priority)
    assertEquals(TaskCategory.WORK, task.category)
    assertFalse(task.isCompleted)
  }
}
