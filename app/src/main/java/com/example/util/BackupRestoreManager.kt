package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.SchoolProfile
import com.example.data.model.Student
import com.example.data.model.StudentMarks
import com.example.data.model.TermAttendance
import com.example.data.repository.SchoolRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Handles complete JSON backup and restore for all school data:
 * School Profile, Students, Marks, and Attendance.
 */
object BackupRestoreManager {

    suspend fun exportBackup(
        context: Context,
        repository: SchoolRepository
    ): Pair<File, String> {
        val school = repository.getSchoolProfileOnce() ?: SchoolProfile()
        val students = repository.getAllStudentsOnce()
        val marks = repository.getAllMarksOnce()
        val attendance = repository.getAllAttendanceOnce()

        val root = JSONObject()
        root.put("app", "TNPUMS_Marks_Register")
        root.put("version", 1)
        root.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        // School Profile
        val schoolObj = JSONObject().apply {
            put("id", school.id)
            put("schoolName", school.schoolName)
            put("udiseCode", school.udiseCode)
            put("unionName", school.unionName)
            put("districtName", school.districtName)
            put("academicYear", school.academicYear)
            put("headmasterName", school.headmasterName)
            put("term1WorkingDays", school.term1WorkingDays)
            put("term2WorkingDays", school.term2WorkingDays)
            put("term3WorkingDays", school.term3WorkingDays)
        }
        root.put("schoolProfile", schoolObj)

        // Students
        val studentsArr = JSONArray()
        for (s in students) {
            val sObj = JSONObject().apply {
                put("id", s.id)
                put("admissionNo", s.admissionNo)
                put("name", s.name)
                put("stdClass", s.stdClass)
                put("section", s.section)
                put("gender", s.gender)
                put("community", s.community)
                put("parentName", s.parentName)
                put("academicYear", s.academicYear)
            }
            studentsArr.put(sObj)
        }
        root.put("students", studentsArr)

        // Marks
        val marksArr = JSONArray()
        for (m in marks) {
            val mObj = JSONObject().apply {
                put("id", m.id)
                put("studentId", m.studentId)
                put("term", m.term)
                put("subjectKey", m.subjectKey)
                put("naney1Oral", m.naney1Oral)
                put("naney1Activity", m.naney1Activity)
                put("naney1Written", m.naney1Written)
                put("naney2Oral", m.naney2Oral)
                put("naney2Activity", m.naney2Activity)
                put("naney2Written", m.naney2Written)
                put("thiranariOral", m.thiranariOral)
                put("thiranariWritten", m.thiranariWritten)
                put("faA", m.faA)
                put("faB", m.faB)
                put("faTotal", m.faTotal)
                put("sa", m.sa)
                put("total", m.total)
                put("learningLevel", m.learningLevel)
                put("grade", m.grade)
            }
            marksArr.put(mObj)
        }
        root.put("marks", marksArr)

        // Attendance
        val attArr = JSONArray()
        for (a in attendance) {
            val aObj = JSONObject().apply {
                put("id", a.id)
                put("studentId", a.studentId)
                put("term", a.term)
                put("totalWorkingDays", a.totalWorkingDays)
                put("presentDays", a.presentDays)
            }
            attArr.put(aObj)
        }
        root.put("attendance", attArr)

        // Write to file
        val backupDir = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "TNPUMS_Marks_Backup_$timeStamp.json"
        val file = File(backupDir, fileName)

        FileOutputStream(file).use { fos ->
            fos.write(root.toString(2).toByteArray(Charsets.UTF_8))
        }

        val summary = "வெற்றிகரமாக காப்புநகல் எடுக்கப்பட்டது!\n• பள்ளி விவரங்கள்\n• மாணவர்கள்: ${students.size} பேர்\n• மதிப்பெண் பதிவுகள்: ${marks.size}\n• வருகைப் பதிவுகள்: ${attendance.size}"
        return Pair(file, summary)
    }

