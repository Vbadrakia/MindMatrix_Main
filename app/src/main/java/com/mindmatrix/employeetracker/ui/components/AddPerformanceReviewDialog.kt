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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindmatrix.employeetracker.ui.theme.Primary
import java.util.Locale

@Composable
fun AddPerformanceReviewDialog(
    employeeName: String,
    onDismiss: () -> Unit,
    onSubmit: (Int, Int, Int, Int, Int, String, String, Double) -> Unit
) {
    var quality by remember { mutableIntStateOf(3) }
    var timeliness by remember { mutableIntStateOf(3) }
    var attendance by remember { mutableIntStateOf(3) }
    var communication by remember { mutableIntStateOf(3) }
    var innovation by remember { mutableIntStateOf(3) }
    var comments by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("Monthly Review") }

    val overallScore = remember(quality, timeliness, attendance, communication, innovation) {
        (quality + timeliness + attendance + communication + innovation) / 5.0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Performance Evaluation: $employeeName",
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
                    label = { Text("Review Period") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                HorizontalDivider()

                StarRatingItem(label = "Quality of Work", score = quality, onScoreChange = { quality = it })
                StarRatingItem(label = "Timeliness", score = timeliness, onScoreChange = { timeliness = it })
                StarRatingItem(label = "Attendance", score = attendance, onScoreChange = { attendance = it })
                StarRatingItem(label = "Communication", score = communication, onScoreChange = { communication = it })
                StarRatingItem(label = "Innovation", score = innovation, onScoreChange = { innovation = it })

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
                            "Calculated Score:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f / 5.0", overallScore),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Primary
                        )
                    }
                }

                OutlinedTextField(
                    value = comments,
                    onValueChange = { comments = it },
                    label = { Text("Remarks / Feedback") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(quality, timeliness, attendance, communication, innovation, comments, period, overallScore)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Submit Evaluation", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun StarRatingItem(
    label: String,
    score: Int,
    onScoreChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = "$score/5 rating", style = MaterialTheme.typography.labelSmall, color = Primary.copy(alpha = 0.7f))
        }
        StarRatingWidget(
            rating = score,
            onRatingChange = onScoreChange,
            starSize = 28.dp
        )
    }
}
