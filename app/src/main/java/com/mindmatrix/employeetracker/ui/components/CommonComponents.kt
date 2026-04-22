package com.mindmatrix.employeetracker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindmatrix.employeetracker.data.model.AttendanceStatus
import com.mindmatrix.employeetracker.data.model.Employee
import com.mindmatrix.employeetracker.data.model.Task
import com.mindmatrix.employeetracker.data.model.TaskPriority
import com.mindmatrix.employeetracker.data.model.TaskStatus
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.Insight
import com.mindmatrix.employeetracker.viewmodel.InsightPriority

@Composable
fun BadgeChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

fun getDeadlineColor(dueDateStr: String): Color {
    if (dueDateStr.isBlank()) return OnSurfaceVariant
    try {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.getDefault())
        val dueDate = java.time.LocalDate.parse(dueDateStr, formatter)
        val today = java.time.LocalDate.now()
        val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate)
        
        return when {
            daysBetween < 0 -> Error // Overdue
            daysBetween <= 3 -> Tertiary // Approaching
            else -> OnSurfaceVariant
        }
    } catch (e: Exception) {
        return OnSurfaceVariant
    }
}

@Composable
fun ShimmerItem(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(brush, shape = RoundedCornerShape(24.dp))
    )
}

@Composable
fun DashboardTopBar(
    title: String = stringResource(R.string.performance),
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    onNotificationClick: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Primary
                    )
                }
        } else {
            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f))
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Primary)
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (actions != null) {
                actions()
            }
            if (onNotificationClick != null) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.view_notifications), tint = Primary)
                }
            }
        }
    }
}

import coil.compose.SubcomposeAsyncImage

@Composable
fun EmployeeAvatar(
    name: String,
    photoUrl: String? = null,
    modifier: Modifier = Modifier,
    size: Int = 40,
    onClick: (() -> Unit)? = null
) {
    val initials = name.split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .map { it[0].uppercase() }
        .joinToString("")

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Primary, PrimaryDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrEmpty()) {
            SubcomposeAsyncImage(
                model = photoUrl,
                contentDescription = stringResource(R.string.avatar_icon_desc, name),
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(size.dp / 2), strokeWidth = 2.dp, color = Color.White)
                    }
                },
                error = {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size / 2.5).sp
                    )
                }
            )
        } else {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size / 2.5).sp
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "scale"
    )

    Card(
        modifier = modifier
            .height(120.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.icon_desc_format, title),
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = value,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = title,
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun EmptyStateCard(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Inbox
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = OnSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TaskSmallCard(task: Task, onClick: () -> Unit = {}) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 4.dp,
        label = "elevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = getLocalizedTaskPriority(task.priority),
                    style = MaterialTheme.typography.bodySmall,
                    color = getTaskPriorityColor(task.priority),
                    fontWeight = FontWeight.ExtraBold
                )
            }
            
            StatusChip(
                text = getLocalizedTaskStatus(task.status),
                color = getTaskStatusColor(task.status)
            )
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = Primary.copy(alpha = 0.05f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Primary.copy(alpha = 0.3f)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryDark,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(24.dp))
            action()
        }
    }
}

@Composable
fun LoadingOverlay(isLoading: Boolean, modifier: Modifier = Modifier) {
    if (isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Primary)
        }
    }
}

fun getAttendanceStatusColor(status: AttendanceStatus): Color {
    return when (status) {
        AttendanceStatus.PRESENT -> StatusPresent
        AttendanceStatus.ABSENT -> StatusAbsent
        AttendanceStatus.LATE -> StatusLate
        AttendanceStatus.LEAVE -> StatusOnLeave
        AttendanceStatus.HALF_DAY -> Tertiary
    }
}

fun getTaskStatusColor(status: TaskStatus): Color {
    return when (status) {
        TaskStatus.COMPLETED -> Success
        TaskStatus.REVIEWED -> Accent
        TaskStatus.IN_PROGRESS -> Tertiary
        TaskStatus.PENDING -> Primary
        TaskStatus.OVERDUE -> Error
        TaskStatus.CANCELLED -> Outline
    }
}

fun getTaskPriorityColor(priority: TaskPriority): Color {
    return when (priority) {
        TaskPriority.LOW -> PriorityLow
        TaskPriority.MEDIUM -> PriorityMedium
        TaskPriority.HIGH -> PriorityHigh
        TaskPriority.CRITICAL -> PriorityCritical
    }
}

