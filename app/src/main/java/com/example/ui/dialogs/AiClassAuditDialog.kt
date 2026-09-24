package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import com.example.data.model.StudentConsolidatedRecord
import com.example.data.model.Subject
import com.example.ui.theme.*

/**
 * AI Class-wide Audit dialog for teachers in ConsolidationScreen.
 * Scans all students in the class for data anomalies, attendance errors, and learning trends.
 */
@Composable
fun AiClassAuditDialog(
    stdClass: Int,
    term: Int,
    records: List<StudentConsolidatedRecord>,
    onDismiss: () -> Unit,
    onAutoFixAllAttendanceErrors: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    // Analyze class data
    val totalStudents = records.size
    val effectiveTerm = if (term in 1..3) term else 1

    val attendanceErrors = records.filter { record ->
        val att = when (effectiveTerm) {
            1 -> record.term1Attendance
            2 -> record.term2Attendance
            else -> record.term3Attendance
        }
        att != null && att.totalWorkingDays > 0 && att.presentDays > att.totalWorkingDays
    }

    val lowAttendanceStudents = records.filter { record ->
        val att = when (effectiveTerm) {
            1 -> record.term1Attendance
            2 -> record.term2Attendance
            else -> record.term3Attendance
        }
        if (att != null && att.totalWorkingDays > 0) {
            (att.presentDays.toFloat() / att.totalWorkingDays.toFloat()) < 0.75f
        } else false
    }

    val missingMarksStudents = records.filter { record ->
        val mainSubjects = Subject.getMainSubjectsForClass(record.student.stdClass)
        val termTotal = mainSubjects.sumOf { subj ->
            val threeTerms = record.subjectMarks[subj]
            when (effectiveTerm) {
                1 -> threeTerms?.term1Total ?: 0
                2 -> threeTerms?.term2Total ?: 0
                else -> threeTerms?.term3Total ?: 0
            }
        }
        termTotal <= 0
    }

    val classAvgPct = if (records.isNotEmpty()) {
        val validPcts = records.mapNotNull { record ->
            val mainSubjects = Subject.getMainSubjectsForClass(record.student.stdClass)
            val termTotal = mainSubjects.sumOf { subj ->
                val threeTerms = record.subjectMarks[subj]
                when (effectiveTerm) {
                    1 -> threeTerms?.term1Total ?: 0
                    2 -> threeTerms?.term2Total ?: 0
                    else -> threeTerms?.term3Total ?: 0
                }
            }
            val maxMarks = mainSubjects.size * 100
            if (termTotal > 0 && maxMarks > 0) (termTotal.toDouble() / maxMarks.toDouble()) * 100.0 else null
        }
        if (validPcts.isNotEmpty()) validPcts.average() else 0.0
    } else 0.0

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
                .testTag("ai_class_audit_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
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
                                    Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI வகுப்புத் தரவு ஆய்வு (Class Audit)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextDark
                            )
                            Text(
                                text = "$stdClass-ம் வகுப்பு • பருவம் $term • மொத்தம் $totalStudents மாணவர்கள்",
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Quick Status Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Class Average
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("வகுப்பு சராசரி", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    "${String.format("%.1f", classAvgPct)}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (classAvgPct >= 60.0) EmeraldPass else NavyPrimary
                                )
                            }
                        }

                        // Attendance Errors Card
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (attendanceErrors.isNotEmpty()) (if (isDark) Color(0xFF3B151A) else Color(0xFFFEF2F2)) else (if (isDark) Color(0xFF0F2E1E) else Color(0xFFF0FDF4))
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (attendanceErrors.isNotEmpty()) (if (isDark) Color(0xFF7F1D1D) else Color(0xFFFECDD3)) else (if (isDark) Color(0xFF166534) else Color(0xFFBBF7D0))
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("வருகைப் பிழைகள்", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    "${attendanceErrors.size} மாணவர்கள்",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (attendanceErrors.isNotEmpty()) Color(0xFFDC2626) else EmeraldPass
                                )
                            }
                        }
                    }

                    // Section 1: Detected Errors
                    Text(
                        text = "1. தரவு முரண்பாடுகள் & பிழைகள் (Detected Anomalies)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = TextDark
                    )

                    if (attendanceErrors.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF3B151A) else Color(0xFFFEF2F2)),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF7F1D1D) else Color(0xFFFECDD3)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "${attendanceErrors.size} மாணவர்களில் வருகை நாட்கள் பள்ளி வேலை நாட்களை விட அதிகம்!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = Color(0xFFB91C1C)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = attendanceErrors.take(4).joinToString(", ") { "${it.student.name} (${it.student.admissionNo})" } +
                                            (if (attendanceErrors.size > 4) " மற்றும் மேலும் ${attendanceErrors.size - 4} பேர்" else ""),
                                    fontSize = 11.5.sp,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        onAutoFixAllAttendanceErrors()
                                        Toast.makeText(context, "அனைத்து மாணவர்களின் வருகைப் பிழைகளும் தானாக சரிசெய்யப்பட்டன!", Toast.LENGTH_LONG).show()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPass),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("அனைத்துப் பிழைகளையும் ஒரே கிளிக்கில் தானாக சரிசெய் (Auto-Fix All)", fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F2E1E) else Color(0xFFF0FDF4)),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF166534) else Color(0xFFBBF7D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPass, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "அனைத்து மாணவர்களின் வருகைப் பதிவும் சரியான வரம்பில் உள்ளது (No attendance errors).",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark
                                )
                            }
                        }
                    }

                    if (missingMarksStudents.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF382305) else Color(0xFFFFFBEB)),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF78350F) else Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "${missingMarksStudents.size} மாணவர்களுக்கு இன்னும் மதிப்பெண் முழுமையாக உள்ளிடப்படவில்லை",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = TextDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = missingMarksStudents.take(3).joinToString(", ") { it.student.name } +
                                            (if (missingMarksStudents.size > 3) " உள்ளிட்டோர்" else ""),
                                    fontSize = 11.5.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    // Section 2: AI Pedagogical Summary
                    Text(
                        text = "2. AI வகுப்புத் தேர்ச்சி ஆய்வு & ஆசிரியர் வழிகாட்டல்",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = TextDark
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "💡 ஆசிரியர் வழிகாட்டல் குறிப்பு:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = NavyPrimary
                            )
                            val summaryText = if (classAvgPct >= 70.0) {
                                "வகுப்பில் பெரும்பாலான மாணவர்கள் சிறந்த தேர்ச்சி அடைந்துள்ளனர். கற்றல் குறைபாடுள்ள சில மாணவர்களுக்கு மட்டும் மாலை நேர சிறப்பு வகுப்புகள் மூலம் எளிய கணக்குகள் மற்றும் ஆங்கில வாசிப்புப் பயிற்சி அளிக்கலாம்."
                            } else if (classAvgPct >= 50.0) {
                                "வகுப்பின் தேர்ச்சி நிலை சீராக உள்ளது. கற்றல் நிலையை 'மொட்டு' மற்றும் 'அரும்பு' நிலையிலிருந்து 'மலர்' நிலைக்கு உயர்த்த குழு கற்றல் (Peer Learning) மற்றும் செயல்பாட்டு வழி கற்றல் முறைகளை ஊக்கப்படுத்தலாம்."
                            } else {
                                "மாணவர்களின் கற்றல் அடைவுகளை மேம்படுத்த தனிநபர் கவனம் மற்றும் கற்றல் துணைக்கருவிகள் (TLM) பயன்பாட்டை அதிகரிக்கலாம். தினசரி வருகையை உறுதிப்படுத்த பெற்றோருடன் தொடர்பு கொள்வது சிறந்தது."
                            }
                            Text(
                                text = summaryText,
                                fontSize = 12.sp,
                                color = TextDark,
                                lineHeight = 17.sp
                            )
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
