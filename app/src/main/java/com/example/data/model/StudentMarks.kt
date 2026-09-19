package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * CCE Marks for a student per term and subject.
 */
@Entity(
    tableName = "student_marks",
    indices = [Index(value = ["studentId", "term", "subjectKey"], unique = true)]
)
data class StudentMarks(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val studentId: Long,
    val term: Int,               // பருவம்: 1, 2, 3
    val subjectKey: String,      // TAMIL, ENGLISH, MATHS, SCIENCE, SOCIAL, PE

    // --- வகுப்பு 1 முதல் 3 புதிய பாடத்திட்டம் CCE கூறுகள் ---
    // நானே செய்வேன் - 1 [25 மதிப்பெண்]
    val naney1Oral: Int = 10,       // வாய்மொழி [10]
    val naney1Activity: Int = 10,   // செயல்திட்டம் / செயல்பாடு [10]
    val naney1Written: Int = 5,     // எழுத்துவழி [5]

    // நானே செய்வேன் - 2 [25 மதிப்பெண்]
    val naney2Oral: Int = 10,       // வாய்மொழி [10]
    val naney2Activity: Int = 10,   // செயல்திட்டம் / செயல்பாடு [10]
    val naney2Written: Int = 5,     // எழுத்துவழி [5]

    // திறனறி மதிப்பீடு [50 மதிப்பெண்]
    val thiranariOral: Int = 10,    // வாய்மொழி [10]
    val thiranariWritten: Int = 40, // எழுத்துவழி [40]

    // பொதுவான மதிப்புகள் (General & Summary)
    val faA: Int = 0,            // Class 1-3 naney1Total (out of 25)
    val faB: Int = 0,            // Class 1-3 naney2Total (out of 25)
    val faTotal: Int = 0,        // FA total (out of 50 for 1-3, or 40 for 4-8)
    val sa: Int = 0,             // SA / திறனறி (out of 50 for 1-3, or 60 for 4-8)
    val total: Int = 0,          // Total mark (out of 100)
    val learningLevel: String = "அரும்பு", // கற்றல் நிலை: அரும்பு / மொட்டு / மலர் (Class 1-3)
    val grade: String = "A"      // தரநிலை: A, B, C, D, E
) {
    val naney1Total: Int get() = naney1Oral + naney1Activity + naney1Written
    val naney2Total: Int get() = naney2Oral + naney2Activity + naney2Written
    val thiranariTotal: Int get() = thiranariOral + thiranariWritten
    val percentageStr: String get() = "$total%"
}

@Entity(
    tableName = "term_attendance",
    indices = [Index(value = ["studentId", "term"], unique = true)]
)
data class TermAttendance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val studentId: Long,
    val term: Int,               // பருவம்: 1, 2, 3
    val totalWorkingDays: Int = 80, // பள்ளி வேலை நாட்கள்
    val presentDays: Int = 75       // மாணவன் வருகை புரிந்த நாட்கள்
)
