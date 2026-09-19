package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Student admission details.
 */
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val admissionNo: String,       // சேர்க்கை எண் (e.g. 1267)
    val name: String,              // மாணவர் பெயர் (e.g. அ. அகிலேஷ்)
    val stdClass: Int,             // வகுப்பு (1 to 8)
    val section: String = "A",     // பிரிவு
    val gender: String = "ஆண்",    // பாலினம்: ஆண் / பெண்
    val community: String = "BC",  // இனம்: BC, MBC, SC, ST, OC, BCM, SCA
    val parentName: String = "",   // பெற்றோர் / பாதுகாவலர் பெயர்
    val academicYear: String = "2026-2027"
)
