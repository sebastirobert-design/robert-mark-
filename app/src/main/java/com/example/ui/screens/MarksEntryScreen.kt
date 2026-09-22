package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CceGradeEvaluator
import com.example.data.model.Student
import com.example.data.model.StudentConsolidatedRecord
import com.example.data.model.StudentMarks
import com.example.data.model.Subject
import com.example.ui.components.ClassChipsSelector
import com.example.ui.dialogs.TermRankCardDialog
import com.example.ui.theme.AcademicBlue
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.Slate50
import com.example.util.ExcelReportGenerator
import com.example.util.PdfReportGenerator
import com.example.viewmodel.MarksInputData
import com.example.viewmodel.SchoolMarksViewModel

@Composable
fun MarksEntryScreen(
    viewModel: SchoolMarksViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val selectedTerm by viewModel.selectedTerm.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val schoolProfile by viewModel.schoolProfile.collectAsState()

    val classStudents = allStudents.filter { it.stdClass == selectedClass }

    var consolidatedRecords by remember { mutableStateOf<List<StudentConsolidatedRecord>>(emptyList()) }
    var selectedStudentIndex by remember { mutableIntStateOf(0) }
    var showRankCardDialog by remember { mutableStateOf(false) }
    var currentRankCardRecord by remember { mutableStateOf<StudentConsolidatedRecord?>(null) }

    // Refresh consolidated records when class, students or term changes
    LaunchedEffect(selectedClass, allStudents, selectedTerm) {
        consolidatedRecords = viewModel.loadConsolidatedRecordsForClass(selectedClass)
    }

    // Keep student index in bounds when class changes
    LaunchedEffect(selectedClass, classStudents.size) {
        if (selectedStudentIndex >= classStudents.size) {
            selectedStudentIndex = 0
        }
    }

    val currentStudent = classStudents.getOrNull(selectedStudentIndex)
    var entryViewMode by remember { mutableIntStateOf(0) } // 0: மாணவர் வாரியாக, 1: வகுப்புப் பதிவேடு (அட்டவணை)

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("marks_entry_screen")
    ) {
        // Class selection chips
        ClassChipsSelector(
            selectedClass = selectedClass,
            onSelectClass = {
                viewModel.selectClass(it)
                selectedStudentIndex = 0
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Term selection tabs
        val terms = listOf(1 to "பருவம் 1 (Term 1)", 2 to "பருவம் 2 (Term 2)", 3 to "பருவம் 3 (Term 3)")
        TabRow(
            selectedTabIndex = selectedTerm - 1,
            containerColor = Color.White,
            contentColor = NavyPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            terms.forEach { (termNum, termLabel) ->
                Tab(
                    selected = selectedTerm == termNum,
                    onClick = { viewModel.selectTerm(termNum) },
                    text = {
                        Text(
                            text = termLabel,
                            fontWeight = if (selectedTerm == termNum) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.5.sp
                        )
                    }
                )
            }
        }

        // View Mode Switch for Class 1-3: மாணவர் வாரியாக / வகுப்புப் பதிவேடு அட்டவணை
        if (selectedClass in 1..3 && classStudents.isNotEmpty()) {
            Surface(
                color = Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = entryViewMode == 0,
                        onClick = { entryViewMode = 0 },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("மாணவர் வாரியாக (Individual)", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    FilterChip(
                        selected = entryViewMode == 1,
                        onClick = { entryViewMode = 1 },
                        leadingIcon = {
                            Icon(
                                Icons.Default.TableChart,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("வகுப்புப் பதிவேடு (அட்டவணை)", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AcademicBlue,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        if (classStudents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "வகுப்பு $selectedClass -ல் மாணவர்கள் பதிவு செய்யப்படவில்லை.\nமுதலில் 'மாணவர் சேர்க்கை' பகுதியில் மாணவர்களை சேர்க்கவும்.",
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
            }
        } else if (selectedClass in 1..3 && entryViewMode == 1) {
            // Whole Class Subject Register Table View (Exact Format from Image)
            Class1To3RegisterTableView(
                students = classStudents,
                stdClass = selectedClass,
                term = selectedTerm,
                viewModel = viewModel
            )
        } else if (currentStudent != null) {
            // Student Selector Horizontal Strip
            Surface(
                color = Slate50,
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
                    classStudents.forEachIndexed { idx, st ->
                        val isSelected = idx == selectedStudentIndex
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStudentIndex = idx },
                            label = {
                                Text(
                                    text = "${st.admissionNo}. ${st.name}",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AcademicBlue,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Student Mark Entry Form
            StudentMarkForm(
                student = currentStudent,
                term = selectedTerm,
                studentIndex = selectedStudentIndex,
                totalStudents = classStudents.size,
                viewModel = viewModel,
                onPrevious = {
                    if (selectedStudentIndex > 0) selectedStudentIndex--
                },
                onNext = {
                    if (selectedStudentIndex < classStudents.size - 1) selectedStudentIndex++
                },
                onSave = { marksMap, workDays, presentDays ->
                    viewModel.saveStudentMarksAndAttendance(
                        studentId = currentStudent.id,
                        stdClass = currentStudent.stdClass,
                        term = selectedTerm,
                        marksMap = marksMap,
                        totalWorkingDays = workDays,
                        presentDays = presentDays
                    )
                    coroutineScope.launch {
                        consolidatedRecords = viewModel.loadConsolidatedRecordsForClass(selectedClass)
                    }
                    Toast.makeText(context, "${currentStudent.name} பருவம் $selectedTerm மதிப்பெண்கள் சேமிக்கப்பட்டன!", Toast.LENGTH_SHORT).show()
                },
                onOpenRankCard = {
                    coroutineScope.launch {
                        consolidatedRecords = viewModel.loadConsolidatedRecordsForClass(selectedClass)
                        val rec = consolidatedRecords.find { it.student.id == currentStudent.id }
                        if (rec != null) {
                            currentRankCardRecord = rec
                            showRankCardDialog = true
                        } else {
                            Toast.makeText(context, "மதிப்பெண்கள் சேமிக்கப்பட்டு தர அட்டை தயாராகிறது...", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onOpenClassAllRankCards = {
                    coroutineScope.launch {
                        consolidatedRecords = viewModel.loadConsolidatedRecordsForClass(selectedClass)
                        if (consolidatedRecords.isNotEmpty()) {
                            viewModel.exportClassAllTermRankCardsPdf(context, consolidatedRecords, selectedTerm) { file ->
                                PdfReportGenerator.printPdfFile(context, file)
                            }
                        } else {
                            Toast.makeText(context, "மதிப்பெண்கள் பதிவேற்றம் செய்தபின் தர அட்டை அச்சிடலாம்", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        if (showRankCardDialog && currentRankCardRecord != null) {
            val ranksMap = viewModel.calculateClassRanks(consolidatedRecords, selectedTerm)
            val rank = ranksMap[currentRankCardRecord!!.student.id] ?: 1

            TermRankCardDialog(
                record = currentRankCardRecord!!,
                school = schoolProfile,
                term = selectedTerm,
                rank = rank,
                totalClassStudents = classStudents.size,
                onDismiss = { showRankCardDialog = false },
                onPrintPdf = {
                    viewModel.exportTermRankCardPdf(context, currentRankCardRecord!!, selectedTerm, rank, classStudents.size) {
                        PdfReportGenerator.printPdfFile(context, it)
                    }
                },
                onSharePdf = {
                    viewModel.exportTermRankCardPdf(context, currentRankCardRecord!!, selectedTerm, rank, classStudents.size) {
                        PdfReportGenerator.sharePdfFile(context, it)
                    }
                },
                onViewPdf = {
                    viewModel.exportTermRankCardPdf(context, currentRankCardRecord!!, selectedTerm, rank, classStudents.size) {
                        PdfReportGenerator.viewPdfFile(context, it)
                    }
                },
                onPrintAllClassPdf = {
                    viewModel.exportClassAllTermRankCardsPdf(context, consolidatedRecords, selectedTerm) {
                        PdfReportGenerator.printPdfFile(context, it)
                    }
                }
            )
        }
    }
}

@Composable
fun StudentMarkForm(
    student: Student,
    term: Int,
    studentIndex: Int,
    totalStudents: Int,
    viewModel: SchoolMarksViewModel,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSave: (marksMap: Map<Subject, MarksInputData>, workDays: Int, presentDays: Int) -> Unit,
    onOpenRankCard: () -> Unit,
    onOpenClassAllRankCards: () -> Unit
) {
    val subjects = Subject.getSubjectsForClass(student.stdClass)
    val isPrimary = student.stdClass in 1..3

    val schoolProfile by viewModel.schoolProfile.collectAsState()
    val defaultWorkingDays = schoolProfile.getWorkingDaysForTerm(term)

    // Working Days & Present Days state initialized from School Profile settings
    var workingDaysStr by remember(student.id, term, defaultWorkingDays) { mutableStateOf(defaultWorkingDays.toString()) }
    var presentDaysStr by remember(student.id, term, defaultWorkingDays) { mutableStateOf(((defaultWorkingDays * 0.95).toInt()).toString()) }

    var term1Att by remember(student.id, term) { mutableStateOf<com.example.data.model.TermAttendance?>(null) }
    var term2Att by remember(student.id, term) { mutableStateOf<com.example.data.model.TermAttendance?>(null) }

    // Marks state map
    val marksState = remember(student.id, term) {
        mutableStateMapOf<Subject, MarksInputData>().apply {
            subjects.forEach { sub ->
                if (isPrimary) {
                    put(sub, MarksInputData(
                        naney1Oral = 10,
                        naney1Activity = 10,
                        naney1Written = 5,
                        naney2Oral = 10,
                        naney2Activity = 10,
                        naney2Written = 5,
                        thiranariOral = 10,
                        thiranariWritten = 40
                    ))
                } else if (student.stdClass == 8) {
                    // Req 3: எட்டாம் வகுப்பிற்கு மட்டும் நேரடி 100 மதிப்பெண்
                    put(sub, MarksInputData(directTotal = 75, sa = 75, faTotal = 0))
                } else {
                    put(sub, MarksInputData(faTotal = 35, sa = 50))
                }
            }
        }
    }

    // Load any existing saved marks from database
    LaunchedEffect(student.id, term) {
        if (term >= 2) {
            term1Att = viewModel.getAttendanceForStudentAndTerm(student.id, 1)
        } else {
            term1Att = null
        }
        if (term >= 3) {
            term2Att = viewModel.getAttendanceForStudentAndTerm(student.id, 2)
        } else {
            term2Att = null
        }

        val savedMarks = viewModel.getMarksForStudentAndTerm(student.id, term)
        if (savedMarks.isNotEmpty()) {
            savedMarks.forEach { sm ->
                val sub = subjects.find { it.key == sm.subjectKey }
                if (sub != null) {
                    marksState[sub] = MarksInputData(
                        faA = sm.faA,
                        faB = sm.faB,
                        faTotal = sm.faTotal,
                        sa = sm.sa,
                        directTotal = if (sm.total > 0) sm.total else sm.sa,
                        naney1Oral = sm.naney1Oral,
                        naney1Activity = sm.naney1Activity,
                        naney1Written = sm.naney1Written,
                        naney2Oral = sm.naney2Oral,
                        naney2Activity = sm.naney2Activity,
                        naney2Written = sm.naney2Written,
                        thiranariOral = sm.thiranariOral,
                        thiranariWritten = sm.thiranariWritten
                    )
                }
            }
        }
        val att = viewModel.getAttendanceForStudentAndTerm(student.id, term)
        if (att != null) {
            workingDaysStr = att.totalWorkingDays.toString()
            presentDaysStr = att.presentDays.toString()
        } else {
            workingDaysStr = defaultWorkingDays.toString()
            presentDaysStr = ((defaultWorkingDays * 0.95).toInt()).toString()
        }
    }

    val mainSubjects = remember(student.stdClass) { Subject.getMainSubjectsForClass(student.stdClass) }
    val hasPe = remember(student.stdClass) { Subject.hasPeSubject(student.stdClass) }
    val orderedSubjects = remember(student.stdClass) {
        mainSubjects + (if (hasPe) listOf(Subject.PE) else emptyList())
    }

    // Calculations - ONLY 5 main academic subjects in grandTotal and maxMarks
    val grandTotal = mainSubjects.sumOf { sub ->
        val data = marksState[sub] ?: MarksInputData()
        when {
            isPrimary -> data.calculatedTotal
            student.stdClass == 8 -> (if (data.directTotal == 0 && data.sa > 0) data.sa else data.directTotal).coerceIn(0, 100)
            else -> (data.faTotal + data.sa).coerceAtMost(100)
        }
    }

    val maxMarks = mainSubjects.size * 100
    val percentage = if (maxMarks > 0) (grandTotal.toFloat() / maxMarks.toFloat()) * 100f else 0f

    val peTotal = if (hasPe) {
        val data = marksState[Subject.PE] ?: MarksInputData()
        when {
            student.stdClass == 8 -> (if (data.directTotal == 0 && data.sa > 0) data.sa else data.directTotal).coerceIn(0, 100)
            else -> (data.faTotal + data.sa).coerceAtMost(100)
        }
    } else null

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Student Info Card with Nav Arrows
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPrevious,
                        enabled = studentIndex > 0,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "முந்தைய",
                            tint = if (studentIndex > 0) Color.White else Color.White.copy(alpha = 0.3f)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${student.name} (${student.admissionNo})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "வகுப்பு: ${student.stdClass} - பிரிவு ${student.section} • பருவம் $term • [${studentIndex + 1} / $totalStudents]",
                            fontSize = 12.sp,
                            color = AmberGold
                        )
                    }

                    IconButton(
                        onClick = onNext,
                        enabled = studentIndex < totalStudents - 1,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "அடுத்த",
                            tint = if (studentIndex < totalStudents - 1) Color.White else Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }

        // Attendance Card (பள்ளி வேலை நாட்கள் & வருகை நாட்கள்)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "பருவம் $term வருகைப் பதிவு (Attendance)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = NavyDark
                        )

                        TextButton(
                            onClick = {
                                presentDaysStr = workingDaysStr
                            }
                        ) {
                            Text("முழு வருகை (100%)", fontSize = 11.5.sp, color = EmeraldPass)
                        }
                    }

                    // Display previous terms if term >= 2
                    if (term == 2) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("முந்தைய பருவம் 1:", fontSize = 11.5.sp, color = Color.DarkGray)
                                Text(
                                    text = "${term1Att?.presentDays ?: 0} / ${term1Att?.totalWorkingDays ?: 0} நாட்கள்",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = NavyDark
                                )
                            }
                        }
                    } else if (term == 3) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "பருவம் 1: ${term1Att?.presentDays ?: 0}/${term1Att?.totalWorkingDays ?: 0}",
                                    fontSize = 11.5.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "பருவம் 2: ${term2Att?.presentDays ?: 0}/${term2Att?.totalWorkingDays ?: 0}",
                                    fontSize = 11.5.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = workingDaysStr,
                                onValueChange = { workingDaysStr = it },
                                label = { Text("பருவம் $term வேலை நாட்கள்") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "பள்ளி அமைப்பில்: ${schoolProfile.getWorkingDaysForTerm(term)} நாட்கள்",
                                fontSize = 10.5.sp,
                                color = NavyPrimary,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }

                        OutlinedTextField(
                            value = presentDaysStr,
                            onValueChange = { presentDaysStr = it },
                            label = { Text("வருகை நாட்கள்") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Summary banner based on term
                    val curW = workingDaysStr.toIntOrNull() ?: 0
                    val curP = presentDaysStr.toIntOrNull() ?: 0

                    if (term == 2) {
                        val t1W = term1Att?.totalWorkingDays ?: 0
                        val t1P = term1Att?.presentDays ?: 0
                        val cumW = t1W + curW
                        val cumP = t1P + curP
                        val cumPct = if (cumW > 0) (cumP * 100.0) / cumW else 0.0

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "பருவம் 1 + 2 கூடுதல் வருகை:",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark
                                )
                                Text(
                                    text = "$cumP / $cumW நாட்கள் (${String.format("%.1f%%", cumPct)})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = NavyPrimary
                                )
                            }
                        }
                    } else if (term == 3) {
                        val t1W = term1Att?.totalWorkingDays ?: 0
                        val t1P = term1Att?.presentDays ?: 0
                        val t2W = term2Att?.totalWorkingDays ?: 0
                        val t2P = term2Att?.presentDays ?: 0
                        val yearW = t1W + t2W + curW
                        val yearP = t1P + t2P + curP
                        val yearPct = if (yearW > 0) (yearP * 100.0) / yearW else 0.0

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "1, 2, 3 ஆம் பருவங்கள் ஆண்டு மொத்தம்:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = NavyDark
                                )
                                Text(
                                    text = "$yearP / $yearW நாட்கள் (${String.format("%.1f%%", yearPct)})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = NavyPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Subject Mark Input Cards
        items(orderedSubjects) { subject ->
            val isPe = subject == Subject.PE
            val data = marksState[subject] ?: MarksInputData()

            if (isPe) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "உடற்கல்வி (தனி மதிப்பீடு - முதன்மை 5 பாடங்கள் மொத்தத்தில் சேராது)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isPe) Color(0xFFFFFBEB) else Color.White),
                border = if (isPe) BorderStroke(1.dp, Color(0xFFFDE68A)) else null,
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val curTotal = when {
                        isPrimary -> data.calculatedTotal
                        student.stdClass == 8 -> (if (data.directTotal == 0 && data.sa > 0) data.sa else data.directTotal).coerceIn(0, 100)
                        else -> (data.faTotal + data.sa).coerceAtMost(100)
                    }
                    val grade = if (isPrimary) {
                        CceGradeEvaluator.getGradeForClass1To3(curTotal)
                    } else {
                        CceGradeEvaluator.getGradeForClass4To8(curTotal)
                    }
                    val learningLevel = if (curTotal >= 80) "மலர்" else if (curTotal >= 60) "மொட்டு" else "அரும்பு"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isPe) "${subject.tamilName} (${subject.name}) - தனி மதிப்பீடு" else "${subject.tamilName} (${subject.name})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isPe) Color(0xFF92400E) else NavyPrimary
                            )
                            if (isPrimary) {
                                Text(
                                    text = "கற்றல் நிலை: $learningLevel",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (curTotal >= 80) EmeraldPass else AmberGold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isPe) Color(0xFFFEF3C7) else NavyPrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = if (isPe) "மதிப்பெண்: $curTotal / 100 • தரம்: $grade (தனி மதிப்பீடு)" else "மொத்தம்: $curTotal / 100 • $curTotal% • தரம்: $grade",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isPe) Color(0xFF92400E) else NavyPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (student.stdClass in 1..2) {
                        // Class 1 & 2 புதிய பாடத்திட்டம்:
                        // நானே செய்வேன் - 1 [25 மதிப்பெண்]
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                            border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "நானே செய்வேன் - 1",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFB91C1C)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFEE2E2)
                                    ) {
                                        Text(
                                            text = "கூட்டுத்தொகை: ${data.naney1Total} / 25",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFFB91C1C),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = data.naney1Oral.toString(),
                                        onValueChange = {
                                            val v = it.toIntOrNull() ?: 0
                                            marksState[subject] = data.copy(naney1Oral = v.coerceIn(0, 10))
                                        },
                                        label = { Text("வாய்மொழி [10]") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = data.naney1Activity.toString(),
                                        onValueChange = {
                                            val v = it.toIntOrNull() ?: 0
                                            marksState[subject] = data.copy(naney1Activity = v.coerceIn(0, 10))
                                        },
                                        label = { Text("செயல்பாடு [10]") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = data.naney1Written.toString(),
                                        onValueChange = {
                                            val v = it.toIntOrNull() ?: 0
                                            marksState[subject] = data.copy(naney1Written = v.coerceIn(0, 5))
                                        },
                                        label = { Text("எழுத்துவழி [5]") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // நானே செய்வேன் - 2 [25 மதிப்பெண்]
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                            border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "நானே செய்வேன் - 2",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFB91C1C)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFEE2E2)
                                    ) {
                                        Text(
                                            text = "கூட்டுத்தொகை: ${data.naney2Total} / 25",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFFB91C1C),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = data.naney2Oral.toString(),
                                        onValueChange = {
                                            val v = it.toIntOrNull() ?: 0
                                            marksState[subject] = data.copy(naney2Oral = v.coerceIn(0, 10))
                                        },
                                        label = { Text("வாய்மொழி [10]") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = data.naney2Activity.toString(),
                                        onValueChange = {
                                            val v = it.toIntOrNull() ?: 0
                                            marksState[subject] = data.copy(naney2Activity = v.coerceIn(0, 10))
                                        },
                                        label = { Text("செயல்பாடு [10]") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = data.naney2Written.toString(),
                                        onValueChange = {
                                            val v = it.toIntOrNull() ?: 0
                                            marksState[subject] = data.copy(naney2Written = v.coerceIn(0, 5))
                                        },
                                        label = { Text("எழுத்துவழி [5]") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // திறனறி தேர்வு [50 மதிப்பெண்]
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFCE8)),
                            border = BorderStroke(1.dp, Color(0xFFFEF08A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "திறனறி தேர்வு",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFB45309)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFEF9C3)
                                    ) {
                                        Text(
                                            text = "கூட்டுத்தொகை: ${data.thiranariTotal} / 50",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFFB45309),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = data.thiranariOral.toString(),
                                        onValueChange = {
                                            val v = it.toIntOrNull() ?: 0
                                            marksState[subject] = data.copy(thiranariOral = v.coerceIn(0, 10))
                                        },
                                        label = { Text("வாய்மொழி [10]") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = data.thiranariWritten.toString(),
                                        onValueChange = {
                                            val v = it.toIntOrNull() ?: 0
                                            marksState[subject] = data.copy(thiranariWritten = v.coerceIn(0, 40))
                                        },
                                        label = { Text("எழுத்துவழி [40]") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    } else if (student.stdClass == 3) {
                        // Class 3 புதிய பாடத்திட்டம்:
                        // நானே செய்வேன் - 1 [20 மதிப்பெண்]
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                            border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "நானே செய்வேன் - 1",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFB91C1C)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFEE2E2)
                                    ) {
                                        Text(
                                            text = "கூட்டுத்தொகை: ${data.naney1Total} / 20",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFFB91C1C),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = data.naney1Oral.toString(),
                                        onValueChange = {
                                            val v = it.toIntOrNull() ?: 0
                                            marksState[subject] = data.copy(naney1Oral = v.coerceIn(0, 8))
                                        },
                                        label = { Text("வாய்மொழி [8]") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = data.naney1Activity.toString(),
                                        onValueChange = {
                                            val v = it.toIntOrNull() ?: 0
                                            marksState[subject] = data.copy(naney1Activity = v.coerceIn(0, 8))
                                        },
                                        label = { Text("செயல்பாடு [8]") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = data.naney1Written.toString(),
                                        onValueChange = {
                                            val v = it.toIntOrNull() ?: 0
                                            marksState[subject] = data.copy(naney1Written = v.coerceIn(0, 4))
                                        },
                                        label = { Text("எழுத்துவழி [4]") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // நானே செய்வேன் - 2 [20 மதிப்பெண்]
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                            border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "நானே செய்வேன் - 2",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFB91C1C)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFEE2E2)
                                    ) {
                                        Text(
                                            text = "கூட்டுத்தொகை: ${data.naney2Total} / 20",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFFB91C1C),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = data.naney2Oral.toString(),
                                        onValueChange = {
                                            val v = it.toIntOrNull() ?: 0
                                            marksState[subject] = data.copy(naney2Oral = v.coerceIn(0, 8))
                                        },
                                        label = { Text("வாய்மொழி [8]") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = data.naney2Activity.toString(),
                                        onValueChange = {
                                            val v = it.toIntOrNull() ?: 0
                                            marksState[subject] = data.copy(naney2Activity = v.coerceIn(0, 8))
                                        },
                                        label = { Text("செயல்பாடு [8]") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = data.naney2Written.toString(),
                                        onValueChange = {
                                            val v = it.toIntOrNull() ?: 0
                                            marksState[subject] = data.copy(naney2Written = v.coerceIn(0, 4))
                                        },
                                        label = { Text("எழுத்துவழி [4]") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // திரனறி மதிப்பீடு [60 மதிப்பெண்]
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFCE8)),
                            border = BorderStroke(1.dp, Color(0xFFFEF08A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "திரனறி மதிப்பீடு",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFB45309)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFEF9C3)
                                    ) {
                                        Text(
                                            text = "கூட்டுத்தொகை: ${data.thiranariTotal} / 60",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFFB45309),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = data.thiranariOral.toString(),
                                        onValueChange = {
                                            val v = it.toIntOrNull() ?: 0
                                            marksState[subject] = data.copy(thiranariOral = v.coerceIn(0, 10))
                                        },
                                        label = { Text("வாய்மொழி [10]") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = data.thiranariWritten.toString(),
                                        onValueChange = {
                                            val v = it.toIntOrNull() ?: 0
                                            marksState[subject] = data.copy(thiranariWritten = v.coerceIn(0, 50))
                                        },
                                        label = { Text("எழுத்துவழி [50]") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    } else if (student.stdClass == 8) {
                        // Class 8 format: Single 100 mark input (Req 3: வளர அறி, தொகுத்தறி என பிரிக்கவேண்டாம்)
                        val curVal = if (data.directTotal == 0 && data.sa > 0) data.sa else data.directTotal
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF0FDF4),
                                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "8-ம் வகுப்பு: நேரடி 100 மதிப்பெண் (வளரறி / தொகுத்தறி பிரிக்காமல்)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF166534),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = if (curVal == 0) "" else curVal.toString(),
                                    onValueChange = { input ->
                                        val v = input.toIntOrNull() ?: 0
                                        val clamped = v.coerceIn(0, 100)
                                        marksState[subject] = data.copy(directTotal = clamped, sa = clamped, faTotal = 0)
                                    },
                                    label = { Text("தேர்வு மதிப்பெண் [100]") },
                                    placeholder = { Text("0 - 100") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("class8_mark_input_${subject.key}")
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFDCFCE7),
                                    modifier = Modifier.weight(0.7f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "$curVal / 100",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF15803D)
                                        )
                                        Text(
                                            text = "தரம்: $grade",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = NavyPrimary
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Class 4-7 format: FA (40), SA (60)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = data.faTotal.toString(),
                                onValueChange = {
                                    val v = it.toIntOrNull() ?: 0
                                    marksState[subject] = data.copy(faTotal = v.coerceIn(0, 40))
                                },
                                label = { Text("வளர் அறி FA [40]") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = data.sa.toString(),
                                onValueChange = {
                                    val v = it.toIntOrNull() ?: 0
                                    marksState[subject] = data.copy(sa = v.coerceIn(0, 60))
                                },
                                label = { Text("தொகுத்தறி SA [60]") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Summary Bar & Save Button & Rank Card Actions
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "முதன்மைப் பாடங்கள் மொத்தம்: $grandTotal / $maxMarks",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = NavyDark
                            )
                            Text(
                                text = "சராசரி: ${String.format("%.1f", percentage)}%" +
                                        (if (hasPe && peTotal != null) " • உடற்கல்வி: $peTotal / 100 (தனி மதிப்பீடு)" else ""),
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }

                        Button(
                            onClick = {
                                val w = workingDaysStr.toIntOrNull() ?: 80
                                val p = presentDaysStr.toIntOrNull() ?: 76
                                onSave(marksState, w, p)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPass),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_marks_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("சேமிக்க", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val w = workingDaysStr.toIntOrNull() ?: 80
                                val p = presentDaysStr.toIntOrNull() ?: 76
                                onSave(marksState, w, p)
                                onOpenRankCard()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("open_rank_card_btn")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavyDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("🖨️ A4 தர அட்டை", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenClassAllRankCards,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_class_rank_cards_btn")
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavyPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("முழு வகுப்பு தர அட்டைகள்", fontSize = 11.sp, maxLines = 1, color = NavyPrimary)
                        }
                    }
                }
            }
        }

        // Next Student quick action button
        item {
            if (studentIndex < totalStudents - 1) {
                OutlinedButton(
                    onClick = {
                        val w = workingDaysStr.toIntOrNull() ?: 80
                        val p = presentDaysStr.toIntOrNull() ?: 76
                        onSave(marksState, w, p)
                        onNext()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("சேமித்து அடுத்த மாணவர் செல்க ->", fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

/**
 * Whole-class Subject Register Table View for Class 1 to 3
 * Exactly reproduces the official register format from the user's attachment.
 */
@Composable
fun Class1To3RegisterTableView(
    students: List<Student>,
    stdClass: Int,
    term: Int,
    viewModel: SchoolMarksViewModel
) {
    val context = LocalContext.current
    val schoolProfile by viewModel.schoolProfile.collectAsState()
    val subjects = Subject.getSubjectsForClass(stdClass)
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull() ?: Subject.TAMIL) }

    // Map of studentId -> MarksInputData for selectedSubject
    val classMarksMap = remember(students, stdClass, term, selectedSubject) {
        mutableStateMapOf<Long, MarksInputData>().apply {
            students.forEach { st ->
                put(st.id, MarksInputData(
                    naney1Oral = 10,
                    naney1Activity = 10,
                    naney1Written = 5,
                    naney2Oral = 10,
                    naney2Activity = 10,
                    naney2Written = 5,
                    thiranariOral = 10,
                    thiranariWritten = 40
                ))
            }
        }
    }

    // Load existing values from DB
    LaunchedEffect(students, term, selectedSubject) {
        students.forEach { st ->
            val saved = viewModel.getMarksForStudentAndTerm(st.id, term)
            val subMark = saved.find { it.subjectKey == selectedSubject.key }
            if (subMark != null) {
                classMarksMap[st.id] = MarksInputData(
                    faA = subMark.faA,
                    faB = subMark.faB,
                    faTotal = subMark.faTotal,
                    sa = subMark.sa,
                    naney1Oral = subMark.naney1Oral,
                    naney1Activity = subMark.naney1Activity,
                    naney1Written = subMark.naney1Written,
                    naney2Oral = subMark.naney2Oral,
                    naney2Activity = subMark.naney2Activity,
                    naney2Written = subMark.naney2Written,
                    thiranariOral = subMark.thiranariOral,
                    thiranariWritten = subMark.thiranariWritten
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // Subject selector pills & Action Buttons Bar
        Surface(
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "பாடம் தேர்வு செய்க:",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.exportClass1To3SubjectCsv(context, stdClass, term, selectedSubject) { file ->
                                    Toast.makeText(context, "CSV கோப்பு தயார்", Toast.LENGTH_SHORT).show()
                                    ExcelReportGenerator.shareCsvFile(context, file, "${selectedSubject.tamilName} பதிவேடு")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPass),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel", fontSize = 11.5.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.exportClass1To3SubjectPdf(context, stdClass, term, selectedSubject) { file ->
                                    Toast.makeText(context, "PDF அறிக்கை தயார்", Toast.LENGTH_SHORT).show()
                                    PdfReportGenerator.sharePdfFile(context, file, "${selectedSubject.tamilName} பதிவேடு PDF")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", fontSize = 11.5.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.saveBatchPrimarySubjectMarks(
                                    term = term,
                                    subject = selectedSubject,
                                    marksMap = classMarksMap
                                )
                                Toast.makeText(context, "அனைத்து மாணவர்களுக்கும் மதிப்பெண்கள் சேமிக்கப்பட்டன!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AcademicBlue),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("அனைத்தும் சேமி", fontSize = 11.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    subjects.forEach { sub ->
                        FilterChip(
                            selected = selectedSubject == sub,
                            onClick = { selectedSubject = sub },
                            label = { Text(sub.tamilName, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // Official Register Header & Scrollable Table
        val horizontalScrollState = rememberScrollState()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Banner
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = schoolProfile.schoolName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NavyPrimary
                        )
                        Text(
                            text = "2026-27 மாணவர் மதிப்பெண் பதிவேடு பருவம் : $term",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFB91C1C)
                        )
                        Text(
                            text = "புதிய பாடத்திட்டம் வகுப்பு : $stdClass     பாடம் : ${selectedSubject.tamilName} (${selectedSubject.name})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }
            }

            // Scrollable Register Table
            val isClass3 = stdClass == 3
            val n1Label = if (isClass3) "நானே செய்வேன் - 1 [20]" else "நானே செய்வேன் - 1 [25]"
            val n2Label = if (isClass3) "நானே செய்வேன் - 2 [20]" else "நானே செய்வேன் - 2 [25]"
            val thLabel = if (isClass3) "திரனறி மதிப்பீடு [60]" else "திறனறி தேர்வு [50]"

            val n1OralMax = if (isClass3) 8 else 10
            val n1ActMax = if (isClass3) 8 else 10
            val n1WriMax = if (isClass3) 4 else 5

            val n2OralMax = if (isClass3) 8 else 10
            val n2ActMax = if (isClass3) 8 else 10
            val n2WriMax = if (isClass3) 4 else 5

            val thOralMax = 10
            val thWriMax = if (isClass3) 50 else 40

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
                        // Header Level 1 (Top Categories)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.background(Color(0xFFE2E8F0))
                        ) {
                            RegisterCell("வ.எண்", 50, Color.Black, FontWeight.Bold)
                            RegisterCell("சேர்க்கை எண்", 90, Color.Black, FontWeight.Bold)
                            RegisterCell("மாணவர் பெயர்", 140, Color.Black, FontWeight.Bold)
                            RegisterCell(n1Label, 240, Color(0xFF991B1B), FontWeight.Bold, Color(0xFFFEE2E2))
                            RegisterCell(n2Label, 240, Color(0xFF991B1B), FontWeight.Bold, Color(0xFFFEE2E2))
                            RegisterCell(thLabel, 170, Color(0xFF92400E), FontWeight.Bold, Color(0xFFFEF3C7))
                            RegisterCell("மொத்தம்", 70, Color.Black, FontWeight.Bold, Color(0xFFE2E8F0))
                            RegisterCell("விழுக்காடு", 70, Color.Black, FontWeight.Bold, Color(0xFFE2E8F0))
                        }

                        // Header Level 2 (Sub-columns)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.background(Color(0xFFF1F5F9))
                        ) {
                            RegisterCell("", 50)
                            RegisterCell("", 90)
                            RegisterCell("", 140)

                            // Naney 1 sub-columns
                            RegisterCell("வாய்மொழி\n[$n1OralMax]", 80, Color(0xFF991B1B), FontWeight.Bold, Color(0xFFFFF1F2))
                            RegisterCell("செயல்பாடு\n[$n1ActMax]", 80, Color(0xFF991B1B), FontWeight.Bold, Color(0xFFFFF1F2))
                            RegisterCell("எழுத்து\n[$n1WriMax]", 80, Color(0xFF991B1B), FontWeight.Bold, Color(0xFFFFF1F2))

                            // Naney 2 sub-columns
                            RegisterCell("வாய்மொழி\n[$n2OralMax]", 80, Color(0xFF991B1B), FontWeight.Bold, Color(0xFFFFF1F2))
                            RegisterCell("செயல்பாடு\n[$n2ActMax]", 80, Color(0xFF991B1B), FontWeight.Bold, Color(0xFFFFF1F2))
                            RegisterCell("எழுத்து\n[$n2WriMax]", 80, Color(0xFF991B1B), FontWeight.Bold, Color(0xFFFFF1F2))

                            // Thiranari sub-columns
                            RegisterCell("வாய்மொழி\n[$thOralMax]", 85, Color(0xFF92400E), FontWeight.Bold, Color(0xFFFEFCE8))
                            RegisterCell("எழுத்து\n[$thWriMax]", 85, Color(0xFF92400E), FontWeight.Bold, Color(0xFFFEFCE8))

                            RegisterCell("100", 70, Color.Black, FontWeight.Bold)
                            RegisterCell("%", 70, Color.Black, FontWeight.Bold)
                        }

                        // Student Rows
                        students.forEachIndexed { index, student ->
                            val currentInput = classMarksMap[student.id] ?: MarksInputData()
                            val n1Oral = currentInput.naney1Oral
                            val n1Act = currentInput.naney1Activity
                            val n1Wri = currentInput.naney1Written
                            val n2Oral = currentInput.naney2Oral
                            val n2Act = currentInput.naney2Activity
                            val n2Wri = currentInput.naney2Written
                            val thOral = currentInput.thiranariOral
                            val thWri = currentInput.thiranariWritten
                            val total = n1Oral + n1Act + n1Wri + n2Oral + n2Act + n2Wri + thOral + thWri

                            val rowBg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.background(rowBg)
                            ) {
                                RegisterCell("${index + 1}", 50)
                                RegisterCell(student.admissionNo, 90)
                                RegisterCell(student.name, 140, alignLeft = true)

                                // Editable cells: Naney 1
                                RegisterEditableCell(n1Oral, 80, n1OralMax) {
                                    classMarksMap[student.id] = currentInput.copy(naney1Oral = it)
                                }
                                RegisterEditableCell(n1Act, 80, n1ActMax) {
                                    classMarksMap[student.id] = currentInput.copy(naney1Activity = it)
                                }
                                RegisterEditableCell(n1Wri, 80, n1WriMax) {
                                    classMarksMap[student.id] = currentInput.copy(naney1Written = it)
                                }

                                // Editable cells: Naney 2
                                RegisterEditableCell(n2Oral, 80, n2OralMax) {
                                    classMarksMap[student.id] = currentInput.copy(naney2Oral = it)
                                }
                                RegisterEditableCell(n2Act, 80, n2ActMax) {
                                    classMarksMap[student.id] = currentInput.copy(naney2Activity = it)
                                }
                                RegisterEditableCell(n2Wri, 80, n2WriMax) {
                                    classMarksMap[student.id] = currentInput.copy(naney2Written = it)
                                }

                                // Editable cells: Thiranari
                                RegisterEditableCell(thOral, 85, thOralMax) {
                                    classMarksMap[student.id] = currentInput.copy(thiranariOral = it)
                                }
                                RegisterEditableCell(thWri, 85, thWriMax) {
                                    classMarksMap[student.id] = currentInput.copy(thiranariWritten = it)
                                }

                                // Calculated Total & %
                                RegisterCell("$total", 70, Color.Black, FontWeight.Bold)
                                RegisterCell("$total%", 70, Color.Black, FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RegisterCell(
    text: String,
    widthDp: Int,
    textColor: Color = Color.Black,
    fontWeight: FontWeight = FontWeight.Normal,
    bgColor: Color = Color.Transparent,
    alignLeft: Boolean = false
) {
    Box(
        modifier = Modifier
            .width(widthDp.dp)
            .height(38.dp)
            .background(bgColor)
            .padding(horizontal = 4.dp),
        contentAlignment = if (alignLeft) Alignment.CenterStart else Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.5.sp,
            fontWeight = fontWeight,
            color = textColor
        )
    }
}

@Composable
private fun RegisterEditableCell(
    value: Int,
    widthDp: Int,
    maxVal: Int,
    onValueChange: (Int) -> Unit
) {
    var textVal by remember(value) { mutableStateOf(value.toString()) }

    Box(
        modifier = Modifier
            .width(widthDp.dp)
            .height(38.dp)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        OutlinedTextField(
            value = textVal,
            onValueChange = { input ->
                textVal = input
                val parsed = input.toIntOrNull()
                if (parsed != null) {
                    onValueChange(parsed.coerceIn(0, maxVal))
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}