@Composable
fun getLocalizedAttendanceStatus(status: AttendanceStatus): String {
    return when (status) {
        AttendanceStatus.PRESENT -> stringResource(R.string.attendance_status_present)
        AttendanceStatus.ABSENT -> stringResource(R.string.attendance_status_absent)
        AttendanceStatus.LATE -> stringResource(R.string.attendance_status_late)
        AttendanceStatus.LEAVE -> stringResource(R.string.attendance_status_on_leave)
        AttendanceStatus.HALF_DAY -> stringResource(R.string.attendance_status_half_day)
    }
}

@Composable
fun getLocalizedTaskStatus(status: TaskStatus): String {
    return when (status) {
        TaskStatus.PENDING -> stringResource(R.string.task_status_pending)
        TaskStatus.IN_PROGRESS -> stringResource(R.string.task_status_in_progress)
        TaskStatus.COMPLETED -> stringResource(R.string.task_status_completed)
        TaskStatus.REVIEWED -> stringResource(R.string.task_status_reviewed)
        TaskStatus.OVERDUE -> stringResource(R.string.task_status_overdue)
        TaskStatus.CANCELLED -> stringResource(R.string.task_status_cancelled)
    }
}

@Composable
fun getLocalizedTaskPriority(priority: TaskPriority): String {
    return when (priority) {
        TaskPriority.LOW -> stringResource(R.string.task_priority_low)
        TaskPriority.MEDIUM -> stringResource(R.string.task_priority_medium)
        TaskPriority.HIGH -> stringResource(R.string.task_priority_high)
        TaskPriority.CRITICAL -> stringResource(R.string.task_priority_critical)
    }
}

