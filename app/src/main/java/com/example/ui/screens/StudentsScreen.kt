package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import android.widget.Toast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.components.ClassChipsSelector
import com.example.ui.dialogs.ClassPromotionDialog
import com.example.ui.theme.AcademicBlue
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.util.CsvStudentImporter
import com.example.viewmodel.SchoolMarksViewModel

@Composable
fun StudentsScreen(
    viewModel: SchoolMarksViewModel,
    modifier: Modifier = Modifier
) {
    val selectedClass by viewModel.selectedClass.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val schoolProfile by viewModel.schoolProfile.collectAsState()

    val classStudents = allStudents.filter { it.stdClass == selectedClass }

    var showAddDialog by remember { mutableStateOf(false) }
    var showCsvImportDialog by remember { mutableStateOf(false) }
    var showPromotionDialog by remember { mutableStateOf(false) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NavyPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_student_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "மாணவர் சேர்க்கை")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("சேர்க்கை", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .testTag("students_screen")
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Class Filter
            ClassChipsSelector(
                selectedClass = selectedClass,
                onSelectClass = { viewModel.selectClass(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Google Sheet Upload / Clear / Reset / Promotion Quick Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showCsvImportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPass),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sheet பதிவேற்றம்", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                Button(
                    onClick = { showPromotionDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 7.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(15.dp), tint = NavyDark)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("வகுப்பு உயர்வு", fontSize = 11.sp, color = NavyDark, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showClearAllConfirmDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    contentPadding = PaddingValues(horizontal = 7.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFFDC2626))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("நீக்குக", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.resetToOfficial54Students() },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 7.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp), tint = NavyPrimary)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("54 பேர்", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Header summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "வகுப்பு $selectedClass மாணவர் பட்டியல் (${classStudents.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyDark,
                        fontSize = 15.sp
                    )
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AcademicBlue.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "மொத்த மாணவர்கள்: ${allStudents.size}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AcademicBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (classStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "வகுப்பு $selectedClass -ல் மாணவர்கள் பதிவு செய்யப்படவில்லை",
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "கீழே உள்ள 'சேர்க்கை' பொத்தானை அழுத்தி புதிய மாணவர்களை பதிவு செய்யவும்.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(classStudents) { index, student ->
                        StudentCard(
                            index = index + 1,
                            student = student,
                            onEdit = { studentToEdit = student },
                            onDelete = { studentToDelete = student }
                        )
                    }
                }
            }
        }
    }

    // Add Student Dialog
    if (showAddDialog) {
        StudentFormDialog(
            title = "புதிய மாணவர் சேர்க்கை (வகுப்பு $selectedClass)",
            defaultClass = selectedClass,
            onDismiss = { showAddDialog = false },
            onSave = { admNo, name, cls, sec, gender, comm, pName ->
                viewModel.addStudent(admNo, name, cls, sec, gender, comm, pName)
                showAddDialog = false
            }
        )
    }

    // Edit Student Dialog
    studentToEdit?.let { student ->
        StudentFormDialog(
            title = "மாணவர் விவரம் திருத்துக",
            student = student,
            defaultClass = student.stdClass,
            onDismiss = { studentToEdit = null },
            onSave = { admNo, name, cls, sec, gender, comm, pName ->
                viewModel.updateStudent(
                    student.copy(
                        admissionNo = admNo,
                        name = name,
                        stdClass = cls,
                        section = sec,
                        gender = gender,
                        community = comm,
                        parentName = pName
                    )
                )
                studentToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    studentToDelete?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("மாணவரை நீக்க வேண்டுமா?") },
            text = { Text("${student.name} (சேர்க்கை எண்: ${student.admissionNo}) அவர்களின் மதிப்பெண்கள் மற்றும் வருகை தகவல்களும் நீக்கப்படும்.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(student.id)
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("நீக்குக")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("ரத்து")
                }
            }
        )
    }

    // Google Sheet / CSV Import Dialog
    if (showCsvImportDialog) {
        CsvImportDialog(
            onDismiss = { showCsvImportDialog = false },
            onImport = { csvContent, replaceExisting ->
                viewModel.importStudentsFromCsv(
                    csvText = csvContent,
                    replaceExisting = replaceExisting,
                    onSuccess = { showCsvImportDialog = false }
                )
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

    // Clear All Students Confirmation Dialog
    if (showClearAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmDialog = false },
            icon = {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(36.dp))
            },
            title = {
                Text(
                    text = "மாணவர்களின் பெயர்களை நீக்கவா?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NavyDark
                )
            },
            text = {
                Text(
                    text = "இந்த ஆப்பில் தற்போது உள்ள மாதிரி மாணவர்களின் பெயர்கள் மற்றும் மதிப்பெண்கள் முழுவதும் நீக்கப்படும்.\n\nநீக்கிய பின், உங்கள் பள்ளி Google Sheet-லிருந்து புதிய மாணவர் பட்டியலை நேரடியாக 'Sheet பதிவேற்றம்' பொத்தான் மூலம் பதிவேற்றலாம்.",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllStudents {
                            showClearAllConfirmDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("ஆம், அனைவரையும் நீக்குக", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirmDialog = false }) {
                    Text("ரத்து")
                }
            }
        )
    }
}

