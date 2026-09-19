package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.model.Student
import java.io.BufferedReader
import java.io.InputStreamReader

object CsvStudentImporter {

    const val OFFICIAL_54_STUDENTS_CSV = """EMIS No,மாணவர் பெயர்,வகுப்பு,தந்தை பெயர்
1032450482,ராஜமுருகன் வி,I,வேல்முருகன்
1032452231,ரித்திகா,I,சுரேஷ்
1031378171,கோபிநாத் எம்,II,முத்துக்குமார்
1031377281,பிரணித் ஜே,II,ஜோதிராமலிங்கம்
2029135591,அமுதன் கே,II,கோகிலன் டி
1031426772,தஸ்வந்த் எம்,II,முருகன்
1031476756,நவீன் குமார் எஸ்,II,செல்வராஜ்
1031385262,ரிஷாந்த் ஆர்,II,ரமேஷ்
2030872479,வளன் ஏ,II,அருளாந்து
1031385007,ஜீவிதா எல்,II,லட்சுமணன்
2030745395,பூஜா ஸ்ரீ எம்,II,மாரிகோபால்
1031385169,சாதனா எஸ்,II,எம் சோனை
1031371660,சஞ்சனா எஸ்,II,சரவணன்
1031375175,நாகலட்சுமி டி,II,தணிகாசலம்
1030491854,திருமுருகன்,III,மாரி
1030735315,ரிதான்யா எஸ்,III,சுரேஷ்
1030491964,சிவசத்திப்ரியா,III,ஈஸ்வரன்
1029403793,லோகேஷ் பாண்டியன் எம்,IV,மயிலேறிநாதன்
1029378278,சுபிஷ்  எஸ்,IV,சுந்தரபாண்டி
1029379140,அனன்யா சி,IV,சந்திரசேகர்
1029398955,மித்ரா வி,IV,வீரமுகம்
1029379594,நிகி வைஷ்ணவி ஜே,IV,ஜோதி ராமலிங்கம்
1027911255,அன்ஷிகா எஸ்,V,செபஸ்திராஜ்
1027863803,ஜியா உத்ரா எல்,V,லட்சுமணன்
1027911351,நீலா டி,V,தணிக்காசலம் எம்
1026695054,மோஹித் இ,VI,ஈஸ்வரன்
1026694801,ராம்பிரசந்த் ஆர்,VI,ரமேஷ்
1026621972,சர்வேஷ் எஸ்,VI,சோனை
1026998356,சதீஷ் எஸ்,VI,சக்திவேல்
1026750496,கீர்த்தியா எம்,VI,மலைக்கண்ணு
1026621220,தன்ஷிகா V,VI,வேல்முருகன்
2022712603,ஈஸ்வரமூர்த்தி எம்,VII,முருகன்
2022724547,கமலேஷ்வரன் வி,VII,வீரமுகம்
1025920669,கரண் பாலாஜி எம்,VII,மாரி
1025803393,யோகேஷ் பி,VII,பழனி
2025526517,பிரஜின் டி,VII,தேவேந்திரன் ஏ 
1025864600,யோசியா ஒரு,VII,ஆரோக்கிய ஜார்ஜ்
2022771581,ஜெசிகா கே,VII,கோகிலன் டி
1025801410,தீக்ஷா எம்,VII,மதிவாணன்
1025802555,கவினிலா எம்,VII,முனியசாமி
2022776282,மேகாலவல்லி வி,VII,வேல்முருகன்
1025801897,சர்மிளா  எஸ்,VII,சோனைமுத்து
2025255817,ராஜாத்தி டி,VII,தணிகாசலம்
2023311821,வைதிஸ்வரி வி,VII,வெங்கடேஸ்வரன்
1024378342,ஹரிஹரன் எஸ்,VIII,சக்திவேல்
1022327761,அப்துல் ஹமீது ,VIII,இப்ராஹிம் கனி
1024622823,யோகேஷ் ஆர்,VIII,ராஜாங்கம்
1024376203,தரணிதரன் ஒய்,VIII,யசோதரன்
1024377267,தில்சன் ஜோயல் கே,VIII,காணிக்கைராஜ்
1024373603,யாழிஸ்வரன் I,VIII,இளமாறன்
1024520491,பர்கவி பி,VIII,பஞ்சவேல்
1024373333,தாராணி எஸ்,VIII,சுந்தர பாண்டி
1024756607,தினிஷா ஸ்ரீ எஸ்,VIII,செல்வக்குமார்
1024376700,யுவஸ்ரீ ஜே,VIII,ஜோதிராமலிங்கம்"""

