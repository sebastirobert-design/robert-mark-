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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentConsolidatedRecord
import com.example.data.model.Subject
import com.example.ui.AppNavDestination
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

data class AcademicSection(
    val id: Int,
    val title: String,
    val shortName: String,
    val categoryName: String,
    val description: String,
    val classes: List<Int>,
    val totalMarks: Int,
    val hasPe: Boolean = false,
    val hasCertificate: Boolean = false
)

val ACADEMIC_SECTIONS = listOf(
    AcademicSection(
        id = 1,
        title = "வகுப்பு 1, 2",
        shortName = "வகுப்பு 1, 2",
        categoryName = "வகுப்பு 1, 2",
        description = "4 பாடங்கள்: தமிழ், ஆங்கிலம், கணிதம், சூழ்நிலையியல் (மொத்தம் 400 மதிப்பெண்கள்)",
        classes = listOf(1, 2),
        totalMarks = 400
    ),
    AcademicSection(
        id = 2,
        title = "வகுப்பு 3, 4, 5",
        shortName = "வகுப்பு 3, 4, 5",
        categoryName = "வகுப்பு 3, 4, 5",
        description = "5 பாடங்கள்: தமிழ், ஆங்கிலம், கணிதம், அறிவியல், சமூக அறிவியல் (மொத்தம் 500 மதிப்பெண்கள்)",
        classes = listOf(3, 4, 5),
        totalMarks = 500
    ),
    AcademicSection(
        id = 3,
        title = "வகுப்பு 6, 7",
        shortName = "வகுப்பு 6, 7",
        categoryName = "வகுப்பு 6, 7",
        description = "5 பாடங்கள் (மொத்தம் 500 மதிப்பெண்கள்)",
        classes = listOf(6, 7),
        totalMarks = 500
    ),
    AcademicSection(
        id = 4,
        title = "வகுப்பு 8",
        shortName = "வகுப்பு 8",
        categoryName = "வகுப்பு 8",
        description = "5 பாடங்கள் (ஒவ்வொரு பாடமும் நேரடி 100 மதிப்பெண்) + உடற்கல்வி மற்றும் ஆண்டு இறுதி சான்றிதழ்",
        classes = listOf(8),
        totalMarks = 500,
        hasPe = true,
        hasCertificate = true
    )
)

