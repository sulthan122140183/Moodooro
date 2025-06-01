package com.example.moodooro.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // Import by viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Removed direct ViewModelProvider import as we'll use by viewModels
import com.example.moodooro.ui.theme.MoodooroTheme
import com.example.moodooro.ui.timer.TimerActivity
import com.example.moodooro.ui.insights.WeeklyInsightActivity
import com.example.moodooro.data.local.entity.StudySessionEntity // Keep this if RecentStudySessionItem uses it directly
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val TIMER_DURATION_MINUTES = "TIMER_DURATION_MINUTES"

private val recentSessionDateFormatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
private val moodInsightDateFormatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

class MainActivity : ComponentActivity() {

    // Use by viewModels with the custom factory
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MoodooroTheme {
                // Observe the loading state for a better UX
                val isLoading by mainViewModel.isDataLoading.collectAsState()
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    StudyAppScreen(mainViewModel = mainViewModel)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyAppScreen(mainViewModel: MainViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Moodooro") })
        },
        bottomBar = {}
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) 
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { UserProfileSection() }
            item { WeeklyStudyInsightsSection(mainViewModel = mainViewModel) }
            item { RecentStudySessionsSection(mainViewModel = mainViewModel) }
            item { MoodTrackingInsightsSection(mainViewModel = mainViewModel) }
        }
    }
}

@Composable
fun UserProfileSection() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth() 
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally 
    ) {
        Spacer(modifier = Modifier.height(16.dp)) 
        CategoryChip(
            text = "Start Study Session",
            iconRes = android.R.drawable.ic_menu_agenda, // Consider using a custom icon
            onClick = {
                Log.d("MainActivity", "Start Study Session chip clicked, attempting to start TimerActivity for DEMO (5 seconds).")
                val intent = Intent(context, TimerActivity::class.java).apply {
                    putExtra(TIMER_DURATION_MINUTES, 0) 
                }
                context.startActivity(intent)
            }
        )
    }
}

@Composable
fun CategoryChip(text: String, iconRes: Int, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .padding(vertical = 8.dp) 
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, fontSize = 12.sp)
        }
    }
}

@Composable
fun WeeklyStudyInsightsSection(mainViewModel: MainViewModel) { 
    val context = LocalContext.current
    val averageMinutes by mainViewModel.averageDailyStudyTimeMinutes.collectAsState()

    val hours = averageMinutes / 60
    val minutes = averageMinutes % 60

    val averageStudyTimeText = when {
        hours > 0 && minutes > 0 -> "$hours hours $minutes min"
        hours > 0 -> "$hours hours"
        minutes > 0 -> "$minutes min"
        else -> "No study time"
    }

    Column(modifier = Modifier.padding(vertical = 8.dp)) { 
        Text("Weekly Study Insights", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Average Study Time", fontSize = 14.sp, color = Color.Gray)
                Text(averageStudyTimeText, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        Log.d("ViewDetails", "View Details clicked, launching WeeklyInsightActivity.")
                        val intent = Intent(context, WeeklyInsightActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Details")
                }
            }
        }
    }
}

@Composable
fun RecentStudySessionsSection(mainViewModel: MainViewModel) {
    val sessions by mainViewModel.recentStudySessions.collectAsState()

    Column(modifier = Modifier.padding(vertical = 8.dp)) { 
        Text("Recent Study Sessions", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        if (sessions.isEmpty()) {
            Text("No recent study sessions.", fontSize = 14.sp, color = Color.Gray)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) { 
                sessions.forEachIndexed { index, session ->
                    RecentStudySessionItem(session = session)
                    if (index < sessions.size - 1) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun RecentStudySessionItem(session: StudySessionEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Alarm,
            contentDescription = "Study Session",
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            val titleText = if (!session.subject.isNullOrBlank()) {
                session.subject
            } else {
                try {
                    recentSessionDateFormatter.format(Date(session.timestamp))
                } catch (e: Exception) {
                    "Session" 
                }
            }
            Text(text = titleText ?: "Study Session", fontWeight = FontWeight.SemiBold)

            val durationInMinutes = session.focusDurationMinutes
            val hours = durationInMinutes / 60
            val minutes = durationInMinutes % 60
            val durationText = when {
                hours > 0 && minutes > 0 -> "$hours hr $minutes min"
                hours > 0 -> "$hours hr"
                else -> "$minutes min"
            }
            Text(durationText, fontSize = 12.sp, color = Color.Gray)
        }
        Text(
            text = session.sessionOutcome ?: session.focusStatus ?: "N/A",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = if (session.sessionOutcome?.equals("Focused", ignoreCase = true) == true ||
                       session.focusStatus?.equals("Focused", ignoreCase = true) == true) {
                Color(0xFF4CAF50) 
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            }
        )
    }
}

@Composable
fun MoodTrackingInsightsSection(mainViewModel: MainViewModel) {
    val moodInsights: List<DailyMoodData> by mainViewModel.moodInsights.collectAsState()

    Column(modifier = Modifier.padding(vertical = 8.dp)) { 
        Text(
            text = "Wawasan Pelacakan Suasana Hati (7 Hari Terakhir)",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (moodInsights.isEmpty()) {
            Text(
                text = "Belum ada data suasana hati untuk ditampilkan selama 7 hari terakhir.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        } else {
            Column {
                moodInsights.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = moodInsightDateFormatter.format(Date(item.date)),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(maxOf(0f, item.averageMoodScore * 100).dp) 
                                .background(
                                    when {
                                        item.averageMoodScore > 0.7f -> Color(0xFF4CAF50)
                                        item.averageMoodScore > 0.35f -> Color(0xFFFFC107)
                                        else -> Color(0xFFF44336)
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f", item.averageMoodScore),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(30.dp) 
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.moodLabel,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (index < moodInsights.size - 1) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 1200)
@Composable
fun DefaultPreview() {
    MoodooroTheme {
        Text("Preview needs a ViewModel instance or a modified Composable for previewing sections individually.")
    }
}
