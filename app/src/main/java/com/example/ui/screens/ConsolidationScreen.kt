package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.ui.dialogs.ClassPromotionDialog
import com.example.ui.dialogs.TermRankCardDialog
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.util.ExcelReportGenerator
import com.example.util.PdfReportGenerator
import com.example.viewmodel.SchoolMarksViewModel

@Composable
fun ConsolidationScreen(
    viewModel: SchoolMarksViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val schoolProfile by viewModel.schoolProfile.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()

    var groupMode by remember { mutableIntStateOf(0) } // 0: தனி வகுப்பு, 1: வகுப்பு 1-3, 2: வகுப்பு 4-5, 3: வகுப்பு 6-7, 4: வகுப்பு 8
    var groupRecordsMap by remember { mutableStateOf<Map<Int, List<StudentConsolidatedRecord>>>(emptyMap()) }
    var records by remember { mutableStateOf<List<StudentConsolidatedRecord>>(emptyList()) }
    var viewModeTab by remember { mutableIntStateOf(0) } // 0: முப்பருவ சராசரி, 1: பருவம் 1, 2: பருவம் 2, 3: பருவம் 3
    var selectedPrimarySubject by remember(selectedClass, viewModeTab) { mutableStateOf<Subject?>(null) }

    var showPromotionDialog by remember { mutableStateOf(false) }
    var showRankCardDialog by remember { mutableStateOf(false) }
    var selectedRankCardRecord by remember { mutableStateOf<StudentConsolidatedRecord?>(null) }

    // Refresh records when class, students, or groupMode change
    LaunchedEffect(selectedClass, allStudents, groupMode) {
        if (groupMode == 1) {
            val map = mutableMapOf<Int, List<StudentConsolidatedRecord>>()
            for (c in 1..3) {
                map[c] = viewModel.loadConsolidatedRecordsForClass(c)
            }
            groupRecordsMap = map
            records = map.values.flatten()
        } else if (groupMode == 2) {
            val map = mutableMapOf<Int, List<StudentConsolidatedRecord>>()
            for (c in 4..5) {
                map[c] = viewModel.loadConsolidatedRecordsForClass(c)
            }
            groupRecordsMap = map
            records = map.values.flatten()
        } else if (groupMode == 3) {
            val map = mutableMapOf<Int, List<StudentConsolidatedRecord>>()
            for (c in 6..7) {
                map[c] = viewModel.loadConsolidatedRecordsForClass(c)
            }
            groupRecordsMap = map
            records = map.values.flatten()
        } else if (groupMode == 4) {
            viewModel.selectClass(8)
            records = viewModel.loadConsolidatedRecordsForClass(8)
            groupRecordsMap = mapOf(8 to records)
        } else {
            records = viewModel.loadConsolidatedRecordsForClass(selectedClass)
            groupRecordsMap = mapOf(selectedClass to records)
        }
    }

    val subjects = when (groupMode) {
        1 -> listOf(Subject.TAMIL, Subject.ENGLISH, Subject.MATHS)
        2, 3 -> listOf(Subject.TAMIL, Subject.ENGLISH, Subject.MATHS, Subject.SCIENCE, Subject.SOCIAL)
        4 -> Subject.getSubjectsForClass(8)
        else -> Subject.getSubjectsForClass(selectedClass)
    }
    val isPrimary = if (groupMode in 1..3) groupMode == 1 else (if (groupMode == 4) false else selectedClass in 1..3)

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("consolidation_screen")
    ) {
        // Class Group Selection Mode (காகிதம் சிக்கனம் / வகுப்பு குழுக்கள்)
        Surface(
            color = Color(0xFFF8FAFC),
            border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
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
                Text(
                    text = "வகுப்பு தேர்வு:",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )

                FilterChip(
                    selected = groupMode == 0,
                    onClick = { groupMode = 0 },
                    label = { Text("தனி வகுப்பு (1-8)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp)
                )

                FilterChip(
                    selected = groupMode == 1,
                    onClick = { groupMode = 1 },
                    label = { Text("வகுப்பு 1 - 3", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF047857),
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp)
                )

                FilterChip(
                    selected = groupMode == 2,
                    onClick = { groupMode = 2 },
                    label = { Text("வகுப்பு 4 - 5", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2563EB),
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp)
                )

                FilterChip(
                    selected = groupMode == 3,
                    onClick = { groupMode = 3 },
                    label = { Text("வகுப்பு 6 - 7", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF7C3AED),
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp)
                )

                FilterChip(
                    selected = groupMode == 4,
                    onClick = {
                        groupMode = 4
                        viewModel.selectClass(8)
                    },
                    label = { Text("வகுப்பு 8 (சான்றிதழ்)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFB45309),
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }

        // Individual Class Selector if in individual mode
        if (groupMode == 0) {
            ClassChipsSelector(
                selectedClass = selectedClass,
                onSelectClass = { viewModel.selectClass(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        } else if (groupMode == 1) {
            Surface(
                color = Color(0xFFECFDF5),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🌱 வகுப்பு 1, 2, 3 ஒருங்கிணைந்த பட்டியல் (தமிழ், ஆங்கிலம், கணிதம் • மொத்தம்: 300) — காகித சிக்கனம்",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF065F46),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        } else if (groupMode == 2) {
            Surface(
                color = Color(0xFFEFF6FF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📘 வகுப்பு 4, 5 ஒருங்கிணைந்த பட்டியல் (5 பாடங்கள் • மொத்தம்: 500) — காகித சிக்கனம்",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E40AF),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        } else if (groupMode == 3) {
            Surface(
                color = Color(0xFFF5F3FF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📙 வகுப்பு 6, 7 ஒருங்கிணைந்த பட்டியல் (5 பாடங்கள் • மொத்தம்: 500) — காகித சிக்கனம்",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF5B21B6),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        } else if (groupMode == 4) {
            Surface(
                color = Color(0xFFFFFBEB),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🎓 எட்டாம் வகுப்பு (முப்பருவ சராசரி • உடற்கல்வி தனியாகக் கணக்கீடு • ஆண்டு இறுதி சான்றிதழ்)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF92400E),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        // View Mode Tabs
        val tabs = listOf("முப்பருவ சராசரி", "பருவம் 1", "பருவம் 2", "பருவம் 3")
        TabRow(
            selectedTabIndex = viewModeTab,
            containerColor = Color.White,
            contentColor = NavyPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = viewModeTab == index,
                    onClick = {
                        viewModeTab = index
                        selectedPrimarySubject = null
                    },
                    text = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (viewModeTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Sub-filter for Class 1-3 when a specific term is selected
        if (isPrimary && viewModeTab > 0) {
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "பார்வை வடிவம்:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )

                    FilterChip(
                        selected = selectedPrimarySubject == null,
                        onClick = { selectedPrimarySubject = null },
                        label = { Text("அனைத்துப் பாடங்கள்", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )

                    subjects.forEach { sub ->
                        FilterChip(
                            selected = selectedPrimarySubject == sub,
                            onClick = { selectedPrimarySubject = sub },
                            label = { Text("${sub.tamilName} (பதிவேடு)", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFB91C1C),
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }
            }
        }

        // Export Controls Bar
        Surface(
            color = Color(0xFFF1F5F9),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = when (groupMode) {
                        1 -> "வகுப்பு 1 - 3 ஒருங்கிணைந்த ${tabs[viewModeTab]} பட்டியல்"
                        2 -> "வகுப்பு 4 - 5 ஒருங்கிணைந்த ${tabs[viewModeTab]} பட்டியல்"
                        3 -> "வகுப்பு 6 - 7 ஒருங்கிணைந்த ${tabs[viewModeTab]} பட்டியல்"
                        4 -> "வகுப்பு 8 ${tabs[viewModeTab]} பட்டியல்"
                        else -> if (selectedPrimarySubject != null) {
                            "${selectedPrimarySubject!!.tamilName} - ${tabs[viewModeTab]} பதிவேடு"
                        } else if (viewModeTab == 0) {
                            "முப்பருவ சராசரி பட்டியல்"
                        } else {
                            "${tabs[viewModeTab]} பட்டியல்"
                        }
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NavyDark
                )
                Text(
                    text = when (groupMode) {
                        1 -> "வகுப்பு 1-3 மொத்தம்: ${records.size} மாணவர்கள் • காகித சிக்கன முறை"
                        2 -> "வகுப்பு 4-5 மொத்தம்: ${records.size} மாணவர்கள் • காகித சிக்கன முறை"
                        3 -> "வகுப்பு 6-7 மொத்தம்: ${records.size} மாணவர்கள் • காகித சிக்கன முறை"
                        4 -> "வகுப்பு 8 மொத்தம்: ${records.size} மாணவர்கள் • சான்றிதழ் பிரிவு"
                        else -> "வகுப்பு: $selectedClass • ${records.size} மாணவர்கள்"
                    },
                    fontSize = 11.5.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Landscape orientation button row with compact button heights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small / Landscape green Excel button
                    Button(
                        onClick = {
                            if (groupMode == 1) {
                                viewModel.exportCombinedGroupExcel(context, "வகுப்பு 1 - 3 (தொடக்கப் பிரிவு)", listOf(1, 2, 3), viewModeTab) { file ->
                                    Toast.makeText(context, "வகுப்பு 1-3 Excel தயார்", Toast.LENGTH_SHORT).show()
                                    ExcelReportGenerator.shareCsvFile(context, file, "${schoolProfile.schoolName} வகுப்பு 1-3 ஒருங்கிணைந்த Excel")
                                }
                            } else if (groupMode == 2) {
                                viewModel.exportCombinedGroupExcel(context, "வகுப்பு 4 - 5", listOf(4, 5), viewModeTab) { file ->
                                    Toast.makeText(context, "வகுப்பு 4-5 Excel தயார்", Toast.LENGTH_SHORT).show()
                                    ExcelReportGenerator.shareCsvFile(context, file, "${schoolProfile.schoolName} வகுப்பு 4-5 ஒருங்கிணைந்த Excel")
                                }
                            } else if (groupMode == 3) {
                                viewModel.exportCombinedGroupExcel(context, "வகுப்பு 6 - 7", listOf(6, 7), viewModeTab) { file ->
                                    Toast.makeText(context, "வகுப்பு 6-7 Excel தயார்", Toast.LENGTH_SHORT).show()
                                    ExcelReportGenerator.shareCsvFile(context, file, "${schoolProfile.schoolName} வகுப்பு 6-7 ஒருங்கிணைந்த Excel")
                                }
                            } else if (selectedPrimarySubject != null) {
                                viewModel.exportClass1To3SubjectCsv(context, selectedClass, viewModeTab, selectedPrimarySubject!!) { file ->
                                    Toast.makeText(context, "Excel CSV பதிவிறக்கம் தயார்", Toast.LENGTH_SHORT).show()
                                    ExcelReportGenerator.shareCsvFile(context, file, "${selectedPrimarySubject!!.tamilName} பதிவேடு")
                                }
                            } else if (viewModeTab == 0) {
                                val targetClass = if (groupMode == 4) 8 else selectedClass
                                viewModel.exportConsolidatedExcel(context, targetClass) { file ->
                                    Toast.makeText(context, "Excel CSV பதிவிறக்கம் தயார்", Toast.LENGTH_SHORT).show()
                                    ExcelReportGenerator.shareCsvFile(context, file, "${schoolProfile.schoolName} முப்பருவ சராசரி")
                                }
                            } else {
                                val targetClass = if (groupMode == 4) 8 else selectedClass
                                viewModel.exportTermExcel(context, targetClass, viewModeTab) { file ->
                                    Toast.makeText(context, "Excel CSV பதிவிறக்கம் தயார்", Toast.LENGTH_SHORT).show()
                                    ExcelReportGenerator.shareCsvFile(context, file, "${schoolProfile.schoolName} பருவம் $viewModeTab")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPass),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("consolidation_excel_btn")
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Excel பதிவிறக்கம்", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    // PDF Button
                    Button(
                        onClick = {
                            if (groupMode == 1) {
                                viewModel.exportCombinedGroupPdf(context, "வகுப்பு 1 - 3 (தொடக்கப் பிரிவு)", listOf(1, 2, 3), viewModeTab) { file ->
                                    Toast.makeText(context, "வகுப்பு 1-3 ஒருங்கிணைந்த PDF தயார்", Toast.LENGTH_SHORT).show()
                                    PdfReportGenerator.sharePdfFile(context, file, "${schoolProfile.schoolName} வகுப்பு 1-3 ஒருங்கிணைந்த பதிவேடு")
                                }
                            } else if (groupMode == 2) {
                                viewModel.exportCombinedGroupPdf(context, "வகுப்பு 4 - 5", listOf(4, 5), viewModeTab) { file ->
                                    Toast.makeText(context, "வகுப்பு 4-5 ஒருங்கிணைந்த PDF தயார்", Toast.LENGTH_SHORT).show()
                                    PdfReportGenerator.sharePdfFile(context, file, "${schoolProfile.schoolName} வகுப்பு 4-5 ஒருங்கிணைந்த பதிவேடு")
                                }
                            } else if (groupMode == 3) {
                                viewModel.exportCombinedGroupPdf(context, "வகுப்பு 6 - 7", listOf(6, 7), viewModeTab) { file ->
                                    Toast.makeText(context, "வகுப்பு 6-7 ஒருங்கிணைந்த PDF தயார்", Toast.LENGTH_SHORT).show()
                                    PdfReportGenerator.sharePdfFile(context, file, "${schoolProfile.schoolName} வகுப்பு 6-7 ஒருங்கிணைந்த பதிவேடு")
                                }
                            } else if (selectedPrimarySubject != null) {
                                viewModel.exportClass1To3SubjectPdf(context, selectedClass, viewModeTab, selectedPrimarySubject!!) { file ->
                                    Toast.makeText(context, "PDF அறிக்கை தயார்", Toast.LENGTH_SHORT).show()
                                    PdfReportGenerator.sharePdfFile(context, file, "${selectedPrimarySubject!!.tamilName} பதிவேடு PDF")
                                }
                            } else {
                                val targetClass = if (groupMode == 4) 8 else selectedClass
                                viewModel.exportConsolidatedPdf(context, targetClass) { file ->
                                    Toast.makeText(context, "PDF அறிக்கை தயார்", Toast.LENGTH_SHORT).show()
                                    PdfReportGenerator.sharePdfFile(context, file, "${schoolProfile.schoolName} முப்பருவ சராசரி பதிவேடு")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("consolidation_pdf_btn")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (groupMode in 1..3) "ஒருங்கிணைந்த PDF" else "PDF பதிவிறக்கம்", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    // A4 Rank Card quick action for selected term
                    if (viewModeTab in 1..3 && records.isNotEmpty() && groupMode == 0) {
                        Button(
                            onClick = {
                                viewModel.exportClassAllTermRankCardsPdf(context, records, viewModeTab) { file ->
                                    PdfReportGenerator.printPdfFile(context, file)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("consolidation_rank_cards_btn")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp), tint = NavyDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("A4 அட்டைகள்", fontSize = 11.5.sp, color = NavyDark, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Promotion Banner when in Term 3 or Annual Consolidated view
        if (viewModeTab == 3 || viewModeTab == 0) {
            Surface(
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = Color(0xFF92400E),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "3-ம் பருவத் தேர்வு முடிவுகள்: அடுத்த வகுப்பிற்கு மாணவர் உயர்வு (Promotion)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "தேர்ச்சி பெற்ற மாணவர்களை அடுத்த வகுப்பிற்கும், 8-ம் வகுப்பு தேர்ச்சி நிலையும் பதிவு செய்யலாம்.",
                                fontSize = 10.5.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                    }

                    Button(
                        onClick = { showPromotionDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("promotion_banner_btn")
                    ) {
                        Text("வகுப்பு உயர்வு", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "வகுப்பு $selectedClass -ல் மாணவர்கள் பதிவு செய்யப்படவில்லை.",
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
            }
        } else {
            // Horizontal scrollable table container
            val horizontalScrollState = rememberScrollState()

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .horizontalScroll(horizontalScrollState)
                                    .padding(8.dp)
                            ) {
                                if (selectedPrimarySubject != null) {
                                    // Official Register Format for Class 1-3
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (schoolProfile.udiseCode.isNotBlank()) "${schoolProfile.schoolName} (UDISE: ${schoolProfile.udiseCode})" else schoolProfile.schoolName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = NavyPrimary
                                        )
                                        Text(
                                            text = "2026-27 மாணவர் மதிப்பெண் பதிவேடு பருவம் : $viewModeTab",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFFB91C1C)
                                        )
                                        Text(
                                            text = "புதிய பாடத்திட்டம் வகுப்பு : $selectedClass     பாடம் : ${selectedPrimarySubject!!.tamilName} (${selectedPrimarySubject!!.name})",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF1E3A8A)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Class1To3OfficialHeader()

                                    records.forEachIndexed { idx, record ->
                                        val sm = record.subjectMarks[selectedPrimarySubject!!]
                                        val m = when (viewModeTab) {
                                            1 -> sm?.term1Marks
                                            2 -> sm?.term2Marks
                                            3 -> sm?.term3Marks
                                            else -> null
                                        }

                                        Class1To3OfficialStudentRow(
                                            sNo = idx + 1,
                                            admissionNo = record.student.admissionNo,
                                            name = record.student.name,
                                            naney1Oral = m?.naney1Oral ?: 0,
                                            naney1Act = m?.naney1Activity ?: 0,
                                            naney1Wri = m?.naney1Written ?: 0,
                                            naney2Oral = m?.naney2Oral ?: 0,
                                            naney2Act = m?.naney2Activity ?: 0,
                                            naney2Wri = m?.naney2Written ?: 0,
                                            thiranariOral = m?.thiranariOral ?: 0,
                                            thiranariWri = m?.thiranariWritten ?: 0,
                                            total = m?.total ?: 0
                                        )
                                    }
                                } else {
                                    // Multi-subject Consolidated View
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (schoolProfile.udiseCode.isNotBlank()) "${schoolProfile.schoolName} (UDISE: ${schoolProfile.udiseCode})" else schoolProfile.schoolName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = NavyPrimary
                                        )
                                        Text(
                                            text = if (schoolProfile.udiseCode.isNotBlank()) "${schoolProfile.unionName}   |   ${schoolProfile.districtName}   |   UDISE: ${schoolProfile.udiseCode}" else "${schoolProfile.unionName}   |   ${schoolProfile.districtName}",
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = when (groupMode) {
                                                1 -> if (viewModeTab == 0) "முப்பருவ சராசரி மதிப்பெண் பட்டியல் (வகுப்பு 1 - 3)" else "${tabs[viewModeTab]} மதிப்பெண் பட்டியல் (வகுப்பு 1 - 3)"
                                                2 -> if (viewModeTab == 0) "முப்பருவ சராசரி மதிப்பெண் பட்டியல் (வகுப்பு 4 - 5)" else "${tabs[viewModeTab]} மதிப்பெண் பட்டியல் (வகுப்பு 4 - 5)"
                                                3 -> if (viewModeTab == 0) "முப்பருவ சராசரி மதிப்பெண் பட்டியல் (வகுப்பு 6 - 7)" else "${tabs[viewModeTab]} மதிப்பெண் பட்டியல் (வகுப்பு 6 - 7)"
                                                4 -> if (viewModeTab == 0) "முப்பருவ சராசரி மதிப்பெண் பட்டியல் (வகுப்பு 8)" else "${tabs[viewModeTab]} மதிப்பெண் பட்டியல் (வகுப்பு 8)"
                                                else -> if (viewModeTab == 0) "முப்பருவ சராசரி மதிப்பெண் பட்டியல் (வகுப்பு $selectedClass)" else "${tabs[viewModeTab]} மதிப்பெண் பட்டியல் (வகுப்பு $selectedClass)"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = AmberGold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    if (groupMode in 1..3) {
                                        TableHeader(subjects = subjects, isAverageMode = viewModeTab == 0, isClass1To3 = groupMode == 1)

                                        var sNoCounter = 1
                                        val classesInGroup = when (groupMode) {
                                            1 -> listOf(1, 2, 3)
                                            2 -> listOf(4, 5)
                                            3 -> listOf(6, 7)
                                            else -> emptyList()
                                        }
                                        classesInGroup.forEach { cls ->
                                            val classRecs = groupRecordsMap[cls] ?: emptyList()
                                            if (classRecs.isNotEmpty()) {
                                                // Class Divider Banner Row
                                                Surface(
                                                    color = if (cls % 2 == 1) Color(0xFFE0E7FF) else Color(0xFFFEF3C7),
                                                    border = BorderStroke(0.5.dp, Color.LightGray),
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "★ வகுப்பு $cls (CLASS $cls) — ${classRecs.size} மாணவர்கள் ★",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = NavyDark,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                                    )
                                                }

                                                classRecs.forEach { record ->
                                                    StudentRow(
                                                        sNo = sNoCounter++,
                                                        record = record,
                                                        subjects = subjects,
                                                        viewMode = viewModeTab,
                                                        isClass1To3 = groupMode == 1,
                                                        onRowClick = {
                                                            selectedRankCardRecord = record
                                                            showRankCardDialog = true
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // Standard Single Class
                                        TableHeader(subjects = subjects, isAverageMode = viewModeTab == 0, isClass1To3 = selectedClass in 1..3)

                                        // Student Rows
                                        records.forEachIndexed { idx, record ->
                                            StudentRow(
                                                sNo = idx + 1,
                                                record = record,
                                                subjects = subjects,
                                                viewMode = viewModeTab,
                                                isClass1To3 = selectedClass in 1..3,
                                                onRowClick = {
                                                    selectedRankCardRecord = record
                                                    showRankCardDialog = true
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Rank Card Dialog
    if (showRankCardDialog && selectedRankCardRecord != null) {
        val targetTerm = if (viewModeTab in 1..3) viewModeTab else 1
        val ranksMap = viewModel.calculateClassRanks(records, targetTerm)
        val rank = ranksMap[selectedRankCardRecord!!.student.id] ?: 1

        TermRankCardDialog(
            record = selectedRankCardRecord!!,
            school = schoolProfile,
            term = targetTerm,
            rank = rank,
            totalClassStudents = records.size,
            onDismiss = { showRankCardDialog = false },
            onPrintPdf = {
                viewModel.exportTermRankCardPdf(context, selectedRankCardRecord!!, targetTerm, rank, records.size) {
                    PdfReportGenerator.printPdfFile(context, it)
                }
            },
            onSharePdf = {
                viewModel.exportTermRankCardPdf(context, selectedRankCardRecord!!, targetTerm, rank, records.size) {
                    PdfReportGenerator.sharePdfFile(context, it)
                }
            },
            onViewPdf = {
                viewModel.exportTermRankCardPdf(context, selectedRankCardRecord!!, targetTerm, rank, records.size) {
                    PdfReportGenerator.viewPdfFile(context, it)
                }
            },
            onPrintAllClassPdf = {
                viewModel.exportClassAllTermRankCardsPdf(context, records, targetTerm) {
                    PdfReportGenerator.printPdfFile(context, it)
                }
            }
        )
    }

    // Class Promotion Dialog
    if (showPromotionDialog) {
        ClassPromotionDialog(
            allStudents = allStudents,
            currentAcademicYear = schoolProfile.academicYear,
            onDismiss = { showPromotionDialog = false },
            onConfirmPromotion = { promoteAll, targetClass, newYear, archiveClass8 ->
                viewModel.promoteStudents(
                    promoteAll = promoteAll,
                    targetClass = targetClass,
                    newAcademicYear = newYear,
                    archiveClass8 = archiveClass8
                ) { count ->
                    showPromotionDialog = false
                }
            }
        )
    }
}

@Composable
fun TableHeader(
    subjects: List<Subject>,
    isAverageMode: Boolean,
    isClass1To3: Boolean
) {
    val borderColor = Color.LightGray
    val thBg = Color(0xFFEEF2FF)

    val mainSubjects = remember(subjects) { subjects.filter { it != Subject.PE } }
    val hasPe = remember(subjects) { subjects.contains(Subject.PE) }

    Column(
        modifier = Modifier
            .background(thBg)
            .border(1.dp, borderColor)
    ) {
        // Top row
        Row(
            modifier = Modifier.height(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableCell(text = "வ.எண்", width = 36.dp, isHeader = true)
            TableCell(text = "சே.எண்", width = 54.dp, isHeader = true)
            TableCell(text = "மாணவர் பெயர்", width = 130.dp, isHeader = true)
            TableCell(text = "இனம்", width = 42.dp, isHeader = true)

            mainSubjects.forEach { sub ->
                val w = if (isClass1To3 && !isAverageMode) 140.dp else 105.dp
                TableCell(text = sub.shortName, width = w, isHeader = true)
            }

            TableCell(text = "TOTAL", width = 56.dp, isHeader = true)

            if (hasPe) {
                TableCell(text = Subject.PE.shortName, width = 105.dp, isHeader = true)
            }

            TableCell(text = "வேலை\nநாட்கள்", width = 50.dp, isHeader = true)
            TableCell(text = "வருகை\nநாட்கள்", width = 50.dp, isHeader = true)
            TableCell(text = "தேர்ச்சி விபரம்", width = 80.dp, isHeader = true)
        }

        // Sub-header row for subjects: SA, FA, மொ
        Row(
            modifier = Modifier
                .height(24.dp)
                .background(Color(0xFFE0E7FF)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableCell(text = "", width = 36.dp)
            TableCell(text = "", width = 54.dp)
            TableCell(text = "", width = 130.dp)
            TableCell(text = "", width = 42.dp)

            mainSubjects.forEach { _ ->
                if (isClass1To3 && !isAverageMode) {
                    TableCell(text = "FA(a)", width = 35.dp, isSubHeader = true)
                    TableCell(text = "FA(b)", width = 35.dp, isSubHeader = true)
                    TableCell(text = "SA", width = 35.dp, isSubHeader = true)
                    TableCell(text = "மொ", width = 35.dp, isSubHeader = true)
                } else {
                    TableCell(text = "SA", width = 35.dp, isSubHeader = true)
                    TableCell(text = "FA", width = 35.dp, isSubHeader = true)
                    TableCell(text = "மொ", width = 35.dp, isSubHeader = true)
                }
            }

            TableCell(text = "", width = 56.dp)

            if (hasPe) {
                TableCell(text = "SA", width = 35.dp, isSubHeader = true)
                TableCell(text = "FA", width = 35.dp, isSubHeader = true)
                TableCell(text = "மொ", width = 35.dp, isSubHeader = true)
            }

            TableCell(text = "", width = 50.dp)
            TableCell(text = "", width = 50.dp)
            TableCell(text = "", width = 80.dp)
        }
    }
}

@Composable
fun StudentRow(
    sNo: Int,
    record: StudentConsolidatedRecord,
    subjects: List<Subject>,
    viewMode: Int,
    isClass1To3: Boolean,
    onRowClick: () -> Unit = {}
) {
    val mainSubjects = remember(subjects) { subjects.filter { it != Subject.PE } }
    val hasPe = remember(subjects) { subjects.contains(Subject.PE) }

    val borderColor = Color(0xFFE2E8F0)
    val rowBg = if (sNo % 2 == 0) Color(0xFFFAFAFA) else Color.White

    Row(
        modifier = Modifier
            .background(rowBg)
            .border(0.5.dp, borderColor)
            .clickable { onRowClick() }
            .height(26.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(text = "$sNo", width = 36.dp)
        TableCell(text = record.student.admissionNo, width = 54.dp, isBold = true)
        TableCell(text = record.student.name, width = 130.dp, textAlign = TextAlign.Start, isBold = true)
        TableCell(text = record.student.community, width = 42.dp)

        var mainTotal = 0

        mainSubjects.forEach { sub ->
            val sm = record.subjectMarks[sub]
            if (viewMode == 0) {
                // Average
                val sa = sm?.avgSa ?: 0
                val fa = sm?.avgFa ?: 0
                val tot = sm?.avgTotal ?: 0
                mainTotal += tot

                TableCell(text = "$sa", width = 35.dp)
                TableCell(text = "$fa", width = 35.dp)
                TableCell(text = "$tot", width = 35.dp, isBold = true, textColor = NavyPrimary)
            } else {
                // Single term (1, 2, 3)
                val m = when (viewMode) {
                    1 -> sm?.term1Marks
                    2 -> sm?.term2Marks
                    3 -> sm?.term3Marks
                    else -> null
                }
                val tot = m?.total ?: 0
                mainTotal += tot

                if (isClass1To3) {
                    TableCell(text = "${m?.faA ?: 0}", width = 35.dp)
                    TableCell(text = "${m?.faB ?: 0}", width = 35.dp)
                    TableCell(text = "${m?.sa ?: 0}", width = 35.dp)
                    TableCell(text = "$tot", width = 35.dp, isBold = true, textColor = NavyPrimary)
                } else {
                    TableCell(text = "${m?.sa ?: 0}", width = 35.dp)
                    TableCell(text = "${m?.faTotal ?: 0}", width = 35.dp)
                    TableCell(text = "$tot", width = 35.dp, isBold = true, textColor = NavyPrimary)
                }
            }
        }

        // TOTAL for 5 main subjects only (excluding PE)
        TableCell(text = "$mainTotal", width = 56.dp, isBold = true, textColor = NavyDark)

        // PE marks placed AFTER TOTAL
        if (hasPe) {
            val sm = record.subjectMarks[Subject.PE]
            if (viewMode == 0) {
                val sa = sm?.avgSa ?: 0
                val fa = sm?.avgFa ?: 0
                val tot = sm?.avgTotal ?: 0
                TableCell(text = "$sa", width = 35.dp)
                TableCell(text = "$fa", width = 35.dp)
                TableCell(text = "$tot", width = 35.dp, isBold = true, textColor = AmberGold)
            } else {
                val m = when (viewMode) {
                    1 -> sm?.term1Marks
                    2 -> sm?.term2Marks
                    3 -> sm?.term3Marks
                    else -> null
                }
                val tot = m?.total ?: 0
                TableCell(text = "${m?.sa ?: 0}", width = 35.dp)
                TableCell(text = "${m?.faTotal ?: 0}", width = 35.dp)
                TableCell(text = "$tot", width = 35.dp, isBold = true, textColor = AmberGold)
            }
        }

        val workDays = if (viewMode == 0) record.totalWorkingDays else {
            when (viewMode) {
                1 -> record.term1Attendance?.totalWorkingDays ?: 0
                2 -> record.term2Attendance?.totalWorkingDays ?: 0
                3 -> record.term3Attendance?.totalWorkingDays ?: 0
                else -> 0
            }
        }

        val presentDays = if (viewMode == 0) record.totalPresentDays else {
            when (viewMode) {
                1 -> record.term1Attendance?.presentDays ?: 0
                2 -> record.term2Attendance?.presentDays ?: 0
                3 -> record.term3Attendance?.presentDays ?: 0
                else -> 0
            }
        }

        TableCell(text = "$workDays", width = 50.dp)
        TableCell(text = "$presentDays", width = 50.dp)

        val resColor = if (record.resultStatus == "தேர்ச்சி") EmeraldPass else Color(0xFFDC2626)
        TableCell(text = record.resultStatus, width = 80.dp, isBold = true, textColor = resColor)
    }
}

@Composable
fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    isSubHeader: Boolean = false,
    isBold: Boolean = false,
    textAlign: TextAlign = TextAlign.Center,
    textColor: Color = Color.Black
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 2.dp),
        contentAlignment = when (textAlign) {
            TextAlign.Start -> Alignment.CenterStart
            TextAlign.End -> Alignment.CenterEnd
            else -> Alignment.Center
        }
    ) {
        Text(
            text = text,
            fontSize = if (isHeader) 10.5.sp else if (isSubHeader) 9.sp else 10.sp,
            fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isHeader) NavyDark else textColor,
            textAlign = textAlign,
            maxLines = 2,
            lineHeight = 12.sp
        )
    }
}

@Composable
fun Class1To3OfficialHeader() {
    val borderColor = Color.LightGray
    val thBg = Color(0xFFEEF2FF)

    Column(
        modifier = Modifier
            .background(thBg)
            .border(1.dp, borderColor)
    ) {
        // Level 1: Categories
        Row(
            modifier = Modifier.height(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableCell(text = "வ.எண்", width = 36.dp, isHeader = true)
            TableCell(text = "சே.எண்", width = 54.dp, isHeader = true)
            TableCell(text = "மாணவர் பெயர்", width = 130.dp, isHeader = true)
            TableCell(text = "நானே செய்வேன் - 1 [25]", width = 195.dp, isHeader = true, textColor = Color(0xFF991B1B))
            TableCell(text = "நானே செய்வேன் - 2 [25]", width = 195.dp, isHeader = true, textColor = Color(0xFF991B1B))
            TableCell(text = "திறனறி மதிப்பீடு [50]", width = 140.dp, isHeader = true, textColor = Color(0xFF92400E))
            TableCell(text = "மொத்தம்", width = 56.dp, isHeader = true)
            TableCell(text = "விழுக்காடு", width = 56.dp, isHeader = true)
        }

        // Level 2: Sub-columns
        Row(
            modifier = Modifier
                .height(24.dp)
                .background(Color(0xFFE0E7FF)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableCell(text = "", width = 36.dp)
            TableCell(text = "", width = 54.dp)
            TableCell(text = "", width = 130.dp)

            // Naney 1 sub-columns
            TableCell(text = "வாய்மொழி [10]", width = 65.dp, isSubHeader = true)
            TableCell(text = "செயல்பாடு [10]", width = 65.dp, isSubHeader = true)
            TableCell(text = "எழுத்து [5]", width = 65.dp, isSubHeader = true)

            // Naney 2 sub-columns
            TableCell(text = "வாய்மொழி [10]", width = 65.dp, isSubHeader = true)
            TableCell(text = "செயல்பாடு [10]", width = 65.dp, isSubHeader = true)
            TableCell(text = "எழுத்து [5]", width = 65.dp, isSubHeader = true)

            // Thiranari sub-columns
            TableCell(text = "வாய்மொழி [10]", width = 70.dp, isSubHeader = true)
            TableCell(text = "எழுத்து [40]", width = 70.dp, isSubHeader = true)

            TableCell(text = "100", width = 56.dp, isSubHeader = true, isBold = true)
            TableCell(text = "%", width = 56.dp, isSubHeader = true, isBold = true)
        }
    }
}

@Composable
fun Class1To3OfficialStudentRow(
    sNo: Int,
    admissionNo: String,
    name: String,
    naney1Oral: Int,
    naney1Act: Int,
    naney1Wri: Int,
    naney2Oral: Int,
    naney2Act: Int,
    naney2Wri: Int,
    thiranariOral: Int,
    thiranariWri: Int,
    total: Int
) {
    val borderColor = Color(0xFFE2E8F0)
    val rowBg = if (sNo % 2 == 0) Color(0xFFFAFAFA) else Color.White

    Row(
        modifier = Modifier
            .background(rowBg)
            .border(0.5.dp, borderColor)
            .height(26.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(text = "$sNo", width = 36.dp)
        TableCell(text = admissionNo, width = 54.dp, isBold = true)
        TableCell(text = name, width = 130.dp, textAlign = TextAlign.Start, isBold = true)

        TableCell(text = "$naney1Oral", width = 65.dp)
        TableCell(text = "$naney1Act", width = 65.dp)
        TableCell(text = "$naney1Wri", width = 65.dp)

        TableCell(text = "$naney2Oral", width = 65.dp)
        TableCell(text = "$naney2Act", width = 65.dp)
        TableCell(text = "$naney2Wri", width = 65.dp)

        TableCell(text = "$thiranariOral", width = 70.dp)
        TableCell(text = "$thiranariWri", width = 70.dp)

        TableCell(text = "$total", width = 56.dp, isBold = true, textColor = NavyDark)
        TableCell(text = "$total%", width = 56.dp, isBold = true, textColor = NavyPrimary)
    }
}