@Composable
fun StudentCard(
    index: Int,
    student: Student,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_card_${student.admissionNo}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index & Admission Badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = NavyPrimary.copy(alpha = 0.09f),
                modifier = Modifier.size(36.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$index",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NavyPrimary
                    )
                    Text(
                        text = "எண்",
                        fontSize = 7.5.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Student Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = student.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NavyDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AmberGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "சே.எண்: ${student.admissionNo}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberGold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "பிரிவு: ${student.section} • ${student.gender} • ${student.community}",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    if (student.parentName.isNotEmpty()) {
                        Text(
                            text = " • த/பெ: ${student.parentName}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Actions
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "திருத்துக",
                    tint = AcademicBlue,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "நீக்குக",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun StudentFormDialog(
    title: String,
    student: Student? = null,
    defaultClass: Int = 8,
    onDismiss: () -> Unit,
    onSave: (admissionNo: String, name: String, stdClass: Int, section: String, gender: String, community: String, parentName: String) -> Unit
) {
    var admissionNo by remember { mutableStateOf(student?.admissionNo ?: "") }
    var name by remember { mutableStateOf(student?.name ?: "") }
    var stdClass by remember { mutableStateOf(student?.stdClass ?: defaultClass) }
    var section by remember { mutableStateOf(student?.section ?: "A") }
    var gender by remember { mutableStateOf(student?.gender ?: "ஆண்") }
    var community by remember { mutableStateOf(student?.community ?: "BC") }
    var parentName by remember { mutableStateOf(student?.parentName ?: "") }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    val communities = listOf("BC", "MBC", "SC", "ST", "OC", "BCM", "SCA")
    val genders = listOf("ஆண்", "பெண்")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = NavyDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                errorMsg?.let {
                    Text(text = it, color = Color.Red, fontSize = 12.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = admissionNo,
                        onValueChange = { admissionNo = it },
                        label = { Text("சேர்க்கை எண் *") },
                        placeholder = { Text("எ.கா. 1267") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_admission_no")
                    )

                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("பிரிவு") },
                        placeholder = { Text("A") },
                        singleLine = true,
                        modifier = Modifier.weight(0.5f)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("மாணவர் பெயர் (தமிழில்) *") },
                    placeholder = { Text("எ.கா. அ. அகிலேஷ்") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_name")
                )

                OutlinedTextField(
                    value = parentName,
                    onValueChange = { parentName = it },
                    label = { Text("பெற்றோர் / பாதுகாவலர் பெயர்") },
                    placeholder = { Text("எ.கா. அருண்") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Gender selector
                Text("பாலினம்:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    genders.forEach { g ->
                        val isSelected = gender == g
                        Button(
                            onClick = { gender = g },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) NavyPrimary else Color(0xFFE2E8F0),
                                contentColor = if (isSelected) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(g, fontSize = 12.sp)
                        }
                    }
                }

                // Community selector
                Text("இனம் (Community):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    communities.take(4).forEach { c ->
                        val isSelected = community == c
                        Button(
                            onClick = { community = c },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) AcademicBlue else Color(0xFFE2E8F0),
                                contentColor = if (isSelected) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(c, fontSize = 11.sp)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    communities.drop(4).forEach { c ->
                        val isSelected = community == c
                        Button(
                            onClick = { community = c },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) AcademicBlue else Color(0xFFE2E8F0),
                                contentColor = if (isSelected) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(c, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (admissionNo.isBlank() || name.isBlank()) {
                        errorMsg = "சேர்க்கை எண் மற்றும் பெயர் கட்டாயம்."
                    } else {
                        onSave(admissionNo, name, stdClass, section, gender, community, parentName)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier.testTag("save_student_btn")
            ) {
                Text("சேமிக்க")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ரத்து")
            }
        }
    )
}

@Composable
fun CsvImportDialog(
    onDismiss: () -> Unit,
    onImport: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var csvText by remember { mutableStateOf("") }
    var replaceExisting by remember { mutableStateOf(true) }
    var fileName by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val text = stream.bufferedReader().use { r -> r.readText() }
                    csvText = text
                    fileName = it.lastPathSegment ?: "தேர்ந்தெடுக்கப்பட்ட கோப்பு"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val parsedStudents = remember(csvText) {
        if (csvText.isBlank()) emptyList()
        else CsvStudentImporter.parseStudentsFromText(csvText)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = EmeraldPass,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Google Sheet / CSV பதிவேற்றம்", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "💡 கூகுள் ஷீட் (Google Sheet) மூலம் பதிவேற்றும் முறை:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NavyDark
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "1. Google Sheet அல்லது Excel-ல் EMIS எண், பெயர், வகுப்பு, பெற்றோர் பெயர் உள்ள வரிகளை அப்படியே Copy செய்து கீழே Paste செய்யவும்.\n2. அல்லது Google Sheet-ஐ 'கோப்பு > பதிவிறக்கு > .csv' செய்து 'கோப்பைத் தேர்ந்தெடு' அழுத்தவும்.",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString("EMIS No\tமாணவர் பெயர்\tவகுப்பு\tதந்தை பெயர்\n1032450482\tராஜமுருகன் வி\tI\tவேல்முருகன்"))
                                Toast.makeText(context, "Google Sheet மாதிரி வரிசை தலைப்புகள் நகலெடுக்கப்பட்டன!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = NavyPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("மாதிரி தலைப்புகளை நகலெடு (Copy Template)", fontSize = 10.5.sp, color = NavyPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = AcademicBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(fileName ?: "கோப்பைத் தேர்ந்தெடு", fontSize = 11.5.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            csvText = CsvStudentImporter.OFFICIAL_54_STUDENTS_CSV
                            fileName = "54_மாணவர்கள்_அரசு_பட்டியல்.csv"
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("அரசு 54 மாணவர்கள்", fontSize = 11.5.sp)
                    }
                }

                Text(
                    text = "அல்லது உரையை இங்கே ஒட்டவும் (Paste):",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyDark
                )

                OutlinedTextField(
                    value = csvText,
                    onValueChange = { csvText = it },
                    placeholder = {
                        Text("எ.கா:\n1032450482,ராஜமுருகன் வி,I,வேல்முருகன்\n1032452231,ரித்திகா,I,சுரேஷ்", fontSize = 11.sp)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                )

                if (parsedStudents.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldPass.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "✓ கண்டறியப்பட்ட மாணவர்கள்: ${parsedStudents.size} பேர்",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = EmeraldPass
                            )
                            val classCounts = (1..8).mapNotNull { c ->
                                val cnt = parsedStudents.count { it.stdClass == c }
                                if (cnt > 0) "வகுப்பு $c: $cnt" else null
                            }
                            if (classCounts.isNotEmpty()) {
                                Text(
                                    text = classCounts.joinToString(" • "),
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = replaceExisting,
                        onCheckedChange = { replaceExisting = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "பழைய மாதிரி மாணவர்களை நீக்கிவிட்டு புதிய மாணவர்களை சேர்க்க",
                        fontSize = 12.sp,
                        color = NavyDark
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (csvText.isNotBlank()) {
                        onImport(csvText, replaceExisting)
                    }
                },
                enabled = parsedStudents.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPass)
            ) {
                Text("பதிவேற்றுக (${parsedStudents.size})", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ரத்து")
            }
        }
    )
}

