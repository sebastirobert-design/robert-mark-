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
    val unionName: String = "இளையான்குடி ஒன்றியம்",
    val districtName: String = "சிவகங்கை மாவட்டம்",
    val academicYear: String = "2026-2027",
    val headmasterName: String = "மு. ஆரோக்கியசாமி M.A., B.Ed."
)

