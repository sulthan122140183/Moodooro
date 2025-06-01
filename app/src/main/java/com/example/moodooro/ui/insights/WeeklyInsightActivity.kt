package com.example.moodooro.ui.insights

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.example.moodooro.data.local.MoodoroDatabase
import com.example.moodooro.data.local.entity.StudySessionEntity
import com.example.moodooro.ui.theme.MoodooroTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeeklyInsightActivity : ComponentActivity() {

    private val database by lazy { MoodoroDatabase.getDatabase(this) }
    private val viewModel: WeeklyInsightViewModel by viewModels {
        WeeklyInsightViewModelFactory(database.studySessionDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoodooroTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    WeeklyInsightScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun WeeklyInsightScreen(viewModel: WeeklyInsightViewModel) {
    val stats by viewModel.weeklyStats.collectAsState()
    val sessions by viewModel.studySessions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (stats.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (stats.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${stats.error}", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Text("Statistik Mingguan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Durasi Belajar: ${stats.totalStudyDurationMinutes} menit")
            Text("Sesi Fokus: ${stats.focusedSessionsCount}")
            Text("Sesi Terdistraksi: ${stats.distractedSessionsCount}")
            Spacer(modifier = Modifier.height(16.dp))
            Text("Log Sesi Studi (7 Hari Terakhir)", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (sessions.isEmpty()) {
                Text("Tidak ada sesi studi dalam 7 hari terakhir.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(sessions, key = { it.id }) { session ->
                        StudySessionLogItem(session)
                    }
                }
            }
        }
    }
}

@Composable
fun StudySessionLogItem(session: StudySessionEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            Text("Tanggal: ${sdf.format(Date(session.startTimeMillis))}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("Durasi Fokus: ${session.focusDurationMinutes} menit")
            if (session.breakDurationMinutes > 0) {
                Text("Durasi Istirahat: ${session.breakDurationMinutes} menit")
            }
            session.subject?.let {
                Text("Subjek: $it")
            }
            session.sessionOutcome?.let {
                Text("Hasil Sesi: $it")
            }
        }
    }
}
