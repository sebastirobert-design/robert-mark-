package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.CceGradeEvaluator
import com.example.data.model.SchoolProfile
import com.example.data.model.Student
import com.example.data.model.StudentConsolidatedRecord
import com.example.data.model.StudentMarks
import com.example.data.model.Subject
import com.example.data.model.SubjectThreeTermMarks
import com.example.data.model.TermAttendance
import com.example.data.repository.SchoolRepository
import com.example.util.CsvStudentImporter
import com.example.util.ExcelReportGenerator
import com.example.util.PdfReportGenerator
import com.example.util.SampleDataProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class SchoolMarksViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SchoolRepository

    val schoolProfile: StateFlow<SchoolProfile>
    val selectedClass = MutableStateFlow(8)
    val selectedTerm = MutableStateFlow(1)

    val allStudents: StateFlow<List<Student>>
    val consolidatedRecords: StateFlow<List<StudentConsolidatedRecord>>

    val uiMessage = MutableStateFlow<String?>(null)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SchoolRepository(db)

        schoolProfile = repository.schoolProfile
            .combine(MutableStateFlow(SchoolProfile())) { p, default ->
                p ?: default
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SchoolProfile()
            )

        allStudents = repository.allStudents
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Combine students, selectedClass, marks and attendance to build consolidated records
        consolidatedRecords = combine(
            allStudents,
            selectedClass,
            repository.getMarksForStudents(listOf()), // dummy anchor
            repository.getAttendanceForStudents(listOf()) // dummy anchor
        ) { students, cls, _, _ ->
            // Flow will be dynamically updated via buildConsolidatedRecords
            buildConsolidatedRecords(students.filter { it.stdClass == cls })
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed initial data if empty
        viewModelScope.launch {
            SampleDataProvider.populateIfEmpty(repository)
        }
    }

    fun selectClass(cls: Int) {
        selectedClass.value = cls
    }

    fun selectTerm(term: Int) {
        selectedTerm.value = term
    }

    fun clearMessage() {
        uiMessage.value = null
    }

    suspend fun loadConsolidatedRecordsForClass(cls: Int): List<StudentConsolidatedRecord> {
        val students = allStudents.value.filter { it.stdClass == cls }
        return buildConsolidatedRecords(students)
    }

    private suspend fun buildConsolidatedRecords(students: List<Student>): List<StudentConsolidatedRecord> {
        if (students.isEmpty()) return emptyList()
        val studentIds = students.map { it.id }
        val marksList = repository.getMarksForStudents(studentIds).firstOrNull() ?: emptyList()
        val attendanceList = repository.getAttendanceForStudents(studentIds).firstOrNull() ?: emptyList()

        val marksByStudentAndTerm = marksList.groupBy { it.studentId }
        val attendanceByStudentAndTerm = attendanceList.groupBy { it.studentId }

        return students.map { student ->
            val stdMarks = marksByStudentAndTerm[student.id] ?: emptyList()
            val stdAttendance = attendanceByStudentAndTerm[student.id] ?: emptyList()

            val term1Att = stdAttendance.find { it.term == 1 }
            val term2Att = stdAttendance.find { it.term == 2 }
            val term3Att = stdAttendance.find { it.term == 3 }

            val subjects = Subject.getSubjectsForClass(student.stdClass)
            val subjectMarksMap = subjects.associateWith { sub ->
                val t1 = stdMarks.find { it.term == 1 && it.subjectKey == sub.key }
                val t2 = stdMarks.find { it.term == 2 && it.subjectKey == sub.key }
                val t3 = stdMarks.find { it.term == 3 && it.subjectKey == sub.key }
                SubjectThreeTermMarks(
                    subject = sub,
                    term1Marks = t1,
                    term2Marks = t2,
                    term3Marks = t3
                )
            }

            StudentConsolidatedRecord(
                student = student,
                subjectMarks = subjectMarksMap,
                term1Attendance = term1Att,
                term2Attendance = term2Att,
                term3Attendance = term3Att
            )
        }
    }

    fun updateSchoolProfile(
        schoolName: String,
        unionName: String,
        districtName: String,
        academicYear: String,
        headmasterName: String
    ) {
        viewModelScope.launch {
            repository.updateSchoolProfile(
                SchoolProfile(
                    id = 1,
                    schoolName = schoolName.trim(),
                    unionName = unionName.trim(),
                    districtName = districtName.trim(),
                    academicYear = academicYear.trim(),
                    headmasterName = headmasterName.trim()
                )
            )
            uiMessage.value = "பள்ளி விவரங்கள் சேமிக்கப்பட்டன"
        }
    }

    fun addStudent(
        admissionNo: String,
        name: String,
        stdClass: Int,
        section: String,
        gender: String,
        community: String,
        parentName: String
    ) {
        viewModelScope.launch {
            val student = Student(
                admissionNo = admissionNo.trim(),
                name = name.trim(),
                stdClass = stdClass,
                section = section.trim().ifEmpty { "A" },
                gender = gender,
                community = community,
                parentName = parentName.trim(),
                academicYear = schoolProfile.value.academicYear
            )
            repository.insertStudent(student)
            uiMessage.value = "மாணவர் சேர்க்கை பதிவு செய்யப்பட்டது: $name"
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
            uiMessage.value = "மாணவர் விவரங்கள் புதுப்பிக்கப்பட்டன"
        }
    }

    fun deleteStudent(studentId: Long) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
            uiMessage.value = "மாணவர் நீக்கப்பட்டார்"
        }
    }

    fun importStudentsFromCsv(
        csvText: String,
        replaceExisting: Boolean = true,
        onSuccess: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            val parsedStudents = CsvStudentImporter.parseStudentsFromText(csvText)
            if (parsedStudents.isEmpty()) {
                uiMessage.value = "மாணவர் விவரங்களை அடையாளம் காண முடியவில்லை. சரியான வடிவத்தில் உள்ளிடவும்."
                return@launch
            }

            if (replaceExisting) {
                repository.clearAllStudentsAndData()
            }

            var count = 0
            for (student in parsedStudents) {
                val studentId = repository.insertStudent(student)
                val subjects = Subject.getSubjectsForClass(student.stdClass)

                // Initialize default marks and attendance for all 3 terms
                for (term in 1..3) {
                    repository.saveAttendance(
                        TermAttendance(
                            studentId = studentId,
                            term = term,
                            totalWorkingDays = 80,
                            presentDays = 78
                        )
                    )
                    for (sub in subjects) {
                        if (student.stdClass in 1..3) {
                            repository.saveMark(
                                StudentMarks(
                                    studentId = studentId,
                                    term = term,
                                    subjectKey = sub.key,
                                    naney1Oral = 8,
                                    naney1Activity = 8,
                                    naney1Written = 4,
                                    naney2Oral = 8,
                                    naney2Activity = 8,
                                    naney2Written = 4,
                                    thiranariOral = 8,
                                    thiranariWritten = 36,
                                    faA = 20,
                                    faB = 20,
                                    faTotal = 40,
                                    sa = 44,
                                    total = 84,
                                    learningLevel = "மலர்",
                                    grade = "A"
                                )
                            )
                        } else {
                            repository.saveMark(
                                StudentMarks(
                                    studentId = studentId,
                                    term = term,
                                    subjectKey = sub.key,
                                    faA = 17,
                                    faB = 18,
                                    faTotal = 35,
                                    sa = 50,
                                    total = 85,
                                    grade = "A"
                                )
                            )
                        }
                    }
                }
                count++
            }
            uiMessage.value = "$count மாணவர்கள் Google Sheet-லிருந்து வெற்றிகரமாக சேர்க்கப்பட்டனர்!"
            onSuccess(count)
        }
    }

    fun resetToOfficial54Students() {
        viewModelScope.launch {
            SampleDataProvider.populateOfficialStudents(repository, forceReset = true)
            uiMessage.value = "பள்ளியின் 54 மாணவர்கள் பட்டியல் புதுப்பிக்கப்பட்டது!"
        }
    }

    fun clearAllStudents(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearAllStudentsAndData()
            uiMessage.value = "அனைத்து மாணவர்களின் பெயர்களும் நீக்கப்பட்டன. இப்போது உங்கள் பள்ளி Google Sheet பட்டியலை பதிவேற்றலாம்."
            onDone()
        }
    }

    suspend fun getMarksForStudentAndTerm(studentId: Long, term: Int): List<StudentMarks> {
        return repository.getMarksForStudentAndTerm(studentId, term).firstOrNull() ?: emptyList()
    }

    suspend fun getAttendanceForStudentAndTerm(studentId: Long, term: Int): TermAttendance? {
        return repository.getAttendanceForStudentAndTerm(studentId, term).firstOrNull()
    }

    fun saveStudentMarksAndAttendance(
        studentId: Long,
        stdClass: Int,
        term: Int,
        marksMap: Map<Subject, MarksInputData>,
        totalWorkingDays: Int,
        presentDays: Int
    ) {
        viewModelScope.launch {
            val marksEntities = marksMap.map { (subject, data) ->
                if (stdClass in 1..3) {
                    val n1Oral = data.naney1Oral.coerceIn(0, 10)
                    val n1Act = data.naney1Activity.coerceIn(0, 10)
                    val n1Wri = data.naney1Written.coerceIn(0, 5)
                    val n1Tot = n1Oral + n1Act + n1Wri

                    val n2Oral = data.naney2Oral.coerceIn(0, 10)
                    val n2Act = data.naney2Activity.coerceIn(0, 10)
                    val n2Wri = data.naney2Written.coerceIn(0, 5)
                    val n2Tot = n2Oral + n2Act + n2Wri

                    val thOral = data.thiranariOral.coerceIn(0, 10)
                    val thWri = data.thiranariWritten.coerceIn(0, 40)
                    val thTot = thOral + thWri

                    val total = (n1Tot + n2Tot + thTot).coerceAtMost(100)
                    val grade = CceGradeEvaluator.getGradeForClass1To3(total)
                    val level = when {
                        total >= 80 -> "மலர்"
                        total >= 60 -> "மொட்டு"
                        else -> "அரும்பு"
                    }

                    StudentMarks(
                        studentId = studentId,
                        term = term,
                        subjectKey = subject.key,
                        naney1Oral = n1Oral,
                        naney1Activity = n1Act,
                        naney1Written = n1Wri,
                        naney2Oral = n2Oral,
                        naney2Activity = n2Act,
                        naney2Written = n2Wri,
                        thiranariOral = thOral,
                        thiranariWritten = thWri,
                        faA = n1Tot,
                        faB = n2Tot,
                        faTotal = n1Tot + n2Tot,
                        sa = thTot,
                        total = total,
                        learningLevel = level,
                        grade = grade
                    )
                } else if (stdClass == 8) {
                    // எட்டாம் வகுப்பிற்கு மட்டும் நேரடி 100 மதிப்பெண் (Req 3: வளரறி, தொகுத்தறி என பிரிக்கவேண்டாம்)
                    val total = (if (data.directTotal > 0) data.directTotal else data.sa).coerceIn(0, 100)
                    val grade = CceGradeEvaluator.getGradeForClass4To8(total)
                    StudentMarks(
                        studentId = studentId,
                        term = term,
                        subjectKey = subject.key,
                        faTotal = 0,
                        sa = total,
                        total = total,
                        grade = grade
                    )
                } else {
                    val total = (data.faTotal + data.sa).coerceAtMost(100)
                    val grade = CceGradeEvaluator.getGradeForClass4To8(total)
                    StudentMarks(
                        studentId = studentId,
                        term = term,
                        subjectKey = subject.key,
                        faTotal = data.faTotal,
                        sa = data.sa,
                        total = total,
                        grade = grade
                    )
                }
            }

            repository.saveMarks(marksEntities)

            repository.saveAttendance(
                TermAttendance(
                    studentId = studentId,
                    term = term,
                    totalWorkingDays = totalWorkingDays,
                    presentDays = presentDays
                )
            )

            uiMessage.value = "மதிப்பெண்கள் மற்றும் வருகை வெற்றிகரமாக சேமிக்கப்பட்டன!"
        }
    }

    fun saveBatchClassMarks(marksList: List<StudentMarks>) {
        viewModelScope.launch {
            repository.saveMarks(marksList)
            uiMessage.value = "அனைத்து மாணவர் மதிப்பெண்களும் சேமிக்கப்பட்டன!"
        }
    }

    fun saveBatchPrimarySubjectMarks(
        term: Int,
        subject: Subject,
        marksMap: Map<Long, MarksInputData>
    ) {
        viewModelScope.launch {
            val marksEntities = marksMap.map { (studentId, data) ->
                val total = data.calculatedTotal
                val level = if (total >= 80) "மலர்" else if (total >= 60) "மொட்டு" else "அரும்பு"
                val grade = CceGradeEvaluator.getGradeForClass1To3(total)
                StudentMarks(
                    studentId = studentId,
                    term = term,
                    subjectKey = subject.key,
                    faA = data.naney1Total,
                    faB = data.naney2Total,
                    faTotal = (data.naney1Total + data.naney2Total).coerceAtMost(50),
                    sa = data.thiranariTotal,
                    naney1Oral = data.naney1Oral,
                    naney1Activity = data.naney1Activity,
                    naney1Written = data.naney1Written,
                    naney2Oral = data.naney2Oral,
                    naney2Activity = data.naney2Activity,
                    naney2Written = data.naney2Written,
                    thiranariOral = data.thiranariOral,
                    thiranariWritten = data.thiranariWritten,
                    total = total,
                    learningLevel = level,
                    grade = grade
                )
            }
            repository.saveMarks(marksEntities)
            uiMessage.value = "அனைத்து மாணவர் மதிப்பெண்களும் சேமிக்கப்பட்டன!"
        }
    }

    fun exportConsolidatedExcel(context: Context, stdClass: Int, onComplete: (File) -> Unit) {
        viewModelScope.launch {
            val records = loadConsolidatedRecordsForClass(stdClass)
            val file = ExcelReportGenerator.generateConsolidatedCsv(
                context = context,
                school = schoolProfile.value,
                stdClass = stdClass,
                records = records
            )
            onComplete(file)
        }
    }

    fun exportTermExcel(context: Context, stdClass: Int, term: Int, onComplete: (File) -> Unit) {
        viewModelScope.launch {
            val records = loadConsolidatedRecordsForClass(stdClass)
            val file = ExcelReportGenerator.generateTermCsv(
                context = context,
                school = schoolProfile.value,
                stdClass = stdClass,
                term = term,
                records = records
            )
            onComplete(file)
        }
    }

    fun exportClass1To3SubjectCsv(
        context: Context,
        stdClass: Int,
        term: Int,
        subject: Subject,
        onComplete: (File) -> Unit
    ) {
        viewModelScope.launch {
            val records = loadConsolidatedRecordsForClass(stdClass)
            val file = ExcelReportGenerator.generateClass1To3SubjectCsv(
                context = context,
                school = schoolProfile.value,
                stdClass = stdClass,
                term = term,
                subject = subject,
                records = records
            )
            onComplete(file)
        }
    }

    fun exportConsolidatedPdf(context: Context, stdClass: Int, onComplete: (File) -> Unit) {
        viewModelScope.launch {
            val records = loadConsolidatedRecordsForClass(stdClass)
            val file = PdfReportGenerator.generateConsolidatedPdf(
                context = context,
                school = schoolProfile.value,
                stdClass = stdClass,
                records = records
            )
            onComplete(file)
        }
    }

    fun exportClass1To3SubjectPdf(
        context: Context,
        stdClass: Int,
        term: Int,
        subject: Subject,
        onComplete: (File) -> Unit
    ) {
        viewModelScope.launch {
            val records = loadConsolidatedRecordsForClass(stdClass)
            val file = PdfReportGenerator.generateClass1To3SubjectPdf(
                context = context,
                school = schoolProfile.value,
                stdClass = stdClass,
                term = term,
                subject = subject,
                records = records
            )
            onComplete(file)
        }
    }

    fun exportCertificatePdf(context: Context, record: StudentConsolidatedRecord, onComplete: (File) -> Unit) {
        viewModelScope.launch {
            val file = PdfReportGenerator.generateClass8CertificatePdf(
                context = context,
                school = schoolProfile.value,
                record = record
            )
            onComplete(file)
        }
    }

    fun calculateClassRanks(records: List<StudentConsolidatedRecord>, term: Int): Map<Long, Int> {
        val sorted = records.sortedByDescending { it.getTermTotal(term) }
        val rankMap = mutableMapOf<Long, Int>()
        var currentRank = 1
        sorted.forEachIndexed { index, record ->
            if (index > 0 && record.getTermTotal(term) < sorted[index - 1].getTermTotal(term)) {
                currentRank = index + 1
            }
            rankMap[record.student.id] = currentRank
        }
        return rankMap
    }

    fun exportTermRankCardPdf(
        context: Context,
        record: StudentConsolidatedRecord,
        term: Int,
        rank: Int,
        totalStudents: Int,
        onComplete: (File) -> Unit
    ) {
        viewModelScope.launch {
            val file = PdfReportGenerator.generateTermRankCardPdf(
                context = context,
                school = schoolProfile.value,
                record = record,
                term = term,
                rank = rank,
                totalClassStudents = totalStudents
            )
            onComplete(file)
        }
    }

    fun exportClassAllTermRankCardsPdf(
        context: Context,
        records: List<StudentConsolidatedRecord>,
        term: Int,
        onComplete: (File) -> Unit
    ) {
        viewModelScope.launch {
            val ranksMap = calculateClassRanks(records, term)
            val file = PdfReportGenerator.generateClassAllTermRankCardsPdf(
                context = context,
                school = schoolProfile.value,
                records = records,
                term = term,
                ranksMap = ranksMap
            )
            onComplete(file)
        }
    }

    fun promoteStudents(
        promoteAll: Boolean,
        targetClass: Int?,
        newAcademicYear: String,
        archiveClass8: Boolean,
        onComplete: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val studentsList = allStudents.value
            val count: Int
            if (promoteAll) {
                count = studentsList.count { it.stdClass in 1..8 }
                repository.promoteAllClasses(newAcademicYear, archiveClass8)
            } else if (targetClass != null) {
                count = studentsList.count { it.stdClass == targetClass }
                val nextClass = if (targetClass >= 8) 9 else targetClass + 1
                repository.promoteSingleClass(targetClass, nextClass, newAcademicYear)
            } else {
                count = 0
            }

            val currentProfile = schoolProfile.value
            if (currentProfile.academicYear != newAcademicYear) {
                repository.updateSchoolProfile(currentProfile.copy(academicYear = newAcademicYear))
            }

            uiMessage.value = "மாணவர் வகுப்பு உயர்வு வெற்றிகரமாக நிறைவடைந்தது! ($count மாணவர்கள் உயர்த்தப்பட்டனர்)"
            onComplete(count)
        }
    }
}