@Composable
fun ConsolidationScreen(
    viewModel: SchoolMarksViewModel,
    onNavigate: (AppNavDestination) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val schoolProfile by viewModel.schoolProfile.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()

    // Top Mode Toggle: True = முப்பருவப் பிரிவுகள் (1-3, 4-5, 6-7, 8), False = தனி வகுப்பு (1 - 8)
    var isGroupMode by remember { mutableStateOf(true) }
    var selectedSectionIndex by remember { mutableIntStateOf(0) }
    val currentSection = ACADEMIC_SECTIONS[selectedSectionIndex]

    var sectionRecordsMap by remember { mutableStateOf<Map<Int, List<StudentConsolidatedRecord>>>(emptyMap()) }
    var singleClassRecords by remember { mutableStateOf<List<StudentConsolidatedRecord>>(emptyList()) }

    var viewModeTab by remember { mutableIntStateOf(0) } // 0: முப்பருவ சராசரி, 1: பருவம் 1, 2: பருவம் 2, 3: பருவம் 3
    var selectedPrimarySubject by remember(selectedClass, viewModeTab) { mutableStateOf<Subject?>(null) }

    var showPromotionDialog by remember { mutableStateOf(false) }
    var showRankCardDialog by remember { mutableStateOf(false) }
    var selectedRankCardRecord by remember { mutableStateOf<StudentConsolidatedRecord?>(null) }

    // Load data based on active mode
    LaunchedEffect(isGroupMode, selectedSectionIndex, allStudents) {
        if (isGroupMode) {
            sectionRecordsMap = viewModel.loadConsolidatedRecordsForClassGroup(currentSection.classes)
        }
    }

    LaunchedEffect(isGroupMode, selectedClass, allStudents) {
        if (!isGroupMode) {
            singleClassRecords = viewModel.loadConsolidatedRecordsForClass(selectedClass)
        }
    }

    val activeRecords = if (isGroupMode) {
        sectionRecordsMap.values.flatten()
    } else {
        singleClassRecords
    }

    val subjects = if (isGroupMode) {
        when (currentSection.id) {
            1 -> listOf(Subject.TAMIL, Subject.ENGLISH, Subject.MATHS)
            2, 3 -> listOf(Subject.TAMIL, Subject.ENGLISH, Subject.MATHS, Subject.SCIENCE, Subject.SOCIAL)
            4 -> listOf(Subject.TAMIL, Subject.ENGLISH, Subject.MATHS, Subject.SCIENCE, Subject.SOCIAL, Subject.PE)
            else -> Subject.getMainSubjectsForClass(1)
        }
    } else {
        Subject.getSubjectsForClass(selectedClass)
    }

    val isPrimary = if (isGroupMode) currentSection.id == 1 else selectedClass in 1..2
    val isClass3 = if (isGroupMode) false else selectedClass == 3

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("consolidation_screen")
    ) {
        // Mode Switcher: 4 Sections vs Individual Class
        Surface(
            color = Color(0xFFF1F5F9),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isGroupMode,
                    onClick = { isGroupMode = true },
                    label = {
                        Text(
                            "வகுப்புகள் (1,2 • 3,4,5 • 6,7 • 8)",
                            fontSize = 11.5.sp,
                            fontWeight = if (isGroupMode) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = !isGroupMode,
                    onClick = { isGroupMode = false },
                    label = {
                        Text(
                            "தனி வகுப்பு (1-8)",
                            fontSize = 11.5.sp,
                            fontWeight = if (!isGroupMode) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Sub-selector Bar
        if (isGroupMode) {
            // 4 Sections Chips
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
                    ACADEMIC_SECTIONS.forEachIndexed { index, sec ->
                        FilterChip(
                            selected = selectedSectionIndex == index,
                            onClick = { selectedSectionIndex = index },
                            label = {
                                Text(
                                    text = sec.shortName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (sec.id) {
                                    1 -> EmeraldPass
                                    2 -> NavyPrimary
                                    3 -> Color(0xFF0284C7)
                                    else -> AmberGold
                                },
                                selectedLabelColor = if (sec.id == 4) NavyDark else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        } else {
            // Individual Class Selector Chips (1 to 8)
            ClassChipsSelector(
                selectedClass = selectedClass,
                onSelectClass = { viewModel.selectClass(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // View Mode Tabs: Average / Term 1 / Term 2 / Term 3
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

        // Sub-filter for Class 1-3 when single-class and a specific term is selected
        if (!isGroupMode && isPrimary && viewModeTab > 0) {
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
            color = Color(0xFFF8FAFC),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (isGroupMode) {
                    Text(
                        text = "${currentSection.title} - ${tabs[viewModeTab]}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NavyDark
                    )
                    Text(
                        text = "${currentSection.description} • மொத்தம்: ${activeRecords.size} மாணவர்கள்",
                        fontSize = 11.5.sp,
                        color = Color.Gray
                    )
                } else {
                    Text(
                        text = if (selectedPrimarySubject != null) {
                            "${selectedPrimarySubject!!.tamilName} - ${tabs[viewModeTab]} பதிவேடு"
                        } else if (viewModeTab == 0) {
                            "வகுப்பு $selectedClass - முப்பருவ சராசரி பட்டியல்"
                        } else {
                            "வகுப்பு $selectedClass - ${tabs[viewModeTab]} பட்டியல்"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NavyDark
                    )
                    Text(
                        text = "வகுப்பு: $selectedClass • ${activeRecords.size} மாணவர்கள்",
                        fontSize = 11.5.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action button row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Excel (CSV) Button
                    Button(
                        onClick = {
                            if (isGroupMode) {
                                viewModel.exportCombinedGroupExcel(
                                    context = context,
                                    groupTitle = currentSection.title,
                                    classes = currentSection.classes,
                                    term = viewModeTab
                                ) { file ->
                                    Toast.makeText(context, "${currentSection.shortName} Excel தயாராக உள்ளது", Toast.LENGTH_SHORT).show()
                                    ExcelReportGenerator.shareCsvFile(
                                        context,
                                        file,
                                        "${currentSection.title} ஒருங்கிணைந்த பதிவேடு"
                                    )
                                }
                            } else {
                                if (selectedPrimarySubject != null) {
                                    viewModel.exportClass1To3SubjectCsv(context, selectedClass, viewModeTab, selectedPrimarySubject!!) { file ->
                                        Toast.makeText(context, "Excel CSV தயார்", Toast.LENGTH_SHORT).show()
                                        ExcelReportGenerator.shareCsvFile(context, file, "${selectedPrimarySubject!!.tamilName} பதிவேடு")
                                    }
                                } else if (viewModeTab == 0) {
                                    viewModel.exportConsolidatedExcel(context, selectedClass) { file ->
                                        Toast.makeText(context, "Excel CSV தயார்", Toast.LENGTH_SHORT).show()
                                        ExcelReportGenerator.shareCsvFile(context, file, "${schoolProfile.schoolName} முப்பருவ சராசரி")
                                    }
                                } else {
                                    viewModel.exportTermExcel(context, selectedClass, viewModeTab) { file ->
                                        Toast.makeText(context, "Excel CSV தயார்", Toast.LENGTH_SHORT).show()
                                        ExcelReportGenerator.shareCsvFile(context, file, "${schoolProfile.schoolName} பருவம் $viewModeTab")
                                    }
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
                        Text("Excel அவுட்புட்", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    // PDF Button
                    Button(
                        onClick = {
                            if (isGroupMode) {
                                viewModel.exportCombinedGroupPdf(
                                    context = context,
                                    groupTitle = currentSection.title,
                                    classes = currentSection.classes,
                                    term = viewModeTab
                                ) { file ->
                                    Toast.makeText(context, "${currentSection.shortName} PDF தயாராக உள்ளது", Toast.LENGTH_SHORT).show()
                                    PdfReportGenerator.sharePdfFile(
                                        context,
                                        file,
                                        "${currentSection.title} ஒருங்கிணைந்த பதிவேடு"
                                    )
                                }
                            } else {
                                if (selectedPrimarySubject != null) {
                                    viewModel.exportClass1To3SubjectPdf(context, selectedClass, viewModeTab, selectedPrimarySubject!!) { file ->
                                        Toast.makeText(context, "PDF அறிக்கை தயார்", Toast.LENGTH_SHORT).show()
                                        PdfReportGenerator.sharePdfFile(context, file, "${selectedPrimarySubject!!.tamilName} பதிவேடு PDF")
                                    }
                                } else if (viewModeTab > 0) {
                                    viewModel.exportCombinedGroupPdf(
                                        context = context,
                                        groupTitle = "வகுப்பு $selectedClass (பருவம் $viewModeTab)",
                                        classes = listOf(selectedClass),
                                        term = viewModeTab
                                    ) { file ->
                                        Toast.makeText(context, "PDF அறிக்கை தயார்", Toast.LENGTH_SHORT).show()
                                        PdfReportGenerator.sharePdfFile(context, file, "${schoolProfile.schoolName} வகுப்பு $selectedClass பருவம் $viewModeTab பதிவேடு")
                                    }
                                } else {
                                    viewModel.exportConsolidatedPdf(context, selectedClass) { file ->
                                        Toast.makeText(context, "PDF அறிக்கை தயார்", Toast.LENGTH_SHORT).show()
                                        PdfReportGenerator.sharePdfFile(context, file, "${schoolProfile.schoolName} முப்பருவ சராசரி பதிவேடு")
                                    }
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
                        Text("PDF பதிவேடு", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    // For Section 4 (Class 8): Direct Certificate shortcut button
                    if (isGroupMode && currentSection.hasCertificate) {
                        Button(
                            onClick = { onNavigate(AppNavDestination.CERTIFICATE) },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("consolidation_certificate_btn")
                        ) {
                            Icon(
                                Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = NavyDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "சான்றிதழ்",
                                fontSize = 11.5.sp,
                                color = NavyDark,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    // A4 Rank Card quick action for selected term in individual mode
                    if (!isGroupMode && viewModeTab in 1..3 && activeRecords.isNotEmpty()) {
                        Button(
                            onClick = {
                                viewModel.exportClassAllTermRankCardsPdf(context, activeRecords, viewModeTab) { file ->
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
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
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
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "3-ம் பருவத் தேர்வு முடிவுகள்: மாணவர் வகுப்பு உயர்வு (Promotion)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "தேர்ச்சி பெற்ற மாணவர்களை அடுத்த வகுப்பிற்கும், 8-ம் வகுப்பு தேர்ச்சி நிலையும் பதிவு செய்யலாம்.",
                                fontSize = 10.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                    }

                    Button(
                        onClick = { showPromotionDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("promotion_banner_btn")
                    ) {
                        Text("வகுப்பு உயர்வு", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Interactive Table Display
        if (activeRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isGroupMode) {
                        "${currentSection.title} -ல் மாணவர்கள் பதிவு செய்யப்படவில்லை."
                    } else {
                        "வகுப்பு $selectedClass -ல் மாணவர்கள் பதிவு செய்யப்படவில்லை."
                    },
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
            }
        } else {
            val horizontalScrollState = rememberScrollState()
            val isClass8 = if (isGroupMode) (currentSection.classes == listOf(8)) else (selectedClass == 8)
            val mainSubjectsCount = subjects.filter { it != Subject.PE }.size
            val hasPe = subjects.contains(Subject.PE)
            val calculatedTableWidth: Dp = if (isClass8) {
                (36 + 54 + 130 + 42 + (5 * 68) + 64 + 68 + 50 + 50 + 80).dp
            } else {
                (36 + 54 + 130 + 42 +
                    (mainSubjectsCount * (if ((isPrimary || isClass3) && viewModeTab != 0 && selectedPrimarySubject == null) 140 else 105)) +
                    56 + (if (hasPe) 105 else 0) + 50 + 50 + 80).dp
            }

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
                                if (!isGroupMode && selectedPrimarySubject != null) {
                                    // Official Register Format for Class 1-3 single subject
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

                                    Class1To3OfficialHeader(isClass3 = (selectedClass == 3))

                                    activeRecords.forEachIndexed { idx, record ->
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
                                    // Consolidated Header
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
                                            text = if (isGroupMode) {
                                                if (viewModeTab == 0) "${currentSection.title} - முப்பருவ சராசரி பதிவேடு" else "${currentSection.title} - ${tabs[viewModeTab]} பதிவேடு"
                                            } else {
                                                if (viewModeTab == 0) "வகுப்பு $selectedClass - முப்பருவ சராசரி பட்டியல்" else "வகுப்பு $selectedClass - ${tabs[viewModeTab]} பட்டியல்"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = AmberGold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    TableHeader(
                                        subjects = subjects,
                                        isAverageMode = viewModeTab == 0,
                                        isClass1To3 = isPrimary,
                                        isClass8 = isClass8,
                                        isClass3 = isClass3
                                    )

                                    if (isGroupMode) {
                                        // Section Mode: Grouped with class dividers
                                        var globalSno = 1
                                        sectionRecordsMap.toSortedMap().forEach { (cls, recs) ->
                                            if (recs.isNotEmpty()) {
                                                ClassDividerTableRow(
                                                    cls = cls,
                                                    studentCount = recs.size,
                                                    totalTableWidth = calculatedTableWidth
                                                )

                                                recs.forEach { record ->
                                                    val rowIsClass8 = record.student.stdClass == 8
                                                    val rowIsClass1To3 = record.student.stdClass in 1..3
                                                    StudentRow(
                                                        sNo = globalSno++,
                                                        record = record,
                                                        subjects = subjects,
                                                        viewMode = viewModeTab,
                                                        isClass1To3 = rowIsClass1To3,
                                                        isClass8 = rowIsClass8,
                                                        onRowClick = {
                                                            selectedRankCardRecord = record
                                                            showRankCardDialog = true
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // Single Class Mode
                                        activeRecords.forEachIndexed { idx, record ->
                                            StudentRow(
                                                sNo = idx + 1,
                                                record = record,
                                                subjects = subjects,
                                                viewMode = viewModeTab,
                                                isClass1To3 = (selectedClass in 1..3),
                                                isClass8 = isClass8,
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
        val ranksMap = viewModel.calculateClassRanks(activeRecords, targetTerm)
        val rank = ranksMap[selectedRankCardRecord!!.student.id] ?: 1

        TermRankCardDialog(
            record = selectedRankCardRecord!!,
            school = schoolProfile,
            term = targetTerm,
            rank = rank,
            totalClassStudents = activeRecords.size,
            onDismiss = { showRankCardDialog = false },
            onPrintPdf = {
                viewModel.exportTermRankCardPdf(context, selectedRankCardRecord!!, targetTerm, rank, activeRecords.size) {
                    PdfReportGenerator.printPdfFile(context, it)
                }
            },
            onSharePdf = {
                viewModel.exportTermRankCardPdf(context, selectedRankCardRecord!!, targetTerm, rank, activeRecords.size) {
                    PdfReportGenerator.sharePdfFile(context, it)
                }
            },
            onViewPdf = {
                viewModel.exportTermRankCardPdf(context, selectedRankCardRecord!!, targetTerm, rank, activeRecords.size) {
                    PdfReportGenerator.viewPdfFile(context, it)
                }
            },
            onPrintAllClassPdf = {
                viewModel.exportClassAllTermRankCardsPdf(context, activeRecords, targetTerm) {
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
                ) {
                    showPromotionDialog = false
                }
            }
        )
    }
}

@Composable
fun ClassDividerTableRow(
    cls: Int,
    studentCount: Int,
    totalTableWidth: Dp
) {
    Row(
        modifier = Modifier
            .width(totalTableWidth)
            .background(Color(0xFFEEF2FF))
            .border(0.5.dp, Color(0xFFC7D2FE))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            tint = NavyPrimary,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "--- வகுப்பு $cls (CLASS $cls) - $studentCount மாணவர்கள் ---",
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            color = Color(0xFF1E3A8A)
        )
    }
}

@Composable
fun TableHeader(
    subjects: List<Subject>,
    isAverageMode: Boolean,
    isClass1To3: Boolean,
    isClass8: Boolean = false,
    isClass3: Boolean = false
) {
    val borderColor = Color.LightGray
    val thBg = Color(0xFFEEF2FF)

    val mainSubjects = remember(subjects) { subjects.filter { it != Subject.PE } }
    val hasPe = remember(subjects) { subjects.contains(Subject.PE) }
    val maxMarks = mainSubjects.size * 100

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
                if (isClass8) {
                    TableCell(text = "${sub.shortName}\n(100)", width = 68.dp, isHeader = true)
                } else {
                    val w = if ((isClass1To3 || isClass3) && !isAverageMode) 140.dp else 105.dp
                    TableCell(text = sub.shortName, width = w, isHeader = true)
                }
            }

            TableCell(text = "TOTAL\n($maxMarks)", width = if (isClass8) 64.dp else 56.dp, isHeader = true)

            if (hasPe) {
                if (isClass8) {
                    TableCell(text = "உடற்கல்வி\n(100)", width = 68.dp, isHeader = true)
                } else {
                    TableCell(text = Subject.PE.shortName, width = 105.dp, isHeader = true)
                }
            }

            TableCell(text = "வேலை\nநாட்கள்", width = 50.dp, isHeader = true)
            TableCell(text = "வருகை\nநாட்கள்", width = 50.dp, isHeader = true)
            TableCell(text = "தேர்ச்சி விபரம்", width = 80.dp, isHeader = true)
        }

        // Sub-header row for subjects: SA, FA, மொ (for Class 8: direct 100 max mark)
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

            if (isClass8) {
                mainSubjects.forEach { _ ->
                    TableCell(text = "100", width = 68.dp, isSubHeader = true)
                }

                TableCell(text = "500", width = 64.dp, isSubHeader = true)

                if (hasPe) {
                    TableCell(text = "100", width = 68.dp, isSubHeader = true)
                }
            } else {
                mainSubjects.forEach { _ ->
                    if ((isClass1To3 || isClass3) && !isAverageMode) {
                        val fa1 = if (isClass3) "FA(20)" else "FA(25)"
                        val fa2 = if (isClass3) "FA(20)" else "FA(25)"
                        val sa = if (isClass3) "SA(60)" else "SA(50)"
                        TableCell(text = fa1, width = 35.dp, isSubHeader = true)
                        TableCell(text = fa2, width = 35.dp, isSubHeader = true)
                        TableCell(text = sa, width = 35.dp, isSubHeader = true)
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
    isClass8: Boolean = false,
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

        if (isClass8) {
            // 8-ம் வகுப்பு: SA/FA பிரிக்கப்படாமல் ஒவ்வொரு பாடத்திற்கும் நேரடி 100 மதிப்பெண்
            mainSubjects.forEach { sub ->
                val sm = record.subjectMarks[sub]
                val mark = if (viewMode == 0) {
                    sm?.avgTotal ?: 0
                } else {
                    val m = when (viewMode) {
                        1 -> sm?.term1Marks
                        2 -> sm?.term2Marks
                        3 -> sm?.term3Marks
                        else -> null
                    }
                    m?.total ?: 0
                }
                mainTotal += mark
                TableCell(text = "$mark", width = 68.dp, isBold = true, textColor = NavyPrimary)
            }

            // TOTAL for main 5 subjects (out of 500)
            TableCell(text = "$mainTotal", width = 64.dp, isBold = true, textColor = NavyDark)

            // Physical Education (PE) single mark out of 100
            if (hasPe) {
                val peSm = record.subjectMarks[Subject.PE]
                val peMark = if (viewMode == 0) {
                    peSm?.avgTotal ?: 0
                } else {
                    val m = when (viewMode) {
                        1 -> peSm?.term1Marks
                        2 -> peSm?.term2Marks
                        3 -> peSm?.term3Marks
                        else -> null
                    }
                    m?.total ?: 0
                }
                TableCell(text = "$peMark", width = 68.dp, isBold = true, textColor = AmberGold)
            }
        } else {
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

            // TOTAL for main subjects only (excluding PE) - out of 300 for 1-3, 500 for others
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
    width: Dp,
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
fun Class1To3OfficialHeader(isClass3: Boolean = false) {
    val borderColor = Color.LightGray
    val thBg = Color(0xFFEEF2FF)

    val naney1Title = if (isClass3) "நானே செய்வேன் - 1 [20]" else "நானே செய்வேன் - 1 [25]"
    val naney2Title = if (isClass3) "நானே செய்வேன் - 2 [20]" else "நானே செய்வேன் - 2 [25]"
    val thiranariTitle = if (isClass3) "திரனறி மதிப்பீடு [60]" else "திறனறி மதிப்பீடு [50]"

    val oral1 = if (isClass3) "வாய்மொழி [8]" else "வாய்மொழி [10]"
    val act1 = if (isClass3) "செயல்பாடு [8]" else "செயல்பாடு [10]"
    val wri1 = if (isClass3) "எழுத்து [4]" else "எழுத்து [5]"

    val oral2 = if (isClass3) "வாய்மொழி [8]" else "வாய்மொழி [10]"
    val act2 = if (isClass3) "செயல்பாடு [8]" else "செயல்பாடு [10]"
    val wri2 = if (isClass3) "எழுத்து [4]" else "எழுத்து [5]"

    val thOral = if (isClass3) "வாய்மொழி [10]" else "வாய்மொழி [10]"
    val thWri = if (isClass3) "எழுத்து [50]" else "எழுத்து [40]"

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
            TableCell(text = naney1Title, width = 195.dp, isHeader = true, textColor = Color(0xFF991B1B))
            TableCell(text = naney2Title, width = 195.dp, isHeader = true, textColor = Color(0xFF991B1B))
            TableCell(text = thiranariTitle, width = 140.dp, isHeader = true, textColor = Color(0xFF92400E))
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
            TableCell(text = oral1, width = 65.dp, isSubHeader = true)
            TableCell(text = act1, width = 65.dp, isSubHeader = true)
            TableCell(text = wri1, width = 65.dp, isSubHeader = true)

            // Naney 2 sub-columns
            TableCell(text = oral2, width = 65.dp, isSubHeader = true)
            TableCell(text = act2, width = 65.dp, isSubHeader = true)
            TableCell(text = wri2, width = 65.dp, isSubHeader = true)

            // Thiranari sub-columns
            TableCell(text = thOral, width = 70.dp, isSubHeader = true)
            TableCell(text = thWri, width = 70.dp, isSubHeader = true)

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
