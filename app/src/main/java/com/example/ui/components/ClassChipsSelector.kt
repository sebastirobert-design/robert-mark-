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
import com.example.ui.theme.CardBg
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TextDark

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
                        containerColor = CardBg,
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
            1, 2 -> Triple(
                "வகுப்பு 1, 2 வடிவம்",
                "4 பாடங்கள் (தமிழ், ஆங்கிலம், கணிதம், சூழ்நிலையியல்) • FA(a): 20, FA(b): 20, SA: 60 = மொத்தம் 100 (பருவம் மொத்தம்: 400)",
                AcademicBlue
            )
            3, 4, 5 -> Triple(
                "வகுப்பு 3, 4, 5 வடிவம்",
                "5 பாடங்கள் (தமிழ், ஆங்கிலம், கணிதம், அறிவியல், சமூக அறிவியல்) • FA: 40, SA: 60 = மொத்தம் 100 (பருவம் மொத்தம்: 500)",
                AmberGold
            )
            6, 7 -> Triple(
                "வகுப்பு 6, 7 வடிவம்",
                "5 முதன்மைப் பாடங்கள் (தமிழ், ஆங்கிலம், கணிதம், அறிவியல், சமூக அறிவியல்) • மொத்தம் 500 மதிப்பெண்கள்",
                AcademicBlue
            )
            else -> Triple(
                "வகுப்பு 8 வடிவம்",
                "5 பாடங்கள் (நேரடி 100 மதிப்பெண் வீதம் மொத்தம் 500) • உடற்கல்வி & ஆண்டு இறுதி சான்றிதழ்",
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
                        color = TextDark,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
