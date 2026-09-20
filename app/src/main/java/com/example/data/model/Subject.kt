package com.example.data.model

/**
 * Subject definition with Tamil display names.
 */
enum class Subject(val key: String, val tamilName: String, val shortName: String) {
    TAMIL("TAMIL", "தமிழ்", "தமிழ்"),
    ENGLISH("ENGLISH", "ஆங்கிலம்", "ஆங்கிலம்"),
    MATHS("MATHS", "கணிதம்", "கணக்கு"),
    EVS("EVS", "சூழ்நிலையியல் (EVS)", "சூழ்நிலை"),
    SCIENCE("SCIENCE", "அறிவியல்", "அறிவியல்"),
    SOCIAL("SOCIAL", "சமூக அறிவியல்", "சமூகவியல்"),
    PE("PE", "உடற்கல்வி", "உ.கல்வி");

    companion object {
        fun getSubjectsForClass(stdClass: Int): List<Subject> {
            return when (stdClass) {
                1, 2, 3 -> listOf(TAMIL, ENGLISH, MATHS, EVS)
                4, 5, 6, 7 -> listOf(TAMIL, ENGLISH, MATHS, SCIENCE, SOCIAL)
                8 -> listOf(TAMIL, ENGLISH, MATHS, SCIENCE, SOCIAL, PE)
                else -> listOf(TAMIL, ENGLISH, MATHS, SCIENCE, SOCIAL)
            }
        }

        /**
         * The core academic subjects whose marks are included in the Grand Total (out of 500 or 400).
         * Physical Education (PE) is excluded from the Total as per school norms.
         */
        fun getMainSubjectsForClass(stdClass: Int): List<Subject> {
            return when (stdClass) {
                1, 2, 3 -> listOf(TAMIL, ENGLISH, MATHS, EVS)
                else -> listOf(TAMIL, ENGLISH, MATHS, SCIENCE, SOCIAL)
            }
        }

        fun getMainSubjectsForClass8(): List<Subject> {
            return listOf(TAMIL, ENGLISH, MATHS, SCIENCE, SOCIAL)
        }

        fun hasPeSubject(stdClass: Int): Boolean {
            return stdClass == 8
        }
    }
}

/**
 * Grade evaluation utilities matching Tamil Nadu School CCE regulations.
 */
object CceGradeEvaluator {

    /**
     * For Class 1-3:
     * A: 80 - 100%
     * B: 60 - 79%
     * C: 40 - 59%
     * D: 20 - 39%
     * E: 19% and below
     */
    fun getGradeForClass1To3(totalMark: Int): String {
        return when {
            totalMark >= 80 -> "A"
            totalMark >= 60 -> "B"
            totalMark >= 40 -> "C"
            totalMark >= 20 -> "D"
            else -> "E"
        }
    }

    /**
     * For Class 4-7 & Class 8:
     * As per TN CCE standard:
     * A: 81 - 100
     * B: 61 - 80
     * C: 41 - 60
     * D: 21 - 40
     * E: 0 - 20
     */
    fun getGradeForClass4To8(totalMark: Int): String {
        return when {
            totalMark >= 81 -> "A"
            totalMark >= 61 -> "B"
            totalMark >= 41 -> "C"
            totalMark >= 21 -> "D"
            else -> "E"
        }
    }

    fun getFaGrade(faMark: Int): String {
        return when {
            faMark >= 33 -> "A"
            faMark >= 25 -> "B"
            faMark >= 17 -> "C"
            faMark >= 9 -> "D"
            else -> "E"
        }
    }

    fun getSaGrade(saMark: Int): String {
        return when {
            saMark >= 49 -> "A"
            saMark >= 37 -> "B"
            saMark >= 25 -> "C"
            saMark >= 13 -> "D"
            else -> "E"
        }
    }

    fun isPassed(subjectMarks: List<StudentMarks>, stdClass: Int): Boolean {
        if (subjectMarks.isEmpty()) return false
        // For Class 8 & 4-7, passing benchmark is 35% in each subject
        return subjectMarks.all { it.total >= 35 }
    }
}
