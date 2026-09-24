package com.example.util

import com.example.BuildConfig
import com.example.data.model.Student
import com.example.data.model.StudentMarks
import com.example.data.model.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * AI Error detection and educational guidance assistant using Gemini 3.5 Flash
 * with offline heuristic intelligence fallback.
 */
object AiErrorAndGuidanceManager {

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    data class ValidationIssue(
        val type: IssueType,
        val title: String,
        val description: String,
        val canAutoFix: Boolean = false,
        val autoFixAction: (() -> Unit)? = null
    )

    enum class IssueType {
        ERROR,
        WARNING,
        SUCCESS
    }

    data class StudentAnalysisResult(
        val issues: List<ValidationIssue>,
        val strongSubjects: List<String>,
        val weakSubjects: List<String>,
        val teacherRemark: String,
        val isAiGenerated: Boolean
    )

    /**
     * Perform local instant validation of student marks and attendance
     */
    fun validateStudentData(
        student: Student,
        term: Int,
        workingDays: Int,
        presentDays: Int,
        marksMap: Map<Subject, StudentMarks>
    ): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()

        // 1. Attendance checks
        if (workingDays <= 0) {
            issues.add(
                ValidationIssue(
                    type = IssueType.ERROR,
                    title = "பள்ளி வேலை நாட்கள் விடுபட்டுள்ளது",
                    description = "பள்ளி வேலை நாட்கள் 0 என உள்ளது. பள்ளி அமைப்புகளில் சரியான வேலை நாட்களைப் பதிவிடவும்."
                )
            )
        } else if (presentDays > workingDays) {
            issues.add(
                ValidationIssue(
                    type = IssueType.ERROR,
                    title = "வருகை நாட்கள் பிழை (Present > Working Days)",
                    description = "மாணவர் வருகை நாட்கள் ($presentDays) பள்ளி வேலை நாட்களை விட ($workingDays) அதிகமாக உள்ளது. வருகை 100%-க்கு மேல் இருக்கக்கூடாது.",
                    canAutoFix = true
                )
            )
        } else {
            val attPct = (presentDays.toDouble() / workingDays.toDouble()) * 100.0
            if (attPct < 75.0) {
                issues.add(
                    ValidationIssue(
                        type = IssueType.WARNING,
                        title = "குறைந்த வருகை விழுக்காடு (${String.format("%.1f", attPct)}%)",
                        description = "மாணவரின் வருகை விழுக்காடு 75%-க்கு குறைவாக உள்ளது. பள்ளி வருகையை அதிகரிக்க வழிகாட்டவும்."
                    )
                )
            }
        }

        // 2. Marks checks
        val mainSubjects = Subject.getMainSubjectsForClass(student.stdClass)
        var missingCount = 0
        var outOfBoundsCount = 0

