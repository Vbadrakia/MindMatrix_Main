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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val performanceState by performanceViewModel.state.collectAsStateWithLifecycle()
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
                title = stringResource(R.string.performance),
                subtitle = stringResource(R.string.review_history),
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
                                text = stringResource(R.string.overall_average),
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
                                    text = stringResource(R.string.total_reviews_count, performanceState.reviews.size),
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
                    text = stringResource(R.string.review_history),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            if (performanceState.reviews.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Assessment,
                        title = stringResource(R.string.no_reviews_yet),
                        subtitle = stringResource(R.string.review_history_desc)
                    )
                }
            } else {
                items(performanceState.reviews.sortedByDescending { it.reviewDate }) { review ->
                    PerformanceReviewCard(
                        review = review,
                        showApproveButton = false
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
@Composable
fun PerformanceReviewCard(
    review: com.mindmatrix.employeetracker.data.model.PerformanceReview,
    showApproveButton: Boolean = false,
    onApprove: () -> Unit = {}
) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = review.period.ifBlank { stringResource(R.string.annual_review_default) },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Status Badge
                        val isApproved = review.status == com.mindmatrix.employeetracker.data.model.ReviewStatus.APPROVED
                        Surface(
                            color = if (isApproved) Success.copy(alpha = 0.1f) else PriorityMedium.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (isApproved) stringResource(R.string.approved) else stringResource(R.string.submitted),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isApproved) Success else PriorityMedium
                            )
                        }
                    }
                    Text(
                        text = review.reviewDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = when {
                            review.weightedScore >= 80 -> Success.copy(alpha = 0.1f)
                            review.weightedScore >= 60 -> Primary.copy(alpha = 0.1f)
                            else -> PriorityMedium.copy(alpha = 0.1f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${review.weightedScore.toInt()}/100",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                review.weightedScore >= 80 -> Success
                                review.weightedScore >= 60 -> Primary
                                else -> PriorityMedium
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            // Score breakdown with progress bars
            val breakdownItems = listOf(
                Triple(stringResource(R.string.productivity), review.productivityScore.toFloat(), Success),
                Triple(stringResource(R.string.quality), review.qualityScore.toFloat(), Primary),
                Triple(stringResource(R.string.attendance), review.attendanceScore.toFloat(), Tertiary),
                Triple(stringResource(R.string.teamwork), review.teamworkScore.toFloat(), SecondaryDark),
                Triple(stringResource(R.string.soft_skills), review.softSkillsScore.toFloat(), PriorityMedium)
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
                            text = "${score.toInt()}/100",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { score / 100f },
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

            if (showApproveButton && review.status == com.mindmatrix.employeetracker.data.model.ReviewStatus.SUBMITTED) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onApprove,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.approve_review))
                }
            }
        }
    }
}
