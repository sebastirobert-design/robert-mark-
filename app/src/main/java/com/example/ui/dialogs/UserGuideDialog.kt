package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AcademicBlue
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

data class GuideSection(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val points: List<String>
)

@Composable
fun UserGuideDialog(
    onDismiss: () -> Unit
) {
    val sections = remember {
        listOf(
            GuideSection(
                title = "1. பள்ளி விவரங்கள் அமைத்தல் (Settings)",
                subtitle = "பள்ளியின் ஆரம்ப அமைப்புகள் & காப்புப்பிரதி",
                icon = Icons.Default.Settings,
                color = AcademicBlue,
                points = listOf(
                    "பள்ளியின் பெயர், UDISE குறியீடு, ஒன்றியம், மாவட்டம் மற்றும் தலைமையாசிரியர் பெயரை உள்ளிட்டு சேமிக்கவும்.",
                    "பருவம் 1, 2, 3 ஆகியவற்றின் பள்ளி வேலை நாட்களை உள்ளிடவும். இது மாணவர்களின் வருகை சதவீதத்தை தானாகக் கணக்கிட உதவும்.",
                    "காப்புப்பிரதி (Backup Data): உங்கள் பள்ளி மாணவர் மற்றும் மதிப்பெண் தகவல்களை JSON கோப்பாக உங்கள் போனில் பாதுகாப்பாக சேமிக்கலாம். தேவைப்படும் போது 'மீட்டெடு (Restore)' மூலம் பழைய தகவல்களைப் பெறலாம்."
                )
            ),
            GuideSection(
                title = "2. மாணவர் சேர்க்கை (Student Admissions)",
                subtitle = "மாணவர் பட்டியல் & EMIS பதிவேற்றம்",
                icon = Icons.Default.People,
                color = EmeraldPass,
                points = listOf(
                    "வகுப்பு 1 முதல் 8 வரை தேர்ந்தெடுக்கலாம். 'புதிய மாணவர் சேர்க்கை' மூலம் மாணவர் பெயர், EMIS எண், சேர்க்கை எண், பாலினம், பிறந்த தேதி பதிவு செய்யலாம்.",
                    "EMIS CSV பதிவேற்றம்: TN EMIS தளத்தில் இருந்து பதிவிறக்கம் செய்த CSV கோப்பை ஒரே கிளிக்கில் பதிவேற்றி அனைத்து மாணவர்களையும் சேர்க்கலாம்.",
                    "மாணவர் பெயரைத் தொட்டு விவரங்களைத் திருத்தவோ (Edit) அல்லது நீக்கவோ (Delete) செய்யலாம்."
                )
            ),
            GuideSection(
                title = "3. CCE மதிப்பெண் உள்ளீடு (Marks Entry)",
                subtitle = "பருவ மதிப்பெண்கள், கற்றல் நிலைகள் & உடற்கல்வி",
                icon = Icons.Default.EditNote,
                color = AmberGold,
                points = listOf(
                    "வகுப்பு 1 முதல் 3 (தொடக்கப் பிரிவு): 4 பாடங்கள் (தமிழ், ஆங்கிலம், கணிதம், சூழ்நிலையியல் - EVS). FA(a): 20, FA(b): 20, SA: 60 = மொத்தம் 100 மதிப்பெண்கள். கற்றல் நிலை (அரும்பு: 0-40, மொட்டு: 41-70, மலர்: 71-100) தானாக நிர்ணயிக்கப்படும். பருவம் மொத்தம்: 400 மதிப்பெண்கள்.",
                    "வகுப்பு 4 & 5: 5 பாடங்கள் (தமிழ், ஆங்கிலம், கணிதம், அறிவியல், சமூக அறிவியல்) - மொத்தம் 500 மதிப்பெண்கள்.",
                    "வகுப்பு 6 முதல் 8 (நடுநிலைப் பிரிவு): 5 முதன்மைப் பாடங்கள் (மொத்தம் 500) + உடற்கல்வி (100 மதிப்பெண்கள்) தனியாக கணக்கிடப்பட்டு தரம் (A, B, C, D) வழங்கப்படும்.",
                    "மாணவர் வருகை நாட்களை உள்ளிடும் போது தானாக வருகை சதவீதம் கணக்கிடப்படும்."
                )
            ),
            GuideSection(
                title = "4. முப்பருவ சராசரி பட்டியல் (Consolidation)",
                subtitle = "அலுவலக பதிவேடு, Excel & PDF பதிவிறக்கம்",
                icon = Icons.Default.Assessment,
                color = NavyPrimary,
                points = listOf(
                    "பருவம் 1, பருவம் 2, பருவம் 3 மற்றும் ஆண்டு முப்பருவ சராசரி மதிப்பெண் பட்டியலை வகுப்பு வாரியாகப் பார்க்கலாம்.",
                    "Excel (CSV) பதிவிறக்கம்: அனைத்து மாணவர்களின் மதிப்பெண்களையும் கணினியில் பயன்படுத்தும் வகையில் CSV வடிவில் பதிவிறக்கலாம்.",
                    "PDF பதிவிறக்கம்: தமிழ்நாடு அரசு பள்ளி கல்வித்துறை படிவ அமைப்பில் நேரடி அச்சுக்கு உகந்த PDF பதிவேட்டைத் தயாரித்துக் கொள்ளலாம்.",
                    "A4 அட்டைகள் (Rank Cards): பருவம் 1, 2, 3-இல் ஒரு A4 தாளில் இரண்டு மாணவர்களின் தரவரிசை அறிக்கையை அச்சு எடுக்கலாம்.",
                    "வகுப்பு உயர்வு (Class Promotion): கல்வி ஆண்டு நிறைவில் ஒரு கிளிக்கில் அனைத்து மாணவர்களையும் அடுத்த வகுப்பிற்கு உயர்த்தலாம் (வகுப்பு 8 மாணவர்கள் தேர்ச்சி பெற்று வெளியேறுவர்)."
                )
            ),
            GuideSection(
                title = "5. ஆண்டு இறுதி சான்றிதழ் (Certificates)",
                subtitle = "8-ஆம் வகுப்பு நிறைவுச் சான்றிதழ்",
                icon = Icons.Default.School,
                color = Color(0xFFB45309),
                points = listOf(
                    "எட்டாம் வகுப்பு முடிக்கும் மாணவர்களுக்கு தமிழ்நாடு அரசு வழிகாட்டுதலின்படி 5 முதன்மைப் பாடங்களின் முப்பருவ சராசரி மற்றும் உடற்கல்வி தரத்துடன் கூடிய சான்றிதழ் தானாக உருவாக்கப்படும்.",
                    "மாணவர் நன்னடத்தை சான்று, வருகை சதவீதம் மற்றும் தலைமையாசிரியர் கையொப்பத்துடன் அழகிய A4 PDF வடிவில் அச்சிடலாம்."
                )
            ),
            GuideSection(
                title = "6. பெற்றோர் கூட்ட கடிதம் (Parent Letters)",
                subtitle = "PTA / SMC அழைப்பிதழ்கள்",
                icon = Icons.Default.MenuBook,
                color = Color(0xFF0284C7),
                points = listOf(
                    "பெற்றோர் ஆசிரியர் கழகம் (PTA) மற்றும் பள்ளி மேலாண்மைக் குழு (SMC) கூட்டங்களுக்கு ஒவ்வொரு மாணவர் பெயரிட்ட அழைப்பிதழ் கடிதங்களை உருவாக்கலாம்.",
                    "கூட்டத் தேதி மற்றும் விவரங்களைத் தட்டச்சு செய்து, முழு வகுப்பு மாணவர்களுக்கும் ஒரே நேரத்தில் PDF கடிதங்கள் தயார் செய்யலாம்."
                )
            )
        )
    }

    var expandedIndex by remember { mutableStateOf<Int?>(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .testTag("user_guide_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Surface(
                    color = NavyPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "பயனர் வழிகாட்டி (User Guide)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "செயலியை எளிதாகப் பயன்படுத்தும் வழிமுறைகள்",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "மூடு",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Quick Note Banner
                Surface(
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(0.5.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡 குறிப்பு: தலைப்புகளைத் தொட்டு விரிவான வழிகாட்டுக் குறிப்புகளை வாசிக்கலாம்.",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = NavyDark
                        )
                    }
                }

                // Guide Content List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sections.size) { index ->
                        val section = sections[index]
                        val isExpanded = expandedIndex == index

                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isExpanded) section.color.copy(alpha = 0.04f) else Color.White
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isExpanded) section.color.copy(alpha = 0.4f) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedIndex = if (isExpanded) null else index
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = section.color.copy(alpha = 0.12f),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = section.icon,
                                                contentDescription = null,
                                                tint = section.color,
                                                modifier = Modifier.padding(6.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = section.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = NavyDark
                                            )
                                            Text(
                                                text = section.subtitle,
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = if (isExpanded) section.color else Color.Gray,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                AnimatedVisibility(visible = isExpanded) {
                                    Column(
                                        modifier = Modifier.padding(top = 10.dp, start = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        section.points.forEach { point ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Text(
                                                    text = "•",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = section.color,
                                                    modifier = Modifier.padding(end = 6.dp)
                                                )
                                                Text(
                                                    text = point,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF1E293B),
                                                    lineHeight = 17.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Footer Close Button
                Surface(
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        androidx.compose.material3.Button(
                            onClick = onDismiss,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = NavyPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("புரிந்தது (Close)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
