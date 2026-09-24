package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CceGradeEvaluator
import com.example.data.model.SchoolProfile
import com.example.data.model.StudentConsolidatedRecord
import com.example.data.model.Subject
import com.example.ui.theme.AcademicBlue
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardBg
import com.example.ui.theme.CardBgSubtle
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextNavy

@Composable
fun TermRankCardDialog(
    record: StudentConsolidatedRecord,
    school: SchoolProfile,
    term: Int,
    rank: Int,
    totalClassStudents: Int,
    onDismiss: () -> Unit,
    onPrintPdf: () -> Unit,
    onSharePdf: () -> Unit,
    onViewPdf: () -> Unit,
    onPrintAllClassPdf: () -> Unit
) {
    val subjects = Subject.getSubjectsForClass(record.student.stdClass)
    val termTotal = record.getTermTotal(term)
    val maxMarks = subjects.size * 100
    val percentage = if (maxMarks > 0) (termTotal.toFloat() / maxMarks.toFloat()) * 100f else 0f
    val overallGrade = if (record.student.stdClass in 1..3) {
        CceGradeEvaluator.getGradeForClass1To3(percentage.toInt())
    } else {
        CceGradeEvaluator.getGradeForClass4To8(percentage.toInt())
    }

    val att = when (term) {
        1 -> record.term1Attendance
        2 -> record.term2Attendance
        3 -> record.term3Attendance
        else -> null
    }
    val workDays = att?.totalWorkingDays ?: 80
    val presDays = att?.presentDays ?: 76
    val attPct = if (workDays > 0) (presDays.toFloat() / workDays.toFloat()) * 100f else 0f

    val termTamil = when (term) {
        1 -> "முதல் பருவம் (Term 1)"
        2 -> "இரண்டாம் பருவம் (Term 2)"
        3 -> "மூன்றாம் பருவம் (Term 3)"
        else -> "பருவம் $term"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .testTag("term_rank_card_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Bar with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = AmberGold.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🏆", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "மாணவர் பருவத் தேர்வு தர அட்டை (A4)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextNavy
                            )
                            Text(
                                text = "$termTamil • வகுப்பு ${record.student.stdClass}",
                                fontSize = 11.5.sp,
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "மூடுக", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons Strip: Print, Share, View, All Class
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPrintPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).testTag("dialog_print_rank_btn")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("அச்சிடுக", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSharePdf,
                        colors = ButtonDefaults.buttonColors(containerColor = AcademicBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).testTag("dialog_share_rank_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("பகிர்க", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onViewPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPass),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).testTag("dialog_view_rank_btn")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF பார்க்க", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onPrintAllClassPdf,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavyPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("முழு வகுப்பு மாணவர்களின் தர அட்டைகள் (All Students A4 PDF)", fontSize = 11.5.sp, color = NavyPrimary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card Preview Surface (Simulating A4 layout)
                    val isDark = isSystemInDarkTheme()
                    val infoBg = if (isDark) CardBgSubtle else Color(0xFFF1F5F9)
                    val rankBg = if (isDark) Color(0xFF78350F).copy(alpha = 0.35f) else Color(0xFFFEF3C7)
                    val rankBorder = if (isDark) Color(0xFFD97706) else Color(0xFFF59E0B)
                    val rankText = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                    val totalMarksBg = if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.35f) else Color(0xFFEEF2FF)
                    val gradeBg = if (isDark) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFECFDF5)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CardBg,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, NavyPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "தமிழ்நாடு அரசு • பள்ளிக் கல்வித்துறை",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberGold
                                )
                                Text(
                                    text = school.schoolName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextNavy,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${school.unionName}, ${school.districtName} | கல்வியாண்டு: ${school.academicYear}",
                                    fontSize = 9.5.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = NavyPrimary.copy(alpha = 0.08f),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = "$termTamil - மாணவர் முன்னேற்ற & தர அட்டை",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Student Info Row
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = infoBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "மாணவர் பெயர்: ${record.student.name}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextNavy
                                        )
                                        Text(
                                            text = "சேர்க்கை எண்: ${record.student.admissionNo}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AcademicBlue
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "வகுப்பு: ${record.student.stdClass} - பிரிவு: ${record.student.section}",
                                            fontSize = 11.sp,
                                            color = TextDark
                                        )
                                        Text(
                                            text = "இனம்: ${record.student.community} | பாலினம்: ${record.student.gender}",
                                            fontSize = 11.sp,
                                            color = TextDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Rank & Score Highlight Metrics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Rank Box
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = rankBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, rankBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("வகுப்பு தரம்", fontSize = 9.sp, color = rankText)
                                        Text(
                                            text = "🏆 $rank-ம் இடம்",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = rankText
                                        )
                                        Text("மொத்தம்: $totalClassStudents", fontSize = 8.5.sp, color = rankText.copy(alpha = 0.85f))
                                    }
                                }

                                // Total Marks Box
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = totalMarksBg,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("பருவ மொத்தம்", fontSize = 9.sp, color = AcademicBlue)
                                        Text(
                                            text = "$termTotal / $maxMarks",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = NavyPrimary
                                        )
                                        Text("${String.format("%.1f", percentage)}%", fontSize = 8.5.sp, color = TextMuted)
                                    }
                                }

                                // Grade Box
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = gradeBg,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("தரநிலை", fontSize = 9.sp, color = EmeraldPass)
                                        Text(
                                            text = overallGrade,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = EmeraldPass
                                        )
                                        Text(if (percentage >= 35) "தேர்ச்சி" else "ஊக்கம்", fontSize = 8.5.sp, color = TextDark)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Subject Marks Preview Table
                            Text(
                                text = if (record.student.stdClass == 8) "பாடவாரியான மதிப்பெண்கள் [நேரடி 100 மதிப்பெண்]:" else "பாடவாரியான மதிப்பெண்கள்:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CardBg, RoundedCornerShape(6.dp))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                            ) {
                                // Table Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(NavyPrimary)
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("பாடம்", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    if (record.student.stdClass == 8) {
                                        Text("மதிப்பெண் [100]", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    } else if (record.student.stdClass in 1..3) {
                                        Text("நானே1 / நானே2 / திறனறி", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    } else {
                                        Text("FA [40] + SA [60]", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Text("மொத்தம் / தரம்", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                subjects.forEachIndexed { i, sub ->
                                    val sm = record.subjectMarks[sub]
                                    val m = when (term) {
                                        1 -> sm?.term1Marks
                                        2 -> sm?.term2Marks
                                        3 -> sm?.term3Marks
                                        else -> null
                                    }
                                    val markVal = m?.total ?: 0
                                    val gradeVal = m?.grade ?: CceGradeEvaluator.getGradeForClass4To8(markVal)

                                    val rowBg = if (isDark) {
                                        if (i % 2 == 1) Color(0xFF1E293B) else Color(0xFF0F172A)
                                    } else {
                                        if (i % 2 == 1) Color(0xFFF8FAFC) else Color.White
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(rowBg)
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${i + 1}. ${sub.tamilName}", fontSize = 10.5.sp, color = TextNavy, fontWeight = FontWeight.Medium)

                                        if (record.student.stdClass == 8) {
                                            Text("$markVal / 100", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = EmeraldPass)
                                        } else if (record.student.stdClass in 1..3) {
                                            val n1 = (m?.naney1Oral ?: 10) + (m?.naney1Activity ?: 10) + (m?.naney1Written ?: 5)
                                            val n2 = (m?.naney2Oral ?: 10) + (m?.naney2Activity ?: 10) + (m?.naney2Written ?: 5)
                                            val th = (m?.thiranariOral ?: 10) + (m?.thiranariWritten ?: 40)
                                            Text("$n1 + $n2 + $th", fontSize = 10.sp, color = TextDark)
                                        } else {
                                            val fa = m?.faTotal ?: 0
                                            val sa = m?.sa ?: 0
                                            Text("$fa + $sa", fontSize = 10.sp, color = TextDark)
                                        }

                                        Text("$markVal ($gradeVal)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Footer with Attendance & Teacher Remarks
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("வருகை: $presDays / $workDays நாட்கள் (${attPct.toInt()}%)", fontSize = 10.sp, color = TextDark)
                                Text("முடிவு: ${if (percentage >= 35) "தேர்ச்சி (PASS)" else "ஊக்கம் தேவை"}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldPass)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "வகுப்பாசிரியர் குறிப்பு: நல்ல ஈடுபாட்டுடன் பயில்கிறார். தொடர் நல்முயற்சிக்கு வாழ்த்துகள்!",
                                fontSize = 9.5.sp,
                                color = TextMuted
                            )
                        }
                    }
            }
        }
    }
}
