package com.example.data.model

import kotlin.math.roundToInt

/**
 * Subject mark summary holding individual terms and computed 3-term average.
 */
data class SubjectThreeTermMarks(
    val subject: Subject,
    val term1Marks: StudentMarks? = null,
    val term2Marks: StudentMarks? = null,
    val term3Marks: StudentMarks? = null
) {
    val term1Total: Int get() = term1Marks?.total ?: 0
    val term2Total: Int get() = term2Marks?.total ?: 0
    val term3Total: Int get() = term3Marks?.total ?: 0

    val term1Fa: Int get() = term1Marks?.faTotal ?: 0
    val term2Fa: Int get() = term2Marks?.faTotal ?: 0
    val term3Fa: Int get() = term3Marks?.faTotal ?: 0

    val term1Sa: Int get() = term1Marks?.sa ?: 0
    val term2Sa: Int get() = term2Marks?.sa ?: 0
    val term3Sa: Int get() = term3Marks?.sa ?: 0

    // Average calculations across terms that have entered marks (or 3 terms)
    val avgFa: Int
        get() {
            val terms = listOfNotNull(term1Marks, term2Marks, term3Marks)
            if (terms.isEmpty()) return 0
            return (terms.sumOf { it.faTotal }.toFloat() / 3f).roundToInt()
        }

    val avgSa: Int
        get() {
            val terms = listOfNotNull(term1Marks, term2Marks, term3Marks)
            if (terms.isEmpty()) return 0
            return (terms.sumOf { it.sa }.toFloat() / 3f).roundToInt()
        }

    val avgTotal: Int
        get() {
            val terms = listOfNotNull(term1Marks, term2Marks, term3Marks)
            if (terms.isEmpty()) return 0
            return (terms.sumOf { it.total }.toFloat() / 3f).roundToInt()
        }

    fun getGrade(stdClass: Int): String {
        return if (stdClass in 1..3) {
            CceGradeEvaluator.getGradeForClass1To3(avgTotal)
        } else {
            CceGradeEvaluator.getGradeForClass4To8(avgTotal)
        }
    }
}

/**
 * Consolidated row for a student in the Three Terms Average Register.
 * Exactly corresponds to the user's attached format:
 * - வரிசை எண் (S.No)
 * - சேர்க்கை எண் (Admission No)
 * - மாணவனின் பெயர் (Student Name)
 * - வகுப்பு (Class)
 * - இனம் (Community)
 * - தமிழ் (SA, FA, மொத்தம்)
 * - ஆங்கிலம் (SA, FA, மொத்தம்)
 * - கணக்கு (SA, FA, மொத்தம்)
 * - அறிவியல் (SA, FA, மொத்தம்)
 * - சமூக அறிவியல் (SA, FA, மொத்தம்)
 * - உடற்கல்வி (SA, FA, மொத்தம்)
 * - TOTAL (மொத்தம்)
 * - பள்ளி மொத்த வேலை நாட்கள் (Total Working Days)
 * - மாணவன் வருகை நாட்கள் (Present Days)
 * - தேர்ச்சி விபரம் (Result)
 */
data class StudentConsolidatedRecord(
    val student: Student,
    val subjectMarks: Map<Subject, SubjectThreeTermMarks>,
    val term1Attendance: TermAttendance? = null,
    val term2Attendance: TermAttendance? = null,
    val term3Attendance: TermAttendance? = null
) {
    val totalWorkingDays: Int
        get() = (term1Attendance?.totalWorkingDays ?: 0) +
                (term2Attendance?.totalWorkingDays ?: 0) +
                (term3Attendance?.totalWorkingDays ?: 0)

    val totalPresentDays: Int
        get() = (term1Attendance?.presentDays ?: 0) +
                (term2Attendance?.presentDays ?: 0) +
                (term3Attendance?.presentDays ?: 0)

    val attendancePercentage: Float
        get() {
            if (totalWorkingDays == 0) return 0f
            return (totalPresentDays.toFloat() / totalWorkingDays.toFloat()) * 100f
        }

    // Consolidated Average Total (Only 5 main academic subjects: Tamil, English, Maths, Science, Social; Total out of 500 or 300)
    val grandAvgTotal: Int
        get() {
            val subjects = Subject.getMainSubjectsForClass(student.stdClass)
            return subjects.sumOf { subjectMarks[it]?.avgTotal ?: 0 }
        }

    val maxPossibleMarks: Int
        get() = Subject.getMainSubjectsForClass(student.stdClass).size * 100

    val overallPercentage: Float
        get() {
            if (maxPossibleMarks == 0) return 0f
            return (grandAvgTotal.toFloat() / maxPossibleMarks.toFloat()) * 100f
        }

    val overallGrade: String
        get() {
            val pct = overallPercentage.roundToInt()
            return if (student.stdClass in 1..3) {
                CceGradeEvaluator.getGradeForClass1To3(pct)
            } else {
                CceGradeEvaluator.getGradeForClass4To8(pct)
            }
        }

    // Physical Education helpers (kept completely separate from academic total)
    val peAvgTotal: Int
        get() = subjectMarks[Subject.PE]?.avgTotal ?: 0

    fun getPeTermTotal(term: Int): Int {
        val m = subjectMarks[Subject.PE]
        return when (term) {
            1 -> m?.term1Total ?: 0
            2 -> m?.term2Total ?: 0
            3 -> m?.term3Total ?: 0
            else -> 0
        }
    }

    val resultStatus: String
        get() {
            val subjects = Subject.getMainSubjectsForClass(student.stdClass)

            // In TN elementary/middle schools, all main subjects >= 35
            val allPassed = subjects.all { (subjectMarks[it]?.avgTotal ?: 0) >= 35 }
            return if (allPassed) "தேர்ச்சி" else "ஊக்கப்படுத்தல் தேவை"
        }

    // Single term summary helper (Only 5 main academic subjects out of 500 or 300)
    fun getTermTotal(term: Int): Int {
        val subjects = Subject.getMainSubjectsForClass(student.stdClass)
        return subjects.sumOf { sub ->
            val m = subjectMarks[sub]
            when (term) {
                1 -> m?.term1Total ?: 0
                2 -> m?.term2Total ?: 0
                3 -> m?.term3Total ?: 0
                else -> 0
            }
        }
    }
}