data class MarksInputData(
    val faA: Int = 0,
    val faB: Int = 0,
    val faTotal: Int = 0,
    val sa: Int = 0,
    val directTotal: Int = 0,       // எட்டாம் வகுப்பு: நேரடி 100 மதிப்பெண் (வளரறி / தொகுத்தறி பிரிக்காமல்)
    // வகுப்பு 1-3 புதிய பாடத்திட்ட கூறுகள்:
    val naney1Oral: Int = 10,       // நானே செய்வேன் - 1: வாய்மொழி [10]
    val naney1Activity: Int = 10,   // நானே செய்வேன் - 1: செயல்திட்டம்/செயல்பாடு [10]
    val naney1Written: Int = 5,     // நானே செய்வேன் - 1: எழுத்துவழி [5]
    val naney2Oral: Int = 10,       // நானே செய்வேன் - 2: வாய்மொழி [10]
    val naney2Activity: Int = 10,   // நானே செய்வேன் - 2: செயல்திட்டம்/செயல்பாடு [10]
    val naney2Written: Int = 5,     // நானே செய்வேன் - 2: எழுத்துவழி [5]
    val thiranariOral: Int = 10,    // திறனறி மதிப்பீடு: வாய்மொழி [10]
    val thiranariWritten: Int = 40  // திறனறி மதிப்பீடு: எழுத்துவழி [40]
) {
    val naney1Total: Int get() = naney1Oral + naney1Activity + naney1Written
    val naney2Total: Int get() = naney2Oral + naney2Activity + naney2Written
    val thiranariTotal: Int get() = thiranariOral + thiranariWritten
    val calculatedTotal: Int get() = naney1Total + naney2Total + thiranariTotal
}
