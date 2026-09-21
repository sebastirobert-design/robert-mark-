package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.SchoolProfile
import com.example.data.model.Student
import com.example.data.model.StudentConsolidatedRecord
import com.example.data.model.Subject
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {

    sealed class GroupRowItem {
        data class ClassDivider(val stdClass: Int, val count: Int) : GroupRowItem()
        data class StudentItem(val record: StudentConsolidatedRecord, val classIndex: Int) : GroupRowItem()
    }

    /**
     * Generates a printable A4 Landscape PDF for the Three Terms Average Register.
     */
    fun generateConsolidatedPdf(
        context: Context,
        school: SchoolProfile,
        stdClass: Int,
        records: List<StudentConsolidatedRecord>
    ): File {
        val fileName = "Marks_Register_Class_${stdClass}_${school.academicYear.replace("-", "_")}.pdf"
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val file = File(reportsDir, fileName)

        // A4 Landscape: 842 x 595 points
        val pageWidth = 842
        val pageHeight = 595
        val document = PdfDocument()

        val subjects = Subject.getSubjectsForClass(stdClass)

        // Items per page: ~15 students per page
        val itemsPerPage = 14
        val pageCount = if (records.isEmpty()) 1 else ((records.size - 1) / itemsPerPage) + 1

        for (p in 0 until pageCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, p + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            drawRegisterPage(
                canvas = canvas,
                pageWidth = pageWidth,
                pageHeight = pageHeight,
                school = school,
                stdClass = stdClass,
                subjects = subjects,
                pageIndex = p,
                totalPages = pageCount,
                records = records.drop(p * itemsPerPage).take(itemsPerPage),
                startSno = p * itemsPerPage + 1
            )

            document.finishPage(page)
        }

        FileOutputStream(file).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        return file
    }

    private fun drawRegisterPage(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int,
        school: SchoolProfile,
        stdClass: Int,
        subjects: List<Subject>,
        pageIndex: Int,
        totalPages: Int,
        records: List<StudentConsolidatedRecord>,
        startSno: Int
    ) {
        val margin = 24f
        val paint = Paint().apply { isAntiAlias = true }
        val strokePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.DKGRAY
        }

        // 1. Header Banner
        paint.color = Color.rgb(26, 35, 126) // Deep Navy
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        val schoolHeader = if (school.udiseCode.isNotBlank()) "${school.schoolName} (UDISE: ${school.udiseCode})" else school.schoolName
        canvas.drawText(schoolHeader, pageWidth / 2f, margin + 16f, paint)

        paint.color = Color.rgb(55, 65, 81)
        paint.textSize = 10.5f
        paint.typeface = Typeface.DEFAULT
        val subHeader = if (school.udiseCode.isNotBlank()) "${school.unionName}   |   ${school.districtName}   |   UDISE: ${school.udiseCode}" else "${school.unionName}   |   ${school.districtName}"
        canvas.drawText(subHeader, pageWidth / 2f, margin + 30f, paint)

        paint.color = Color.rgb(180, 83, 9) // Amber/Gold accent
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(
            "முப்பருவ சராசரி மதிப்பெண் பட்டியல் (Three Terms Average Marks Register)",
            pageWidth / 2f,
            margin + 46f,
            paint
        )

        // Class and Academic year details
        paint.color = Color.BLACK
        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("வகுப்பு: $stdClass", margin, margin + 58f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("கல்வியாண்டு: ${school.academicYear}   (பக்கம் ${pageIndex + 1}/$totalPages)", pageWidth - margin, margin + 58f, paint)

        // 2. Table Layout Coordinates
        val tableTop = margin + 65f
        val tableLeft = margin
        val tableRight = pageWidth - margin
        val tableWidth = tableRight - tableLeft

        val mainSubjects = Subject.getMainSubjectsForClass(stdClass)
        val hasPe = Subject.hasPeSubject(stdClass)

        // Column widths
        // Fixed columns: Sno(24), AdmNo(44), Name(106), Comm(28)
        // Middle columns for subjects: main subjects + (PE if exists)
        // Fixed end columns: Total(40), WorkDays(36), Present(36), Result(56)
        val sNoW = 24f
        val admW = 44f
        val nameW = 106f
        val commW = 28f
        val endW = 40f + 36f + 36f + 56f
        val remainingW = tableWidth - (sNoW + admW + nameW + commW + endW)
        val totalSubjectCount = if (hasPe) mainSubjects.size + 1 else mainSubjects.size
        val subjectW = remainingW / totalSubjectCount
        val subColW = subjectW / 3f // SA, FA, Tot

        val headerH1 = 18f
        val headerH2 = 14f
        val rowH = 22f

        // Draw Table Header Background
        paint.color = Color.rgb(238, 242, 255) // Light Indigo
        paint.style = Paint.Style.FILL
        canvas.drawRect(tableLeft, tableTop, tableRight, tableTop + headerH1 + headerH2, paint)

        // Draw Header Outer Box
        canvas.drawRect(tableLeft, tableTop, tableRight, tableTop + headerH1 + headerH2, strokePaint)

        // Header Text Paint
        val headerTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(30, 41, 59)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // Draw Fixed Columns Headers
        canvas.drawText("வ.எண்", tableLeft + sNoW / 2f, tableTop + 20f, headerTextPaint)
        canvas.drawText("சே.எண்", tableLeft + sNoW + admW / 2f, tableTop + 20f, headerTextPaint)
        canvas.drawText("மாணவர் பெயர்", tableLeft + sNoW + admW + nameW / 2f, tableTop + 20f, headerTextPaint)
        canvas.drawText("இனம்", tableLeft + sNoW + admW + nameW + commW / 2f, tableTop + 20f, headerTextPaint)

        // 1. Five Main Academic Subject Columns
        var curX = tableLeft + sNoW + admW + nameW + commW
        val subTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.DKGRAY
            textSize = 6.5f
            textAlign = Paint.Align.CENTER
        }

        val isClass8 = stdClass == 8

        if (isClass8) {
            for (sub in mainSubjects) {
                canvas.drawText(sub.shortName, curX + subjectW / 2f, tableTop + 13f, headerTextPaint)
                canvas.drawText("(100)", curX + subjectW / 2f, tableTop + headerH1 + 10f, subTextPaint)
                canvas.drawLine(curX, tableTop, curX, tableTop + headerH1 + headerH2, strokePaint)
                curX += subjectW
            }

            // 2. TOTAL column (Sum of 5 main subjects out of 500)
            canvas.drawLine(curX, tableTop, curX, tableTop + headerH1 + headerH2, strokePaint)
            canvas.drawText("TOTAL", curX + 20f, tableTop + 13f, headerTextPaint)
            canvas.drawText("(500)", curX + 20f, tableTop + headerH1 + 10f, subTextPaint)
            curX += 40f

            // 3. Physical Education Column (AFTER TOTAL, separate from academic marks)
            if (hasPe) {
                canvas.drawLine(curX, tableTop, curX, tableTop + headerH1 + headerH2, strokePaint)
                canvas.drawText(Subject.PE.shortName, curX + subjectW / 2f, tableTop + 13f, headerTextPaint)
                canvas.drawText("(100)", curX + subjectW / 2f, tableTop + headerH1 + 10f, subTextPaint)
                curX += subjectW
            }
        } else {
            for (sub in mainSubjects) {
                canvas.drawText(sub.shortName, curX + subjectW / 2f, tableTop + 12f, headerTextPaint)
                // sub-headers SA, FA, மொ
                canvas.drawText("SA", curX + subColW * 0.5f, tableTop + headerH1 + 10f, subTextPaint)
                canvas.drawText("FA", curX + subColW * 1.5f, tableTop + headerH1 + 10f, subTextPaint)
                canvas.drawText("மொ", curX + subColW * 2.5f, tableTop + headerH1 + 10f, subTextPaint)

                // Horizontal sub-divider line
                canvas.drawLine(curX, tableTop + headerH1, curX + subjectW, tableTop + headerH1, strokePaint)

                // Vertical sub-divider lines
                canvas.drawLine(curX + subColW, tableTop + headerH1, curX + subColW, tableTop + headerH1 + headerH2, strokePaint)
                canvas.drawLine(curX + subColW * 2f, tableTop + headerH1, curX + subColW * 2f, tableTop + headerH1 + headerH2, strokePaint)

                // Vertical line dividing subjects
                canvas.drawLine(curX, tableTop, curX, tableTop + headerH1 + headerH2, strokePaint)
                curX += subjectW
            }

            // 2. TOTAL column (Sum of 5 main subjects out of 500)
            canvas.drawLine(curX, tableTop, curX, tableTop + headerH1 + headerH2, strokePaint)
            canvas.drawText("TOTAL", curX + 20f, tableTop + 20f, headerTextPaint)
            curX += 40f

            // 3. Physical Education Column (AFTER TOTAL, separate from academic marks)
            if (hasPe) {
                canvas.drawLine(curX, tableTop, curX, tableTop + headerH1 + headerH2, strokePaint)
                canvas.drawText(Subject.PE.shortName, curX + subjectW / 2f, tableTop + 12f, headerTextPaint)
                canvas.drawText("SA", curX + subColW * 0.5f, tableTop + headerH1 + 10f, subTextPaint)
                canvas.drawText("FA", curX + subColW * 1.5f, tableTop + headerH1 + 10f, subTextPaint)
                canvas.drawText("மொ", curX + subColW * 2.5f, tableTop + headerH1 + 10f, subTextPaint)

                canvas.drawLine(curX, tableTop + headerH1, curX + subjectW, tableTop + headerH1, strokePaint)
                canvas.drawLine(curX + subColW, tableTop + headerH1, curX + subColW, tableTop + headerH1 + headerH2, strokePaint)
                canvas.drawLine(curX + subColW * 2f, tableTop + headerH1, curX + subColW * 2f, tableTop + headerH1 + headerH2, strokePaint)
                curX += subjectW
            }
        }

        // 4. End headers: Working Days, Present Days, Result
        canvas.drawLine(curX, tableTop, curX, tableTop + headerH1 + headerH2, strokePaint)
        canvas.drawText("வேலை", curX + 18f, tableTop + 13f, headerTextPaint)
        canvas.drawText("நாட்கள்", curX + 18f, tableTop + 23f, headerTextPaint)
        curX += 36f

        canvas.drawLine(curX, tableTop, curX, tableTop + headerH1 + headerH2, strokePaint)
        canvas.drawText("வருகை", curX + 18f, tableTop + 13f, headerTextPaint)
        canvas.drawText("நாட்கள்", curX + 18f, tableTop + 23f, headerTextPaint)
        curX += 36f

        canvas.drawLine(curX, tableTop, curX, tableTop + headerH1 + headerH2, strokePaint)
        canvas.drawText("தேர்ச்சி விபரம்", curX + 28f, tableTop + 20f, headerTextPaint)

        // Draw Data Rows
        var rowY = tableTop + headerH1 + headerH2
        val rowTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
        }
        val nameTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 7.5f
            textAlign = Paint.Align.LEFT
        }

        records.forEachIndexed { i, record ->
            val isEven = i % 2 == 0
            if (isEven) {
                paint.color = Color.rgb(250, 250, 250)
                canvas.drawRect(tableLeft, rowY, tableRight, rowY + rowH, paint)
            }
            canvas.drawRect(tableLeft, rowY, tableRight, rowY + rowH, strokePaint)

            // Fixed columns
            canvas.drawText("${startSno + i}", tableLeft + sNoW / 2f, rowY + 14f, rowTextPaint)
            canvas.drawText(record.student.admissionNo, tableLeft + sNoW + admW / 2f, rowY + 14f, rowTextPaint)

            val studentName = record.student.name
            val displayName = if (studentName.length > 18) studentName.take(17) + ".." else studentName
            canvas.drawText(displayName, tableLeft + sNoW + admW + 4f, rowY + 14f, nameTextPaint)

            canvas.drawText(record.student.community, tableLeft + sNoW + admW + nameW + commW / 2f, rowY + 14f, rowTextPaint)

            var rx = tableLeft + sNoW + admW + nameW + commW
            if (isClass8) {
                // 1. Five Main Academic Subjects data (Single 100 mark column each)
                for (sub in mainSubjects) {
                    val sm = record.subjectMarks[sub]
                    val totStr = if (sm?.avgTotal != null) "${sm.avgTotal}" else "-"
                    val boldTextPaint = Paint(rowTextPaint).apply {
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                    canvas.drawText(totStr, rx + subjectW / 2f, rowY + 14f, boldTextPaint)
                    canvas.drawLine(rx, rowY, rx, rowY + rowH, strokePaint)
                    rx += subjectW
                }

                // 2. TOTAL column (Sum of 5 main subjects out of 500)
                canvas.drawLine(rx, rowY, rx, rowY + rowH, strokePaint)
                val totalPaint = Paint(rowTextPaint).apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    color = Color.rgb(30, 58, 138)
                }
                canvas.drawText("${record.grandAvgTotal}", rx + 20f, rowY + 14f, totalPaint)
                rx += 40f

                // 3. Physical Education data (Single 100 mark column)
                if (hasPe) {
                    val peSm = record.subjectMarks[Subject.PE]
                    val totStr = if (peSm?.avgTotal != null) "${peSm.avgTotal}" else "-"
                    canvas.drawLine(rx, rowY, rx, rowY + rowH, strokePaint)
                    val pePaint = Paint(rowTextPaint).apply {
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        color = Color.rgb(133, 77, 14)
                    }
                    canvas.drawText(totStr, rx + subjectW / 2f, rowY + 14f, pePaint)
                    rx += subjectW
                }
            } else {
                // 1. Five Main Academic Subjects data
                for (sub in mainSubjects) {
                    val sm = record.subjectMarks[sub]
                    val saStr = if (sm?.avgSa != null) "${sm.avgSa}" else "-"
                    val faStr = if (sm?.avgFa != null) "${sm.avgFa}" else "-"
                    val totStr = if (sm?.avgTotal != null) "${sm.avgTotal}" else "-"

                    canvas.drawText(saStr, rx + subColW * 0.5f, rowY + 14f, rowTextPaint)
                    canvas.drawText(faStr, rx + subColW * 1.5f, rowY + 14f, rowTextPaint)

                    // Bold total
                    val boldTextPaint = Paint(rowTextPaint).apply {
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                    canvas.drawText(totStr, rx + subColW * 2.5f, rowY + 14f, boldTextPaint)

                    // Sub lines
                    canvas.drawLine(rx + subColW, rowY, rx + subColW, rowY + rowH, strokePaint)
                    canvas.drawLine(rx + subColW * 2f, rowY, rx + subColW * 2f, rowY + rowH, strokePaint)
                    canvas.drawLine(rx, rowY, rx, rowY + rowH, strokePaint)
                    rx += subjectW
                }

                // 2. TOTAL column (Calculated only for 5 main subjects)
                canvas.drawLine(rx, rowY, rx, rowY + rowH, strokePaint)
                val totalPaint = Paint(rowTextPaint).apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    color = Color.rgb(30, 58, 138)
                }
                canvas.drawText("${record.grandAvgTotal}", rx + 20f, rowY + 14f, totalPaint)
                rx += 40f

                // 3. Physical Education data (Placed AFTER TOTAL)
                if (hasPe) {
                    val peSm = record.subjectMarks[Subject.PE]
                    val saStr = if (peSm?.avgSa != null) "${peSm.avgSa}" else "-"
                    val faStr = if (peSm?.avgFa != null) "${peSm.avgFa}" else "-"
                    val totStr = if (peSm?.avgTotal != null) "${peSm.avgTotal}" else "-"

                    canvas.drawLine(rx, rowY, rx, rowY + rowH, strokePaint)
                    canvas.drawText(saStr, rx + subColW * 0.5f, rowY + 14f, rowTextPaint)
                    canvas.drawText(faStr, rx + subColW * 1.5f, rowY + 14f, rowTextPaint)
                    val pePaint = Paint(rowTextPaint).apply {
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        color = Color.rgb(133, 77, 14)
                    }
                    canvas.drawText(totStr, rx + subColW * 2.5f, rowY + 14f, pePaint)

                    canvas.drawLine(rx + subColW, rowY, rx + subColW, rowY + rowH, strokePaint)
                    canvas.drawLine(rx + subColW * 2f, rowY, rx + subColW * 2f, rowY + rowH, strokePaint)
                    rx += subjectW
                }
            }

            // 4. End columns: Working Days, Present Days, Result Status
            canvas.drawLine(rx, rowY, rx, rowY + rowH, strokePaint)
            canvas.drawText("${record.totalWorkingDays}", rx + 18f, rowY + 14f, rowTextPaint)
            rx += 36f

            canvas.drawLine(rx, rowY, rx, rowY + rowH, strokePaint)
            canvas.drawText("${record.totalPresentDays}", rx + 18f, rowY + 14f, rowTextPaint)
            rx += 36f

            canvas.drawLine(rx, rowY, rx, rowY + rowH, strokePaint)
            val resPaint = Paint(rowTextPaint).apply {
                color = if (record.resultStatus == "தேர்ச்சி") Color.rgb(22, 101, 52) else Color.rgb(185, 28, 28)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(record.resultStatus, rx + 28f, rowY + 14f, resPaint)

            rowY += rowH
        }

        // Draw Signatures Block at Bottom
        val sigY = pageHeight - margin - 10f
        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("வகுப்பாசிரியர் கையொப்பம்", margin + 30f, sigY, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("தலைமை ஆசிரியர் கையொப்பம் மற்றும் பள்ளி முத்திரை", pageWidth - margin - 30f, sigY, paint)
    }

    /**
     * Generates an official Annual Mark Certificate for Class 8 student (or any student).
     * A4 Portrait (595 x 842 points).
     */
    fun generateClass8CertificatePdf(
        context: Context,
        school: SchoolProfile,
        record: StudentConsolidatedRecord
    ): File {
        val fileName = "Mark_Certificate_${record.student.admissionNo}_${record.student.name.replace(" ", "_")}.pdf"
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val file = File(reportsDir, fileName)

        // A4 Portrait: 595 x 842 points
        val pageWidth = 595
        val pageHeight = 842
        val document = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawCertificate(canvas, pageWidth, pageHeight, school, record)

        document.finishPage(page)

        FileOutputStream(file).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        return file
    }

    private fun drawCertificate(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int,
        school: SchoolProfile,
        record: StudentConsolidatedRecord
    ) {
        val margin = 32f
        val paint = Paint().apply { isAntiAlias = true }
        val strokePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.rgb(30, 41, 59)
        }

        // Outer Certificate Border (Double border)
        strokePaint.strokeWidth = 3f
        strokePaint.color = Color.rgb(26, 35, 126) // Deep Navy
        canvas.drawRect(margin, margin, pageWidth - margin, pageHeight - margin, strokePaint)

        strokePaint.strokeWidth = 1f
        strokePaint.color = Color.rgb(217, 119, 6) // Gold line
        canvas.drawRect(margin + 5f, margin + 5f, pageWidth - margin - 5f, pageHeight - margin - 5f, strokePaint)

        // Certificate Header
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.rgb(180, 83, 9)
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("தமிழ்நாடு பள்ளி கல்வித்துறை", pageWidth / 2f, margin + 28f, paint)

        paint.color = Color.rgb(26, 35, 126)
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val schoolHeader = if (school.udiseCode.isNotBlank()) "${school.schoolName} (UDISE: ${school.udiseCode})" else school.schoolName
        canvas.drawText(schoolHeader, pageWidth / 2f, margin + 50f, paint)

        paint.color = Color.rgb(75, 85, 99)
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        val subLine = if (school.udiseCode.isNotBlank()) "${school.unionName}, ${school.districtName} | UDISE: ${school.udiseCode}" else "${school.unionName}, ${school.districtName}"
        canvas.drawText(subLine, pageWidth / 2f, margin + 66f, paint)

        // Certificate Title Banner
        val bannerTop = margin + 80f
        val bannerH = 34f
        paint.color = Color.rgb(238, 242, 255)
        canvas.drawRoundRect(RectF(margin + 40f, bannerTop, pageWidth - margin - 40f, bannerTop + bannerH), 8f, 8f, paint)

        strokePaint.strokeWidth = 1f
        strokePaint.color = Color.rgb(199, 210, 254)
        canvas.drawRoundRect(RectF(margin + 40f, bannerTop, pageWidth - margin - 40f, bannerTop + bannerH), 8f, 8f, strokePaint)

        paint.color = Color.rgb(30, 58, 138)
        paint.textSize = 13.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("எட்டாம் வகுப்பு ஆண்டு மதிப்பெண் சான்றிதழ்", pageWidth / 2f, bannerTop + 22f, paint)

        // Student Info Grid Box
        val infoTop = bannerTop + 46f
        val infoH = 86f
        val infoBox = RectF(margin + 20f, infoTop, pageWidth - margin - 20f, infoTop + infoH)
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(infoBox, 6f, 6f, paint)
        strokePaint.color = Color.rgb(203, 213, 225)
        canvas.drawRoundRect(infoBox, 6f, 6f, strokePaint)

        val textPaintLabel = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(71, 85, 105)
            textSize = 9.5f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.LEFT
        }
        val textPaintVal = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }

        val col1X = margin + 35f
        val col2X = pageWidth / 2f + 10f

        canvas.drawText("மாணவர் பெயர் :", col1X, infoTop + 22f, textPaintLabel)
        canvas.drawText(record.student.name, col1X + 85f, infoTop + 22f, textPaintVal)

        canvas.drawText("சேர்க்கை எண் :", col2X, infoTop + 22f, textPaintLabel)
        canvas.drawText(record.student.admissionNo, col2X + 85f, infoTop + 22f, textPaintVal)

        canvas.drawText("வகுப்பு & பிரிவு :", col1X, infoTop + 44f, textPaintLabel)
        canvas.drawText("${record.student.stdClass} - ${record.student.section}", col1X + 85f, infoTop + 44f, textPaintVal)

        canvas.drawText("கல்வியாண்டு :", col2X, infoTop + 44f, textPaintLabel)
        canvas.drawText(school.academicYear, col2X + 85f, infoTop + 44f, textPaintVal)

        canvas.drawText("பெற்றோர் பெயர் :", col1X, infoTop + 66f, textPaintLabel)
        val pName = record.student.parentName.ifEmpty { "-" }
        canvas.drawText(pName, col1X + 85f, infoTop + 66f, textPaintVal)

        canvas.drawText("இனம் / பிரிவு :", col2X, infoTop + 66f, textPaintLabel)
        canvas.drawText(record.student.community, col2X + 85f, infoTop + 66f, textPaintVal)

        // Marks Table
        val tableTop = infoTop + infoH + 16f
        val tableLeft = margin + 20f
        val tableRight = pageWidth - margin - 20f
        val tableW = tableRight - tableLeft

        val isClass8 = record.student.stdClass == 8
        val mainSubjects = Subject.getMainSubjectsForClass(record.student.stdClass)
        val hasPe = Subject.hasPeSubject(record.student.stdClass)

        val headH = 26f
        val rowH = 24f
        var currentY = tableTop

        val rowValPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 9f
            textAlign = Paint.Align.CENTER
        }
        val subNamePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }

        if (isClass8) {
            // Class 8 Certificate: Show only Three-Term Average (முப்பருவ சராசரி மட்டும், பருவம் 1 & 2 தேவையில்லை)
            val colSnoW = 45f
            val colSubW = 200f
            val colMaxW = 100f
            val colAvgW = 120f
            val colGradeW = tableW - (colSnoW + colSubW + colMaxW + colAvgW) // 90f

            // Table Header
            paint.color = Color.rgb(224, 231, 255)
            canvas.drawRect(tableLeft, tableTop, tableRight, tableTop + headH, paint)
            strokePaint.color = Color.rgb(71, 85, 105)
            canvas.drawRect(tableLeft, tableTop, tableRight, tableTop + headH, strokePaint)

            val thPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(30, 41, 59)
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            canvas.drawText("வ.எண்", tableLeft + colSnoW / 2f, tableTop + 17f, thPaint)
            var cx = tableLeft + colSnoW
            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)

            canvas.drawText("பாடங்கள் (Subjects)", cx + colSubW / 2f, tableTop + 17f, thPaint)
            cx += colSubW
            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)

            canvas.drawText("முழு மதிப்பெண்", cx + colMaxW / 2f, tableTop + 17f, thPaint)
            cx += colMaxW
            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)

            canvas.drawText("முப்பருவ சராசரி", cx + colAvgW / 2f, tableTop + 17f, thPaint)
            cx += colAvgW
            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)

            canvas.drawText("தரநிலை", cx + colGradeW / 2f, tableTop + 17f, thPaint)

            currentY = tableTop + headH

            // 1. Five Main Academic Subjects (Tamil, English, Maths, Science, Social)
            mainSubjects.forEachIndexed { i, sub ->
                val sm = record.subjectMarks[sub]
                val avg = sm?.avgTotal?.toString() ?: "-"
                val grade = sm?.getGrade(record.student.stdClass) ?: "-"

                if (i % 2 == 1) {
                    paint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowH, paint)
                }
                canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowH, strokePaint)

                // S.No
                canvas.drawText("${i + 1}", tableLeft + colSnoW / 2f, currentY + 16f, rowValPaint)
                var colX = tableLeft + colSnoW
                canvas.drawLine(colX, currentY, colX, currentY + rowH, strokePaint)

                // Subject Name
                canvas.drawText("${sub.tamilName} (${sub.name})", colX + 10f, currentY + 16f, subNamePaint)
                colX += colSubW
                canvas.drawLine(colX, currentY, colX, currentY + rowH, strokePaint)

                // Max Marks (100)
                canvas.drawText("100", colX + colMaxW / 2f, currentY + 16f, rowValPaint)
                colX += colMaxW
                canvas.drawLine(colX, currentY, colX, currentY + rowH, strokePaint)

                // Three-Term Average Mark
                val boldAvgPaint = Paint(rowValPaint).apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    color = Color.rgb(30, 58, 138)
                }
                canvas.drawText(avg, colX + colAvgW / 2f, currentY + 16f, boldAvgPaint)
                colX += colAvgW
                canvas.drawLine(colX, currentY, colX, currentY + rowH, strokePaint)

                // Grade
                val gradePaint = Paint(rowValPaint).apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    color = Color.rgb(26, 35, 126)
                }
                canvas.drawText(grade, colX + colGradeW / 2f, currentY + 16f, gradePaint)

                currentY += rowH
            }

            // 2. TOTAL Row (Only 5 main subjects: 500)
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowH, paint)
            canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowH, strokePaint)

            val totalLabelPaint = Paint(subNamePaint).apply { color = Color.rgb(30, 58, 138) }
            canvas.drawText("", tableLeft + colSnoW / 2f, currentY + 16f, rowValPaint)
            var totX = tableLeft + colSnoW
            canvas.drawLine(totX, currentY, totX, currentY + rowH, strokePaint)

            canvas.drawText("மொத்தம் (TOTAL)", totX + 10f, currentY + 16f, totalLabelPaint)
            totX += colSubW
            canvas.drawLine(totX, currentY, totX, currentY + rowH, strokePaint)

            canvas.drawText("${mainSubjects.size * 100}", totX + colMaxW / 2f, currentY + 16f, totalLabelPaint)
            totX += colMaxW
            canvas.drawLine(totX, currentY, totX, currentY + rowH, strokePaint)

            canvas.drawText("${record.grandAvgTotal}", totX + colAvgW / 2f, currentY + 16f, totalLabelPaint)
            totX += colAvgW
            canvas.drawLine(totX, currentY, totX, currentY + rowH, strokePaint)

            canvas.drawText(record.overallGrade, totX + colGradeW / 2f, currentY + 16f, totalLabelPaint)

            currentY += rowH

            // 3. Physical Education (உடற்கல்வி) Row: Drawn AFTER Total, NOT added into Total
            if (hasPe) {
                val peSm = record.subjectMarks[Subject.PE]
                val peAvg = if (peSm?.avgTotal != null && peSm.avgTotal > 0) "${peSm.avgTotal}" else "${record.peAvgTotal}"
                val peGrade = peSm?.getGrade(record.student.stdClass) ?: "-"

                paint.color = Color.rgb(254, 252, 232)
                canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowH, paint)
                canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowH, strokePaint)

                val peTextPaint = Paint(subNamePaint).apply { color = Color.rgb(133, 77, 14) }
                val peValPaint = Paint(rowValPaint).apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    color = Color.rgb(133, 77, 14)
                }

                canvas.drawText("6", tableLeft + colSnoW / 2f, currentY + 16f, peValPaint)
                var peX = tableLeft + colSnoW
                canvas.drawLine(peX, currentY, peX, currentY + rowH, strokePaint)

                canvas.drawText("${Subject.PE.tamilName} (தனி மதிப்பீடு)", peX + 10f, currentY + 16f, peTextPaint)
                peX += colSubW
                canvas.drawLine(peX, currentY, peX, currentY + rowH, strokePaint)

                canvas.drawText("100", peX + colMaxW / 2f, currentY + 16f, peValPaint)
                peX += colMaxW
                canvas.drawLine(peX, currentY, peX, currentY + rowH, strokePaint)

                canvas.drawText(peAvg, peX + colAvgW / 2f, currentY + 16f, peValPaint)
                peX += colAvgW
                canvas.drawLine(peX, currentY, peX, currentY + rowH, strokePaint)

                canvas.drawText(peGrade, peX + colGradeW / 2f, currentY + 16f, peValPaint)

                currentY += rowH
            }
        } else {
            // General multi-term layout for other classes
            val colSubW = 120f
            val colTermW = (tableW - colSubW) / 5f

            // Table Header
            paint.color = Color.rgb(224, 231, 255)
            canvas.drawRect(tableLeft, tableTop, tableRight, tableTop + headH, paint)
            strokePaint.color = Color.rgb(71, 85, 105)
            canvas.drawRect(tableLeft, tableTop, tableRight, tableTop + headH, strokePaint)

            val thPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(30, 41, 59)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            canvas.drawText("பாடங்கள்", tableLeft + colSubW / 2f, tableTop + 16f, thPaint)
            var cx = tableLeft + colSubW
            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)
            canvas.drawText("பருவம் 1 (100)", cx + colTermW / 2f, tableTop + 16f, thPaint)
            cx += colTermW

            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)
            canvas.drawText("பருவம் 2 (100)", cx + colTermW / 2f, tableTop + 16f, thPaint)
            cx += colTermW

            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)
            canvas.drawText("பருவம் 3 (100)", cx + colTermW / 2f, tableTop + 16f, thPaint)
            cx += colTermW

            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)
            canvas.drawText("முப்பருவ சராசரி", cx + colTermW / 2f, tableTop + 16f, thPaint)
            cx += colTermW

            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)
            canvas.drawText("தரநிலை", cx + colTermW / 2f, tableTop + 16f, thPaint)

            currentY = tableTop + headH

            for (sub in mainSubjects) {
                val sm = record.subjectMarks[sub]

                canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowH, strokePaint)
                canvas.drawText(sub.tamilName, tableLeft + 10f, currentY + 16f, subNamePaint)

                var colX = tableLeft + colSubW
                canvas.drawLine(colX, currentY, colX, currentY + rowH, strokePaint)
                val t1 = sm?.term1Marks?.total?.toString() ?: "-"
                canvas.drawText(t1, colX + colTermW / 2f, currentY + 16f, rowValPaint)
                colX += colTermW

                canvas.drawLine(colX, currentY, colX, currentY + rowH, strokePaint)
                val t2 = sm?.term2Marks?.total?.toString() ?: "-"
                canvas.drawText(t2, colX + colTermW / 2f, currentY + 16f, rowValPaint)
                colX += colTermW

                canvas.drawLine(colX, currentY, colX, currentY + rowH, strokePaint)
                val t3 = sm?.term3Marks?.total?.toString() ?: "-"
                canvas.drawText(t3, colX + colTermW / 2f, currentY + 16f, rowValPaint)
                colX += colTermW

                canvas.drawLine(colX, currentY, colX, currentY + rowH, strokePaint)
                val avg = sm?.avgTotal?.toString() ?: "-"
                val boldAvgPaint = Paint(rowValPaint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
                canvas.drawText(avg, colX + colTermW / 2f, currentY + 16f, boldAvgPaint)
                colX += colTermW

                canvas.drawLine(colX, currentY, colX, currentY + rowH, strokePaint)
                val grade = sm?.getGrade(record.student.stdClass) ?: "-"
                val gradePaint = Paint(rowValPaint).apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    color = Color.rgb(26, 35, 126)
                }
                canvas.drawText(grade, colX + colTermW / 2f, currentY + 16f, gradePaint)

                currentY += rowH
            }

            // Total Row
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowH, paint)
            canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowH, strokePaint)

            val totalLabelPaint = Paint(subNamePaint).apply { color = Color.rgb(30, 58, 138) }
            canvas.drawText("மொத்தம் (TOTAL)", tableLeft + 10f, currentY + 16f, totalLabelPaint)

            var totX = tableLeft + colSubW
            canvas.drawLine(totX, currentY, totX, currentY + rowH, strokePaint)
            canvas.drawText("${record.getTermTotal(1)}", totX + colTermW / 2f, currentY + 16f, totalLabelPaint)
            totX += colTermW

            canvas.drawLine(totX, currentY, totX, currentY + rowH, strokePaint)
            canvas.drawText("${record.getTermTotal(2)}", totX + colTermW / 2f, currentY + 16f, totalLabelPaint)
            totX += colTermW

            canvas.drawLine(totX, currentY, totX, currentY + rowH, strokePaint)
            canvas.drawText("${record.getTermTotal(3)}", totX + colTermW / 2f, currentY + 16f, totalLabelPaint)
            totX += colTermW

            canvas.drawLine(totX, currentY, totX, currentY + rowH, strokePaint)
            canvas.drawText("${record.grandAvgTotal}", totX + colTermW / 2f, currentY + 16f, totalLabelPaint)
            totX += colTermW

            canvas.drawLine(totX, currentY, totX, currentY + rowH, strokePaint)
            canvas.drawText(record.overallGrade, totX + colTermW / 2f, currentY + 16f, totalLabelPaint)

            currentY += rowH
        }

        // Attendance & Result Box
        val resultTop = currentY + rowH + 16f
        val resultH = 74f
        val resultBox = RectF(tableLeft, resultTop, tableRight, resultTop + resultH)
        paint.color = Color.rgb(240, 253, 244) // Light green bg
        canvas.drawRoundRect(resultBox, 6f, 6f, paint)
        strokePaint.color = Color.rgb(187, 247, 208)
        canvas.drawRoundRect(resultBox, 6f, 6f, strokePaint)

        val attLabelPaint = Paint(textPaintLabel).apply { textSize = 9.5f }
        val attValPaint = Paint(textPaintVal).apply { textSize = 10f }

        canvas.drawText("பள்ளி மொத்த வேலை நாட்கள் :", tableLeft + 16f, resultTop + 22f, attLabelPaint)
        canvas.drawText("${record.totalWorkingDays} நாட்கள்", tableLeft + 160f, resultTop + 22f, attValPaint)

        canvas.drawText("மாணவர் வருகை புரிந்த நாட்கள் :", tableLeft + 16f, resultTop + 42f, attLabelPaint)
        val pct = String.format("%.1f", record.attendancePercentage)
        canvas.drawText("${record.totalPresentDays} நாட்கள்  ($pct %)", tableLeft + 160f, resultTop + 42f, attValPaint)

        canvas.drawText("ஆண்டு இறுதி முடிவு (Result) :", tableLeft + 16f, resultTop + 62f, attLabelPaint)
        val statusColor = if (record.resultStatus == "தேர்ச்சி") Color.rgb(22, 101, 52) else Color.rgb(185, 28, 28)
        val resultStatusPaint = Paint(attValPaint).apply {
            color = statusColor
            textSize = 12f
        }
        val resultText = if (record.resultStatus == "தேர்ச்சி") "தேர்ச்சி (PROMOTED TO 9th STD)" else "ஊக்கப்படுத்தல் தேவை"
        canvas.drawText(resultText, tableLeft + 160f, resultTop + 62f, resultStatusPaint)

        // Bottom Seal & Signatures
        val signTop = pageHeight - margin - 60f

        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.rgb(51, 65, 85)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT

        canvas.drawText("தேதி: .........................", margin + 70f, signTop, paint)
        canvas.drawText("வகுப்பாசிரியர் கையொப்பம்", margin + 70f, signTop + 32f, paint)

        canvas.drawText("பள்ளி முத்திரை", pageWidth / 2f, signTop + 16f, paint)

        canvas.drawText(school.headmasterName, pageWidth - margin - 85f, signTop, paint)
        val boldHmPaint = Paint(paint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        canvas.drawText("தலைமை ஆசிரியர் கையொப்பம்", pageWidth - margin - 85f, signTop + 32f, boldHmPaint)
    }

    /**
     * Generates a printable A4 Landscape PDF for Class 1 to 3 Single Subject CCE Register
     * matching the official Panchayat Union Middle School format.
     */
    fun generateClass1To3SubjectPdf(
        context: Context,
        school: SchoolProfile,
        stdClass: Int,
        term: Int,
        subject: Subject,
        records: List<StudentConsolidatedRecord>
    ): File {
        val fileName = "Class_${stdClass}_Term_${term}_${subject.name}_Marks_${school.academicYear.replace("-", "_")}.pdf"
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val file = File(reportsDir, fileName)

        val pageWidth = 842
        val pageHeight = 595
        val document = PdfDocument()

        val itemsPerPage = 14
        val pageCount = if (records.isEmpty()) 1 else ((records.size - 1) / itemsPerPage) + 1

        for (p in 0 until pageCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, p + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            drawClass1To3SubjectPage(
                canvas = canvas,
                pageWidth = pageWidth,
                pageHeight = pageHeight,
                school = school,
                stdClass = stdClass,
                term = term,
                subject = subject,
                pageIndex = p,
                totalPages = pageCount,
                records = records.drop(p * itemsPerPage).take(itemsPerPage),
                startSno = p * itemsPerPage + 1
            )

            document.finishPage(page)
        }

        FileOutputStream(file).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        return file
    }

    private fun drawClass1To3SubjectPage(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int,
        school: SchoolProfile,
        stdClass: Int,
        term: Int,
        subject: Subject,
        pageIndex: Int,
        totalPages: Int,
        records: List<StudentConsolidatedRecord>,
        startSno: Int
    ) {
        val margin = 28f
        val paint = Paint().apply { isAntiAlias = true }
        val strokePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = Color.rgb(148, 163, 184) // Slate border
        }

        // Top School Title
        paint.color = Color.rgb(30, 58, 138) // Deep Blue
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        val schoolHeader = if (school.udiseCode.isNotBlank()) "${school.schoolName} (UDISE: ${school.udiseCode})" else school.schoolName
        canvas.drawText(schoolHeader, pageWidth / 2f, margin + 14f, paint)

        // Sub Header: 2026-27 மாணவர் மதிப்பெண் பதிவேடு பருவம் : 1
        val subBarTop = margin + 22f
        val subBarH = 22f
        paint.color = Color.rgb(224, 231, 255) // Light Blue banner
        canvas.drawRect(margin, subBarTop, pageWidth - margin, subBarTop + subBarH, paint)
        canvas.drawRect(margin, subBarTop, pageWidth - margin, subBarTop + subBarH, strokePaint)

        paint.color = Color.rgb(30, 58, 138)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(school.academicYear, margin + 14f, subBarTop + 15f, paint)

        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("மாணவர் மதிப்பெண் பதிவேடு", pageWidth / 2f, subBarTop + 15f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("பருவம் : $term", pageWidth - margin - 14f, subBarTop + 15f, paint)

        // Sub-bar 2: புதிய பாடத்திட்டம் வகுப்பு : 1 பாடம்: தமிழ் (Tamil)
        val infoBarTop = subBarTop + subBarH + 2f
        val infoBarH = 20f
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(margin, infoBarTop, pageWidth - margin, infoBarTop + infoBarH, paint)
        canvas.drawRect(margin, infoBarTop, pageWidth - margin, infoBarTop + infoBarH, strokePaint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("புதிய பாடத்திட்டம்", margin + 14f, infoBarTop + 14f, paint)

        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("வகுப்பு : $stdClass", pageWidth / 2f - 40f, infoBarTop + 14f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("பாடம்: ${subject.tamilName} (${subject.name})", pageWidth - margin - 14f, infoBarTop + 14f, paint)

        // Table coordinates
        val tableTop = infoBarTop + infoBarH + 4f
        val tableLeft = margin
        val tableRight = pageWidth - margin
        val tableW = tableRight - tableLeft

        // Column widths matching the user's PDF
        val colSnoW = 32f
        val colAdmW = 52f
        val colNameW = 126f

        // Naney 1 (25): Oral(46), Activity(58), Written(42) = 146f
        val n1OralW = 46f
        val n1ActW = 58f
        val n1WriW = 42f
        val n1TotalW = n1OralW + n1ActW + n1WriW // 146f

        // Naney 2 (25): Oral(46), Activity(58), Written(42) = 146f
        val n2OralW = 46f
        val n2ActW = 58f
        val n2WriW = 42f
        val n2TotalW = n2OralW + n2ActW + n2WriW // 146f

        // Thiranari (50): Oral(50), Written(70) = 120f
        val thOralW = 50f
        val thWriW = 70f
        val thTotalW = thOralW + thWriW // 120f

        val colTotW = 58f
        val colPctW = tableW - (colSnoW + colAdmW + colNameW + n1TotalW + n2TotalW + thTotalW + colTotW) // remaining ~56f

        val headH1 = 28f
        val headH2 = 26f
        val rowH = 22f

        // Draw Headers:
        // S.No, Admission No, Student Name: Deep Blue background with white text
        paint.color = Color.rgb(30, 58, 138)
        canvas.drawRect(tableLeft, tableTop, tableLeft + colSnoW + colAdmW + colNameW, tableTop + headH1 + headH2, paint)

        // Naney 1 Header: Salmon / Pink background
        val n1Left = tableLeft + colSnoW + colAdmW + colNameW
        paint.color = Color.rgb(254, 226, 226) // Soft salmon
        canvas.drawRect(n1Left, tableTop, n1Left + n1TotalW, tableTop + headH1 + headH2, paint)

        // Naney 2 Header: Salmon / Pink background
        val n2Left = n1Left + n1TotalW
        paint.color = Color.rgb(254, 226, 226)
        canvas.drawRect(n2Left, tableTop, n2Left + n2TotalW, tableTop + headH1 + headH2, paint)

        // Thiranari Header: Light Yellow / Cream background
        val thLeft = n2Left + n2TotalW
        paint.color = Color.rgb(254, 240, 138) // Soft cream yellow
        canvas.drawRect(thLeft, tableTop, thLeft + thTotalW, tableTop + headH1 + headH2, paint)

        // Total & Pct Headers: Soft blue/gray
        val totLeft = thLeft + thTotalW
        paint.color = Color.rgb(224, 231, 255)
        canvas.drawRect(totLeft, tableTop, totLeft + colTotW, tableTop + headH1 + headH2, paint)

        val pctLeft = totLeft + colTotW
        paint.color = Color.rgb(209, 250, 229) // Soft emerald
        canvas.drawRect(pctLeft, tableTop, tableRight, tableTop + headH1 + headH2, paint)

        // Draw Header Border
        canvas.drawRect(tableLeft, tableTop, tableRight, tableTop + headH1 + headH2, strokePaint)

        // Horizontal divider between header row 1 and row 2 for Naney 1, Naney 2, Thiranari
        canvas.drawLine(n1Left, tableTop + headH1, tableRight, tableTop + headH1, strokePaint)

        // Vertical lines in Header Row 1
        canvas.drawLine(tableLeft + colSnoW, tableTop, tableLeft + colSnoW, tableTop + headH1 + headH2, strokePaint)
        canvas.drawLine(tableLeft + colSnoW + colAdmW, tableTop, tableLeft + colSnoW + colAdmW, tableTop + headH1 + headH2, strokePaint)
        canvas.drawLine(n1Left, tableTop, n1Left, tableTop + headH1 + headH2, strokePaint)
        canvas.drawLine(n2Left, tableTop, n2Left, tableTop + headH1 + headH2, strokePaint)
        canvas.drawLine(thLeft, tableTop, thLeft, tableTop + headH1 + headH2, strokePaint)
        canvas.drawLine(totLeft, tableTop, totLeft, tableTop + headH1 + headH2, strokePaint)
        canvas.drawLine(pctLeft, tableTop, pctLeft, tableTop + headH1 + headH2, strokePaint)

        // Vertical sub-lines in Header Row 2:
        // Naney 1
        canvas.drawLine(n1Left + n1OralW, tableTop + headH1, n1Left + n1OralW, tableTop + headH1 + headH2, strokePaint)
        canvas.drawLine(n1Left + n1OralW + n1ActW, tableTop + headH1, n1Left + n1OralW + n1ActW, tableTop + headH1 + headH2, strokePaint)
        // Naney 2
        canvas.drawLine(n2Left + n2OralW, tableTop + headH1, n2Left + n2OralW, tableTop + headH1 + headH2, strokePaint)
        canvas.drawLine(n2Left + n2OralW + n2ActW, tableTop + headH1, n2Left + n2OralW + n2ActW, tableTop + headH1 + headH2, strokePaint)
        // Thiranari
        canvas.drawLine(thLeft + thOralW, tableTop + headH1, thLeft + thOralW, tableTop + headH1 + headH2, strokePaint)

        // Header Text Paints
        val whiteThPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val redThPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(185, 28, 28) // Dark red
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val subThPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(51, 65, 85)
            textSize = 7f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // Draw Text - Fixed columns (White on Blue)
        canvas.drawText("வரிசை", tableLeft + colSnoW / 2f, tableTop + 24f, whiteThPaint)
        canvas.drawText("எண்", tableLeft + colSnoW / 2f, tableTop + 36f, whiteThPaint)

        canvas.drawText("சேர்க்கை", tableLeft + colSnoW + colAdmW / 2f, tableTop + 24f, whiteThPaint)
        canvas.drawText("எண்", tableLeft + colSnoW + colAdmW / 2f, tableTop + 36f, whiteThPaint)

        canvas.drawText("மாணவர் பெயர்", tableLeft + colSnoW + colAdmW + colNameW / 2f, tableTop + 30f, whiteThPaint)

        // Naney 1 Header
        canvas.drawText("நானே செய்வேன் - 1", n1Left + n1TotalW / 2f, tableTop + 13f, redThPaint)
        canvas.drawText("[25 மதிப்பெண்]", n1Left + n1TotalW / 2f, tableTop + 23f, redThPaint)
        // Naney 1 Sub-columns
        canvas.drawText("வாய்மொழி", n1Left + n1OralW / 2f, tableTop + headH1 + 11f, subThPaint)
        canvas.drawText("[10]", n1Left + n1OralW / 2f, tableTop + headH1 + 21f, subThPaint)

        canvas.drawText("செயல்பாடு", n1Left + n1OralW + n1ActW / 2f, tableTop + headH1 + 11f, subThPaint)
        canvas.drawText("[10]", n1Left + n1OralW + n1ActW / 2f, tableTop + headH1 + 21f, subThPaint)

        canvas.drawText("எழுத்துவழி", n1Left + n1OralW + n1ActW + n1WriW / 2f, tableTop + headH1 + 11f, subThPaint)
        canvas.drawText("[5]", n1Left + n1OralW + n1ActW + n1WriW / 2f, tableTop + headH1 + 21f, subThPaint)

        // Naney 2 Header
        canvas.drawText("நானே செய்வேன் - 2", n2Left + n2TotalW / 2f, tableTop + 13f, redThPaint)
        canvas.drawText("[25 மதிப்பெண்]", n2Left + n2TotalW / 2f, tableTop + 23f, redThPaint)
        // Naney 2 Sub-columns
        canvas.drawText("வாய்மொழி", n2Left + n2OralW / 2f, tableTop + headH1 + 11f, subThPaint)
        canvas.drawText("[10]", n2Left + n2OralW / 2f, tableTop + headH1 + 21f, subThPaint)

        canvas.drawText("செயல்பாடு", n2Left + n2OralW + n2ActW / 2f, tableTop + headH1 + 11f, subThPaint)
        canvas.drawText("[10]", n2Left + n2OralW + n2ActW / 2f, tableTop + headH1 + 21f, subThPaint)

        canvas.drawText("எழுத்துவழி", n2Left + n2OralW + n2ActW + n2WriW / 2f, tableTop + headH1 + 11f, subThPaint)
        canvas.drawText("[5]", n2Left + n2OralW + n2ActW + n2WriW / 2f, tableTop + headH1 + 21f, subThPaint)

        // Thiranari Header
        canvas.drawText("திறனறி மதிப்பீடு", thLeft + thTotalW / 2f, tableTop + 13f, redThPaint)
        canvas.drawText("[50 மதிப்பெண்]", thLeft + thTotalW / 2f, tableTop + 23f, redThPaint)
        // Thiranari Sub-columns
        canvas.drawText("வாய்மொழி", thLeft + thOralW / 2f, tableTop + headH1 + 11f, subThPaint)
        canvas.drawText("[10]", thLeft + thOralW / 2f, tableTop + headH1 + 21f, subThPaint)

        canvas.drawText("எழுத்துவழி", thLeft + thOralW + thWriW / 2f, tableTop + headH1 + 11f, subThPaint)
        canvas.drawText("[40]", thLeft + thOralW + thWriW / 2f, tableTop + headH1 + 21f, subThPaint)

        // Total & Pct Headers
        val blueThPaint = Paint(redThPaint).apply { color = Color.rgb(30, 58, 138) }
        canvas.drawText("மொத்தம்", totLeft + colTotW / 2f, tableTop + 24f, blueThPaint)
        canvas.drawText("[100]", totLeft + colTotW / 2f, tableTop + 36f, blueThPaint)

        val greenThPaint = Paint(redThPaint).apply { color = Color.rgb(5, 150, 105) }
        canvas.drawText("விழுக்காடு", pctLeft + colPctW / 2f, tableTop + 24f, greenThPaint)
        canvas.drawText("(%)", pctLeft + colPctW / 2f, tableTop + 36f, greenThPaint)

        // Data Rows
        var curY = tableTop + headH1 + headH2
        val cellValPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 8.5f
            textAlign = Paint.Align.CENTER
        }
        val studentNamePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }

        records.forEachIndexed { i, record ->
            val sno = startSno + i
            val isEven = i % 2 == 0
            if (isEven) {
                paint.color = Color.rgb(250, 250, 250)
                canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, paint)
            }
            canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, strokePaint)

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

            // S.No
            canvas.drawText("$sno", tableLeft + colSnoW / 2f, curY + 15f, cellValPaint)
            // Adm No
            canvas.drawText(record.student.admissionNo, tableLeft + colSnoW + colAdmW / 2f, curY + 15f, cellValPaint)
            // Name
            canvas.drawText(record.student.name, tableLeft + colSnoW + colAdmW + 6f, curY + 15f, studentNamePaint)

            // Naney 1
            canvas.drawText("$n1Oral", n1Left + n1OralW / 2f, curY + 15f, cellValPaint)
            canvas.drawText("$n1Act", n1Left + n1OralW + n1ActW / 2f, curY + 15f, cellValPaint)
            canvas.drawText("$n1Wri", n1Left + n1OralW + n1ActW + n1WriW / 2f, curY + 15f, cellValPaint)

            // Naney 2
            canvas.drawText("$n2Oral", n2Left + n2OralW / 2f, curY + 15f, cellValPaint)
            canvas.drawText("$n2Act", n2Left + n2OralW + n2ActW / 2f, curY + 15f, cellValPaint)
            canvas.drawText("$n2Wri", n2Left + n2OralW + n2ActW + n2WriW / 2f, curY + 15f, cellValPaint)

            // Thiranari
            canvas.drawText("$thOral", thLeft + thOralW / 2f, curY + 15f, cellValPaint)
            canvas.drawText("$thWri", thLeft + thOralW + thWriW / 2f, curY + 15f, cellValPaint)

            // Total
            val boldTotPaint = Paint(cellValPaint).apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.rgb(30, 58, 138)
            }
            canvas.drawText("$tot", totLeft + colTotW / 2f, curY + 15f, boldTotPaint)

            // Percentage
            val boldPctPaint = Paint(cellValPaint).apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.rgb(5, 150, 105)
            }
            canvas.drawText(pct, pctLeft + colPctW / 2f, curY + 15f, boldPctPaint)

            // Draw vertical column divider lines in data row
            canvas.drawLine(tableLeft + colSnoW, curY, tableLeft + colSnoW, curY + rowH, strokePaint)
            canvas.drawLine(tableLeft + colSnoW + colAdmW, curY, tableLeft + colSnoW + colAdmW, curY + rowH, strokePaint)
            canvas.drawLine(n1Left, curY, n1Left, curY + rowH, strokePaint)
            canvas.drawLine(n1Left + n1OralW, curY, n1Left + n1OralW, curY + rowH, strokePaint)
            canvas.drawLine(n1Left + n1OralW + n1ActW, curY, n1Left + n1OralW + n1ActW, curY + rowH, strokePaint)

            canvas.drawLine(n2Left, curY, n2Left, curY + rowH, strokePaint)
            canvas.drawLine(n2Left + n2OralW, curY, n2Left + n2OralW, curY + rowH, strokePaint)
            canvas.drawLine(n2Left + n2OralW + n2ActW, curY, n2Left + n2OralW + n2ActW, curY + rowH, strokePaint)

            canvas.drawLine(thLeft, curY, thLeft, curY + rowH, strokePaint)
            canvas.drawLine(thLeft + thOralW, curY, thLeft + thOralW, curY + rowH, strokePaint)

            canvas.drawLine(totLeft, curY, totLeft, curY + rowH, strokePaint)
            canvas.drawLine(pctLeft, curY, pctLeft, curY + rowH, strokePaint)

            curY += rowH
        }

        // Bottom Signatures
        val signTop = pageHeight - margin - 45f
        paint.color = Color.rgb(51, 65, 85)
        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT
        paint.textAlign = Paint.Align.CENTER

        canvas.drawText("வகுப்பாசிரியர் கையொப்பம்", margin + 110f, signTop + 24f, paint)
        canvas.drawText("பள்ளி முத்திரை", pageWidth / 2f, signTop + 24f, paint)

        val boldHm = Paint(paint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        canvas.drawText(school.headmasterName, pageWidth - margin - 120f, signTop + 10f, boldHm)
        canvas.drawText("தலைமை ஆசிரியர் கையொப்பம்", pageWidth - margin - 120f, signTop + 24f, boldHm)
    }

    /**
     * Generates a printable A4 Portrait PDF Rank Card for a student for a specific Term.
     * Dimensions: 595 x 842 points (A4 Portrait).
     */
    fun generateTermRankCardPdf(
        context: Context,
        school: SchoolProfile,
        record: StudentConsolidatedRecord,
        term: Int,
        rank: Int,
        totalClassStudents: Int
    ): File {
        val fileName = "Rank_Card_${record.student.admissionNo}_Term_${term}_${record.student.name.replace(" ", "_")}.pdf"
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val file = File(reportsDir, fileName)

        // A4 Portrait: 595 x 842 points
        val pageWidth = 595
        val pageHeight = 842
        val document = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawStudentTermRankCard(
            canvas = canvas,
            pageWidth = pageWidth,
            pageHeight = pageHeight,
            school = school,
            record = record,
            term = term,
            rank = rank,
            totalClassStudents = totalClassStudents
        )

        document.finishPage(page)

        FileOutputStream(file).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        return file
    }

    /**
     * Generates a single compiled A4 Portrait PDF containing Rank Cards for ALL students in a class.
     * Each student is printed on their own distinct A4 page.
     */
    fun generateClassAllTermRankCardsPdf(
        context: Context,
        school: SchoolProfile,
        records: List<StudentConsolidatedRecord>,
        term: Int,
        ranksMap: Map<Long, Int>
    ): File {
        val stdClass = records.firstOrNull()?.student?.stdClass ?: 1
        val fileName = "Class_${stdClass}_Term_${term}_All_Rank_Cards_${school.academicYear.replace("-", "_")}.pdf"
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val file = File(reportsDir, fileName)

        val pageWidth = 595
        val pageHeight = 842
        val document = PdfDocument()

        val totalStudents = records.size
        records.forEachIndexed { index, record ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val studentRank = ranksMap[record.student.id] ?: (index + 1)

            drawStudentTermRankCard(
                canvas = canvas,
                pageWidth = pageWidth,
                pageHeight = pageHeight,
                school = school,
                record = record,
                term = term,
                rank = studentRank,
                totalClassStudents = totalStudents
            )

            document.finishPage(page)
        }

        FileOutputStream(file).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        return file
    }

    private fun drawStudentTermRankCard(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int,
        school: SchoolProfile,
        record: StudentConsolidatedRecord,
        term: Int,
        rank: Int,
        totalClassStudents: Int
    ) {
        val margin = 26f
        val contentW = pageWidth - margin * 2f
        val paint = Paint().apply { isAntiAlias = true }
        val strokePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.rgb(30, 41, 59)
        }

        // 1. Dual Outer Borders (Deep Navy + Amber Gold)
        strokePaint.strokeWidth = 2.5f
        strokePaint.color = Color.rgb(26, 35, 126) // Deep Navy
        canvas.drawRect(margin, margin, pageWidth - margin, pageHeight - margin, strokePaint)

        strokePaint.strokeWidth = 1f
        strokePaint.color = Color.rgb(217, 119, 6) // Gold border
        canvas.drawRect(margin + 4f, margin + 4f, pageWidth - margin - 4f, pageHeight - margin - 4f, strokePaint)

        // 2. School Header
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.rgb(180, 83, 9) // Amber/Dark Maroon
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("தமிழ்நாடு அரசு • பள்ளிக் கல்வித்துறை", pageWidth / 2f, margin + 22f, paint)

        paint.color = Color.rgb(26, 35, 126)
        paint.textSize = 14.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val schoolHeader = if (school.udiseCode.isNotBlank()) "${school.schoolName} (UDISE: ${school.udiseCode})" else school.schoolName
        canvas.drawText(schoolHeader, pageWidth / 2f, margin + 42f, paint)

        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        val subLine = if (school.udiseCode.isNotBlank()) {
            "${school.unionName}, ${school.districtName} | UDISE: ${school.udiseCode} | கல்வியாண்டு: ${school.academicYear}"
        } else {
            "${school.unionName}, ${school.districtName} | கல்வியாண்டு: ${school.academicYear}"
        }
        canvas.drawText(subLine, pageWidth / 2f, margin + 57f, paint)

        // 3. Title Banner
        val bannerTop = margin + 68f
        val bannerH = 26f
        paint.color = Color.rgb(238, 242, 255) // Indigo 50
        canvas.drawRoundRect(RectF(margin + 20f, bannerTop, pageWidth - margin - 20f, bannerTop + bannerH), 6f, 6f, paint)

        strokePaint.color = Color.rgb(199, 210, 254)
        strokePaint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(margin + 20f, bannerTop, pageWidth - margin - 20f, bannerTop + bannerH), 6f, 6f, strokePaint)

        paint.color = Color.rgb(30, 58, 138)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val termTamil = when (term) {
            1 -> "முதல் பருவம் (Term 1)"
            2 -> "இரண்டாம் பருவம் (Term 2)"
            3 -> "மூன்றாம் பருவம் (Term 3)"
            else -> "பருவம் $term"
        }
        canvas.drawText("$termTamil - மாணவர் முன்னேற்ற & தர அட்டை (PROGRESS & RANK CARD)", pageWidth / 2f, bannerTop + 17f, paint)

        // 4. Student Details Box
        val infoTop = bannerTop + 33f
        val infoH = 68f
        val infoBox = RectF(margin + 10f, infoTop, pageWidth - margin - 10f, infoTop + infoH)
        paint.color = Color.rgb(248, 250, 252) // Slate 50
        canvas.drawRoundRect(infoBox, 6f, 6f, paint)
        strokePaint.color = Color.rgb(203, 213, 225)
        canvas.drawRoundRect(infoBox, 6f, 6f, strokePaint)

        val textPaintLabel = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(71, 85, 105)
            textSize = 9f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.LEFT
        }
        val textPaintVal = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }

        val col1X = margin + 20f
        val col2X = pageWidth / 2f + 10f

        canvas.drawText("சேர்க்கை / EMIS எண் :", col1X, infoTop + 18f, textPaintLabel)
        canvas.drawText(record.student.admissionNo, col1X + 110f, infoTop + 18f, textPaintVal)

        canvas.drawText("தந்தை / பெற்றோர் :", col2X, infoTop + 18f, textPaintLabel)
        val pName = record.student.parentName.ifEmpty { "-" }
        canvas.drawText(pName, col2X + 95f, infoTop + 18f, textPaintVal)

        canvas.drawText("மாணவர் பெயர் :", col1X, infoTop + 38f, textPaintLabel)
        canvas.drawText(record.student.name, col1X + 110f, infoTop + 38f, textPaintVal)

        canvas.drawText("இனம் / பிரிவு :", col2X, infoTop + 38f, textPaintLabel)
        canvas.drawText(record.student.community, col2X + 95f, infoTop + 38f, textPaintVal)

        canvas.drawText("வகுப்பு & பிரிவு :", col1X, infoTop + 58f, textPaintLabel)
        canvas.drawText("வகுப்பு ${record.student.stdClass} - பிரிவு ${record.student.section}", col1X + 110f, infoTop + 58f, textPaintVal)

        canvas.drawText("பாலினம் :", col2X, infoTop + 58f, textPaintLabel)
        canvas.drawText(record.student.gender, col2X + 95f, infoTop + 58f, textPaintVal)

        // 5. Calculations for selected Term
        val mainSubjects = Subject.getMainSubjectsForClass(record.student.stdClass)
        val hasPe = Subject.hasPeSubject(record.student.stdClass)
        val subjects = Subject.getSubjectsForClass(record.student.stdClass)
        val termTotal = record.getTermTotal(term)
        val maxMarks = mainSubjects.size * 100
        val percentage = if (maxMarks > 0) (termTotal.toFloat() / maxMarks.toFloat()) * 100f else 0f
        val overallGrade = if (record.student.stdClass in 1..3) {
            com.example.data.model.CceGradeEvaluator.getGradeForClass1To3(percentage.toInt())
        } else {
            com.example.data.model.CceGradeEvaluator.getGradeForClass4To8(percentage.toInt())
        }

        val att = when (term) {
            1 -> record.term1Attendance
            2 -> record.term2Attendance
            3 -> record.term3Attendance
            else -> null
        }
        val workDays = att?.totalWorkingDays ?: 80
        val presDays = att?.presentDays ?: 76
        val attPct = if (workDays > 0) (presDays.toFloat() / workDays.toFloat()) * 100f else 0f

        // 6. Rank & Performance Highlights Banner (5 cards)
        val ribbonTop = infoTop + infoH + 8f
        val ribbonH = 48f
        val cardW = (contentW - 20f - 16f) / 5f // 5 equal cards with 4dp spacing

        val highlights = listOf(
            Triple("வகுப்புத் தரம்", "🏆 $rank-ம் இடம்", Color.rgb(254, 243, 199)), // Gold
            Triple("பருவ மொத்தம்", "$termTotal / $maxMarks", Color.rgb(238, 242, 255)), // Indigo
            Triple("சதவீதம்", String.format("%.1f%%", percentage), Color.rgb(236, 253, 245)), // Emerald
            Triple("ஒட்டுமொத்த தரம்", overallGrade, Color.rgb(243, 232, 255)), // Purple
            Triple("வருகைப் பதிவு", "$presDays / $workDays (${attPct.toInt()}%)", Color.rgb(236, 254, 255)) // Cyan
        )

        highlights.forEachIndexed { i, (title, value, bgColor) ->
            val cx = margin + 10f + i * (cardW + 4f)
            val cardBox = RectF(cx, ribbonTop, cx + cardW, ribbonTop + ribbonH)
            paint.color = bgColor
            canvas.drawRoundRect(cardBox, 5f, 5f, paint)
            strokePaint.color = Color.rgb(203, 213, 225)
            canvas.drawRoundRect(cardBox, 5f, 5f, strokePaint)

            paint.textAlign = Paint.Align.CENTER
            paint.color = Color.rgb(71, 85, 105)
            paint.textSize = 7.5f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText(title, cx + cardW / 2f, ribbonTop + 16f, paint)

            paint.color = if (i == 0) Color.rgb(180, 83, 9) else Color.rgb(30, 58, 138)
            paint.textSize = if (i == 0) 10.5f else 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(value, cx + cardW / 2f, ribbonTop + 34f, paint)
        }

        // 7. Subject Marks Table
        val tableTop = ribbonTop + ribbonH + 10f
        val tableLeft = margin + 10f
        val tableRight = pageWidth - margin - 10f
        val tableW = tableRight - tableLeft

        val isClass8 = record.student.stdClass == 8
        val isClass1To3 = record.student.stdClass in 1..3

        val headH = 26f
        val rowH = 22f

        // Draw Table Header
        paint.color = Color.rgb(30, 58, 138) // Deep Navy
        canvas.drawRect(tableLeft, tableTop, tableRight, tableTop + headH, paint)
        strokePaint.color = Color.rgb(30, 41, 59)
        canvas.drawRect(tableLeft, tableTop, tableRight, tableTop + headH, strokePaint)

        val thWhite = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        if (isClass8) {
            // Class 8 format (Req 3: Direct 100 mark only, NO FA/SA split)
            val cSnoW = 40f
            val cSubW = 200f
            val cMaxW = 95f
            val cMarkW = 104f
            val cGradeW = tableW - (cSnoW + cSubW + cMaxW + cMarkW) // ~80f

            canvas.drawText("வ.எண்", tableLeft + cSnoW / 2f, tableTop + 17f, thWhite)
            var curColX = tableLeft + cSnoW
            canvas.drawLine(curColX, tableTop, curColX, tableTop + headH, strokePaint)

            canvas.drawText("பாடங்கள் (Subjects)", curColX + cSubW / 2f, tableTop + 17f, thWhite)
            curColX += cSubW
            canvas.drawLine(curColX, tableTop, curColX, tableTop + headH, strokePaint)

            canvas.drawText("முழு மதிப்பெண்", curColX + cMaxW / 2f, tableTop + 17f, thWhite)
            curColX += cMaxW
            canvas.drawLine(curColX, tableTop, curColX, tableTop + headH, strokePaint)

            canvas.drawText("பெற்ற மதிப்பெண்", curColX + cMarkW / 2f, tableTop + 17f, thWhite)
            curColX += cMarkW
            canvas.drawLine(curColX, tableTop, curColX, tableTop + headH, strokePaint)

            canvas.drawText("தரம் (Grade)", curColX + cGradeW / 2f, tableTop + 17f, thWhite)

            // Rows
            var curY = tableTop + headH
            val rowTextPaint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
                textSize = 9f
                textAlign = Paint.Align.CENTER
            }
            val subNamePaint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.LEFT
            }

            mainSubjects.forEachIndexed { i, sub ->
                val sm = record.subjectMarks[sub]
                val m = when (term) {
                    1 -> sm?.term1Marks
                    2 -> sm?.term2Marks
                    3 -> sm?.term3Marks
                    else -> null
                }
                val markVal = m?.total ?: 0
                val gradeVal = m?.grade ?: com.example.data.model.CceGradeEvaluator.getGradeForClass4To8(markVal)

                if (i % 2 == 1) {
                    paint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, paint)
                }
                canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, strokePaint)

                // S.No
                canvas.drawText("${i + 1}", tableLeft + cSnoW / 2f, curY + 15f, rowTextPaint)
                curColX = tableLeft + cSnoW
                canvas.drawLine(curColX, curY, curColX, curY + rowH, strokePaint)

                // Subject
                canvas.drawText("${sub.tamilName} (${sub.name})", curColX + 10f, curY + 15f, subNamePaint)
                curColX += cSubW
                canvas.drawLine(curColX, curY, curColX, curY + rowH, strokePaint)

                // Max
                canvas.drawText("100", curColX + cMaxW / 2f, curY + 15f, rowTextPaint)
                curColX += cMaxW
                canvas.drawLine(curColX, curY, curColX, curY + rowH, strokePaint)

                // Mark Obtained
                val boldMarkPaint = Paint(rowTextPaint).apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    color = Color.rgb(30, 58, 138)
                }
                canvas.drawText("$markVal", curColX + cMarkW / 2f, curY + 15f, boldMarkPaint)
                curColX += cMarkW
                canvas.drawLine(curColX, curY, curColX, curY + rowH, strokePaint)

                // Grade
                canvas.drawText(gradeVal, curColX + cGradeW / 2f, curY + 15f, boldMarkPaint)

                curY += rowH
            }

            // Total Row (Only 5 main academic subjects)
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, paint)
            canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, strokePaint)

            val totBoldPaint = Paint(subNamePaint).apply { color = Color.rgb(30, 58, 138) }
            canvas.drawText("மொத்தம் (TOTAL)", tableLeft + cSnoW + 10f, curY + 15f, totBoldPaint)

            var totColX = tableLeft + cSnoW + cSubW
            canvas.drawLine(totColX, curY, totColX, curY + rowH, strokePaint)
            canvas.drawText("$maxMarks", totColX + cMaxW / 2f, curY + 15f, totBoldPaint)

            totColX += cMaxW
            canvas.drawLine(totColX, curY, totColX, curY + rowH, strokePaint)
            canvas.drawText("$termTotal", totColX + cMarkW / 2f, curY + 15f, totBoldPaint)

            totColX += cMarkW
            canvas.drawLine(totColX, curY, totColX, curY + rowH, strokePaint)
            canvas.drawText(overallGrade, totColX + cGradeW / 2f, curY + 15f, totBoldPaint)

            curY += rowH

            // Physical Education (உடற்கல்வி) Row: Placed AFTER TOTAL
            if (hasPe) {
                val sm = record.subjectMarks[Subject.PE]
                val m = when (term) {
                    1 -> sm?.term1Marks
                    2 -> sm?.term2Marks
                    3 -> sm?.term3Marks
                    else -> null
                }
                val peMarkVal = m?.total ?: 0
                val peGradeVal = m?.grade ?: com.example.data.model.CceGradeEvaluator.getGradeForClass4To8(peMarkVal)

                paint.color = Color.rgb(254, 252, 232)
                canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, paint)
                canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, strokePaint)

                val peValP = Paint(rowTextPaint).apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    color = Color.rgb(133, 77, 14)
                }
                val peNameP = Paint(subNamePaint).apply { color = Color.rgb(133, 77, 14) }

                canvas.drawText("6", tableLeft + cSnoW / 2f, curY + 15f, peValP)
                curColX = tableLeft + cSnoW
                canvas.drawLine(curColX, curY, curColX, curY + rowH, strokePaint)

                canvas.drawText("${Subject.PE.tamilName} (தனி மதிப்பீடு)", curColX + 10f, curY + 15f, peNameP)
                curColX += cSubW
                canvas.drawLine(curColX, curY, curColX, curY + rowH, strokePaint)

                canvas.drawText("100", curColX + cMaxW / 2f, curY + 15f, peValP)
                curColX += cMaxW
                canvas.drawLine(curColX, curY, curColX, curY + rowH, strokePaint)

                canvas.drawText("$peMarkVal", curColX + cMarkW / 2f, curY + 15f, peValP)
                curColX += cMarkW
                canvas.drawLine(curColX, curY, curColX, curY + rowH, strokePaint)

                canvas.drawText(peGradeVal, curColX + cGradeW / 2f, curY + 15f, peValP)

                curY += rowH
            }

        } else if (isClass1To3) {
            // Class 1-3 format: Naney 1 (25), Naney 2 (25), Thiranari (50), Total (100), Grade
            val cSnoW = 32f
            val cSubW = 120f
            val cN1W = 100f
            val cN2W = 100f
            val cThW = 85f
            val cTotW = 42f
            val cGrdW = tableW - (cSnoW + cSubW + cN1W + cN2W + cThW + cTotW) // ~40f

            canvas.drawText("வ.எண்", tableLeft + cSnoW / 2f, tableTop + 17f, thWhite)
            var cx = tableLeft + cSnoW
            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)

            canvas.drawText("பாடங்கள்", cx + cSubW / 2f, tableTop + 17f, thWhite)
            cx += cSubW
            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)

            canvas.drawText("நானே செய்வேன் 1 [25]", cx + cN1W / 2f, tableTop + 17f, thWhite)
            cx += cN1W
            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)

            canvas.drawText("நானே செய்வேன் 2 [25]", cx + cN2W / 2f, tableTop + 17f, thWhite)
            cx += cN2W
            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)

            canvas.drawText("திறனறி [50]", cx + cThW / 2f, tableTop + 17f, thWhite)
            cx += cThW
            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)

            canvas.drawText("மொத்தம்", cx + cTotW / 2f, tableTop + 17f, thWhite)
            cx += cTotW
            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)

            canvas.drawText("தரம்", cx + cGrdW / 2f, tableTop + 17f, thWhite)

            var curY = tableTop + headH
            val rowTextPaint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
                textSize = 8.5f
                textAlign = Paint.Align.CENTER
            }
            val subNamePaint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.LEFT
            }

            subjects.forEachIndexed { i, sub ->
                val sm = record.subjectMarks[sub]
                val m = when (term) {
                    1 -> sm?.term1Marks
                    2 -> sm?.term2Marks
                    3 -> sm?.term3Marks
                    else -> null
                }
                val n1 = (m?.naney1Oral ?: 10) + (m?.naney1Activity ?: 10) + (m?.naney1Written ?: 5)
                val n2 = (m?.naney2Oral ?: 10) + (m?.naney2Activity ?: 10) + (m?.naney2Written ?: 5)
                val th = (m?.thiranariOral ?: 10) + (m?.thiranariWritten ?: 40)
                val tot = m?.total ?: (n1 + n2 + th)
                val grade = m?.grade ?: com.example.data.model.CceGradeEvaluator.getGradeForClass1To3(tot)

                if (i % 2 == 1) {
                    paint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, paint)
                }
                canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, strokePaint)

                // S.No
                canvas.drawText("${i + 1}", tableLeft + cSnoW / 2f, curY + 15f, rowTextPaint)
                var rcx = tableLeft + cSnoW
                canvas.drawLine(rcx, curY, rcx, curY + rowH, strokePaint)

                // Sub
                canvas.drawText(sub.tamilName, rcx + 6f, curY + 15f, subNamePaint)
                rcx += cSubW
                canvas.drawLine(rcx, curY, rcx, curY + rowH, strokePaint)

                // N1
                canvas.drawText("$n1", rcx + cN1W / 2f, curY + 15f, rowTextPaint)
                rcx += cN1W
                canvas.drawLine(rcx, curY, rcx, curY + rowH, strokePaint)

                // N2
                canvas.drawText("$n2", rcx + cN2W / 2f, curY + 15f, rowTextPaint)
                rcx += cN2W
                canvas.drawLine(rcx, curY, rcx, curY + rowH, strokePaint)

                // Th
                canvas.drawText("$th", rcx + cThW / 2f, curY + 15f, rowTextPaint)
                rcx += cThW
                canvas.drawLine(rcx, curY, rcx, curY + rowH, strokePaint)

                // Tot
                val boldP = Paint(rowTextPaint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = Color.rgb(30, 58, 138) }
                canvas.drawText("$tot", rcx + cTotW / 2f, curY + 15f, boldP)
                rcx += cTotW
                canvas.drawLine(rcx, curY, rcx, curY + rowH, strokePaint)

                // Grade
                canvas.drawText(grade, rcx + cGrdW / 2f, curY + 15f, boldP)

                curY += rowH
            }

            // Total row
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, paint)
            canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, strokePaint)

            val totP = Paint(subNamePaint).apply { color = Color.rgb(30, 58, 138) }
            canvas.drawText("மொத்தம் (TOTAL)", tableLeft + cSnoW + 6f, curY + 15f, totP)

            var tcx = tableLeft + cSnoW + cSubW + cN1W + cN2W + cThW
            canvas.drawLine(tcx, curY, tcx, curY + rowH, strokePaint)
            canvas.drawText("$termTotal", tcx + cTotW / 2f, curY + 15f, totP)
            tcx += cTotW
            canvas.drawLine(tcx, curY, tcx, curY + rowH, strokePaint)
            canvas.drawText(overallGrade, tcx + cGrdW / 2f, curY + 15f, totP)

        } else {
            // Class 4 to 7 format: FA (40), SA (60), Total (100), Grade
            val cSnoW = 36f
            val cSubW = 175f
            val cFaW = 100f
            val cSaW = 100f
            val cTotW = 64f
            val cGrdW = tableW - (cSnoW + cSubW + cFaW + cSaW + cTotW) // ~44f

            canvas.drawText("வ.எண்", tableLeft + cSnoW / 2f, tableTop + 17f, thWhite)
            var cx = tableLeft + cSnoW
            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)

            canvas.drawText("பாடங்கள்", cx + cSubW / 2f, tableTop + 17f, thWhite)
            cx += cSubW
            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)

            canvas.drawText("வளரறி FA [40]", cx + cFaW / 2f, tableTop + 17f, thWhite)
            cx += cFaW
            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)

            canvas.drawText("தொகுத்தறி SA [60]", cx + cSaW / 2f, tableTop + 17f, thWhite)
            cx += cSaW
            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)

            canvas.drawText("மொத்தம் [100]", cx + cTotW / 2f, tableTop + 17f, thWhite)
            cx += cTotW
            canvas.drawLine(cx, tableTop, cx, tableTop + headH, strokePaint)

            canvas.drawText("தரம்", cx + cGrdW / 2f, tableTop + 17f, thWhite)

            var curY = tableTop + headH
            val rowTextPaint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
                textSize = 9f
                textAlign = Paint.Align.CENTER
            }
            val subNamePaint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.LEFT
            }

            subjects.forEachIndexed { i, sub ->
                val sm = record.subjectMarks[sub]
                val m = when (term) {
                    1 -> sm?.term1Marks
                    2 -> sm?.term2Marks
                    3 -> sm?.term3Marks
                    else -> null
                }
                val fa = m?.faTotal ?: 0
                val sa = m?.sa ?: 0
                val tot = m?.total ?: (fa + sa)
                val grade = m?.grade ?: com.example.data.model.CceGradeEvaluator.getGradeForClass4To8(tot)

                if (i % 2 == 1) {
                    paint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, paint)
                }
                canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, strokePaint)

                // S.No
                canvas.drawText("${i + 1}", tableLeft + cSnoW / 2f, curY + 15f, rowTextPaint)
                var rcx = tableLeft + cSnoW
                canvas.drawLine(rcx, curY, rcx, curY + rowH, strokePaint)

                // Sub
                canvas.drawText("${sub.tamilName} (${sub.name})", rcx + 6f, curY + 15f, subNamePaint)
                rcx += cSubW
                canvas.drawLine(rcx, curY, rcx, curY + rowH, strokePaint)

                // FA
                canvas.drawText("$fa", rcx + cFaW / 2f, curY + 15f, rowTextPaint)
                rcx += cFaW
                canvas.drawLine(rcx, curY, rcx, curY + rowH, strokePaint)

                // SA
                canvas.drawText("$sa", rcx + cSaW / 2f, curY + 15f, rowTextPaint)
                rcx += cSaW
                canvas.drawLine(rcx, curY, rcx, curY + rowH, strokePaint)

                // Total
                val boldP = Paint(rowTextPaint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = Color.rgb(30, 58, 138) }
                canvas.drawText("$tot", rcx + cTotW / 2f, curY + 15f, boldP)
                rcx += cTotW
                canvas.drawLine(rcx, curY, rcx, curY + rowH, strokePaint)

                // Grade
                canvas.drawText(grade, rcx + cGrdW / 2f, curY + 15f, boldP)

                curY += rowH
            }

            // Total row
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, paint)
            canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, strokePaint)

            val totP = Paint(subNamePaint).apply { color = Color.rgb(30, 58, 138) }
            canvas.drawText("மொத்தம் (TOTAL)", tableLeft + cSnoW + 6f, curY + 15f, totP)

            var tcx = tableLeft + cSnoW + cSubW + cFaW + cSaW
            canvas.drawLine(tcx, curY, tcx, curY + rowH, strokePaint)
            canvas.drawText("$termTotal", tcx + cTotW / 2f, curY + 15f, totP)
            tcx += cTotW
            canvas.drawLine(tcx, curY, tcx, curY + rowH, strokePaint)
            canvas.drawText(overallGrade, tcx + cGrdW / 2f, curY + 15f, totP)
        }

        // 8. Evaluation & Grade Legend Box
        val legendTop = tableTop + headH + subjects.size * rowH + rowH + 12f
        val legendH = 58f
        val legendBox = RectF(tableLeft, legendTop, tableRight, legendTop + legendH)
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(legendBox, 6f, 6f, paint)
        strokePaint.color = Color.rgb(203, 213, 225)
        canvas.drawRoundRect(legendBox, 6f, 6f, strokePaint)

        val legTitlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(30, 58, 138)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        val legTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(71, 85, 105)
            textSize = 8f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.LEFT
        }

        canvas.drawText("தேர்வு முடிவு மற்றும் வருகை சுருக்கம் :", tableLeft + 12f, legendTop + 16f, legTitlePaint)
        val resStr = if (percentage >= 35f) "தேர்ச்சி (PASS)" else "ஊக்கப்படுத்தல் தேவை"
        val resColor = if (percentage >= 35f) Color.rgb(22, 101, 52) else Color.rgb(185, 28, 28)
        val resPaint = Paint(legTitlePaint).apply { color = resColor; textSize = 9.5f }
        canvas.drawText(resStr, tableLeft + 190f, legendTop + 16f, resPaint)

        canvas.drawText("பள்ளி வேலை நாட்கள்: $workDays  |  வருகை புரிந்த நாட்கள்: $presDays  |  வருகை சதவீதம்: ${String.format("%.1f", attPct)}%", tableLeft + 12f, legendTop + 32f, legTextPaint)
        canvas.drawText("தர மதிப்பீடு: A1 (91-100), A2 (81-90), B1 (71-80), B2 (61-70), C1 (51-60), C2 (41-50), D (35-40)", tableLeft + 12f, legendTop + 48f, legTextPaint)

        // 9. Teacher Remarks Box
        val remarkTop = legendTop + legendH + 10f
        val remarkH = 46f
        val remarkBox = RectF(tableLeft, remarkTop, tableRight, remarkTop + remarkH)
        paint.color = Color.rgb(254, 252, 232) // Warm yellow 50
        canvas.drawRoundRect(remarkBox, 6f, 6f, paint)
        strokePaint.color = Color.rgb(254, 240, 138)
        canvas.drawRoundRect(remarkBox, 6f, 6f, strokePaint)

        val remTitlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(180, 83, 9)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        val remTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(15, 23, 42)
            textSize = 9f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.LEFT
        }

        canvas.drawText("வகுப்பாசிரியரின் கருத்து & வழிகாட்டுதல் (Teacher's Remarks):", tableLeft + 12f, remarkTop + 16f, remTitlePaint)

        val feedback = when {
            percentage >= 85f -> "வகுப்பில் சிறப்பான ஈடுபாட்டுடன் பயில்கிறார். தொடர் நல்முயற்சிக்கு மனமார்ந்த பாராட்டுக்கள்!"
            percentage >= 70f -> "நன்றாகப் பயில்கிறார். கணிதம் மற்றும் மொழிப்பாடங்களில் கூடுதல் பயிற்சி பெற்றால் முதலிடம் பெறலாம்."
            percentage >= 50f -> "சராசரியான கற்றல் திறன். தினசரி வாசிப்பு பயிற்சியும் தொடர் முயற்சியும் தேவை."
            else -> "கற்றல் அடைவுகளுக்கு கூடுதல் கவனமும் சிறப்புக் கவனமும் தேவை. ஆசிரியருடன் இணைந்து முன்னேற வாழ்த்துகள்."
        }
        canvas.drawText(feedback, tableLeft + 12f, remarkTop + 33f, remTextPaint)

        // 10. Signatures & Seal Block
        val signTop = pageHeight - margin - 45f
        paint.color = Color.rgb(51, 65, 85)
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        paint.textAlign = Paint.Align.CENTER

        val sigSpacing = tableW / 4f
        canvas.drawText("பெற்றோர் கையொப்பம்", tableLeft + sigSpacing * 0.5f, signTop + 24f, paint)
        canvas.drawText("வகுப்பாசிரியர் கையொப்பம்", tableLeft + sigSpacing * 1.5f, signTop + 24f, paint)
        canvas.drawText("பள்ளி முத்திரை (Seal)", tableLeft + sigSpacing * 2.5f, signTop + 24f, paint)

        val boldHm = Paint(paint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        canvas.drawText(school.headmasterName, tableLeft + sigSpacing * 3.5f, signTop + 10f, boldHm)
        canvas.drawText("தலைமை ஆசிரியர் கையொப்பம்", tableLeft + sigSpacing * 3.5f, signTop + 24f, boldHm)
    }

    /**
     * Direct print helper using Android's native PrintManager.
     */
    fun printPdfFile(context: Context, file: File, jobName: String = "Print_Document") {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager
            if (printManager != null) {
                val printAdapter = object : android.print.PrintDocumentAdapter() {
                    override fun onLayout(
                        oldAttributes: android.print.PrintAttributes?,
                        newAttributes: android.print.PrintAttributes?,
                        cancellationSignal: android.os.CancellationSignal?,
                        callback: LayoutResultCallback?,
                        extras: android.os.Bundle?
                    ) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onLayoutCancelled()
                            return
                        }
                        val info = android.print.PrintDocumentInfo.Builder(file.name)
                            .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .build()
                        callback?.onLayoutFinished(info, true)
                    }

                    override fun onWrite(
                        pages: Array<out android.print.PageRange>?,
                        destination: android.os.ParcelFileDescriptor?,
                        cancellationSignal: android.os.CancellationSignal?,
                        callback: WriteResultCallback?
                    ) {
                        try {
                            java.io.FileInputStream(file).use { input ->
                                java.io.FileOutputStream(destination?.fileDescriptor).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                        } catch (e: Exception) {
                            callback?.onWriteFailed(e.message)
                        }
                    }
                }

                val printAttributes = android.print.PrintAttributes.Builder()
                    .setMediaSize(android.print.PrintAttributes.MediaSize.ISO_A4)
                    .build()

                printManager.print(jobName, printAdapter, printAttributes)
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Fallback
        viewPdfFile(context, file)
    }

    fun sharePdfFile(context: Context, file: File, title: String = "PDF Document") {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "PDF கோப்பை பகிர்க / அச்சிடுக"))
    }

    fun viewPdfFile(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "PDF பார்க்க"))
    }

    /**
     * Generates a single continuous combined A4 Landscape PDF for multiple classes (e.g. 1-3 or 4-7).
     * Automatically eliminates page waste for small class sizes by printing classes consecutively
     * on the same sheet separated by elegant class header divider banners.
     */
    fun generateCombinedGroupPdf(
        context: Context,
        school: SchoolProfile,
        groupTitle: String,
        classRecordsMap: Map<Int, List<StudentConsolidatedRecord>>,
        term: Int = 0
    ): File {
        val safeTitle = groupTitle.replace(" ", "_").replace("(", "").replace(")", "").replace("-", "_")
        val fileName = "Combined_Register_${safeTitle}_${school.academicYear.replace("-", "_")}.pdf"
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val file = File(reportsDir, fileName)

        val pageWidth = 842
        val pageHeight = 595
        val document = PdfDocument()

        val sampleClass = classRecordsMap.keys.firstOrNull() ?: 1
        val isPrimaryGroup = classRecordsMap.keys.all { it in 1..2 }
        val subjects = if (isPrimaryGroup) {
            listOf(Subject.TAMIL, Subject.ENGLISH, Subject.MATHS, Subject.EVS)
        } else {
            listOf(Subject.TAMIL, Subject.ENGLISH, Subject.MATHS, Subject.SCIENCE, Subject.SOCIAL)
        }

        // Build continuous items list
        val allItems = mutableListOf<GroupRowItem>()
        classRecordsMap.toSortedMap().forEach { (cls, recs) ->
            if (recs.isNotEmpty()) {
                allItems.add(GroupRowItem.ClassDivider(cls, recs.size))
                recs.forEachIndexed { idx, r ->
                    allItems.add(GroupRowItem.StudentItem(r, idx + 1))
                }
            }
        }

        val itemsPerPage = 17
        val pageCount = if (allItems.isEmpty()) 1 else ((allItems.size - 1) / itemsPerPage) + 1

        for (p in 0 until pageCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, p + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val pageItems = allItems.drop(p * itemsPerPage).take(itemsPerPage)
            drawCombinedGroupPage(
                canvas = canvas,
                pageWidth = pageWidth,
                pageHeight = pageHeight,
                school = school,
                groupTitle = groupTitle,
                subjects = subjects,
                pageIndex = p,
                totalPages = pageCount,
                items = pageItems,
                term = term,
                isPrimaryGroup = isPrimaryGroup
            )

            document.finishPage(page)
        }

        FileOutputStream(file).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        return file
    }

    private fun drawCombinedGroupPage(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int,
        school: SchoolProfile,
        groupTitle: String,
        subjects: List<Subject>,
        pageIndex: Int,
        totalPages: Int,
        items: List<Any>, // GroupRowItem
        term: Int,
        isPrimaryGroup: Boolean
    ) {
        val margin = 24f
        val paint = Paint().apply { isAntiAlias = true }
        val strokePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.DKGRAY
        }

        // 1. Header Banner
        paint.color = Color.rgb(26, 35, 126) // Deep Navy
        paint.textSize = 14.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        val schoolHeader = if (school.udiseCode.isNotBlank()) "${school.schoolName} (UDISE: ${school.udiseCode})" else school.schoolName
        canvas.drawText(schoolHeader, pageWidth / 2f, margin + 15f, paint)

        paint.color = Color.rgb(55, 65, 81)
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        val subHeader = if (school.udiseCode.isNotBlank()) "${school.unionName}   |   ${school.districtName}   |   UDISE: ${school.udiseCode}" else "${school.unionName}   |   ${school.districtName}"
        canvas.drawText(subHeader, pageWidth / 2f, margin + 28f, paint)

        paint.color = Color.rgb(180, 83, 9)
        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val termTitle = if (term == 0) "முப்பருவ சராசரி ஒருங்கிணைந்த மதிப்பெண் பதிவேடு" else "பருவம் $term ஒருங்கிணைந்த மதிப்பெண் பதிவேடு"
        canvas.drawText("$groupTitle - $termTitle", pageWidth / 2f, margin + 43f, paint)

        paint.color = Color.BLACK
        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("ஒருங்கிணைந்த பதிவேடு (காகிதம் சிக்கனம்)", margin, margin + 55f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("கல்வியாண்டு: ${school.academicYear}   (பக்கம் ${pageIndex + 1}/$totalPages)", pageWidth - margin, margin + 55f, paint)

        // 2. Table Column Dimensions
        val tableTop = margin + 62f
        val tableLeft = margin
        val tableRight = pageWidth - margin
        val tableWidth = tableRight - tableLeft

        val sNoW = 24f
        val clsW = 28f
        val admW = 42f
        val nameW = 104f
        val commW = 26f
        val endW = 42f + 36f + 36f + 56f
        val remainingW = tableWidth - (sNoW + clsW + admW + nameW + commW + endW)
        val subjectW = remainingW / subjects.size
        val subColW = subjectW / 3f

        val headerH1 = 18f
        val headerH2 = 14f
        val rowH = 22f

        // Draw Table Header Background
        paint.color = Color.rgb(238, 242, 255)
        paint.style = Paint.Style.FILL
        canvas.drawRect(tableLeft, tableTop, tableRight, tableTop + headerH1 + headerH2, paint)
        canvas.drawRect(tableLeft, tableTop, tableRight, tableTop + headerH1 + headerH2, strokePaint)

        val headerTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(30, 41, 59)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText("வ.எண்", tableLeft + sNoW / 2f, tableTop + 20f, headerTextPaint)
        canvas.drawText("வகுப்பு", tableLeft + sNoW + clsW / 2f, tableTop + 20f, headerTextPaint)
        canvas.drawText("சே.எண்", tableLeft + sNoW + clsW + admW / 2f, tableTop + 20f, headerTextPaint)
        canvas.drawText("மாணவர் பெயர்", tableLeft + sNoW + clsW + admW + nameW / 2f, tableTop + 20f, headerTextPaint)
        canvas.drawText("இனம்", tableLeft + sNoW + clsW + admW + nameW + commW / 2f, tableTop + 20f, headerTextPaint)

        var curX = tableLeft + sNoW + clsW + admW + nameW + commW
        val subTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.DKGRAY
            textSize = 6.5f
            textAlign = Paint.Align.CENTER
        }

        val isClass8Group = groupTitle.contains("8") || groupTitle.contains("சான்றிதழ்")

        if (isClass8Group) {
            for (sub in subjects) {
                canvas.drawText(sub.shortName, curX + subjectW / 2f, tableTop + 13f, headerTextPaint)
                canvas.drawText("(100)", curX + subjectW / 2f, tableTop + headerH1 + 10f, subTextPaint)
                canvas.drawLine(curX, tableTop, curX, tableTop + headerH1 + headerH2, strokePaint)
                curX += subjectW
            }
        } else {
            for (sub in subjects) {
                canvas.drawText(sub.shortName, curX + subjectW / 2f, tableTop + 12f, headerTextPaint)
                canvas.drawText("SA", curX + subColW * 0.5f, tableTop + headerH1 + 10f, subTextPaint)
                canvas.drawText("FA", curX + subColW * 1.5f, tableTop + headerH1 + 10f, subTextPaint)
                canvas.drawText("மொ", curX + subColW * 2.5f, tableTop + headerH1 + 10f, subTextPaint)

                canvas.drawLine(curX, tableTop + headerH1, curX + subjectW, tableTop + headerH1, strokePaint)
                canvas.drawLine(curX + subColW, tableTop + headerH1, curX + subColW, tableTop + headerH1 + headerH2, strokePaint)
                canvas.drawLine(curX + subColW * 2f, tableTop + headerH1, curX + subColW * 2f, tableTop + headerH1 + headerH2, strokePaint)
                canvas.drawLine(curX, tableTop, curX, tableTop + headerH1 + headerH2, strokePaint)
                curX += subjectW
            }
        }

        val maxAcademicTotal = subjects.size * 100
        canvas.drawLine(curX, tableTop, curX, tableTop + headerH1 + headerH2, strokePaint)
        canvas.drawText("மொத்தம்", curX + 21f, tableTop + 12f, headerTextPaint)
        canvas.drawText("($maxAcademicTotal)", curX + 21f, tableTop + headerH1 + 10f, subTextPaint)
        curX += 42f

        canvas.drawLine(curX, tableTop, curX, tableTop + headerH1 + headerH2, strokePaint)
        canvas.drawText("வேலை", curX + 18f, tableTop + 20f, headerTextPaint)
        curX += 36f

        canvas.drawLine(curX, tableTop, curX, tableTop + headerH1 + headerH2, strokePaint)
        canvas.drawText("வருகை", curX + 18f, tableTop + 20f, headerTextPaint)
        curX += 36f

        canvas.drawLine(curX, tableTop, curX, tableTop + headerH1 + headerH2, strokePaint)
        canvas.drawText("முடிவு", curX + 28f, tableTop + 20f, headerTextPaint)

        // Draw Rows
        var rowY = tableTop + headerH1 + headerH2
        val rowTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
        }
        val nameTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 7.5f
            textAlign = Paint.Align.LEFT
        }

        var globalSno = 1
        for (item in items) {
            val itemStr = item.toString()
            if (itemStr.contains("ClassDivider")) {
                // Class Divider Banner Row
                val stdClass = when {
                    itemStr.contains("stdClass=1") -> 1
                    itemStr.contains("stdClass=2") -> 2
                    itemStr.contains("stdClass=3") -> 3
                    itemStr.contains("stdClass=4") -> 4
                    itemStr.contains("stdClass=5") -> 5
                    itemStr.contains("stdClass=6") -> 6
                    itemStr.contains("stdClass=7") -> 7
                    itemStr.contains("stdClass=8") -> 8
                    else -> 1
                }
                paint.color = Color.rgb(224, 231, 255) // Indigo tint
                paint.style = Paint.Style.FILL
                canvas.drawRect(tableLeft, rowY, tableRight, rowY + 18f, paint)
                canvas.drawRect(tableLeft, rowY, tableRight, rowY + 18f, strokePaint)

                val dividerPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.rgb(30, 58, 138)
                    textSize = 8.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("★  வகுப்பு $stdClass (CLASS $stdClass)  ★", pageWidth / 2f, rowY + 12f, dividerPaint)
                rowY += 18f
            } else {
                // Student Data Row
                // Extract record safely
                val field = item.javaClass.getDeclaredField("record").apply { isAccessible = true }
                val record = field.get(item) as StudentConsolidatedRecord
                val classIdxField = item.javaClass.getDeclaredField("classIndex").apply { isAccessible = true }
                val classIndex = classIdxField.getInt(item)

                val isEven = globalSno % 2 == 0
                if (isEven) {
                    paint.color = Color.rgb(250, 250, 250)
                    paint.style = Paint.Style.FILL
                    canvas.drawRect(tableLeft, rowY, tableRight, rowY + rowH, paint)
                }
                canvas.drawRect(tableLeft, rowY, tableRight, rowY + rowH, strokePaint)

                canvas.drawText("$globalSno", tableLeft + sNoW / 2f, rowY + 14f, rowTextPaint)
                canvas.drawText("${record.student.stdClass}", tableLeft + sNoW + clsW / 2f, rowY + 14f, rowTextPaint)
                canvas.drawText(record.student.admissionNo, tableLeft + sNoW + clsW + admW / 2f, rowY + 14f, rowTextPaint)

                val studentName = record.student.name
                val displayName = if (studentName.length > 17) studentName.take(16) + ".." else studentName
                canvas.drawText(displayName, tableLeft + sNoW + clsW + admW + 4f, rowY + 14f, nameTextPaint)
                canvas.drawText(record.student.community, tableLeft + sNoW + clsW + admW + nameW + commW / 2f, rowY + 14f, rowTextPaint)

                var rx = tableLeft + sNoW + clsW + admW + nameW + commW
                if (isClass8Group) {
                    for (sub in subjects) {
                        val sm = record.subjectMarks[sub]
                        val totStr = if (term == 0) {
                            if (sm?.avgTotal != null) "${sm.avgTotal}" else "-"
                        } else {
                            val tm = when (term) {
                                1 -> sm?.term1Marks
                                2 -> sm?.term2Marks
                                3 -> sm?.term3Marks
                                else -> null
                            }
                            if (tm?.total != null) "${tm.total}" else "-"
                        }
                        val boldTextPaint = Paint(rowTextPaint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
                        canvas.drawText(totStr, rx + subjectW / 2f, rowY + 14f, boldTextPaint)
                        canvas.drawLine(rx, rowY, rx, rowY + rowH, strokePaint)
                        rx += subjectW
                    }
                } else {
                    for (sub in subjects) {
                        val sm = record.subjectMarks[sub]
                        val (saStr, faStr, totStr) = if (term == 0) {
                            Triple(
                                if (sm?.avgSa != null) "${sm.avgSa}" else "-",
                                if (sm?.avgFa != null) "${sm.avgFa}" else "-",
                                if (sm?.avgTotal != null) "${sm.avgTotal}" else "-"
                            )
                        } else {
                            val tm = when (term) {
                                1 -> sm?.term1Marks
                                2 -> sm?.term2Marks
                                3 -> sm?.term3Marks
                                else -> null
                            }
                            Triple(
                                if (tm?.sa != null) "${tm.sa}" else "-",
                                if (tm?.faTotal != null) "${tm.faTotal}" else "-",
                                if (tm?.total != null) "${tm.total}" else "-"
                            )
                        }

                        canvas.drawText(saStr, rx + subColW * 0.5f, rowY + 14f, rowTextPaint)
                        canvas.drawText(faStr, rx + subColW * 1.5f, rowY + 14f, rowTextPaint)
                        val boldTextPaint = Paint(rowTextPaint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
                        canvas.drawText(totStr, rx + subColW * 2.5f, rowY + 14f, boldTextPaint)

                        canvas.drawLine(rx + subColW, rowY, rx + subColW, rowY + rowH, strokePaint)
                        canvas.drawLine(rx + subColW * 2f, rowY, rx + subColW * 2f, rowY + rowH, strokePaint)
                        canvas.drawLine(rx, rowY, rx, rowY + rowH, strokePaint)
                        rx += subjectW
                    }
                }

                // Total column
                canvas.drawLine(rx, rowY, rx, rowY + rowH, strokePaint)
                val totalPaint = Paint(rowTextPaint).apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    color = Color.rgb(30, 58, 138)
                }
                val grandTotal = if (term == 0) {
                    record.grandAvgTotal
                } else {
                    subjects.sumOf { sub ->
                        val sm = record.subjectMarks[sub]
                        val tm = when (term) { 1 -> sm?.term1Marks; 2 -> sm?.term2Marks; 3 -> sm?.term3Marks; else -> null }
                        tm?.total ?: 0
                    }
                }
                canvas.drawText("$grandTotal", rx + 21f, rowY + 14f, totalPaint)
                rx += 42f

                // Attendance
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

                canvas.drawLine(rx, rowY, rx, rowY + rowH, strokePaint)
                canvas.drawText("$wDays", rx + 18f, rowY + 14f, rowTextPaint)
                rx += 36f

                canvas.drawLine(rx, rowY, rx, rowY + rowH, strokePaint)
                canvas.drawText("$pDays", rx + 18f, rowY + 14f, rowTextPaint)
                rx += 36f

                canvas.drawLine(rx, rowY, rx, rowY + rowH, strokePaint)
                val isPass = grandTotal >= (maxAcademicTotal * 0.35)
                val resultText = if (isPass) "தேர்ச்சி" else "பயிற்சி தேவை"
                val resultPaint = Paint(rowTextPaint).apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    color = if (isPass) Color.rgb(22, 101, 52) else Color.rgb(185, 28, 28)
                }
                canvas.drawText(resultText, rx + 28f, rowY + 14f, resultPaint)

                rowY += rowH
                globalSno++
            }
        }

        // Bottom Signatures
        val sigY = pageHeight - margin - 12f
        val sigPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.DEFAULT
        }
        sigPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("வகுப்பு ஆசிரியர் கையொப்பம்", tableLeft + 16f, sigY, sigPaint)

        sigPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("தலைமை ஆசிரியர் கையொப்பம் (${school.headmasterName})", tableRight - 16f, sigY, sigPaint)
    }

    /**
     * Generates individualized parent letters / meeting invitations for selected students.
     * Supports 2 letters per A4 sheet (with cut guide) to save paper, or 1 letter per sheet.
     */
    fun generateParentLettersPdf(
        context: Context,
        school: SchoolProfile,
        students: List<Student>,
        letterTitle: String,
        letterBody: String,
        meetingDate: String,
        meetingPlace: String = "பள்ளி வளாகம்",
        twoPerSheet: Boolean = true
    ): File {
        val fileName = "Parent_Letters_${school.academicYear.replace("-", "_")}.pdf"
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val file = File(reportsDir, fileName)

        val pageWidth = 595 // A4 Portrait
        val pageHeight = 842
        val document = PdfDocument()

        if (twoPerSheet) {
            val totalPages = if (students.isEmpty()) 1 else ((students.size - 1) / 2) + 1
            for (p in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, p + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                val student1 = students.getOrNull(p * 2)
                val student2 = students.getOrNull(p * 2 + 1)

                if (student1 != null) {
                    drawSingleParentLetter(
                        canvas = canvas,
                        school = school,
                        student = student1,
                        letterTitle = letterTitle,
                        letterBody = letterBody,
                        meetingDate = meetingDate,
                        meetingPlace = meetingPlace,
                        left = 24f,
                        top = 20f,
                        right = pageWidth - 24f,
                        bottom = 405f,
                        isHalfSheet = true
                    )
                }

                // Middle Cutting Guide Line
                val dashPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.GRAY
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                var cx = 30f
                while (cx < pageWidth - 30f) {
                    canvas.drawLine(cx, 415f, cx + 10f, 415f, dashPaint)
                    cx += 18f
                }
                val cutTextPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.DKGRAY
                    textSize = 8.5f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("✂  இங்கு வெட்டவும் (காகித சிக்கனம்: ஒரு தாளில் 2 கடிதங்கள்)  ✂", pageWidth / 2f, 418f, cutTextPaint)

                if (student2 != null) {
                    drawSingleParentLetter(
                        canvas = canvas,
                        school = school,
                        student = student2,
                        letterTitle = letterTitle,
                        letterBody = letterBody,
                        meetingDate = meetingDate,
                        meetingPlace = meetingPlace,
                        left = 24f,
                        top = 430f,
                        right = pageWidth - 24f,
                        bottom = 815f,
                        isHalfSheet = true
                    )
                }

                document.finishPage(page)
            }
        } else {
            // 1 letter per sheet
            students.forEachIndexed { idx, st ->
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, idx + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                drawSingleParentLetter(
                    canvas = canvas,
                    school = school,
                    student = st,
                    letterTitle = letterTitle,
                    letterBody = letterBody,
                    meetingDate = meetingDate,
                    meetingPlace = meetingPlace,
                    left = 28f,
                    top = 28f,
                    right = pageWidth - 28f,
                    bottom = pageHeight - 28f,
                    isHalfSheet = false
                )

                document.finishPage(page)
            }
        }

        FileOutputStream(file).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        return file
    }

    private fun drawSingleParentLetter(
        canvas: Canvas,
        school: SchoolProfile,
        student: Student,
        letterTitle: String,
        letterBody: String,
        meetingDate: String,
        meetingPlace: String,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        isHalfSheet: Boolean
    ) {
        val paint = Paint().apply { isAntiAlias = true }
        val strokePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            color = Color.rgb(30, 41, 59)
        }

        // Outer Frame
        canvas.drawRoundRect(RectF(left, top, right, bottom), 8f, 8f, strokePaint)
        // Inner delicate frame
        val innerMargin = 3f
        val innerStroke = Paint(strokePaint).apply { strokeWidth = 0.6f; color = Color.GRAY }
        canvas.drawRoundRect(RectF(left + innerMargin, top + innerMargin, right - innerMargin, bottom - innerMargin), 6f, 6f, innerStroke)

        var curY = top + (if (isHalfSheet) 18f else 28f)

        // 1. School Header
        paint.color = Color.rgb(26, 35, 126)
        paint.textSize = if (isHalfSheet) 12.5f else 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        val schoolHeader = if (school.udiseCode.isNotBlank()) "${school.schoolName} (UDISE: ${school.udiseCode})" else school.schoolName
        canvas.drawText(schoolHeader, (left + right) / 2f, curY, paint)

        curY += if (isHalfSheet) 13f else 18f
        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = if (isHalfSheet) 9f else 11f
        paint.typeface = Typeface.DEFAULT
        val subHeader = if (school.udiseCode.isNotBlank()) "${school.unionName}   |   ${school.districtName}   |   UDISE: ${school.udiseCode}" else "${school.unionName}   |   ${school.districtName}"
        canvas.drawText(subHeader, (left + right) / 2f, curY, paint)

        // 2. Letter Title Badge
        curY += if (isHalfSheet) 14f else 20f
        val badgeW = (right - left) * 0.7f
        val badgeH = if (isHalfSheet) 18f else 24f
        val badgeLeft = (left + right) / 2f - badgeW / 2f
        paint.color = Color.rgb(241, 245, 249)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(badgeLeft, curY, badgeLeft + badgeW, curY + badgeH), 4f, 4f, paint)
        canvas.drawRoundRect(RectF(badgeLeft, curY, badgeLeft + badgeW, curY + badgeH), 4f, 4f, innerStroke)

        paint.color = Color.rgb(180, 83, 9)
        paint.textSize = if (isHalfSheet) 10f else 12.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(letterTitle, (left + right) / 2f, curY + (if (isHalfSheet) 13f else 17f), paint)

        curY += badgeH + (if (isHalfSheet) 12f else 18f)

        // 3. Date & Place
        paint.color = Color.BLACK
        paint.textSize = if (isHalfSheet) 8.5f else 10f
        paint.typeface = Typeface.DEFAULT
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("தேதி: $meetingDate", left + 14f, curY, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("இடம்: $meetingPlace", right - 14f, curY, paint)

        curY += if (isHalfSheet) 14f else 20f

        // 4. Addressing Box (பெறுநர்)
        val addrBoxH = if (isHalfSheet) 44f else 54f
        paint.color = Color.rgb(248, 250, 252)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(left + 12f, curY, right - 12f, curY + addrBoxH), 4f, 4f, paint)
        canvas.drawRoundRect(RectF(left + 12f, curY, right - 12f, curY + addrBoxH), 4f, 4f, innerStroke)

        val addrPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(15, 23, 42)
            textSize = if (isHalfSheet) 8.5f else 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }

        val parentDisplayName = if (student.parentName.isNotBlank()) student.parentName else "${student.name} அவர்களின் பெற்றோர்"
        canvas.drawText("பெறுநர்:  திரு / திருமதி. $parentDisplayName", left + 18f, curY + (if (isHalfSheet) 14f else 17f), addrPaint)

        addrPaint.typeface = Typeface.DEFAULT
        addrPaint.color = Color.rgb(51, 65, 85)
        canvas.drawText(
            "மாணவர்: ${student.name}   |   வகுப்பு: ${student.stdClass} - ${student.section}   |   சேர்க்கை எண்: ${student.admissionNo}",
            left + 18f,
            curY + (if (isHalfSheet) 30f else 36f),
            addrPaint
        )

        curY += addrBoxH + (if (isHalfSheet) 14f else 20f)

        // 5. Salutation & Letter Body
        paint.color = Color.BLACK
        paint.textSize = if (isHalfSheet) 9f else 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("மதிப்பிற்குரிய பெற்றோர் அவர்களுக்கு, வணக்கம்.", left + 14f, curY, paint)

        curY += if (isHalfSheet) 14f else 20f

        // Format message by replacing placeholders
        val resolvedBody = letterBody
            .replace("{parent_name}", parentDisplayName)
            .replace("{student_name}", student.name)
            .replace("{class_section}", "${student.stdClass} - ${student.section}")
            .replace("{admission_no}", student.admissionNo)
            .replace("{date}", meetingDate)
            .replace("{place}", meetingPlace)
            .replace("{school_name}", school.schoolName)

        val bodyPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(30, 41, 59)
            textSize = if (isHalfSheet) 8.5f else 10.5f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.LEFT
        }

        val maxWidth = (right - left) - 28f
        val lineHeight = if (isHalfSheet) 13f else 18f
        val maxBodyLines = if (isHalfSheet) 7 else 14

        drawWrappedText(
            canvas = canvas,
            text = resolvedBody,
            x = left + 14f,
            y = curY,
            maxWidth = maxWidth,
            lineHeight = lineHeight,
            paint = bodyPaint,
            maxLines = maxBodyLines
        )

        // 6. Signatures (Positioned near bottom of card)
        val sigY = bottom - (if (isHalfSheet) 48f else 65f)
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = if (isHalfSheet) 8.5f else 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("வகுப்பு ஆசிரியர் கையொப்பம்", left + 18f, sigY, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("தலைமை ஆசிரியர் கையொப்பம்", right - 18f, sigY, paint)
        paint.typeface = Typeface.DEFAULT
        paint.textSize = if (isHalfSheet) 7.5f else 9f
        canvas.drawText("(${school.headmasterName})", right - 18f, sigY + (if (isHalfSheet) 11f else 15f), paint)

        // 7. Tear-off Acknowledgement slip at bottom
        val ackY = bottom - (if (isHalfSheet) 22f else 30f)
        val dotPaint = Paint().apply {
            isAntiAlias = true
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        canvas.drawLine(left + 12f, ackY - 6f, right - 12f, ackY - 6f, dotPaint)

        val ackTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.DKGRAY
            textSize = if (isHalfSheet) 7.2f else 8.5f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(
            "ஒப்புகை: மாணவர் ${student.name} பெற்றோர் கடிதம் கிடைக்கப்பெற்றேன்.   பெற்றோர் கையொப்பம்: ____________",
            left + 14f,
            ackY + 8f,
            ackTextPaint
        )
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        lineHeight: Float,
        paint: Paint,
        maxLines: Int
    ): Float {
        var curY = y
        val paragraphs = text.split("\n")
        var lineCount = 0
        for (p in paragraphs) {
            if (p.isBlank()) {
                curY += lineHeight * 0.5f
                continue
            }
            val words = p.split(" ")
            var currentLine = ""
            for (w in words) {
                val testLine = if (currentLine.isEmpty()) w else "$currentLine $w"
                val width = paint.measureText(testLine)
                if (width > maxWidth && currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine, x, curY, paint)
                    curY += lineHeight
                    lineCount++
                    if (lineCount >= maxLines) return curY
                    currentLine = w
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, x, curY, paint)
                curY += lineHeight
                lineCount++
                if (lineCount >= maxLines) return curY
            }
        }
        return curY
    }
}
