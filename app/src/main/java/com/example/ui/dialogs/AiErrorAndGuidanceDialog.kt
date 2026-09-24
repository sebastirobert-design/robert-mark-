package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Student
import com.example.data.model.StudentMarks
import com.example.data.model.Subject
import com.example.ui.theme.*
import com.example.util.AiErrorAndGuidanceManager
import kotlinx.coroutines.launch

/**
 * AI-powered error checking and student performance guidance dialog.
 * Uses Gemini 3.5 Flash / offline heuristics to detect data anomalies and generate
 * actionable teacher remarks in Tamil.
 */
@Composable
fun AiErrorAndGuidanceDialog(
    student: Student,
    term: Int,
    workingDays: Int,
    presentDays: Int,
    marksMap: Map<Subject, StudentMarks>,
    onDismiss: () -> Unit,
    onAutoFixAttendance: ((correctedWorking: Int, correctedPresent: Int) -> Unit)? = null,
    onApplyRemark: ((remark: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()

    var isLoading by remember { mutableStateOf(true) }
    var analysisResult by remember { mutableStateOf<AiErrorAndGuidanceManager.StudentAnalysisResult?>(null) }

    LaunchedEffect(student.id, term) {
        isLoading = true
        analysisResult = AiErrorAndGuidanceManager.analyzeStudentAndGenerateRemarks(
            student = student,
            term = term,
            workingDays = workingDays,
            presentDays = presentDays,
            marksMap = marksMap
        )
        isLoading = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardBg,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .testTag("ai_guidance_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with AI Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = NavyPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI சரிபார்ப்பு & வழிகாட்டல்",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextDark
                            )
                            Text(
                                text = "${student.name} • ${student.stdClass}-ம் வகுப்பு • பருவம் $term",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderSubtle)

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = NavyPrimary)
                            Text(
                                text = "AI தரவுகளை ஆய்வு செய்கிறது...",
                                fontSize = 13.sp,
                                color = TextDark,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "மதிப்பெண் வரம்புகள், வருகைப் பதிவு மற்றும் கற்றல் திறனை மதிப்பீடு செய்கிறது",
                                fontSize = 11.5.sp,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    val result = analysisResult
                    if (result != null) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 1. Data Validation & Issues Section
                            Text(
                                text = "1. பிழை மற்றும் வரம்பு ஆய்வு (Data Audit)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextDark
                            )

                            result.issues.forEach { issue ->
                                val cardBg = when (issue.type) {
                                    AiErrorAndGuidanceManager.IssueType.ERROR -> if (isDark) Color(0xFF3B151A) else Color(0xFFFEF2F2)
                                    AiErrorAndGuidanceManager.IssueType.WARNING -> if (isDark) Color(0xFF382305) else Color(0xFFFFFBEB)
                                    AiErrorAndGuidanceManager.IssueType.SUCCESS -> if (isDark) Color(0xFF0F2E1E) else Color(0xFFF0FDF4)
                                }
                                val borderColor = when (issue.type) {
                                    AiErrorAndGuidanceManager.IssueType.ERROR -> if (isDark) Color(0xFF7F1D1D) else Color(0xFFFECDD3)
                                    AiErrorAndGuidanceManager.IssueType.WARNING -> if (isDark) Color(0xFF78350F) else Color(0xFFFDE68A)
                                    AiErrorAndGuidanceManager.IssueType.SUCCESS -> if (isDark) Color(0xFF166534) else Color(0xFFBBF7D0)
                                }
                                val iconTint = when (issue.type) {
                                    AiErrorAndGuidanceManager.IssueType.ERROR -> Color(0xFFDC2626)
                                    AiErrorAndGuidanceManager.IssueType.WARNING -> Color(0xFFD97706)
                                    AiErrorAndGuidanceManager.IssueType.SUCCESS -> EmeraldPass
                                }
                                val icon = when (issue.type) {
                                    AiErrorAndGuidanceManager.IssueType.ERROR -> Icons.Default.ErrorOutline
                                    AiErrorAndGuidanceManager.IssueType.WARNING -> Icons.Default.WarningAmber
                                    AiErrorAndGuidanceManager.IssueType.SUCCESS -> Icons.Default.CheckCircle
                                }

                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    border = BorderStroke(1.dp, borderColor),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                icon,
                                                contentDescription = null,
                                                tint = iconTint,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = issue.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TextDark,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = issue.description,
                                            fontSize = 12.sp,
                                            color = TextDark,
                                            lineHeight = 16.sp
                                        )

                                        if (issue.canAutoFix && onAutoFixAttendance != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    val correctedPresent = minOf(presentDays, workingDays)
                                                    onAutoFixAttendance(workingDays, correctedPresent)
                                                    Toast.makeText(context, "வருகைப் பிழை தானாக சரிசெய்யப்பட்டது!", Toast.LENGTH_SHORT).show()
                                                    onDismiss()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPass),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.align(Alignment.End)
                                            ) {
                                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("தானாக சரிசெய் (Auto-Fix)", fontSize = 11.5.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // 2. Strengths and Weaknesses
                            Text(
                                text = "2. பாடவாரி தேர்ச்சி ஆய்வு (Subject Insights)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextDark
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Strong subjects
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F2E1E) else Color(0xFFF0FDF4)),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF166534) else Color(0xFFBBF7D0)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.ThumbUp, contentDescription = null, tint = EmeraldPass, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("தனித்திறன் (≥75%)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = TextDark)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (result.strongSubjects.isNotEmpty()) result.strongSubjects.joinToString(", ") else "வளர்ச்சி நிலை",
                                            fontSize = 11.sp,
                                            color = TextDark
                                        )
                                    }
                                }

                                // Needs improvement
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF382305) else Color(0xFFFFFBEB)),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF78350F) else Color(0xFFFDE68A)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.TipsAndUpdates, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("பயிற்சி தேவை (<50%)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = TextDark)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (result.weakSubjects.isNotEmpty()) result.weakSubjects.joinToString(", ") else "குறிப்பிட்ட பலவீனமில்லை",
                                            fontSize = 11.sp,
                                            color = TextDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // 3. AI Generated Teacher Remarks
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "3. AI ஆசிரியர் குறிப்பு (Teacher Remark)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextDark
                                    )
                                    if (result.isAiGenerated) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = NavyPrimary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Gemini AI",
                                                color = NavyPrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("AI Remark", result.teacherRemark)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "குறிப்பு நகலெடுக்கப்பட்டது!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NavyPrimary, modifier = Modifier.size(16.dp))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "\"${result.teacherRemark}\"",
                                        fontSize = 12.5.sp,
                                        color = TextDark,
                                        lineHeight = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (onApplyRemark != null) {
                                Button(
                                    onClick = {
                                        onApplyRemark(result.teacherRemark)
                                        Toast.makeText(context, "ஆசிரியர் குறிப்பு வெற்றிகரமாக பொருத்தப்பட்டது!", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("இக்குறிப்பை ஏற்றுக்கொள்க (Apply to Remarks)", fontSize = 12.5.sp)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderSubtle)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("மூடுக", color = TextDark)
                    }
                }
            }
        }
    }
}