    suspend fun restoreFromJson(
        jsonString: String,
        repository: SchoolRepository
    ): Result<String> {
        return try {
            val root = JSONObject(jsonString)

            // Optional school profile restore
            var schoolCount = 0
            if (root.has("schoolProfile")) {
                val sObj = root.getJSONObject("schoolProfile")
                val school = SchoolProfile(
                    id = sObj.optInt("id", 1),
                    schoolName = sObj.optString("schoolName", "ஊராட்சி ஒன்றிய நடுநிலைப்பள்ளி"),
                    udiseCode = sObj.optString("udiseCode", "33230500103"),
                    unionName = sObj.optString("unionName", ""),
                    districtName = sObj.optString("districtName", ""),
                    academicYear = sObj.optString("academicYear", "2026-2027"),
                    headmasterName = sObj.optString("headmasterName", ""),
                    term1WorkingDays = sObj.optInt("term1WorkingDays", 80),
                    term2WorkingDays = sObj.optInt("term2WorkingDays", 80),
                    term3WorkingDays = sObj.optInt("term3WorkingDays", 60)
                )
                repository.updateSchoolProfile(school)
                schoolCount = 1
            }

            // Restore students
            val restoredStudents = mutableListOf<Student>()
            if (root.has("students")) {
                val sArr = root.getJSONArray("students")
                for (i in 0 until sArr.length()) {
                    val o = sArr.getJSONObject(i)
                    restoredStudents.add(
                        Student(
                            id = o.optLong("id", 0L),
                            admissionNo = o.optString("admissionNo", ""),
                            name = o.optString("name", ""),
                            stdClass = o.optInt("stdClass", 1),
                            section = o.optString("section", "A"),
                            gender = o.optString("gender", "ஆண்"),
                            community = o.optString("community", "BC"),
                            parentName = o.optString("parentName", ""),
                            academicYear = o.optString("academicYear", "2026-2027")
                        )
                    )
                }
            }

            // Restore marks
            val restoredMarks = mutableListOf<StudentMarks>()
            if (root.has("marks")) {
                val mArr = root.getJSONArray("marks")
                for (i in 0 until mArr.length()) {
                    val o = mArr.getJSONObject(i)
                    restoredMarks.add(
                        StudentMarks(
                            id = o.optLong("id", 0L),
                            studentId = o.optLong("studentId", 0L),
                            term = o.optInt("term", 1),
                            subjectKey = o.optString("subjectKey", "TAMIL"),
                            naney1Oral = o.optInt("naney1Oral", 10),
                            naney1Activity = o.optInt("naney1Activity", 10),
                            naney1Written = o.optInt("naney1Written", 5),
                            naney2Oral = o.optInt("naney2Oral", 10),
                            naney2Activity = o.optInt("naney2Activity", 10),
                            naney2Written = o.optInt("naney2Written", 5),
                            thiranariOral = o.optInt("thiranariOral", 10),
                            thiranariWritten = o.optInt("thiranariWritten", 40),
                            faA = o.optInt("faA", 0),
                            faB = o.optInt("faB", 0),
                            faTotal = o.optInt("faTotal", 0),
                            sa = o.optInt("sa", 0),
                            total = o.optInt("total", 0),
                            learningLevel = o.optString("learningLevel", "அரும்பு"),
                            grade = o.optString("grade", "A")
                        )
                    )
                }
            }

            // Restore attendance
            val restoredAtt = mutableListOf<TermAttendance>()
            if (root.has("attendance")) {
                val aArr = root.getJSONArray("attendance")
                for (i in 0 until aArr.length()) {
                    val o = aArr.getJSONObject(i)
                    restoredAtt.add(
                        TermAttendance(
                            id = o.optLong("id", 0L),
                            studentId = o.optLong("studentId", 0L),
                            term = o.optInt("term", 1),
                            totalWorkingDays = o.optInt("totalWorkingDays", 80),
                            presentDays = o.optInt("presentDays", 76)
                        )
                    )
                }
            }

            // Clear old data and insert restored records
            repository.clearAllStudentsAndData()
            if (restoredStudents.isNotEmpty()) {
                repository.insertStudents(restoredStudents)
            }
            if (restoredMarks.isNotEmpty()) {
                repository.saveMarks(restoredMarks)
            }
            if (restoredAtt.isNotEmpty()) {
                repository.saveAttendanceList(restoredAtt)
            }

            Result.success(
                "காப்புநகல் வெற்றிகரமாக மீட்டமைக்கப்பட்டது!\n• பள்ளி விவரங்கள்: $schoolCount\n• மாணவர்கள்: ${restoredStudents.size} பேர்\n• மதிப்பெண் பதிவுகள்: ${restoredMarks.size}\n• வருகைப் பதிவுகள்: ${restoredAtt.size}"
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareBackupFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "TNPUMS பள்ளி மதிப்பெண் பதிவேடு காப்புநகல் (${file.name})")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "காப்புநகல் கோப்பை பகிர்க"))
    }
}
