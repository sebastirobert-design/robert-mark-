package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * School profile information for Tamil Nadu government/panchayat schools.
 */
@Entity(tableName = "school_profile")
data class SchoolProfile(
    @PrimaryKey val id: Int = 1,
    val schoolName: String = "ஊராட்சி ஒன்றிய நடுநிலைப்பள்ளி நகரகுடி",
    val udiseCode: String = "33230500103",
    val unionName: String = "இளையான்குடி ஒன்றியம்",
    val districtName: String = "சிவகங்கை மாவட்டம்",
    val academicYear: String = "2026-2027",
    val headmasterName: String = "ராபர்ட் செபாஸ்டின் ஜோ",
    val term1WorkingDays: Int = 80,
    val term2WorkingDays: Int = 0,
    val term3WorkingDays: Int = 0
) {
    val displayNameWithUdise: String
        get() = if (udiseCode.isNotBlank()) "$schoolName (UDISE: $udiseCode)" else schoolName

    val totalWorkingDays: Int get() = term1WorkingDays + term2WorkingDays + term3WorkingDays

    fun getWorkingDaysForTerm(term: Int): Int = when (term) {
        1 -> term1WorkingDays
        2 -> term2WorkingDays
        3 -> term3WorkingDays
        else -> term1WorkingDays
    }
}