        for (subj in mainSubjects) {
            val mark = marksMap[subj]
            if (mark == null) {
                missingCount++
                continue
            }

            val total = mark.total
            if (student.stdClass == 8) {
                val curVal = if (mark.total > 0) mark.total else mark.sa
                if (curVal > 100 || curVal < 0) {
                    outOfBoundsCount++
                    issues.add(
                        ValidationIssue(
                            type = IssueType.ERROR,
                            title = "${subj.tamilName} மதிப்பெண் வரம்பு பிழை",
                            description = "8-ம் வகுப்பு மதிப்பெண் 0 முதல் 100 வரை மட்டுமே இருக்க வேண்டும் ($curVal என உள்ளது).",
                            canAutoFix = true
                        )
                    )
                }
            } else if (student.stdClass in 1..3) {
                val n1Max = if (student.stdClass == 3) 20 else 25
                val n2Max = if (student.stdClass == 3) 20 else 25
                val thMax = if (student.stdClass == 3) 60 else 50
                val oralMax = if (student.stdClass == 3) 5 else 10
                val actMax = 10
                val wriMax = 5

                val hasComponentError = mark.naney1Oral > oralMax || mark.naney1Activity > actMax || mark.naney1Written > wriMax ||
                        mark.naney2Oral > oralMax || mark.naney2Activity > actMax || mark.naney2Written > wriMax

                if (mark.naney1Total > n1Max || mark.naney2Total > n2Max || mark.thiranariTotal > thMax || hasComponentError) {
                    outOfBoundsCount++
                    issues.add(
                        ValidationIssue(
                            type = IssueType.ERROR,
                            title = "${subj.tamilName} புதிய பாடத்திட்ட மதிப்பீட்டு பிழை",
                            description = if (student.stdClass == 3)
                                "நானே செய்வேன் மதிப்பெண் வரம்புகள்: வாய்மொழி [5], செயல்பாடு [10], எழுத்துவழி [5] (கூடுதல் 20)."
                            else
                                "நானே செய்வேன் அல்லது திறனறி மதிப்பெண் அதிகபட்ச வரம்பைத் தாண்டியுள்ளது.",
                            canAutoFix = true
                        )
                    )
                }
            } else {
                // Class 4-7: FA 40, SA 60
                if (mark.faTotal > 40 || mark.sa > 60) {
                    outOfBoundsCount++
                    issues.add(
                        ValidationIssue(
                            type = IssueType.ERROR,
                            title = "${subj.tamilName} FA/SA வரம்பு பிழை",
                            description = "வளரறி (FA) அதிகபட்சம் 40, தொகுத்தறி (SA) அதிகபட்சம் 60 மதிப்பெண்கள் மட்டுமே இருக்க வேண்டும்.",
                            canAutoFix = true
                        )
                    )
                }
            }

            if (total == 0 && mark.faTotal == 0 && mark.sa == 0 && mark.naney1Total == 0) {
                missingCount++
            }
        }

        if (missingCount > 0) {
            issues.add(
                ValidationIssue(
                    type = IssueType.WARNING,
                    title = "$missingCount பாடங்களில் மதிப்பெண்கள் விடுபட்டுள்ளன",
                    description = "சில பாடங்களுக்கு மதிப்பெண் 0 என உள்ளது. விடுபட்ட மதிப்பெண்களை சரிபார்த்து உள்ளிடவும்."
                )
            )
        }

        if (issues.none { it.type == IssueType.ERROR }) {
            issues.add(
                0,
                ValidationIssue(
                    type = IssueType.SUCCESS,
                    title = "அனைத்துத் தரவுகளும் சரியாக உள்ளன",
                    description = "மதிப்பெண்கள் மற்றும் வருகை நாட்கள் அனைத்தும் தமிழ்நாடு தொடக்கக் கல்வித்துறை விதிமுறைகளுக்கு உட்பட்டு சரியாக உள்ளன."
                )
            )
        }

