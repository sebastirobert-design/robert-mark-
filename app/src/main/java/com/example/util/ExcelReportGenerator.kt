package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.SchoolProfile
import com.example.data.model.StudentConsolidatedRecord
import com.example.data.model.Subject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object ExcelReportGenerator {

    /**
     * Generates a CSV file formatted for Excel with UTF-8 BOM.
     * Contains the Three Terms Average Register as requested.
     */
    fun generateConsolidatedCsv(
        context: Context,
        school: SchoolProfile,
        stdClass: Int,
        records: List<StudentConsolidatedRecord>
    ): File {
        val fileName = "Consolidated_Marks_Class_${stdClass}_${school.academicYear.replace("-", "_")}.csv"
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val file = File(reportsDir, fileName)

        val mainSubjects = Subject.getMainSubjectsForClass(stdClass)
        val hasPe = Subject.hasPeSubject(stdClass)

        FileOutputStream(file).use { fos ->
            // Write UTF-8 Byte Order Mark (BOM) so Excel renders Tamil Unicode correctly
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                // Header rows
                val schoolTitle = if (school.udiseCode.isNotBlank()) "${school.schoolName} (UDISE: ${school.udiseCode})" else school.schoolName
                writer.appendLine("\"$schoolTitle\"")
                writer.appendLine("\"${school.unionName}\",\"${school.districtName}\",\"UDISE: ${school.udiseCode}\"")
                writer.appendLine("\"முப்பருவ சராசரி மதிப்பெண் பட்டியல் (Three Terms Average Marks Register)\"")
                writer.appendLine("\"வகுப்பு: $stdClass\",\"கல்வியாண்டு: ${school.academicYear}\"")
                writer.appendLine("")

                // Multi-row header for columns
                // Row 1: Subject groupings
                val row1 = StringBuilder("\"வ.எண்\",\"சேர்க்கை எண்\",\"மாணவர் பெயர்\",\"வகுப்பு\",\"இனம்\"")
                for (sub in mainSubjects) {
                    row1.append(",\"${sub.tamilName}\",\"\",\"\"")
                }
                row1.append(",\"மொத்தம் (TOTAL 500)\"")
                if (hasPe) {
                    row1.append(",\"${Subject.PE.tamilName} (தனி மதிப்பீடு)\",\"\",\"\"")
                }
                row1.append(",\"பள்ளி வேலை நாட்கள்\",\"வருகை நாட்கள்\",\"தேர்ச்சி விபரம்\"")
                writer.appendLine(row1.toString())

                // Row 2: Sub-columns (SA, FA, மொ)
                val row2 = StringBuilder("\"\",\"\",\"\",\"\",\"\"")
                for (sub in mainSubjects) {
                    row2.append(",\"SA (60)\",\"FA (40)\",\"மொத்தம் (100)\"")
                }
                row2.append(",\"\"")
                if (hasPe) {
                    row2.append(",\"SA (60)\",\"FA (40)\",\"மொத்தம் (100)\"")
                }
                row2.append(",\"\",\"\",\"\"")
                writer.appendLine(row2.toString())

                // Student data rows
                records.forEachIndexed { index, record ->
                    val row = StringBuilder()
                    row.append("\"${index + 1}\",")
                    row.append("\"${record.student.admissionNo}\",")
                    row.append("\"${record.student.name}\",")
                    row.append("\"${record.student.stdClass} - ${record.student.section}\",")
                    row.append("\"${record.student.community}\"")

                    // 1. Five Main Academic Subjects
                    for (sub in mainSubjects) {
                        val subMarks = record.subjectMarks[sub]
                        val sa = subMarks?.avgSa ?: 0
                        val fa = subMarks?.avgFa ?: 0
                        val tot = subMarks?.avgTotal ?: 0
                        row.append(",\"$sa\",\"$fa\",\"$tot\"")
                    }

                    // 2. TOTAL (5 main subjects only out of 500)
                    row.append(",\"${record.grandAvgTotal}\"")

                    // 3. Physical Education (PE) placed AFTER TOTAL
                    if (hasPe) {
                        val peMarks = record.subjectMarks[Subject.PE]
                        val sa = peMarks?.avgSa ?: 0
                        val fa = peMarks?.avgFa ?: 0
                        val tot = peMarks?.avgTotal ?: 0
                        row.append(",\"$sa\",\"$fa\",\"$tot\"")
                    }

                    row.append(",\"${record.totalWorkingDays}\"")
                    row.append(",\"${record.totalPresentDays}\"")
                    row.append(",\"${record.resultStatus}\"")

                    writer.appendLine(row.toString())
                }

                writer.appendLine("")
                writer.appendLine("\"தலைமை ஆசிரியர் கையொப்பம்: ${school.headmasterName}\"")
            }
        }

        return file
    }

    /**
     * Generates a single-term CSV file formatted for Excel.
     */
    fun generateTermCsv(
        context: Context,
        school: SchoolProfile,
        stdClass: Int,
        term: Int,
        records: List<StudentConsolidatedRecord>
    ): File {
        val fileName = "Term_${term}_Marks_Class_${stdClass}_${school.academicYear.replace("-", "_")}.csv"
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val file = File(reportsDir, fileName)

        val mainSubjects = Subject.getMainSubjectsForClass(stdClass)
        val hasPe = Subject.hasPeSubject(stdClass)

        FileOutputStream(file).use { fos ->
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                val schoolTitle = if (school.udiseCode.isNotBlank()) "${school.schoolName} (UDISE: ${school.udiseCode})" else school.schoolName
                writer.appendLine("\"$schoolTitle\"")
                writer.appendLine("\"${school.unionName}\",\"${school.districtName}\",\"UDISE: ${school.udiseCode}\"")
                writer.appendLine("\"பருவம்: $term மதிப்பெண் பட்டியல் (Term $term Marks Register)\"")
                writer.appendLine("\"வகுப்பு: $stdClass\",\"கல்வியாண்டு: ${school.academicYear}\"")
                writer.appendLine("")

                val header = StringBuilder("\"வ.எண்\",\"சேர்க்கை எண்\",\"மாணவர் பெயர்\",\"வகுப்பு\",\"இனம்\"")
                for (sub in mainSubjects) {
                    if (stdClass in 1..3) {
                        header.append(",\"${sub.tamilName} FA(a)\",\"${sub.tamilName} FA(b)\",\"${sub.tamilName} SA\",\"${sub.tamilName} மொ\"")
                    } else {
                        header.append(",\"${sub.tamilName} FA\",\"${sub.tamilName} SA\",\"${sub.tamilName} மொத்தம்\"")
                    }
                }
                header.append(",\"மொத்தம் (TOTAL)\"")
                if (hasPe) {
                    header.append(",\"${Subject.PE.tamilName} FA\",\"${Subject.PE.tamilName} SA\",\"${Subject.PE.tamilName} மொத்தம்\"")
                }
                header.append(",\"பள்ளி வேலை நாட்கள்\",\"வருகை நாட்கள்\"")
                writer.appendLine(header.toString())

                records.forEachIndexed { index, record ->
                    val row = StringBuilder()
                    row.append("\"${index + 1}\",")
                    row.append("\"${record.student.admissionNo}\",")
                    row.append("\"${record.student.name}\",")
                    row.append("\"${record.student.stdClass} - ${record.student.section}\",")
                    row.append("\"${record.student.community}\"")

                    // 1. Five Main Academic Subjects
                    for (sub in mainSubjects) {
                        val subMarks = record.subjectMarks[sub]
                        val m = when (term) {
                            1 -> subMarks?.term1Marks
                            2 -> subMarks?.term2Marks
                            3 -> subMarks?.term3Marks
                            else -> null
                        }
                        if (stdClass in 1..3) {
                            row.append(",\"${m?.faA ?: 0}\",\"${m?.faB ?: 0}\",\"${m?.sa ?: 0}\",\"${m?.total ?: 0}\"")
                        } else {
                            row.append(",\"${m?.faTotal ?: 0}\",\"${m?.sa ?: 0}\",\"${m?.total ?: 0}\"")
                        }
                    }

                    // 2. TOTAL (5 main subjects only)
                    val termTotal = record.getTermTotal(term)
                    row.append(",\"$termTotal\"")

                    // 3. Physical Education placed AFTER TOTAL
                    if (hasPe) {
                        val peMarks = record.subjectMarks[Subject.PE]
                        val m = when (term) {
                            1 -> peMarks?.term1Marks
                            2 -> peMarks?.term2Marks
                            3 -> peMarks?.term3Marks
                            else -> null
                        }
                        row.append(",\"${m?.faTotal ?: 0}\",\"${m?.sa ?: 0}\",\"${m?.total ?: 0}\"")
                    }

                    val att = when (term) {
                        1 -> record.term1Attendance
                        2 -> record.term2Attendance
                        3 -> record.term3Attendance
                        else -> null
                    }

                    row.append(",\"${att?.totalWorkingDays ?: 0}\"")
                    row.append(",\"${att?.presentDays ?: 0}\"")

                    writer.appendLine(row.toString())
                }
            }
        }

        return file
    }

    /**
     * Generates a single-subject CCE CSV matching the official format for Classes 1 to 3:
     * புதிய பாடத்திட்டம் - நானே செய்வேன் 1 [25], நானே செய்வேன் 2 [25], திறனறி மதிப்பீடு [50], மொத்தம் [100], விழுக்காடு (%)
     */
    fun generateClass1To3SubjectCsv(
        context: Context,
        school: SchoolProfile,
        stdClass: Int,
        term: Int,
        subject: Subject,
        records: List<StudentConsolidatedRecord>
    ): File {
        val fileName = "Class_${stdClass}_Term_${term}_${subject.name}_Marks_${school.academicYear.replace("-", "_")}.csv"
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val file = File(reportsDir, fileName)

        FileOutputStream(file).use { fos ->
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                val schoolTitle = if (school.udiseCode.isNotBlank()) "${school.schoolName} (UDISE: ${school.udiseCode})" else school.schoolName
                writer.appendLine("\"$schoolTitle\"")
                writer.appendLine("\"${school.unionName}\",\"${school.districtName}\",\"UDISE: ${school.udiseCode}\"")
                writer.appendLine("\"${school.academicYear} மாணவர் மதிப்பெண் பதிவேடு பருவம் : $term\"")
                writer.appendLine("\"புதிய பாடத்திட்டம் வகுப்பு : $stdClass\",\"பாடம்: ${subject.tamilName} (${subject.name})\"")
                writer.appendLine("")

                // Table Headers matching the user's PDF
                writer.appendLine(
                    "\"வரிசை எண்\",\"சேர்க்கை எண்\",\"மாணவர் பெயர்\"," +
                    "\"நானே செய்வேன் - 1 [25 மதிப்பெண்]\",\"\",\"\",\"" +
                    "\"நானே செய்வேன் - 2 [25 மதிப்பெண்]\",\"\",\"\",\"" +
                    "\"திறனறி மதிப்பீடு [50 மதிப்பெண்]\",\"\",\"மொத்தம் [100]\",\"விழுக்காடு\""
                )
                writer.appendLine(
                    "\"\",\"\",\"\",\"வாய்மொழி [10]\",\"செயல்திட்டம் / செயல்பாடு [10]\",\"எழுத்துவழி [5]\"," +
                    "\"வாய்மொழி [10]\",\"செயல்திட்டம் / செயல்பாடு [10]\",\"எழுத்துவழி [5]\"," +
                    "\"வாய்மொழி [10]\",\"எழுத்துவழி [40]\",\"[100]\",\"%\""
                )

                records.forEachIndexed { index, record ->
                    val sm = record.subjectMarks[subject]
                    val m = when (term) {
                        1 -> sm?.term1Marks
                        2 -> sm?.term2Marks
                        3 -> sm?.term3Marks
                        else -> null
                    }

                    val n1Oral = m?.naney1Oral ?: 10
                    val n1Act = m?.naney1Activity ?: 10
                    val n1Wri = m?.naney1Written ?: 5
                    val n2Oral = m?.naney2Oral ?: 10
                    val n2Act = m?.naney2Activity ?: 10
                    val n2Wri = m?.naney2Written ?: 5
                    val thOral = m?.thiranariOral ?: 10
                    val thWri = m?.thiranariWritten ?: 40
                    val tot = m?.total ?: (n1Oral + n1Act + n1Wri + n2Oral + n2Act + n2Wri + thOral + thWri)
                    val pct = "$tot%"

                    val row = StringBuilder()
                    row.append("\"${index + 1}\",")
                    row.append("\"${record.student.admissionNo}\",")
                    row.append("\"${record.student.name}\",")
                    row.append("\"$n1Oral\",\"$n1Act\",\"$n1Wri\",")
                    row.append("\"$n2Oral\",\"$n2Act\",\"$n2Wri\",")
                    row.append("\"$thOral\",\"$thWri\",")
                    row.append("\"$tot\",\"$pct\"")

                    writer.appendLine(row.toString())
                }
            }
        }

        return file
    }

    /**
     * Generates a combined group CSV report for paper saving across multiple classes.
     */
    fun generateCombinedGroupCsv(
        context: Context,
        school: SchoolProfile,
        groupTitle: String,
        classRecordsMap: Map<Int, List<StudentConsolidatedRecord>>,
        term: Int = 0
    ): File {
        val safeTitle = groupTitle.replace(" ", "_").replace("/", "_")
        val fileName = "Combined_Register_${safeTitle}_${school.academicYear.replace("-", "_")}.csv"
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val file = File(reportsDir, fileName)

        val isPrimaryGroup = classRecordsMap.keys.all { it in 1..3 }
        val isClass8Group = classRecordsMap.keys.all { it == 8 }
        val subjects = if (isPrimaryGroup) {
            listOf(Subject.TAMIL, Subject.ENGLISH, Subject.MATHS)
        } else {
            listOf(Subject.TAMIL, Subject.ENGLISH, Subject.MATHS, Subject.SCIENCE, Subject.SOCIAL)
        }
        val maxTotal = subjects.size * 100

        FileOutputStream(file).use { fos ->
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                val schoolTitle = if (school.udiseCode.isNotBlank()) "${school.schoolName} (UDISE: ${school.udiseCode})" else school.schoolName
                writer.appendLine("\"$schoolTitle\"")
                writer.appendLine("\"${school.unionName}\",\"${school.districtName}\",\"UDISE: ${school.udiseCode}\"")
                val termTitle = if (term == 0) "முப்பருவ சராசரி ஒருங்கிணைந்த பதிவேடு" else "பருவம் $term ஒருங்கிணைந்த பதிவேடு"
                writer.appendLine("\"$groupTitle - $termTitle\"")
                writer.appendLine("\"கல்வியாண்டு: ${school.academicYear}\"")
                writer.appendLine("")

                val row1 = StringBuilder("\"வ.எண்\",\"வகுப்பு\",\"சேர்க்கை எண்\",\"மாணவர் பெயர்\",\"இனம்\"")
                for (sub in subjects) {
                    row1.append(",\"${sub.tamilName}\",\"\",\"\"")
                }
                row1.append(",\"மொத்தம் ($maxTotal)\"")
                if (isClass8Group) {
                    row1.append(",\"உடற்கல்வி (தனி)\",\"\",\"\"")
                }
                row1.append(",\"பள்ளி நாட்கள்\",\"வருகை\",\"சராசரி %\"")
                writer.appendLine(row1.toString())

                val row2 = StringBuilder("\"\",\"\",\"\",\"\",\"\"")
                for (sub in subjects) {
                    row2.append(",\"SA\",\"FA\",\"மொ\"")
                }
                row2.append(",\"\"")
                if (isClass8Group) {
                    row2.append(",\"SA\",\"FA\",\"மொ\"")
                }
                row2.append(",\"\",\"\",\"\"")
                writer.appendLine(row2.toString())

                var sNoCounter = 1
                classRecordsMap.toSortedMap().forEach { (cls, recs) ->
                    if (recs.isNotEmpty()) {
                        writer.appendLine("\"--- வகுப்பு $cls (CLASS $cls) - ${recs.size} மாணவர்கள் ---\"")
                        recs.forEach { record ->
                            val row = StringBuilder()
                            row.append("\"${sNoCounter++}\",")
                            row.append("\"$cls\",")
                            row.append("\"${record.student.admissionNo}\",")
                            row.append("\"${record.student.name}\",")
                            row.append("\"${record.student.community}\"")

                            for (sub in subjects) {
                                val sm = record.subjectMarks[sub]
                                val (sa, fa, tot) = if (term == 0) {
                                    Triple(sm?.avgSa ?: 0, sm?.avgFa ?: 0, sm?.avgTotal ?: 0)
                                } else {
                                    val tm = when (term) {
                                        1 -> sm?.term1Marks
                                        2 -> sm?.term2Marks
                                        3 -> sm?.term3Marks
                                        else -> null
                                    }
                                    Triple(tm?.sa ?: 0, tm?.faTotal ?: 0, tm?.total ?: 0)
                                }
                                row.append(",\"$sa\",\"$fa\",\"$tot\"")
                            }

                            val totalMark = if (term == 0) {
                                subjects.sumOf { sub -> record.subjectMarks[sub]?.avgTotal ?: 0 }
                            } else {
                                subjects.sumOf { sub ->
                                    val sm = record.subjectMarks[sub]
                                    val tm = when (term) {
                                        1 -> sm?.term1Marks
                                        2 -> sm?.term2Marks
                                        3 -> sm?.term3Marks
                                        else -> null
                                    }
                                    tm?.total ?: 0
                                }
                            }
                            row.append(",\"$totalMark\"")

                            if (isClass8Group) {
                                val peSm = record.subjectMarks[Subject.PE]
                                val (peSa, peFa, peTot) = if (term == 0) {
                                    Triple(peSm?.avgSa ?: 0, peSm?.avgFa ?: 0, peSm?.avgTotal ?: 0)
                                } else {
                                    val tm = when (term) {
                                        1 -> peSm?.term1Marks
                                        2 -> peSm?.term2Marks
                                        3 -> peSm?.term3Marks
                                        else -> null
                                    }
                                    Triple(tm?.sa ?: 0, tm?.faTotal ?: 0, tm?.total ?: 0)
                                }
                                row.append(",\"$peSa\",\"$peFa\",\"$peTot\"")
                            }

                            val att = when (term) {
                                1 -> record.term1Attendance
                                2 -> record.term2Attendance
                                3 -> record.term3Attendance
                                else -> null
                            }
                            val (wDays, pDays) = if (term == 0) {
                                Pair(record.totalWorkingDays, record.totalPresentDays)
                            } else {
                                Pair(att?.totalWorkingDays ?: school.getWorkingDaysForTerm(term), att?.presentDays ?: 0)
                            }
                            val pct = if (maxTotal > 0) String.format("%.1f%%", (totalMark.toDouble() / maxTotal) * 100) else "0.0%"

                            row.append(",\"$wDays\",\"$pDays\",\"$pct\"")
                            writer.appendLine(row.toString())
                        }
                    }
                }
            }
        }

        return file
    }

    fun shareCsvFile(context: Context, file: File, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Excel / CSV கோப்பை பகிர்க"))
    }
}
