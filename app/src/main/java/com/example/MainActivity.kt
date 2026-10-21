package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AiInsightsScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NavTab
import com.example.ui.viewmodel.TaskPulseViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TaskPulseApp()
            }
        }
    }
}

@Composable
fun TaskPulseApp(
    viewModel: TaskPulseViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    // Handle back button: if not on TASKS tab, return to TASKS
    if (currentTab != NavTab.TASKS) {
        BackHandler {
            viewModel.selectTab(NavTab.TASKS)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp),
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        NavTab.values().forEach { tab ->
                            val isSelected = currentTab == tab
                            val icon = when (tab) {
                                NavTab.TASKS -> Icons.Default.Checklist
                                NavTab.HABITS -> Icons.Default.Whatshot
                                NavTab.AI_INSIGHTS -> Icons.Default.AutoAwesome
                                NavTab.ANALYTICS -> Icons.Default.Insights
                            }

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text(tab.title) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Crossfade(
                        targetState = currentTab,
                        label = "tab_crossfade"
                    ) { tab ->
                        when (tab) {
                            NavTab.TASKS -> DashboardScreen(viewModel = viewModel)
                            NavTab.HABITS -> HabitsScreen(viewModel = viewModel)
                            NavTab.AI_INSIGHTS -> AiInsightsScreen(viewModel = viewModel)
                            NavTab.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
