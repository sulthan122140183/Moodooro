package com.example.moodooro.ui.insights

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moodooro.data.local.MoodooroDatabase
import com.example.moodooro.data.local.entity.StudySessionEntity
import com.example.moodooro.ui.theme.MoodooroTheme
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeeklyInsightActivity : ComponentActivity() {

    private val database: MoodooroDatabase by lazy {
        runBlocking {
            MoodooroDatabase.getSuspendingDatabase(this@WeeklyInsightActivity)
        }
    }
    private val viewModel: WeeklyInsightViewModel by viewModels {
        WeeklyInsightViewModelFactory(database.studySessionDao())
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoodooroTheme {
                WeeklyInsightPage(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyInsightPage(viewModel: WeeklyInsightViewModel) {
    val activity = (LocalContext.current as? Activity)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wawasan Mingguan", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        WeeklyInsightScreen(viewModel, paddingValues)
    }
}

@Composable
fun WeeklyInsightScreen(viewModel: WeeklyInsightViewModel, paddingValues: PaddingValues) {
    val stats by viewModel.weeklyStats.collectAsState()
    val sessions by viewModel.studySessions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues) // Apply padding from Scaffold
            .padding(16.dp) // Add additional screen padding
    ) {
        if (stats.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            }
        } else if (stats.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Error: ${stats.error}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Text(
                "Statistik Mingguan",
                style = MaterialTheme.typography.headlineMedium, // Adjusted for better hierarchy
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            InfoRow(label = "Total Durasi Belajar:", value = "${stats.totalStudyDurationMinutes} menit")
            InfoRow(label = "Sesi Fokus:", value = "${stats.focusedSessionsCount}")
            InfoRow(label = "Sesi Terdistraksi:", value = "${stats.distractedSessionsCount}")

            Spacer(modifier = Modifier.height(24.dp)) // Increased spacing

            Text(
                "Log Sesi Studi (7 Hari Terakhir)",
                style = MaterialTheme.typography.headlineMedium, // Adjusted for better hierarchy
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (sessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Tidak ada sesi studi dalam 7 hari terakhir.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Add space between items
                ) {
                    items(sessions, key = { it.id }) { session ->
                        StudySessionLogItem(session)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun StudySessionLogItem(session: StudySessionEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Add elevation
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Use surfaceVariant for background
    ) {
        Column(modifier = Modifier.padding(16.dp)) { // Increased padding
            val sdf = SimpleDateFormat("EEEE, dd MMM yyyy, HH:mm", Locale.getDefault()) // More descriptive date format
            Text(
                sdf.format(Date(session.startTimeMillis)),
                style = MaterialTheme.typography.titleMedium, // Adjusted for better hierarchy
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SessionDetailText(label = "Durasi Fokus:", value = "${session.focusDurationMinutes} menit")
            if (session.breakDurationMinutes > 0) {
                SessionDetailText(label = "Durasi Istirahat:", value = "${session.breakDurationMinutes} menit")
            }
            session.subject?.let {
                SessionDetailText(label = "Subjek:", value = it)
            }
            session.sessionOutcome?.let {
                SessionDetailText(label = "Hasil Sesi:", value = it)
            }
             session.mood?.let {
                SessionDetailText(label = "Mood:", value = it)
            }
        }
    }
}

@Composable
fun SessionDetailText(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}
