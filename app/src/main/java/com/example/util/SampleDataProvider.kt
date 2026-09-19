package com.example.util

import com.example.data.model.SchoolProfile
import com.example.data.model.Student
import com.example.data.model.StudentMarks
import com.example.data.model.Subject
import com.example.data.model.TermAttendance
import com.example.data.repository.SchoolRepository
import kotlinx.coroutines.flow.firstOrNull

object SampleDataProvider {

    suspend fun populateIfEmpty(repository: SchoolRepository) {
        val existingProfile = repository.getSchoolProfileOnce()
        if (existingProfile == null) {
            val defaultProfile = SchoolProfile(
                id = 1,
                schoolName = "ஊராட்சி ஒன்றிய நடுநிலைப்பள்ளி நகரகுடி",
                unionName = "இளையான்குடி ஒன்றியம்",
                districtName = "சிவகங்கை மாவட்டம்",
                academicYear = "2026-2027",
                headmasterName = "மு. ஆரோக்கியசாமி M.A., B.Ed."
            )
            repository.updateSchoolProfile(defaultProfile)
        }

        val existingStudents = repository.allStudents.firstOrNull() ?: emptyList()
        val hasOldSampleData = existingStudents.isEmpty() ||
                existingStudents.any { it.admissionNo == "1267" || it.name == "அ. அகிலேஷ்" || it.admissionNo == "101" }

        if (hasOldSampleData) {
            populateOfficialStudents(repository, forceReset = true)
        }
    }

    suspend fun populateOfficialStudents(repository: SchoolRepository, forceReset: Boolean = false) {
        if (forceReset) {
            repository.clearAllStudentsAndData()
        }

        val students = CsvStudentImporter.parseStudentsFromText(CsvStudentImporter.OFFICIAL_54_STUDENTS_CSV)

        for (student in students) {
            val studentId = repository.insertStudent(student)
            val subjects = Subject.getSubjectsForClass(student.stdClass)

            // Populate 3 terms of realistic marks and attendance
            for (term in 1..3) {
                val workingDays = when (term) {
                    1 -> 82
                    2 -> 78
                    3 -> 80
                    else -> 80
                }
                val presentDays = when (term) {
                    1 -> if (student.id % 2L == 0L) 80 else 76
                    2 -> if (student.id % 2L == 0L) 76 else 74
                    3 -> if (student.id % 2L == 0L) 79 else 77
                    else -> 78
                }

                repository.saveAttendance(
                    TermAttendance(
                        studentId = studentId,
                        term = term,
                        totalWorkingDays = workingDays,
                        presentDays = presentDays
                    )
                )

                for (sub in subjects) {
                    if (student.stdClass in 1..3) {
                        // Class 1-3 புதிய பாடத்திட்டம்:
                        // நானே செய்வேன் 1 [25], நானே செய்வேன் 2 [25], திறனறி [50] = 100
                        val n1Oral = 8 + (studentId % 3).toInt()
                        val n1Act = 8 + (studentId % 3).toInt()
                        val n1Wri = 4 + (studentId % 2).toInt()
                        val n2Oral = 8 + (studentId % 3).toInt()
                        val n2Act = 8 + (studentId % 3).toInt()
                        val n2Wri = 4 + (studentId % 2).toInt()
                        val thOral = 8 + (studentId % 3).toInt()
                        val thWri = 32 + ((studentId * 2) % 9).toInt()
                        val tot = n1Oral + n1Act + n1Wri + n2Oral + n2Act + n2Wri + thOral + thWri
                        val level = if (tot >= 80) "மலர்" else if (tot >= 60) "மொட்டு" else "அரும்பு"
                        val grade = if (tot >= 80) "A" else if (tot >= 60) "B" else "C"

                        repository.saveMark(
                            StudentMarks(
                                studentId = studentId,
                                term = term,
                                subjectKey = sub.key,
                                naney1Oral = n1Oral,
                                naney1Activity = n1Act,
                                naney1Written = n1Wri,
                                naney2Oral = n2Oral,
                                naney2Activity = n2Act,
                                naney2Written = n2Wri,
                                thiranariOral = thOral,
                                thiranariWritten = thWri,
                                faA = n1Oral + n1Act + n1Wri,
                                faB = n2Oral + n2Act + n2Wri,
                                faTotal = (n1Oral + n1Act + n1Wri) + (n2Oral + n2Act + n2Wri),
                                sa = thOral + thWri,
                                total = tot,
                                learningLevel = level,
                                grade = grade
                            )
                        )
                    } else {
                        // Class 4-8 format: FA (40), SA (60)
                        val fa = 30 + ((studentId + term) % 9).toInt()
                        val sa = 45 + ((studentId * 3 + term) % 15).toInt()
                        val tot = (fa + sa).coerceAtMost(100)
                        repository.saveMark(
                            StudentMarks(
                                studentId = studentId,
                                term = term,
                                subjectKey = sub.key,
                                faA = fa / 2,
                                faB = fa - (fa / 2),
                                faTotal = fa,
                                sa = sa,
                                total = tot,
                                grade = if (tot >= 81) "A" else if (tot >= 61) "B" else "C"
                            )
                        )
                    }
                }
            }
        }
    }
}

