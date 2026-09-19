package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AcademicBlue
import com.example.ui.theme.AmberGold
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun ClassChipsSelector(
    selectedClass: Int,
    onSelectClass: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "வகுப்பு தேர்வு செய்க (Select Class):",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = NavyDark,
                fontSize = 13.sp
            ),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            (1..8).forEach { cls ->
                val isSelected = cls == selectedClass
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectClass(cls) },
                    label = {
                        Text(
                            text = "வகுப்பு $cls",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = NavyDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("class_chip_$cls")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Format Description Badge based on selected class
        val (formatTitle, formatDesc, badgeColor) = when (selectedClass) {
            1, 2, 3 -> Triple(
                "வகுப்பு 1 - 3 வடிவம் (CCE Primary)",
                "3 பாடங்கள் (தமிழ், ஆங்கிலம், கணிதம்) • FA(a): 20, FA(b): 20, SA: 60 = மொத்தம் 100 • கற்றல் நிலை: அரும்பு/மொட்டு/மலர்",
                AcademicBlue
            )
            4, 5, 6, 7 -> Triple(
                "வகுப்பு 4 - 7 வடிவம் (CCE Upper Primary)",
                "5 பாடங்கள் (தமிழ், ஆங்கிலம், கணிதம், அறிவியல், சமூக அறிவியல்) • FA: 40, SA: 60 = மொத்தம் 100 (மொத்த மதிப்பெண்: 500)",
                AmberGold
            )
            else -> Triple(
                "எட்டாம் வகுப்பு வடிவம் (Class 8 Special CCE)",
                "5 பாடங்கள் (100 மதிப்பெண் வீதம் மொத்தம் 500) • முப்பருவ சராசரி பட்டியல் & ஆண்டு இறுதி மதிப்பெண் சான்றிதழ் அவுட்புட்",
                NavyPrimary
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = badgeColor.copy(alpha = 0.09f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = formatTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = badgeColor
                    )
                    Text(
                        text = formatDesc,
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
