package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppNavDestination(
    val titleTamil: String,
    val titleEnglish: String,
    val icon: ImageVector
) {
    DASHBOARD("முகப்பு", "Home", Icons.Default.Dashboard),
    STUDENTS("மாணவர் சேர்க்கை", "Admissions", Icons.Default.People),
    MARKS_ENTRY("மதிப்பெண் உள்ளீடு", "Marks Entry", Icons.Default.EditNote),
    CONSOLIDATION("முப்பருவ சராசரி", "Consolidation", Icons.Default.Assessment),
    CERTIFICATE("சான்றிதழ்", "Certificate", Icons.Default.School),
    PARENT_LETTERS("பெற்றோர் கடிதம்", "Parent Letters", Icons.Default.Email),
    SETTINGS("பள்ளி விவரங்கள்", "Settings", Icons.Default.Settings)
}
