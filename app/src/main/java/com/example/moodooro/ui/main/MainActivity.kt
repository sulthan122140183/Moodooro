package com.example.moodooro.ui.main

import android.content.Intent // ADD THIS IMPORT
import android.os.Bundle
import android.util.Log // ADD THIS IMPORT
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Image // Using 'Image' as a placeholder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext // ADD THIS IMPORT
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodooro.ui.theme.MoodooroTheme
import com.example.moodooro.ui.timer.TimerActivity
import com.example.moodooro.ui.insights.WeeklyInsightActivity // CORRECTED IMPORT
const val TIMER_DURATION_MINUTES = "TIMER_DURATION_MINUTES" // ADD THIS CONSTANT

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoodooroTheme {
                StudyAppScreen()
            }
        }
    }
}

data class StudySession(val id: Int, val title: String, val duration: String, val status: String, val iconRes: Int)
data class InspirationItem(val id: Int, val imageUrl: String, val title: String, val category: String, val author: String, val isLiked: Boolean)
data class UserFeedbackItem(val id: Int, val userName: String, val feedbackText: String, val rating: Float)

@Composable
fun StudyAppScreen() {
    Scaffold(
        topBar = {},
        bottomBar = {}
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { UserProfileSection() }
            item { RecommendedStudyDurationSection() }
            item { WeeklyStudyInsightsSection() }
            item { RecentStudySessionsSection() }
            item { CommunityInspirationSection() }
            item { UserFeedbackSection() }
            item { QuickActionsSection() }
            item { MoodTrackingInsightsSection() }
        }
    }
}