        return issues
    }

    /**
     * Generate comprehensive AI remarks & guidance for the student using Gemini 3.5 Flash
     * with an offline Tamil heuristic fallback.
     */
    suspend fun analyzeStudentAndGenerateRemarks(
        student: Student,
        term: Int,
        workingDays: Int,
        presentDays: Int,
        marksMap: Map<Subject, StudentMarks>
    ): StudentAnalysisResult = withContext(Dispatchers.IO) {
        val issues = validateStudentData(student, term, workingDays, presentDays, marksMap)
        val mainSubjects = Subject.getMainSubjectsForClass(student.stdClass)

        val subjectScores = mainSubjects.map { subj ->
            val mark = marksMap[subj]
            val score = mark?.total ?: 0
            subj.tamilName to score
        }

        val strong = subjectScores.filter { it.second >= 75 }.map { it.first }
        val weak = subjectScores.filter { it.second in 1..49 }.map { it.first }

        // Attempt Gemini 3.5 Flash call if API key configured
        var aiRemark: String? = null
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                aiRemark = callGeminiForRemarks(student, term, workingDays, presentDays, subjectScores, strong, weak, apiKey)
            } catch (_: Exception) {
                aiRemark = null
            }
        }

        val finalRemark = aiRemark ?: generateLocalTamilRemarks(student, strong, weak, subjectScores)

        StudentAnalysisResult(
            issues = issues,
            strongSubjects = strong,
            weakSubjects = weak,
            teacherRemark = finalRemark,
            isAiGenerated = aiRemark != null
        )
    }

    private fun callGeminiForRemarks(
        student: Student,
        term: Int,
        workingDays: Int,
        presentDays: Int,
        subjectScores: List<Pair<String, Int>>,
        strong: List<String>,
        weak: List<String>,
        apiKey: String
    ): String? {
        val prompt = buildString {
            appendLine("You are an expert Tamil Nadu Primary/Middle school teacher evaluating a student's term performance.")
            appendLine("Student Name: ${student.name}")
            appendLine("Class: ${student.stdClass}th Standard")
            appendLine("Term: பருவம் $term")
            appendLine("Attendance: $presentDays / $workingDays days")
            appendLine("Subject Marks (out of 100):")
            subjectScores.forEach { (sub, score) ->
                appendLine("- $sub: $score/100")
            }
            appendLine()
            appendLine("Generate a concise, constructive, encouraging Teacher Remark (ஆசிரியர் குறிப்பு) strictly in Tamil (2 to 3 sentences).")
            appendLine("Acknowledge strong subjects (${strong.joinToString(", ")}), give positive advice for improvement in weaker areas (${weak.joinToString(", ")}), and motivate the student.")
            appendLine("Do NOT include markdown formatting or quotes, just the clean Tamil sentences.")
        }

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val responseText = response.body?.string() ?: return null
        val responseObj = JSONObject(responseText)
        val candidates = responseObj.optJSONArray("candidates")
        val content = candidates?.optJSONObject(0)?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val remarkText = parts?.optJSONObject(0)?.optString("text")?.trim()

        return if (!remarkText.isNullOrBlank()) remarkText else null
    }

    private fun generateLocalTamilRemarks(
        student: Student,
        strong: List<String>,
        weak: List<String>,
        subjectScores: List<Pair<String, Int>>
    ): String {
        val total = subjectScores.sumOf { it.second }
        val avg = if (subjectScores.isNotEmpty()) total / subjectScores.size else 0

        return buildString {
            if (avg >= 80) {
                append("அனைத்துப் பாடங்களிலும் மிகச் சிறந்த தேர்ச்சி பெற்றுள்ளார். ")
                if (strong.isNotEmpty()) {
                    append("${strong.take(2).joinToString(" மற்றும் ")} பாடங்களில் தனித்திறன் வெளிப்படுகிறது. ")
                }
                append("இதே ஆர்வத்தைத் தொடர்ந்து பேண வாழ்த்துகள்!")
            } else if (avg >= 60) {
                append("படிப்பில் நல்ல முன்னேற்றம் காண்கிறார். ")
                if (weak.isNotEmpty()) {
                    append("${weak.joinToString(", ")} பாடங்களில் கூடுதல் பயிற்சி மேற்கொண்டால் மேலும் அதிக மதிப்பெண் பெறலாம். ")
                }
                append("தொடர் முயற்சி வெற்றி தரும்!")
            } else if (avg >= 40) {
                append("கற்றலில் ஆர்வம் உள்ளது. ")
                if (weak.isNotEmpty()) {
                    append("${weak.joinToString(", ")} பாடங்களின் அடிப்படை கருத்துகளில் சிறப்பு கவனம் தேவை. ")
                }
                append("தினசரி வாசிப்பு மற்றும் எழுத்துப் பயிற்சி அவசியம்.")
            } else {
                append("கற்றல் திறனை மேம்படுத்த ஆசிரியரின் சிறப்பு வழிகாட்டலும் பெற்றோரின் கண்காணிப்பும் தேவை. நம்பிக்கையுடன் முயற்சி செய்ய வேண்டும்.")
            }
        }
    }
}
