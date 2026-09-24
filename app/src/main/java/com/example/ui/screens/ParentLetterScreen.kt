package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.Student
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ParentLetterScreen(
    viewModel: SchoolMarksViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val schoolProfile by viewModel.schoolProfile.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()

    var selectedClassFilter by remember { mutableIntStateOf(0) } // 0 = All classes, 1..8 = specific class
    var selectedStudentIds by remember(allStudents, selectedClassFilter) {
        val filtered = if (selectedClassFilter == 0) allStudents else allStudents.filter { it.stdClass == selectedClassFilter }
        mutableStateOf(filtered.map { it.id }.toSet())
    }

    val todayFormatted = remember {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        sdf.format(Date())
    }

    var letterTitle by remember { mutableStateOf("பெற்றோர் ஆசிரியர் கழகக் கூட்ட அழைப்பிதழ்") }
    var meetingDate by remember { mutableStateOf(todayFormatted) }
    var meetingPlace by remember { mutableStateOf("பிற்பகல் 3:00 மணி, பள்ளி வளாகம்") }
    var twoPerSheet by remember { mutableStateOf(true) }

    val defaultBodyPta = "மதிப்பிற்குரிய {parent_name} அவர்களுக்கு வணக்கம். நமது பள்ளியில் {date} அன்று {place} பெற்றோர் ஆசிரியர் கழக (PTA) கூட்டம் நடைபெற உள்ளது. தங்கள் குழந்தை {student_name} (வகுப்பு: {class_section}) அவர்களின் கல்வி வளர்ச்சி மற்றும் ஒழுக்கம் குறித்து கலந்தாலோசிக்க தாங்கள் தவறாமல் இக்கூட்டத்தில் கலந்துகொள்ளுமாறு அன்போடு அழைக்கிறோம்."
    val defaultBodySmc = "மதிப்பிற்குரிய {parent_name} அவர்களுக்கு வணக்கம். நமது பள்ளியின் வளர்ச்சி, மாணவர் நலன் மற்றும் உட்கட்டமைப்புத் திட்டங்கள் குறித்து ஆலோசிக்க பள்ளி மேலாண்மைக் குழு (SMC) கூட்டம் {date} அன்று {place} நடைபெற உள்ளது. தாங்கள் இக்கூட்டத்தில் தவறாமல் பங்கேற்று தங்களின் மேலான கருத்துக்களை வழங்குமாறு அன்புடன் அழைக்கிறோம்."
    val defaultBodyMarks = "மதிப்பிற்குரிய {parent_name} அவர்களுக்கு வணக்கம். தங்கள் குழந்தை {student_name} (வகுப்பு: {class_section}) அவர்களின் பருவத் தேர்வு மதிப்பெண்கள் மற்றும் கற்றல் அடைவுத்திறன் குறித்து வகுப்பு ஆசிரியருடன் கலந்தாலோசிக்க {date} அன்று {place} நேரில் வருகை தருமாறு கேட்டுக்கொள்கிறோம்."
    val defaultBodyNotice = "மதிப்பிற்குரிய {parent_name} அவர்களுக்கு வணக்கம். நமது பள்ளியில் பயிலும் தங்கள் குழந்தை {student_name} அவர்களின் நலன் மற்றும் கற்றல் முன்னேற்றம் கருதி கீழ்க்கண்ட முக்கிய அறிவிப்புகள் வழங்கப்படுகின்றன. தாங்கள் இவற்றை கவனித்து ஆசிரியர்களுடன் ஒத்துழைக்குமாறு அன்போடு கேட்டுக்கொள்கிறோம்."

    var letterBody by remember { mutableStateOf(defaultBodyPta) }

    val displayedStudents = remember(allStudents, selectedClassFilter) {
        if (selectedClassFilter == 0) allStudents else allStudents.filter { it.stdClass == selectedClassFilter }
    }
    val targetStudents = remember(displayedStudents, selectedStudentIds) {
        displayedStudents.filter { selectedStudentIds.contains(it.id) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("parent_letter_screen"),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header Banner
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AcademicBlue.copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = AcademicBlue,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "பெற்றோர் கூட்ட அழைப்பிதழ் / பொது கடிதம்",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextNavy
                        )
                        Text(
                            text = "ஒவ்வொரு பெற்றோருக்கும் பெயர் குறிப்பிட்டு தானாகவே தனிப்பயனாக்கப்பட்ட PDF கடிதம் உருவாக்கப்படும்.",
                            fontSize = 11.5.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // 2. Class Filter & Student Count Selection
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "1. கடிதம் அனுப்ப வேண்டிய வகுப்பு & மாணவர்கள்:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NavyDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Class Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedClassFilter == 0,
                            onClick = {
                                selectedClassFilter = 0
                                selectedStudentIds = allStudents.map { it.id }.toSet()
                            },
                            label = { Text("அனைத்து வகுப்புகளும் (1-8)", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                        for (c in 1..8) {
                            FilterChip(
                                selected = selectedClassFilter == c,
                                onClick = {
                                    selectedClassFilter = c
                                    val filtered = allStudents.filter { it.stdClass == c }
                                    selectedStudentIds = filtered.map { it.id }.toSet()
                                },
                                label = { Text("வகுப்பு $c", fontSize = 11.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "தேர்ந்தெடுக்கப்பட்ட மாணவர்கள்: ${targetStudents.size} / ${displayedStudents.size}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = AcademicBlue
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    selectedStudentIds = displayedStudents.map { it.id }.toSet()
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("அனைவரும்", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    selectedStudentIds = emptySet()
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("நீக்கு", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Letter Templates
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "2. பொதுவான மாதிரி கடிதங்கள் (Template தேர்வு):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NavyDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = letterTitle.contains("PTA") || letterTitle.contains("பெற்றோர் ஆசிரியர்"),
                            onClick = {
                                letterTitle = "பெற்றோர் ஆசிரியர் கழகக் கூட்ட அழைப்பிதழ்"
                                letterBody = defaultBodyPta
                            },
                            label = { Text("PTA கூட்ட அழைப்பு", fontSize = 11.5.sp) },
                            shape = RoundedCornerShape(6.dp)
                        )
                        FilterChip(
                            selected = letterTitle.contains("SMC") || letterTitle.contains("மேலாண்மைக் குழு"),
                            onClick = {
                                letterTitle = "பள்ளி மேலாண்மைக் குழு (SMC) கூட்ட அழைப்பிதழ்"
                                letterBody = defaultBodySmc
                            },
                            label = { Text("SMC கூட்ட அழைப்பு", fontSize = 11.5.sp) },
                            shape = RoundedCornerShape(6.dp)
                        )
                        FilterChip(
                            selected = letterTitle.contains("முன்னேற்ற") || letterTitle.contains("மதிப்பெண்"),
                            onClick = {
                                letterTitle = "கல்வி முன்னேற்றக் கலந்தாய்வுக் கூட்ட அழைப்பிதழ்"
                                letterBody = defaultBodyMarks
                            },
                            label = { Text("மதிப்பெண் கலந்தாய்வு", fontSize = 11.5.sp) },
                            shape = RoundedCornerShape(6.dp)
                        )
                        FilterChip(
                            selected = letterTitle.contains("அறிவிப்பு"),
                            onClick = {
                                letterTitle = "பள்ளி பொது அறிவிப்புக் கடிதம்"
                                letterBody = defaultBodyNotice
                            },
                            label = { Text("பொது அறிவிப்பு", fontSize = 11.5.sp) },
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }
            }
        }

        // 4. Letter Content Form
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "3. கடித விவரங்களை மாற்றியமைக்க (Custom Copy-Paste):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextNavy
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = letterTitle,
                        onValueChange = { letterTitle = it },
                        label = { Text("கடிதத் தலைப்பு (Title)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parent_letter_title_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = meetingDate,
                            onValueChange = { meetingDate = it },
                            label = { Text("கூட்டத் தேதி") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("parent_letter_date_input"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = meetingPlace,
                            onValueChange = { meetingPlace = it },
                            label = { Text("நேரம் & இடம்") },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("parent_letter_place_input"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = letterBody,
                        onValueChange = { letterBody = it },
                        label = { Text("கடித உள்ளடக்கம் (Body Text)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("parent_letter_body_input"),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 6
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Helpful Tags Explanation Card
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = NavyDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "தானாக மாறும் குறியீடுகள் (Auto-Tags):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = NavyDark
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "{parent_name} = பெற்றோர் பெயர்  |  {student_name} = மாணவர் பெயர்\n{class_section} = வகுப்பு  |  {date} = தேதி  |  {place} = இடம்",
                                fontSize = 10.5.sp,
                                color = Color(0xFF334155),
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Paper Saving Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFECFDF5), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ஒரு A4 தாளில் 2 கடிதங்கள் (காகித சிக்கனம்)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "இடையில் வெட்டுவதற்கான கோட்டுடன் (Cut guide) அச்சிடப்படும்.",
                                fontSize = 10.5.sp,
                                color = Color(0xFF047857)
                            )
                        }
                        Switch(
                            checked = twoPerSheet,
                            onCheckedChange = { twoPerSheet = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPass,
                                checkedTrackColor = EmeraldPass.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }

        // 5. Live Preview Card
        item {
            val isDark = isSystemInDarkTheme()
            val previewCardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFBEB)
            val previewBorder = if (isDark) Color(0xFF78350F) else Color(0xFFFDE68A)
            val previewRecipientBg = if (isDark) Color(0xFF334155) else Color.White

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "4. கடித மாதிரி முன்னோட்டம் (Live Preview):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val sampleStudent = targetStudents.firstOrNull() ?: Student(
                        id = 0,
                        admissionNo = "101",
                        name = "அ. கவியரசன்",
                        stdClass = 5,
                        section = "A",
                        gender = "M",
                        parentName = "அருள்செல்வம்"
                    )

                    val parentName = if (sampleStudent.parentName.isNotBlank()) sampleStudent.parentName else "${sampleStudent.name} அவர்களின் பெற்றோர்"
                    val previewBody = letterBody
                        .replace("{parent_name}", parentName)
                        .replace("{student_name}", sampleStudent.name)
                        .replace("{class_section}", "${sampleStudent.stdClass} - ${sampleStudent.section}")
                        .replace("{admission_no}", sampleStudent.admissionNo)
                        .replace("{date}", meetingDate)
                        .replace("{place}", meetingPlace)
                        .replace("{school_name}", schoolProfile.schoolName)

                    Surface(
                        color = previewCardBg,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, previewBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = schoolProfile.schoolName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextNavy
                            )
                            Text(
                                text = "${schoolProfile.unionName} | ${schoolProfile.districtName}",
                                fontSize = 10.5.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = AmberGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = letterTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("தேதி: $meetingDate", fontSize = 10.5.sp, color = TextDark)
                                Text("இடம்: $meetingPlace", fontSize = 10.5.sp, color = TextDark)
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                color = previewRecipientBg,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text(
                                        text = "பெறுநர்: திரு / திருமதி. $parentName",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "மாணவர்: ${sampleStudent.name}  |  வகுப்பு: ${sampleStudent.stdClass} - ${sampleStudent.section}",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "மதிப்பிற்குரிய பெற்றோர் அவர்களுக்கு வணக்கம்,\n$previewBody",
                                fontSize = 11.sp,
                                color = TextDark,
                                lineHeight = 16.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("வகுப்பு ஆசிரியர் கையொப்பம்", fontSize = 9.5.sp, color = TextDark)
                                Text("தலைமை ஆசிரியர் கையொப்பம்", fontSize = 9.5.sp, color = TextDark)
                            }
                        }
                    }
                }
            }
        }

        // 6. Action Export Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (targetStudents.isEmpty()) {
                            Toast.makeText(context, "மாணவர்களைத் தேர்ந்தெடுக்கவும்", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.exportParentLettersPdf(
                            context = context,
                            students = targetStudents,
                            letterTitle = letterTitle,
                            letterBody = letterBody,
                            meetingDate = meetingDate,
                            meetingPlace = meetingPlace,
                            twoPerSheet = twoPerSheet
                        ) { file ->
                            Toast.makeText(context, "${targetStudents.size} மாணவர்களுக்கான PDF தயார்!", Toast.LENGTH_SHORT).show()
                            PdfReportGenerator.viewPdfFile(context, file)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("generate_letters_pdf_btn")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF பார் / சேமி", fontSize = 12.5.sp)
                }

                Button(
                    onClick = {
                        if (targetStudents.isEmpty()) {
                            Toast.makeText(context, "மாணவர்களைத் தேர்ந்தெடுக்கவும்", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.exportParentLettersPdf(
                            context = context,
                            students = targetStudents,
                            letterTitle = letterTitle,
                            letterBody = letterBody,
                            meetingDate = meetingDate,
                            meetingPlace = meetingPlace,
                            twoPerSheet = twoPerSheet
                        ) { file ->
                            PdfReportGenerator.printPdfFile(context, file, "Parent_Letters")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("print_letters_pdf_btn")
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp), tint = NavyDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("அச்சிடுக (Print)", fontSize = 12.5.sp, color = NavyDark, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (targetStudents.isEmpty()) {
                            Toast.makeText(context, "மாணவர்களைத் தேர்ந்தெடுக்கவும்", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.exportParentLettersPdf(
                            context = context,
                            students = targetStudents,
                            letterTitle = letterTitle,
                            letterBody = letterBody,
                            meetingDate = meetingDate,
                            meetingPlace = meetingPlace,
                            twoPerSheet = twoPerSheet
                        ) { file ->
                            PdfReportGenerator.sharePdfFile(context, file, "$letterTitle - ${schoolProfile.schoolName}")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPass),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("share_letters_pdf_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
