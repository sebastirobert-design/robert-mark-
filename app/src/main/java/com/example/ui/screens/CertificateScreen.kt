package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentConsolidatedRecord
import com.example.data.model.Subject
import com.example.ui.components.ClassChipsSelector
import com.example.ui.dialogs.TermRankCardDialog
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
import com.example.util.PdfReportGenerator
import com.example.viewmodel.SchoolMarksViewModel
import java.io.File

@Composable
fun CertificateScreen(
    viewModel: SchoolMarksViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val schoolProfile by viewModel.schoolProfile.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()

    var records by remember { mutableStateOf<List<StudentConsolidatedRecord>>(emptyList()) }
    var selectedStudentIndex by remember { mutableIntStateOf(0) }
    var showRankCardDialog by remember { mutableStateOf(false) }
    var selectedRankCardTerm by remember { mutableIntStateOf(1) }

    LaunchedEffect(selectedClass, allStudents) {
        records = viewModel.loadConsolidatedRecordsForClass(selectedClass)
        selectedStudentIndex = 0
    }

    val currentRecord = records.getOrNull(selectedStudentIndex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("certificate_screen")
    ) {
        // Class Selector
        ClassChipsSelector(
            selectedClass = selectedClass,
            onSelectClass = { viewModel.selectClass(it) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "வகுப்பு $selectedClass -ல் மாணவர்கள் பதிவு செய்யப்படவில்லை.",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            // Student Selection Strip
            Surface(
                color = CardBgSubtle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    records.forEachIndexed { idx, rec ->
                        val isSelected = idx == selectedStudentIndex
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStudentIndex = idx },
                            label = {
                                Text(
                                    text = "${rec.student.admissionNo}. ${rec.student.name}",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            currentRecord?.let { record ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // A4 Term Rank Card Card Section (Available for All Classes 1 to 8)
                    item {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📄 A4 பருவத் தேர்வு தர அட்டை (Rank Card)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = NavyPrimary
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(1, 2, 3).forEach { t ->
                                            FilterChip(
                                                selected = selectedRankCardTerm == t,
                                                onClick = { selectedRankCardTerm = t },
                                                label = { Text("பருவம் $t", fontSize = 11.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = NavyPrimary,
                                                    selectedLabelColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showRankCardDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("cert_open_rank_card_btn")
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavyDark)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("🖨️ A4 தர அட்டை", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.exportClassAllTermRankCardsPdf(context, records, selectedRankCardTerm) {
                                                PdfReportGenerator.printPdfFile(context, it)
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("cert_class_rank_cards_btn")
                                    ) {
                                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavyPrimary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("வகுப்பு தர அட்டைகள்", fontSize = 11.sp, color = NavyPrimary, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }

                    // Export Actions at Top (Class 8 Certificate)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.exportCertificatePdf(context, record) { file ->
                                        Toast.makeText(context, "${record.student.name} சான்றிதழ் தயார்", Toast.LENGTH_SHORT).show()
                                        PdfReportGenerator.sharePdfFile(
                                            context,
                                            file,
                                            "${record.student.name} - வகுப்பு 8 மதிப்பெண் சான்றிதழ்"
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_cert_btn")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("சான்றிதழ் பகிர்க (PDF)")
                            }

                            Button(
                                onClick = {
                                    viewModel.exportCertificatePdf(context, record) { file ->
                                        PdfReportGenerator.viewPdfFile(context, file)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPass),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("view_cert_btn")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PDF பார்க்க")
                            }
                        }
                    }

                    // Certificate Visual Preview Card
                    item {
                        CertificatePreviewCard(record = record, school = schoolProfile)
                    }
                }
            }
        }

        if (showRankCardDialog && currentRecord != null) {
            val ranksMap = viewModel.calculateClassRanks(records, selectedRankCardTerm)
            val rank = ranksMap[currentRecord.student.id] ?: 1

            TermRankCardDialog(
                record = currentRecord,
                school = schoolProfile,
                term = selectedRankCardTerm,
                rank = rank,
                totalClassStudents = records.size,
                onDismiss = { showRankCardDialog = false },
                onPrintPdf = {
                    viewModel.exportTermRankCardPdf(context, currentRecord, selectedRankCardTerm, rank, records.size) {
                        PdfReportGenerator.printPdfFile(context, it)
                    }
                },
                onSharePdf = {
                    viewModel.exportTermRankCardPdf(context, currentRecord, selectedRankCardTerm, rank, records.size) {
                        PdfReportGenerator.sharePdfFile(context, it)
                    }
                },
                onViewPdf = {
                    viewModel.exportTermRankCardPdf(context, currentRecord, selectedRankCardTerm, rank, records.size) {
                        PdfReportGenerator.viewPdfFile(context, it)
                    }
                },
                onPrintAllClassPdf = {
                    viewModel.exportClassAllTermRankCardsPdf(context, records, selectedRankCardTerm) {
                        PdfReportGenerator.printPdfFile(context, it)
                    }
                }
            )
        }
    }
}

@Composable
fun CertificatePreviewCard(
    record: StudentConsolidatedRecord,
    school: com.example.data.model.SchoolProfile
) {
    val isDark = isSystemInDarkTheme()
    val tableHeaderBg = if (isDark) Color(0xFF1E293B) else Color(0xFFEEF2FF)
    val totalRowBg = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)
    val peRowBg = if (isDark) Color(0xFF3B2505) else Color(0xFFFEFCE8)
    val peBorderColor = if (isDark) Color(0xFF78350F) else Color(0xFFFEF08A)
    val peTextColor = if (isDark) Color(0xFFFDE68A) else Color(0xFF854D0E)
    val attendanceBg = if (isDark) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFF0FDF4)
    val attendanceBorder = if (isDark) Color(0xFF065F46) else Color(0xFFBBF7D0)
    val stampBg = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, NavyPrimary, RoundedCornerShape(12.dp))
            .testTag("certificate_preview_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Certificate Top Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "தமிழ்நாடு பள்ளி கல்வித்துறை",
                    color = AmberGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = school.schoolName,
                    color = TextNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${school.unionName}, ${school.districtName}",
                    color = TextMuted,
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NavyPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (record.student.stdClass == 8) "எட்டாம் வகுப்பு ஆண்டு மதிப்பெண் சான்றிதழ்"
                        else "வகுப்பு ${record.student.stdClass} ஆண்டு மதிப்பெண் சான்றிதழ்",
                        color = TextNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Student Info Table
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CardBgSubtle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "மாணவர் பெயர்: ${record.student.name}", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = TextNavy)
                        Text(text = "சேர்க்கை எண்: ${record.student.admissionNo}", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = AmberGold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "வகுப்பு: ${record.student.stdClass} - பிரிவு ${record.student.section}", fontSize = 11.5.sp, color = TextDark)
                        Text(text = "கல்வியாண்டு: ${school.academicYear}", fontSize = 11.5.sp, color = TextDark)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val pName = record.student.parentName.ifEmpty { "-" }
                        Text(text = "பெற்றோர் பெயர்: $pName", fontSize = 11.5.sp, color = TextDark)
                        Text(text = "இனம்: ${record.student.community}", fontSize = 11.5.sp, color = TextDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Marks Table (Three-Term Average Mark Certificate)
            val mainSubjects = Subject.getMainSubjectsForClass(record.student.stdClass)
            val hasPe = Subject.hasPeSubject(record.student.stdClass)
            val isClass8 = record.student.stdClass == 8

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tableHeaderBg)
                    .border(0.5.dp, BorderSubtle)
                    .padding(vertical = 7.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("வ.எண்", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextNavy, textAlign = TextAlign.Center, modifier = Modifier.weight(0.6f))
                Text("பாடங்கள் (Subjects)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextNavy, modifier = Modifier.weight(2f))
                Text("முழு மதிப்பெண்", fontWeight = FontWeight.Bold, fontSize = 10.5.sp, color = TextNavy, textAlign = TextAlign.Center, modifier = Modifier.weight(1.1f))
                Text("முப்பருவ சராசரி", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextNavy, textAlign = TextAlign.Center, modifier = Modifier.weight(1.3f))
                Text("தரம்", fontWeight = FontWeight.Bold, fontSize = 10.5.sp, color = TextNavy, textAlign = TextAlign.Center, modifier = Modifier.weight(0.8f))
            }

            // 5 Main Academic Subject Rows
            mainSubjects.forEachIndexed { index, sub ->
                val sm = record.subjectMarks[sub]
                val avg = sm?.avgTotal?.toString() ?: "-"
                val grade = sm?.getGrade(record.student.stdClass) ?: "-"
                val rowBg = if (isDark) {
                    if (index % 2 == 1) Color(0xFF1E293B) else Color(0xFF0F172A)
                } else {
                    if (index % 2 == 1) Color(0xFFFAFAFA) else Color.White
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowBg)
                        .border(0.5.dp, BorderSubtle)
                        .padding(vertical = 6.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${index + 1}", fontSize = 11.sp, textAlign = TextAlign.Center, color = TextDark, modifier = Modifier.weight(0.6f))
                    Text(sub.tamilName, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark, modifier = Modifier.weight(2f))
                    Text("100", fontSize = 11.sp, color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.weight(1.1f))
                    Text(avg, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary, textAlign = TextAlign.Center, modifier = Modifier.weight(1.3f))
                    Text(grade, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberGold, textAlign = TextAlign.Center, modifier = Modifier.weight(0.8f))
                }
            }

            // Total Row (Only 5 main subjects, sum out of 500 or 300)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(totalRowBg)
                    .border(0.5.dp, BorderSubtle)
                    .padding(vertical = 7.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("", modifier = Modifier.weight(0.6f))
                Text("மொத்தம் (TOTAL)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextNavy, modifier = Modifier.weight(2f))
                Text("${mainSubjects.size * 100}", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = TextNavy, textAlign = TextAlign.Center, modifier = Modifier.weight(1.1f))
                Text("${record.grandAvgTotal}", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = TextNavy, textAlign = TextAlign.Center, modifier = Modifier.weight(1.3f))
                Text(record.overallGrade, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = AmberGold, textAlign = TextAlign.Center, modifier = Modifier.weight(0.8f))
            }

            // Physical Education (உடற்கல்வி) Row - Shown AFTER the Total row, NOT in Total
            if (hasPe) {
                val peSm = record.subjectMarks[Subject.PE]
                val peAvg = if (peSm?.avgTotal != null && peSm.avgTotal > 0) "${peSm.avgTotal}" else "${record.peAvgTotal}"
                val peGrade = peSm?.getGrade(record.student.stdClass) ?: "-"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(peRowBg)
                        .border(0.5.dp, peBorderColor)
                        .padding(vertical = 6.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("6", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = peTextColor, textAlign = TextAlign.Center, modifier = Modifier.weight(0.6f))
                    Row(
                        modifier = Modifier.weight(2f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Subject.PE.tamilName,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = peTextColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(தனி மதிப்பெண்)",
                            fontSize = 9.5.sp,
                            color = peTextColor.copy(alpha = 0.8f)
                        )
                    }
                    Text("100", fontSize = 11.sp, color = peTextColor, textAlign = TextAlign.Center, modifier = Modifier.weight(1.1f))
                    Text(peAvg, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = peTextColor, textAlign = TextAlign.Center, modifier = Modifier.weight(1.3f))
                    Text(peGrade, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = peTextColor, textAlign = TextAlign.Center, modifier = Modifier.weight(0.8f))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Attendance & Result
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = attendanceBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, attendanceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "பள்ளி மொத்த வேலை நாட்கள்:", fontSize = 11.5.sp, color = TextDark)
                        Text(text = "${record.totalWorkingDays} நாட்கள்", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = TextNavy)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "மாணவர் வருகை புரிந்த நாட்கள்:", fontSize = 11.5.sp, color = TextDark)
                        val pct = String.format("%.1f", record.attendancePercentage)
                        Text(text = "${record.totalPresentDays} நாட்கள் ($pct%)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = TextNavy)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "ஆண்டு இறுதி முடிவு (Result):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        val resColor = if (record.resultStatus == "தேர்ச்சி") EmeraldPass else Color(0xFFDC2626)
                        val resLabel = if (record.resultStatus == "தேர்ச்சி") "தேர்ச்சி (PROMOTED)" else "ஊக்கப்படுத்தல் தேவை"
                        Text(text = resLabel, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = resColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Signature Lines
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "தேதி: .................", fontSize = 10.5.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "வகுப்பாசிரியர்", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextNavy)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = stampBg,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "முத்திரை", fontSize = 9.sp, color = TextDark)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = school.headmasterName, fontSize = 10.5.sp, color = TextDark)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "தலைமை ஆசிரியர்", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextNavy)
                }
            }
        }
    }
}
