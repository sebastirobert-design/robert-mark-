package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.dialogs.UserGuideDialog
import com.example.ui.theme.AcademicBlue
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBg
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextNavy
import com.example.util.BackupRestoreManager
import com.example.viewmodel.SchoolMarksViewModel
import java.io.File

@Composable
fun SchoolSettingsScreen(
    viewModel: SchoolMarksViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val schoolProfile by viewModel.schoolProfile.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()

    var schoolName by remember(schoolProfile) { mutableStateOf(schoolProfile.schoolName) }
    var udiseCode by remember(schoolProfile) { mutableStateOf(schoolProfile.udiseCode) }
    var unionName by remember(schoolProfile) { mutableStateOf(schoolProfile.unionName) }
    var districtName by remember(schoolProfile) { mutableStateOf(schoolProfile.districtName) }
    var academicYear by remember(schoolProfile) { mutableStateOf(schoolProfile.academicYear) }
    var headmasterName by remember(schoolProfile) { mutableStateOf(schoolProfile.headmasterName) }

    // Unified school working days (பள்ளி வேலை நாட்கள் - ஒரே கட்டம்)
    val initialDays = if (schoolProfile.term1WorkingDays > 0) schoolProfile.term1WorkingDays.toString()
        else if (schoolProfile.term2WorkingDays > 0) schoolProfile.term2WorkingDays.toString()
        else if (schoolProfile.term3WorkingDays > 0) schoolProfile.term3WorkingDays.toString()
        else "78"
    var schoolWorkingDaysStr by remember(schoolProfile) {
        mutableStateOf(initialDays)
    }
    val schoolWorkingDays = schoolWorkingDaysStr.toIntOrNull() ?: 78

    var showApplyWorkingDaysDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showUserGuideDialog by remember { mutableStateOf(false) }
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
    var lastExportedFile by remember { mutableStateOf<File?>(null) }

    // File picker launcher for JSON backup restore
    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                }
                if (!jsonString.isNullOrBlank()) {
                    pendingRestoreJson = jsonString
                    showRestoreConfirmDialog = true
                } else {
                    Toast.makeText(context, "தேர்ந்தெடுக்கப்பட்ட கோப்பு காலியாக உள்ளது!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "கோப்பைப் படிக்க முடியவில்லை: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("school_settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Banner
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "பள்ளி அமைப்புகள் & வேலை நாட்கள்",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "அனைத்து சான்றிதழ்கள், பதிவேடு மற்றும் காப்புநகல் அமைப்புகள்",
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Section 1: School Profile Information
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("school_profile_section")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "பள்ளி மற்றும் தலைமை ஆசிரியர் விவரங்கள்",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NavyDark
                    )

                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        label = { Text("பள்ளியின் பெயர் (School Name)") },
                        placeholder = { Text("எ.கா. ஊராட்சி ஒன்றிய நடுநிலைப்பள்ளி நகரகுடி") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_school_name")
                    )

                    OutlinedTextField(
                        value = udiseCode,
                        onValueChange = { udiseCode = it.trim() },
                        label = { Text("பள்ளி UDISE குறியீடு (DISE Code)") },
                        placeholder = { Text("33230500103") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_udise_code")
                    )

                    OutlinedTextField(
                        value = unionName,
                        onValueChange = { unionName = it },
                        label = { Text("ஒன்றியம் (Union / Block)") },
                        placeholder = { Text("எ.கா. இளையான்குடி ஒன்றியம்") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_union_name")
                    )

                    OutlinedTextField(
                        value = districtName,
                        onValueChange = { districtName = it },
                        label = { Text("மாவட்டம் (District)") },
                        placeholder = { Text("எ.கா. சிவகங்கை மாவட்டம்") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_district_name")
                    )

                    OutlinedTextField(
                        value = academicYear,
                        onValueChange = { academicYear = it },
                        label = { Text("கல்வியாண்டு (Academic Year)") },
                        placeholder = { Text("எ.கா. 2026-2027") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_academic_year")
                    )

                    OutlinedTextField(
                        value = headmasterName,
                        onValueChange = { headmasterName = it },
                        label = { Text("தலைமை ஆசிரியர் பெயர் (Headmaster Name)") },
                        placeholder = { Text("எ.கா. ராபர்ட் செபாஸ்டின் ஜோ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_headmaster_name")
                    )
                }
            }
        }

        // Section 2: School Working Days Setting (பள்ளி வேலை நாட்கள்) - Single unified box
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("working_days_section")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AcademicBlue.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = AcademicBlue,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "பள்ளி வேலை நாட்கள் (School Working Days)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyDark
                            )
                            Text(
                                text = "ஒரே பதிவு - உடனடியாக அனைத்து மாணவர்களுக்கும் காட்டப்படும்!",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    // Single Input Box for School Working Days
                    OutlinedTextField(
                        value = schoolWorkingDaysStr,
                        onValueChange = { schoolWorkingDaysStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("பள்ளி வேலை நாட்கள் (School Working Days)") },
                        placeholder = { Text("78") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_school_working_days"),
                        supportingText = {
                            Text(
                                text = "எ.கா: ஜூன் to செப்: 78 | ஜூன் to டிச: 155 | ஜூன் to ஏப்: 220",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    )

                    // Helper banner
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AcademicBlue.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, AcademicBlue.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "பள்ளி வேலை நாட்கள்: $schoolWorkingDays நாட்கள்",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = TextNavy
                            )
                            Text(
                                text = "இங்கு பதிவிடும் $schoolWorkingDays நாட்கள் அனைத்து மாணவர்களுக்கும் பள்ளி வேலை நாட்களாகக் காட்டப்படும். ஆசிரியர் மதிப்பெண் பதிவில் மாணவர் வருகை நாட்களை மட்டும் பதிவிட்டால் போதும்.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Button to apply to all students immediately
                    Button(
                        onClick = {
                            viewModel.applySchoolWorkingDaysToAllStudents(schoolWorkingDays)
                            Toast.makeText(
                                context,
                                "அனைத்து ${allStudents.size} மாணவர்களுக்கும் $schoolWorkingDays வேலை நாட்கள் பொருத்தப்பட்டன!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPass),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("apply_working_days_all_btn")
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "அனைத்து மாணவர்களுக்கும் இந்த வேலை நாட்களைப் பொருத்துக (${allStudents.size} பேர்)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    }

                    // Save settings button
                    Button(
                        onClick = {
                            viewModel.updateSchoolProfileAndApplyWorkingDays(
                                schoolName = schoolName,
                                udiseCode = udiseCode,
                                unionName = unionName,
                                districtName = districtName,
                                academicYear = academicYear,
                                headmasterName = headmasterName,
                                schoolWorkingDays = schoolWorkingDays
                            )
                            Toast.makeText(
                                context,
                                "பள்ளி விவரங்கள் & அனைத்து மாணவர்களுக்கும் ($schoolWorkingDays) வேலை நாட்கள் சேமிக்கப்பட்டன!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("save_school_settings_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("அமைப்புகளை சேமிக்க (Save Settings)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }
                }
            }
        }

        // Section 3: Full Backup & Restore (ஒட்டு மொத்த Backup & Restore)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NavyPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ஒட்டு மொத்த Backup & Restore",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextNavy
                            )
                            Text(
                                text = "அனைத்து வகுப்புகள், மாணவர்கள், மதிப்பெண்கள், வருகை & அமைப்புகள் காப்புநகல்",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Text(
                        text = "மொத்த மாணவர்கள்: ${allStudents.size} பேர் • கல்வி ஆண்டு: ${schoolProfile.academicYear}",
                        fontSize = 12.sp,
                        color = TextDark,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.exportFullBackup(context) { file, summary ->
                                    if (file != null) {
                                        lastExportedFile = file
                                        BackupRestoreManager.shareBackupFile(context, file)
                                        Toast.makeText(context, summary, Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, summary, Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("export_backup_btn")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("காப்புநகல் (Backup)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                restoreFileLauncher.launch("application/json")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
                            border = BorderStroke(1.2.dp, NavyPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("restore_backup_btn")
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("மீட்டமை (Restore)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }

                    if (lastExportedFile != null) {
                        val isDark = isSystemInDarkTheme()
                        val backupInfoBg = if (isDark) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFF0FDF4)
                        val backupInfoBorder = if (isDark) Color(0xFF065F46) else Color(0xFFBBF7D0)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = backupInfoBg,
                            border = BorderStroke(1.dp, backupInfoBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "கடைசி காப்புநகல் கோப்பு:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = EmeraldPass
                                    )
                                    Text(
                                        text = lastExportedFile?.name ?: "",
                                        fontSize = 10.5.sp,
                                        color = TextDark
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        lastExportedFile?.let { BackupRestoreManager.shareBackupFile(context, it) }
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("பகிர்", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // User Guide Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Color(0xFF7C3AED).copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "பயனர் வழிகாட்டி (User Guide)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextNavy
                            )
                            Text(
                                text = "செயலியின் அனைத்து விதிகள் மற்றும் பயன்பாட்டு கையேடு",
                                fontSize = 11.5.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { showUserGuideDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_open_user_guide_btn")
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("பயனர் வழிகாட்டியை வாசிக்க (Open Guide)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Applying Working Days to All Students
    if (showApplyWorkingDaysDialog) {
        AlertDialog(
            onDismissRequest = { showApplyWorkingDaysDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, tint = EmeraldPass)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("வேலை நாட்களை பொருத்துதல்", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    text = "அனைத்து ${allStudents.size} மாணவர்களுக்கும் $schoolWorkingDays பள்ளி வேலை நாட்கள் தானாகப் பொருத்தப்படும்.\n\n" +
                            "(மாணவர்களின் வருகை நாட்கள் பத்திரமாக பாதுகாக்கப்படும். உறுதி செய்கிறீர்களா?)",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showApplyWorkingDaysDialog = false
                        viewModel.applySchoolWorkingDaysToAllStudents(schoolWorkingDays) {
                            Toast.makeText(context, "அனைத்து மாணவர்களுக்கும் $schoolWorkingDays வேலை நாட்கள் வெற்றிகரமாக பொருத்தப்பட்டன!", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPass)
                ) {
                    Text("ஆம், பொருத்துக")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyWorkingDaysDialog = false }) {
                    Text("ரத்து")
                }
            }
        )
    }

    // Confirmation Dialog for Full Restore
    if (showRestoreConfirmDialog && pendingRestoreJson != null) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                pendingRestoreJson = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("காப்புநகலை மீட்டமைக்கவா?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    text = "எச்சரிக்கை: புதிய காப்புநகல் கோப்பிலிருந்து தரவுகளை மீட்டமைக்கும்போது, தற்போதுள்ள தரவுகள் மாற்றப்படும்.\n\n" +
                            "தேர்ந்தெடுக்கப்பட்ட காப்புநகலை மீட்டமைக்க விரும்புகிறீர்களா?",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val json = pendingRestoreJson
                        showRestoreConfirmDialog = false
                        pendingRestoreJson = null
                        if (json != null) {
                            viewModel.restoreFullBackup(context, json) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("ஆம், மீட்டமைக்க")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRestoreConfirmDialog = false
                    pendingRestoreJson = null
                }) {
                    Text("ரத்து")
                }
            }
        )
    }

    if (showUserGuideDialog) {
        UserGuideDialog(onDismiss = { showUserGuideDialog = false })
    }
}