@Composable
fun UserProfileSection() {
    val context = LocalContext.current // ADD THIS LINE to get context

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(), // Allow centering the icon if no text
            horizontalArrangement = Arrangement.Center // Center the icon
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "User Profile",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                tint = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryChip(
                text = "Study",
                iconRes = android.R.drawable.ic_menu_agenda,
                onClick = {
                    Log.d("MainActivity", "Study chip clicked, attempting to start TimerActivity for DEMO (5 seconds).")
                    val intent = Intent(context, TimerActivity::class.java).apply {
                        putExtra(TIMER_DURATION_MINUTES, 0) // MODIFIED FOR DEMO: 0 indicates 5 seconds in TimerActivity
                    }
                    context.startActivity(intent)
                }
            )
            CategoryChip(
                text = "Breaks",
                iconRes = android.R.drawable.ic_menu_recent_history,
                onClick = {
                    Log.d("MainActivity", "Breaks chip clicked, attempting to start TimerActivity.")
                    val intent = Intent(context, TimerActivity::class.java).apply {
                        putExtra(TIMER_DURATION_MINUTES, 5)
                    }
                    context.startActivity(intent)
                }
            )
            CategoryChip(
                text = "Mood", // Added Mood Chip
                iconRes = android.R.drawable.ic_menu_compass, // Example icon
                onClick = {
                    Log.d("MainActivity", "Mood chip clicked")
                    // TODO: Implement navigation to MoodReviewActivity or similar
                    // val intent = Intent(context, MoodReviewActivity::class.java)
                    // context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun CategoryChip(text: String, iconRes: Int, onClick: () -> Unit) { // MODIFIED: Added onClick parameter
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick) // MODIFIED: Made Card clickable
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
fun RecommendedStudyDurationSection() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Recommended Study Duration", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StudyDurationCard(
                "Focus Mode", "25 minutes", Icons.Filled.Alarm, "Clock",
                modifier = Modifier.weight(1f)
            )
            StudyDurationCard(
                "Break Mode", "5 minutes", Icons.Filled.Image, "Relaxation", // Using Icons.Filled.Image as a placeholder
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StudyDurationCard(
    title: String,
    duration: String,
    imageVector: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.height(180.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Recommended", fontSize = 10.sp, color = Color.Gray)
            Image(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(duration, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun WeeklyStudyInsightsSection() {
    val context = LocalContext.current // ADDED to get context
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Weekly Study Insights", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Average Study Time", fontSize = 14.sp, color = Color.Gray)
                Text("3 hours", fontWeight = FontWeight.Bold, fontSize = 28.sp)
                Text("+10%", fontSize = 14.sp, color = Color(0xFF4CAF50))
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        Log.d("ViewDetails", "View Details clicked, launching WeeklyInsightActivity.")
                        val intent = Intent(context, WeeklyInsightActivity::class.java) // CORRECTED CLASS
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
fun RecentStudySessionsSection() {
    val sessions = listOf(
        StudySession(1, "Today", "2 Hours", "Focused", android.R.drawable.ic_menu_today),
        StudySession(2, "Yesterday", "1 Hour", "Distracted", android.R.drawable.ic_menu_week)
    )
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Recent Study Sessions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        sessions.forEach { session ->
            RecentStudySessionItem(session)
            HorizontalDivider()
        }
    }
}

@Composable
fun RecentStudySessionItem(session: StudySession) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = session.iconRes),
            contentDescription = session.title,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(session.title, fontWeight = FontWeight.SemiBold)
            Text(session.duration, fontSize = 12.sp, color = Color.Gray)
        }
        Text(session.status, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun CommunityInspirationSection() {
    val inspirations = remember {
        listOf(
            InspirationItem(1, "motivational_quote_image_url", "Stay focused and never give up! 💪", "Inspiration", "StudyPro123", true),
            InspirationItem(2, "study_setup_image_url", "Create a study environment that sparks joy! ✨", "Study Tips", "LearnSmart", false)
        ).toMutableStateList()
    }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Community Inspiration", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(inspirations, key = { it.id }) { inspiration ->
                InspirationCard(
                    inspiration = inspiration,
                    onLikeClicked = {
                        val index = inspirations.indexOfFirst { item -> item.id == inspiration.id }
                        if (index != -1) {
                            inspirations[index] = inspiration.copy(isLiked = !inspiration.isLiked)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun InspirationCard(inspiration: InspirationItem, onLikeClicked: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.width(200.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Image", color = Color.White)
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(inspiration.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${inspiration.category} • ${inspiration.author}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Like",
                        tint = if (inspiration.isLiked) Color.Red else Color.Gray,
                        modifier = Modifier.clickable(onClick = onLikeClicked)
                    )
                }
            }
        }
    }
}

@Composable
fun UserFeedbackSection() {
    val feedbackItems = listOf(
        UserFeedbackItem(1, "HappyStudier", "Love the simplicity and effectiveness of Moodooro!", 5.0f),
        UserFeedbackItem(2, "LearningIsFun", "Finally found a study app that cares about my mood.", 4.5f)
    )
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("User Feedback", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(feedbackItems) { feedback ->
                UserFeedbackCard(feedback)
            }
        }
    }
}

@Composable
fun UserFeedbackCard(feedback: UserFeedbackItem) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(250.dp)
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "User",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(feedback.userName, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(feedback.feedbackText, fontSize = 14.sp, maxLines = 3)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                repeat(feedback.rating.toInt()) {
                    Icon(
                        imageVector = Icons.Filled.Favorite, // Consider changing to Icons.Filled.Star
                        contentDescription = "Star",
                        tint = Color.Yellow
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection() {
    val context = LocalContext.current // ADDED context for potential clicks
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionItem(
                text = "Take Notes",
                iconRes = android.R.drawable.ic_menu_edit,
                modifier = Modifier.weight(1f),
                onClick = { Log.d("QuickAction", "Take Notes clicked") } // MODIFIED: Added onClick
            )
            QuickActionItem(
                text = "Listen to Focus...",
                iconRes = android.R.drawable.ic_media_play,
                modifier = Modifier.weight(1f),
                onClick = { Log.d("QuickAction", "Listen to Focus clicked") } // MODIFIED: Added onClick
            )
        }
    }
}

@Composable
fun QuickActionItem(text: String, iconRes: Int, modifier: Modifier = Modifier, onClick: () -> Unit) { // MODIFIED: Added onClick
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.clickable(onClick = onClick) // MODIFIED: Made Card clickable
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontSize = 14.sp)
        }
    }
}

@Composable
fun MoodTrackingInsightsSection() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("Mood Tracking Insights", fontSize = 16.sp, color = Color.Gray)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.LightGray, shape = CircleShape)
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 1200)
@Composable
fun DefaultPreview() {
    MoodooroTheme {
        StudyAppScreen()
    }
}
