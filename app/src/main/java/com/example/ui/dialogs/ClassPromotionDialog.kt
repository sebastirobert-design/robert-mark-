package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Student
import com.example.ui.theme.AcademicBlue
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBg
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextNavy

@Composable
fun ClassPromotionDialog(
    allStudents: List<Student>,
    currentAcademicYear: String,
    onDismiss: () -> Unit,
    onConfirmPromotion: (promoteAll: Boolean, targetClass: Int?, newYear: String, archiveClass8: Boolean) -> Unit
) {
    // Auto calculate next academic year, e.g. "2026-2027" -> "2027-2028"
    val defaultNextYear = remember(currentAcademicYear) {
        val parts = currentAcademicYear.split("-")
        if (parts.size == 2) {
            val y1 = parts[0].trim().toIntOrNull() ?: 2026
            val y2 = parts[1].trim().toIntOrNull() ?: 2027
            "${y1 + 1}-${y2 + 1}"
        } else {
            "2027-2028"
        }
    }

    var promoteAll by remember { mutableStateOf(true) }
    var selectedSingleClass by remember { mutableIntStateOf(1) }
    var newAcademicYear by remember { mutableStateOf(defaultNextYear) }
    var keepClass8AsPassedOut by remember { mutableStateOf(true) }

    val totalPromotable = if (promoteAll) {
        allStudents.count { it.stdClass in 1..8 }
    } else {
        allStudents.count { it.stdClass == selectedSingleClass }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("class_promotion_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = NavyPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "மாணவர் வகுப்பு உயர்வு (Promotion)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextNavy
                            )
                            Text(
                                text = "மூன்றாம் பருவத் தேர்வு முடிவுகள் அடிப்படையில்",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "மூடுக", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Info banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AcademicBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "3-ம் பருவத் தேர்வுகள் முடிந்ததும் மாணவர்களை அடுத்த வகுப்பிற்கு முன்னேற்றி புதிய கல்வியாண்டிற்கு மாற்றலாம்.",
                            fontSize = 12.sp,
                            color = Color(0xFF1E40AF),
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Promotion Scope Selector
                Text(
                    text = "உயர்த்த வேண்டிய முறை:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = NavyPrimary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = promoteAll,
                        onClick = { promoteAll = true },
                        colors = RadioButtonDefaults.colors(selectedColor = NavyPrimary)
                    )
                    Text(
                        text = "அனைத்து வகுப்புகளும் ஒரே நேரத்தில் (1 முதல் 8 வரை)",
                        fontSize = 13.sp,
                        fontWeight = if (promoteAll) FontWeight.Bold else FontWeight.Normal,
                        color = NavyDark
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !promoteAll,
                        onClick = { promoteAll = false },
                        colors = RadioButtonDefaults.colors(selectedColor = NavyPrimary)
                    )
                    Text(
                        text = "குறிப்பிட்ட வகுப்பை மட்டும் உயர்த்துதல்",
                        fontSize = 13.sp,
                        fontWeight = if (!promoteAll) FontWeight.Bold else FontWeight.Normal,
                        color = NavyDark
                    )
                }

                if (!promoteAll) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, top = 6.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..8).forEach { c ->
                            val isSel = selectedSingleClass == c
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedSingleClass = c },
                                label = { Text("வகுப்பு $c", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Promotion Plan Cards
                Text(
                    text = "வகுப்பு உயர்வு விபரம்:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = NavyPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val classesToShow = if (promoteAll) (1..8).toList() else listOf(selectedSingleClass)
                    classesToShow.forEach { c ->
                        val count = allStudents.count { it.stdClass == c }
                        val targetLabel = if (c == 8) "பள்ளி நிறைவு (9-ம் வகுப்பு)" else "வகுப்பு ${c + 1}"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "வகுப்பு $c ($count மாணவர்கள்)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyDark
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = AcademicBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = targetLabel,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (c == 8) AmberGold else EmeraldPass
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // New Academic Year Input
                OutlinedTextField(
                    value = newAcademicYear,
                    onValueChange = { newAcademicYear = it },
                    label = { Text("புதிய கல்வியாண்டு (New Academic Year)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Class 8 Option
                if (promoteAll || selectedSingleClass == 8) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = keepClass8AsPassedOut,
                            onCheckedChange = { keepClass8AsPassedOut = it },
                            colors = CheckboxDefaults.colors(checkedColor = NavyPrimary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "வகுப்பு 8 மாணவர்களை 'பள்ளி நிறைவு' (Passed Out) என சேமித்து வைக்க",
                            fontSize = 12.sp,
                            color = TextDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Warning card
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "மொத்தம் $totalPromotable மாணவர்கள் அடுத்த வகுப்பிற்கு உயர்த்தப்படுவர். உறுதி செய்கிறீர்களா?",
                            fontSize = 11.5.sp,
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ரத்து செய்")
                    }

                    Button(
                        onClick = {
                            onConfirmPromotion(
                                promoteAll,
                                if (promoteAll) null else selectedSingleClass,
                                newAcademicYear.trim(),
                                !keepClass8AsPassedOut
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPass),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("confirm_promotion_btn")
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("வகுப்பு உயர்வு செய்", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