    fun parseStudentsFromText(csvContent: String): List<Student> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val students = mutableListOf<Student>()
        var emisCol = 0
        var nameCol = 1
        var classCol = 2
        var parentCol = 3

        var startIdx = 0
        val firstLine = lines.first().lowercase()
        val isHeader = firstLine.contains("emis") || firstLine.contains("மாணவர்") ||
                firstLine.contains("பெயர்") || firstLine.contains("name") ||
                firstLine.contains("வகுப்பு") || firstLine.contains("class") ||
                firstLine.contains("admission")

        if (isHeader) {
            val delimiter = if (lines.first().contains("\t")) "\t" else ","
            val headers = splitCsvLine(lines.first(), delimiter)
            headers.forEachIndexed { index, rawHeader ->
                val h = rawHeader.trim().lowercase()
                when {
                    h.contains("emis") || h.contains("சேர்க்கை") || h.contains("admission") || h.contains("enrol") -> emisCol = index
                    h.contains("தந்தை") || h.contains("பெற்றோர்") || h.contains("parent") || h.contains("father") -> parentCol = index
                    h.contains("வகுப்பு") || h.contains("class") || h.contains("std") -> classCol = index
                    h.contains("மாணவர்") || h.contains("student") || h.contains("பெயர்") || h.contains("name") -> nameCol = index
                }
            }
            startIdx = 1
        }

        for (i in startIdx until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue

            val delimiter = if (line.contains("\t")) "\t" else ","
            val tokens = splitCsvLine(line, delimiter)
            if (tokens.isEmpty()) continue

            val emis = tokens.getOrNull(emisCol)?.trim()?.removeSurrounding("\"") ?: ""
            val name = tokens.getOrNull(nameCol)?.trim()?.removeSurrounding("\"") ?: ""
            val rawClass = tokens.getOrNull(classCol)?.trim()?.removeSurrounding("\"") ?: ""
            val parent = tokens.getOrNull(parentCol)?.trim()?.removeSurrounding("\"") ?: ""

            if (name.isBlank() && emis.isBlank()) continue

            val stdClass = parseClassNumber(rawClass)

            students.add(
                Student(
                    admissionNo = if (emis.isNotBlank()) emis else (1000 + i).toString(),
                    name = name.ifBlank { "மாணவர் $i" },
                    stdClass = stdClass,
                    section = "A",
                    gender = "ஆண்", // default, can be edited in student details
                    community = "BC",
                    parentName = parent,
                    academicYear = "2026-2027"
                )
            )
        }

        return students
    }

    fun parseFromUri(context: Context, uri: Uri): List<Student> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val content = reader.readText()
                    parseStudentsFromText(content)
                }
            } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun parseClassNumber(raw: String): Int {
        val clean = raw.trim().uppercase()
        return when {
            clean == "I" || clean == "1" || clean.contains("1") -> 1
            clean == "II" || clean == "2" || clean.contains("2") -> 2
            clean == "III" || clean == "3" || clean.contains("3") -> 3
            clean == "IV" || clean == "4" || clean.contains("4") -> 4
            clean == "V" || clean == "5" || clean.contains("5") -> 5
            clean == "VI" || clean == "6" || clean.contains("6") -> 6
            clean == "VII" || clean == "7" || clean.contains("7") -> 7
            clean == "VIII" || clean == "8" || clean.contains("8") -> 8
            else -> {
                clean.toIntOrNull()?.coerceIn(1, 8) ?: 1
            }
        }
    }

    private fun splitCsvLine(line: String, delimiter: String): List<String> {
        if (delimiter == "\t") {
            return line.split("\t")
        }
        val result = mutableListOf<String>()
        var cur = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when (ch) {
                '"' -> inQuotes = !inQuotes
                ',' -> {
                    if (inQuotes) {
                        cur.append(ch)
                    } else {
                        result.add(cur.toString())
                        cur = StringBuilder()
                    }
                }
                else -> cur.append(ch)
            }
        }
        result.add(cur.toString())
        return result
    }
}
