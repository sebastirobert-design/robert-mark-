package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.model.SchoolProfile
import com.example.data.model.Student
import com.example.data.model.StudentMarks
import com.example.data.model.TermAttendance
import kotlinx.coroutines.flow.Flow

class SchoolRepository(private val database: AppDatabase) {

    private val schoolProfileDao = database.schoolProfileDao()
    private val studentDao = database.studentDao()
    private val marksDao = database.marksDao()
    private val attendanceDao = database.attendanceDao()

    val schoolProfile: Flow<SchoolProfile?> = schoolProfileDao.getSchoolProfile()

    suspend fun getSchoolProfileOnce(): SchoolProfile? = schoolProfileDao.getSchoolProfileOnce()

    suspend fun updateSchoolProfile(profile: SchoolProfile) {
        schoolProfileDao.insertOrUpdate(profile)
    }

    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()

    fun getStudentsByClass(stdClass: Int): Flow<List<Student>> {
        return studentDao.getStudentsByClass(stdClass)
    }

    suspend fun getStudentById(id: Long): Student? = studentDao.getStudentById(id)

    suspend fun insertStudent(student: Student): Long = studentDao.insertStudent(student)

    suspend fun insertStudents(students: List<Student>) = studentDao.insertStudents(students)

    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)

    suspend fun deleteStudent(studentId: Long) {
        marksDao.deleteMarksForStudent(studentId)
        attendanceDao.deleteAttendanceForStudent(studentId)
        studentDao.deleteStudentById(studentId)
    }

    suspend fun clearAllStudentsAndData() {
        marksDao.deleteAllMarks()
        attendanceDao.deleteAllAttendance()
        studentDao.deleteAllStudents()
    }

    suspend fun promoteAllClasses(newYear: String, archiveClass8: Boolean) {
        if (archiveClass8) {
            studentDao.removeClass8Students()
        } else {
            studentDao.graduateClass8(newYear)
        }
        // Promote starting from class 7 down to class 1 to prevent cascading
        for (c in 7 downTo 1) {
            studentDao.promoteStudentsInClass(fromClass = c, toClass = c + 1, newYear = newYear)
        }
    }

    suspend fun promoteSingleClass(fromClass: Int, toClass: Int, newYear: String) {
        if (fromClass == 8) {
            studentDao.graduateClass8(newYear)
        } else {
            studentDao.promoteStudentsInClass(fromClass, toClass, newYear)
        }
    }

    suspend fun updateStudentClassAndYear(studentId: Long, newClass: Int, newYear: String) {
        studentDao.updateStudentClassAndYear(studentId, newClass, newYear)
    }

    fun getMarksForStudent(studentId: Long): Flow<List<StudentMarks>> {
        return marksDao.getMarksForStudent(studentId)
    }

    fun getMarksForStudentAndTerm(studentId: Long, term: Int): Flow<List<StudentMarks>> {
        return marksDao.getMarksForStudentAndTerm(studentId, term)
    }

    fun getMarksForStudents(studentIds: List<Long>): Flow<List<StudentMarks>> {
        return marksDao.getMarksForStudents(studentIds)
    }

    fun getMarksForStudentsAndTerm(studentIds: List<Long>, term: Int): Flow<List<StudentMarks>> {
        return marksDao.getMarksForStudentsAndTerm(studentIds, term)
    }

    suspend fun saveMark(marks: StudentMarks): Long {
        return marksDao.insertOrUpdateMark(marks)
    }

    suspend fun saveMarks(marksList: List<StudentMarks>) {
        marksDao.insertOrUpdateMarks(marksList)
    }

    fun getAttendanceForStudents(studentIds: List<Long>): Flow<List<TermAttendance>> {
        return attendanceDao.getAttendanceForStudents(studentIds)
    }

    fun getAttendanceForStudentAndTerm(studentId: Long, term: Int): Flow<TermAttendance?> {
        return attendanceDao.getAttendanceForStudentAndTerm(studentId, term)
    }

    fun getAttendanceForStudentsAndTerm(studentIds: List<Long>, term: Int): Flow<List<TermAttendance>> {
        return attendanceDao.getAttendanceForStudentsAndTerm(studentIds, term)
    }

    suspend fun saveAttendance(attendance: TermAttendance): Long {
        return attendanceDao.insertOrUpdateAttendance(attendance)
    }

    suspend fun saveAttendanceList(list: List<TermAttendance>) {
        attendanceDao.insertOrUpdateAttendanceList(list)
    }

    suspend fun getAllStudentsOnce(): List<Student> = studentDao.getAllStudentsOnce()
    suspend fun getAllMarksOnce(): List<StudentMarks> = marksDao.getAllMarksOnce()
    suspend fun getAllAttendanceOnce(): List<TermAttendance> = attendanceDao.getAllAttendanceOnce()

    suspend fun applyWorkingDaysToAllStudents(term1Days: Int, term2Days: Int, term3Days: Int) {
        val students = studentDao.getAllStudentsOnce()
        val allAtt = attendanceDao.getAllAttendanceOnce()
        val attMap = allAtt.groupBy { it.studentId }
        val updatedList = mutableListOf<TermAttendance>()

        for (st in students) {
            val studentAtts = attMap[st.id] ?: emptyList()
            for (t in 1..3) {
                val targetDays = when (t) {
                    1 -> term1Days
                    2 -> term2Days
                    3 -> term3Days
                    else -> term1Days
                }
                if (targetDays <= 0) continue // Skip terms that have no working days entered yet
                val existing = studentAtts.find { it.term == t }
                if (existing != null) {
                    val newPresent = existing.presentDays.coerceAtMost(targetDays)
                    updatedList.add(existing.copy(totalWorkingDays = targetDays, presentDays = newPresent))
                } else {
                    val defaultPresent = (targetDays * 0.95).toInt().coerceAtMost(targetDays)
                    updatedList.add(TermAttendance(studentId = st.id, term = t, totalWorkingDays = targetDays, presentDays = defaultPresent))
                }
            }
        }
        if (updatedList.isNotEmpty()) {
            attendanceDao.insertOrUpdateAttendanceList(updatedList)
        }
    }

    suspend fun applySchoolWorkingDaysToAllStudents(workingDays: Int) {
        if (workingDays <= 0) return
        val students = studentDao.getAllStudentsOnce()
        val allAtt = attendanceDao.getAllAttendanceOnce()
        val attMap = allAtt.groupBy { it.studentId }
        val updatedList = mutableListOf<TermAttendance>()

        for (st in students) {
            val studentAtts = attMap[st.id] ?: emptyList()
            for (t in 1..3) {
                val existing = studentAtts.find { it.term == t }
                if (existing != null) {
                    val newPresent = existing.presentDays.coerceAtMost(workingDays)
                    updatedList.add(existing.copy(totalWorkingDays = workingDays, presentDays = newPresent))
                } else {
                    val defaultPresent = (workingDays * 0.95).toInt().coerceAtMost(workingDays)
                    updatedList.add(TermAttendance(studentId = st.id, term = t, totalWorkingDays = workingDays, presentDays = defaultPresent))
                }
            }
        }
        if (updatedList.isNotEmpty()) {
            attendanceDao.insertOrUpdateAttendanceList(updatedList)
        }
    }
}
