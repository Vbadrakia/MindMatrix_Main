package com.mindmatrix.employeetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindmatrix.employeetracker.R
import com.mindmatrix.employeetracker.ui.theme.Primary
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AddPerformanceReviewDialog(
    employeeName: String,
    onDismiss: () -> Unit,
    onSubmit: (Int, Int, Int, Int, Int, String, String, Double) -> Unit
) {
    var quality by remember { mutableFloatStateOf(80f) }
    var timeliness by remember { mutableFloatStateOf(80f) }
    var attendance by remember { mutableFloatStateOf(80f) }
    var communication by remember { mutableFloatStateOf(80f) }
    var innovation by remember { mutableFloatStateOf(80f) }
    var comments by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("") }
    
    val defaultPeriod = stringResource(R.string.monthly_review)
    LaunchedEffect(Unit) {
        if (period.isEmpty()) period = defaultPeriod
    }

    val overallScore = remember(quality, timeliness, attendance, communication, innovation) {
        (quality + timeliness + attendance + communication + innovation) / 5.0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.evaluation_title_format, employeeName),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = period,
                    onValueChange = { period = it },
                    label = { Text(stringResource(R.string.review_period)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                HorizontalDivider()

                ScoreSliderItem(label = stringResource(R.string.quality_of_work), score = quality, onScoreChange = { quality = it })
                ScoreSliderItem(label = stringResource(R.string.filter_timeliness), score = timeliness, onScoreChange = { timeliness = it })
                ScoreSliderItem(label = stringResource(R.string.attendance), score = attendance, onScoreChange = { attendance = it })
                ScoreSliderItem(label = stringResource(R.string.filter_communication), score = communication, onScoreChange = { communication = it })
                ScoreSliderItem(label = stringResource(R.string.filter_innovation), score = innovation, onScoreChange = { innovation = it })

                HorizontalDivider()

                // Overall Score Display
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Primary.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.calculated_score),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.score_format_with_total, overallScore),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Primary
                        )
                    }
                }

                OutlinedTextField(
                    value = comments,
                    onValueChange = { comments = it },
                    label = { Text(stringResource(R.string.remarks_feedback)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        quality.roundToInt(),
                        timeliness.roundToInt(),
                        attendance.roundToInt(),
                        communication.roundToInt(),
                        innovation.roundToInt(),
                        comments,
                        period,
                        overallScore
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.submit_evaluation), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun ScoreSliderItem(
    label: String,
    score: Float,
    onScoreChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = stringResource(R.string.score_label_with_total, score.roundToInt()), style = MaterialTheme.typography.labelMedium, color = Primary.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
        }
        Slider(
            value = score,
            onValueChange = onScoreChange,
            valueRange = 0f..100f,
            steps = 100,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
