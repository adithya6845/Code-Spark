package com.example.collisionengine.data.network

import android.content.Context
import com.example.collisionengine.data.model.ProfileMatch
import com.example.collisionengine.data.models.Faculty
import com.example.collisionengine.data.models.Student
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.util.Locale

object LocalDatasetClient {
    private var allStudents: List<Student> = emptyList()
    private var allFaculty: List<Faculty> = emptyList()
    private val gson = Gson()
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        try {
            // Load Students
            context.assets.open("Students.json").use { inputStream ->
                val reader = InputStreamReader(inputStream)
                val list = gson.fromJson<List<Student>>(reader, object : TypeToken<List<Student>>() {}.type)
                allStudents = list ?: emptyList()
                reader.close()
            }
            // Load Faculty
            context.assets.open("Faculty.json").use { inputStream ->
                val reader = InputStreamReader(inputStream)
                val list = gson.fromJson<List<Faculty>>(reader, object : TypeToken<List<Faculty>>() {}.type)
                allFaculty = list ?: emptyList()
                reader.close()
            }
            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun extractProfilesMentionedInText(answerText: String): List<ProfileMatch> {
        if (answerText.isBlank()) return emptyList()

        val textLower = answerText.lowercase(Locale.ROOT)
        val matches = mutableListOf<ProfileMatch>()
        val addedNames = mutableSetOf<String>()

        // 1. Check Faculty (must match full name e.g. "Dr. Sameer Iyer" or "Dr. Emily Chen")
        allFaculty.forEach { faculty ->
            val name = faculty.name ?: return@forEach
            val nameLower = name.lowercase(Locale.ROOT)
            val shortName = nameLower.replace("dr.", "").replace("prof.", "").trim()

            if (textLower.contains(nameLower) || (shortName.length > 5 && textLower.contains(shortName))) {
                if (addedNames.add(name)) {
                    matches.add(
                        ProfileMatch(
                            name = name,
                            role = "Faculty • ${faculty.department ?: "Research"}",
                            matchReasonTitle = "Mentioned in response",
                            matchReasonText = faculty.expertise ?: faculty.researchInterests ?: "",
                            tags = faculty.researchInterests?.split(",")?.map { it.trim() }?.take(3) ?: listOf("Faculty", "Mentor")
                        )
                    )
                }
            }
        }

        // 2. Check Students (must match full name e.g. "Abhishek Agarwal", "Sophia Kim", "Bhavya Menon")
        allStudents.forEach { student ->
            val name = student.name ?: return@forEach
            val nameLower = name.lowercase(Locale.ROOT)

            if (textLower.contains(nameLower)) {
                if (addedNames.add(name)) {
                    matches.add(
                        ProfileMatch(
                            name = name,
                            role = "${student.department ?: "Student"} • ${student.year ?: "Campus"}",
                            matchReasonTitle = "Mentioned in response",
                            matchReasonText = student.projects?.takeIf { it.isNotBlank() } ?: student.skills ?: "",
                            tags = student.skills?.split(",")?.map { it.trim() }?.take(3) ?: listOf("Student", "Peer")
                        )
                    )
                }
            }
        }

        return matches.take(3)
    }
}
