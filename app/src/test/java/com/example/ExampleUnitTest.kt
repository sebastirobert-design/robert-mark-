package com.example

import com.example.util.CsvStudentImporter
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testParseOfficial54Students() {
        val students = CsvStudentImporter.parseStudentsFromText(CsvStudentImporter.OFFICIAL_54_STUDENTS_CSV)
        assertEquals(54, students.size)
        assertEquals("1032450482", students[0].admissionNo)
        assertEquals("ராஜமுருகன் வி", students[0].name)
        assertEquals(1, students[0].stdClass)
        assertEquals("வேல்முருகன்", students[0].parentName)

        // Verify Class 8 student at end
        val lastStudent = students.last()
        assertEquals("1024376700", lastStudent.admissionNo)
        assertEquals("யுவஸ்ரீ ஜே", lastStudent.name)
        assertEquals(8, lastStudent.stdClass)
        assertEquals("ஜோதிராமலிங்கம்", lastStudent.parentName)
    }

    @Test
    fun testParseTsvAndCustomCsv() {
        val tsvData = """
            EMIS No	மாணவர் பெயர்	வகுப்பு	தந்தை பெயர்
            1032450001	கணேஷ் கே	3	கார்த்திக்
            1032450002	அஞ்சலி எம்	VI	முருகேசன்
        """.trimIndent()
        val parsed = CsvStudentImporter.parseStudentsFromText(tsvData)
        assertEquals(2, parsed.size)
        assertEquals(3, parsed[0].stdClass)
        assertEquals(6, parsed[1].stdClass)
    }
}