@Composable
fun AdminStatCard(
    label: String,
    value: String,
    trend: String? = null,
    trendColor: Color = StatusPresent,
    subtext: String? = null,
    isRating: Boolean = false,
    rating: Int = 0,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = PrimaryDark
                )
                if (trend != null) {
                    Surface(
                        color = trendColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = stringResource(R.string.trend_up),
                                modifier = Modifier.size(12.dp),
                                tint = trendColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = trend, color = trendColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (subtext != null) {
                    Surface(
                        color = SurfaceVariantDark,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = subtext,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }
                if (isRating) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(5) { index ->
                            val starIcon = when {
                                index < rating -> Icons.Default.Star
                                index < rating + 1 && (rating % 1 != 0) -> Icons.AutoMirrored.Filled.StarHalf // Simplified check
                                else -> Icons.Default.StarOutline
                            }
                            Icon(
                                imageVector = starIcon,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopPerformerCard(
    name: String,
    designation: String,
    score: Double,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "scale"
    )

    Card(
        modifier = Modifier
            .width(160.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EmployeeAvatar(name = name, size = 64)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = PrimaryDark
            )
            Text(
                text = designation,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SuccessContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                val formattedScore = if (score % 1.0 == 0.0) score.toInt().toString() else String.format("%.1f", score)
                Text(
                    text = stringResource(R.string.score_label, formattedScore),
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Success,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PerformanceTrendCard(
    reviews: List<com.mindmatrix.employeetracker.data.model.PerformanceReview> = emptyList(),
    onViewDetailedTrend: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    
    // Process reviews into chart data (last 6 months or reviews)
    val sortedReviews = reviews.sortedBy { it.reviewDate }.takeLast(6)
    val data = sortedReviews.map { review ->
        review.period.take(3) to (review.weightedScore.toFloat() / 100f) // Scale to 0-1
    }.ifEmpty {
        listOf(
            "Jan" to 0.72f, "Feb" to 0.85f, "Mar" to 0.78f, "Apr" to 0.92f, 
            "May" to 0.88f, "Jun" to 0.95f
        )
    }
    
    val scores = sortedReviews.map { it.weightedScore.toInt() }.ifEmpty {
        listOf(72, 85, 78, 92, 88, 95)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.performance_trend),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                    Text(
                        text = stringResource(R.string.avg_monthly_growth, "+12%"),
                        style = MaterialTheme.typography.bodySmall,
                        color = Success
                    )
                }
                IconButton(onClick = onViewDetailedTrend) {
                    Icon(
                        imageVector = Icons.Default.Analytics, 
                        contentDescription = stringResource(R.string.view_detailed_analytics),
                        tint = Primary.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEachIndexed { index, pair ->
                    val isSelected = selectedIndex == index
                    val barHeight by animateFloatAsState(
                        targetValue = pair.second,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "barHeight"
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedIndex = if (selectedIndex == index) -1 else index
                            }
                    ) {
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = "${scores[index]}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .fillMaxHeight(barHeight)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    if (isSelected) Primary else Primary.copy(alpha = 0.3f)
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pair.first,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) PrimaryDark else OnSurfaceVariant
                        )
                    }
                }
            }
            
            AnimatedVisibility(
                visible = selectedIndex != -1,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                if (selectedIndex != -1) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                            .background(PrimaryContainer.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.breakdown_format, data[selectedIndex].first),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDark
                            )
                            Text(
                                text = stringResource(R.string.score_label, scores[selectedIndex].toString()),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { scores[selectedIndex] / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = Primary,
                            trackColor = Primary.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DepartmentScoresCard(
    departmentAverages: List<com.mindmatrix.employeetracker.data.model.DepartmentAverage> = emptyList(),
    onNavigateToReports: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.department_scores),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
                IconButton(onClick = onNavigateToReports) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = stringResource(R.string.view_detailed_reports),
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            val displayData = departmentAverages.ifEmpty {
                listOf(
                    com.mindmatrix.employeetracker.data.model.DepartmentAverage("Engineering", 92.5),
                    com.mindmatrix.employeetracker.data.model.DepartmentAverage("Design", 88.2),
                    com.mindmatrix.employeetracker.data.model.DepartmentAverage("Marketing", 85.0),
                    com.mindmatrix.employeetracker.data.model.DepartmentAverage("Sales", 78.4)
                )
            }

            displayData.forEach { avg ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = avg.department, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PrimaryDark)
                        Text(text = String.format(java.util.Locale.getDefault(), "%.1f", avg.averageScore), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    AnimatedProgressBar(progress = (avg.averageScore.toFloat() / 100f), color = Primary)
                }
            }
        }
    }
}

@Composable
fun WorkloadDistributionCard(
    employees: List<Employee>,
    tasks: List<Task>,
    modifier: Modifier = Modifier
) {
    // Calculate tasks per employee
    val workloadData = employees.map { employee ->
        val taskCount = tasks.count { it.assignedTo == employee.id }
        employee.name.split(" ").first() to taskCount
    }.sortedByDescending { it.second }.take(5)

    val maxTasks = workloadData.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.workload_distribution),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                    Text(
                        text = stringResource(R.string.active_tasks_per_employee),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = Primary.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            workloadData.forEach { (name, count) ->
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                        Text(
                            text = stringResource(R.string.tasks_count_format, count),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (count > 5) Error else Primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val progress = count.toFloat() / maxTasks.toFloat()
                    val barColor = when {
                        count > 5 -> Error
                        count > 3 -> Tertiary
                        else -> Primary
                    }
                    
                    AnimatedProgressBar(progress = progress, color = barColor)
                }
            }
            
            if (workloadData.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_workload_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun InsightsCard(
    insights: List<Insight>,
    modifier: Modifier = Modifier
) {
    if (insights.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.smart_insights),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryDark,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(insights) { insight ->
                val backgroundColor = when (insight.priority) {
                    InsightPriority.POSITIVE -> Success.copy(alpha = 0.1f)
                    InsightPriority.WARNING -> Error.copy(alpha = 0.1f)
                    InsightPriority.NEUTRAL -> Primary.copy(alpha = 0.1f)
                }
                val contentColor = when (insight.priority) {
                    InsightPriority.POSITIVE -> Success
                    InsightPriority.WARNING -> Error
                    InsightPriority.NEUTRAL -> Primary
                }

                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .height(110.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val icon = when (insight.priority) {
                                InsightPriority.POSITIVE -> Icons.AutoMirrored.Filled.TrendingUp
                                InsightPriority.WARNING -> Icons.Default.Notifications
                                InsightPriority.NEUTRAL -> Icons.Default.Analytics
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = insight.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = contentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = insight.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape),
        color = color,
        trackColor = color.copy(alpha = 0.1f)
    )
}

@Composable
fun StarRatingWidget(
    rating: Int,
    onRatingChange: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    starSize: androidx.compose.ui.unit.Dp = 24.dp
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            val isSelected = i <= rating
            val icon = if (isSelected) Icons.Default.Star else Icons.Default.StarOutline
            val tint = if (isSelected) Secondary else Outline
            
            // Modifier for interaction
            var starModifier = Modifier.size(starSize)
            if (onRatingChange != null) {
                starModifier = starModifier.clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = androidx.compose.material.ripple.rememberRipple(bounded = false, radius = starSize / 1.5f),
                    onClick = { onRatingChange(i) }
                )
            }
            
            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.star_n, i),
                tint = tint,
                modifier = starModifier
            )
        }
    }
}
