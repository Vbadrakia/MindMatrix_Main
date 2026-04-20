package com.mindmatrix.employeetracker.ui.screens.performance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindmatrix.employeetracker.ui.components.*
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel
import com.mindmatrix.employeetracker.viewmodel.PerformanceViewModel

@Composable
fun PerformanceHistoryScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    performanceViewModel: PerformanceViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val performanceState by performanceViewModel.state.collectAsState()
    val currentEmployee = authState.currentEmployee

    LaunchedEffect(currentEmployee) {
        currentEmployee?.let {
            performanceViewModel.loadReviewsForEmployee(it.id)
            performanceViewModel.loadAverageScore(it.id)
        }
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Performance",
                subtitle = "Review History",
                onNotificationClick = { /* Handle notifications */ }
            )
        },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Score overview card with gradient
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Primary, PrimaryDark)
                                )
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Overall Average",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format("%.1f", performanceState.averageScore),
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "/100",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${performanceState.reviews.size} total reviews",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Section Title
            item {
                Text(
                    text = "Review History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            if (performanceState.reviews.isEmpty()) {
                item {
                    EmptyStateCard(message = "No performance reviews found yet")
                }
            } else {
                items(performanceState.reviews.sortedByDescending { it.reviewDate }) { review ->
                    PerformanceReviewCard(review = review)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun PerformanceReviewCard(review: com.mindmatrix.employeetracker.data.model.PerformanceReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = review.period.ifBlank { "Annual Review" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Text(
                        text = review.reviewDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Surface(
                    color = PrimaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${review.overallScore.toInt()}%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            // Score breakdown with progress bars
            val breakdownItems = listOf(
                Triple("Productivity", review.productivityScore, Success),
                Triple("Quality", review.qualityScore, Primary),
                Triple("Attendance", review.attendanceScore, Tertiary),
                Triple("Teamwork", review.teamworkScore, SecondaryDark),
                Triple("Soft Skills", review.softSkillsScore, PriorityMedium)
            )

            breakdownItems.forEach { (label, score, color) ->
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { score / 5f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = color,
                        trackColor = Outline.copy(alpha = 0.3f)
                    )
                }
            }

            if (review.comments.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Outline.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.FormatQuote,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp).padding(top = 2.dp),
                        tint = OnSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = review.comments,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
