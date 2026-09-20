package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppNavDestination
import com.example.ui.components.ClassChipsSelector
import com.example.ui.components.SchoolBannerCard
import com.example.ui.dialogs.UserGuideDialog
import com.example.ui.theme.AcademicBlue
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.util.ExcelReportGenerator
import com.example.util.PdfReportGenerator
import com.example.viewmodel.SchoolMarksViewModel
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: SchoolMarksViewModel,
    onNavigate: (AppNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val schoolProfile by viewModel.schoolProfile.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    var showUserGuideDialog by remember { mutableStateOf(false) }

    val classStudents = allStudents.filter { it.stdClass == selectedClass }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // School Profile Banner
        item {
            SchoolBannerCard(schoolProfile = schoolProfile)
        }

        // Class Filter Selector
        item {
            ClassChipsSelector(
                selectedClass = selectedClass,
                onSelectClass = { viewModel.selectClass(it) }
            )
        }

        // Action Buttons Grid
        item {
            Text(
                text = "முக்கிய செயல்பாடுகள் (Quick Actions)",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NavyDark,
                    fontSize = 14.sp
                ),
                modifier = Modifier.padding(top = 2.dp, bottom = 1.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionCard(
                    title = "மாணவர் சேர்க்கை",
                    subtitle = "புதிய மாணவர் பதிவு",
                    icon = Icons.Default.PersonAdd,
                    color = AcademicBlue,
                    onClick = { onNavigate(AppNavDestination.STUDENTS) },
                    modifier = Modifier.weight(1f),
                    testTag = "action_students"
                )

                QuickActionCard(
                    title = "மதிப்பெண் உள்ளீடு",
                    subtitle = "பருவ மதிப்பெண் பதிவு",
                    icon = Icons.Default.EditNote,
                    color = EmeraldPass,
                    onClick = { onNavigate(AppNavDestination.MARKS_ENTRY) },
                    modifier = Modifier.weight(1f),
                    testTag = "action_marks_entry"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionCard(
                    title = "முப்பருவ சராசரி",
                    subtitle = "அலுவலக பதிவேடு",
                    icon = Icons.Default.Assessment,
                    color = NavyPrimary,
                    onClick = { onNavigate(AppNavDestination.CONSOLIDATION) },
                    modifier = Modifier.weight(1f),
                    testTag = "action_consolidation"
                )

                QuickActionCard(
                    title = "மதிப்பெண் சான்றிதழ்",
                    subtitle = "ஆண்டு இறுதி சான்றிதழ்",
                    icon = Icons.Default.School,
                    color = AmberGold,
                    onClick = { onNavigate(AppNavDestination.CERTIFICATE) },
                    modifier = Modifier.weight(1f),
                    testTag = "action_certificate"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionCard(
                    title = "பெற்றோர் கடிதம்",
                    subtitle = "PTA / SMC அழைப்பிதழ்",
                    icon = Icons.Default.Email,
                    color = Color(0xFF0284C7),
                    onClick = { onNavigate(AppNavDestination.PARENT_LETTERS) },
                    modifier = Modifier.weight(1f),
                    testTag = "action_parent_letters"
                )

                QuickActionCard(
                    title = "பயனர் வழிகாட்டி",
                    subtitle = "முழு கையேடு & விதிகள்",
                    icon = Icons.Default.HelpOutline,
                    color = Color(0xFF7C3AED),
                    onClick = { showUserGuideDialog = true },
                    modifier = Modifier.weight(1f),
                    testTag = "action_user_guide"
                )
            }
        }

        // Export Actions Section
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = NavyPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "அலுவலக அவுட்புட் (Excel / PDF Export)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NavyDark
                            )
                            Text(
                                text = "வகுப்பு $selectedClass முப்பருவ சராசரி அறிக்கை",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.exportConsolidatedExcel(context, selectedClass) { file ->
                                    Toast.makeText(context, "Excel (CSV) தயாராக உள்ளது", Toast.LENGTH_SHORT).show()
                                    ExcelReportGenerator.shareCsvFile(
                                        context,
                                        file,
                                        "${schoolProfile.schoolName} - வகுப்பு $selectedClass முப்பருவ சராசரி"
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPass),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_excel_btn")
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Excel அவுட்புட்", fontSize = 11.5.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.exportConsolidatedPdf(context, selectedClass) { file ->
                                    Toast.makeText(context, "PDF அறிக்கை தயாராக உள்ளது", Toast.LENGTH_SHORT).show()
                                    PdfReportGenerator.sharePdfFile(
                                        context,
                                        file,
                                        "${schoolProfile.schoolName} - வகுப்பு $selectedClass முப்பருவ சராசரி பதிவேடு"
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_pdf_btn")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("PDF பதிவேடு", fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }

        // Format Description Explanatory Box
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "முப்பருவப் பிரிவுகள் (CCE வழிகாட்டுதல்):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = NavyDark
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. வகுப்பு 1 - 3 (தொடக்கப் பிரிவு): தமிழ், ஆங்கிலம், கணிதம் (FA(a)-20 + FA(b)-20 + SA-60 = 100, மொத்தம் 300 மதிப்பெண்கள்).",
                        fontSize = 11.5.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "2. வகுப்பு 4 - 5 (தொடக்க மேல்நிலைப் பிரிவு): தமிழ், ஆங்கிலம், கணக்கு, அறிவியல், சமூக அறிவியல் (மொத்தம் 500 மதிப்பெண்கள்).",
                        fontSize = 11.5.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "3. வகுப்பு 6 - 7 (நடுநிலைப் பிரிவு): தமிழ், ஆங்கிலம், கணக்கு, அறிவியல், சமூக அறிவியல் (மொத்தம் 500 மதிப்பெண்கள்).",
                        fontSize = 11.5.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "4. வகுப்பு 8 (சான்றிதழ் பிரிவு): 5 முதன்மைப் பாடங்கள் (மொத்தம் 500 மதிப்பெண்கள்) + உடற்கல்வி தனிக் கணக்கீடு & தரம் (A/B/C/D) மற்றும் ஆண்டு இறுதி சான்றிதழ்.",
                        fontSize = 11.5.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }

    if (showUserGuideDialog) {
        UserGuideDialog(onDismiss = { showUserGuideDialog = false })
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = modifier
            .testTag(testTag)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(6.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = NavyDark
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
