package com.mindmatrix.employeetracker.ui.screens.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.mindmatrix.employeetracker.viewmodel.PerformanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onNavigateBack: () -> Unit = {},
    performanceViewModel: PerformanceViewModel = hiltViewModel()
) {
    val state by performanceViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        performanceViewModel.loadLeaderboard()
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Top Performers",
                subtitle = "Employee Leaderboard",
                onBackClick = onNavigateBack
            )
        },
        containerColor = Background
    ) { padding ->
        if (state.isLoading) {
            LoadingOverlay(isLoading = true, modifier = Modifier.padding(padding))
        } else if (state.leaderboard.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Leaderboard,
                title = "No rankings yet",
                subtitle = "Performance reviews are needed to generate rankings",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top 3 Spotlight
                if (state.leaderboard.size >= 3) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            SpotlightPodium(
                                entry = state.leaderboard[1],
                                rank = 2,
                                heightFactor = 0.8f,
                                color = Silver,
                                modifier = Modifier.weight(1f)
                            )
                            // 1st Place
                            SpotlightPodium(
                                entry = state.leaderboard[0],
                                rank = 1,
                                heightFactor = 1.0f,
                                color = Gold,
                                modifier = Modifier.weight(1.2f)
                            )
                            // 3rd Place
                            SpotlightPodium(
                                entry = state.leaderboard[2],
                                rank = 3,
                                heightFactor = 0.7f,
                                color = Bronze,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Overall Rankings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                itemsIndexed(state.leaderboard) { index, entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.Gray,
                                modifier = Modifier.width(24.dp)
                            )

                            EmployeeAvatar(name = entry.employeeName, size = 48)

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.employeeName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = entry.department,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format("%.1f", entry.averageScore),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryDark
                                )
                                LinearProgressIndicator(
                                    progress = { (entry.averageScore / 100f).toFloat() },
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(4.dp)
                                        .clip(CircleShape),
                                    color = Primary,
                                    trackColor = SurfaceVariantDark
                                )
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun SpotlightPodium(
    entry: com.mindmatrix.employeetracker.data.model.LeaderboardEntry,
    rank: Int,
    heightFactor: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            EmployeeAvatar(name = entry.employeeName, size = if (rank == 1) 72 else 56)
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = color,
                border = BorderStroke(2.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = rank.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = entry.employeeName.split(" ").first(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = String.format("%.1f", entry.averageScore),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = PrimaryDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(heightFactor * 0.5f)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.8f), color.copy(alpha = 0.2f))
                    )
                )
        )
    }
}

