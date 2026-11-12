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

    /**
     * Finds profiles (Faculty & Students) by the technologies or topics requested by the user.
     */
    fun findProfilesByTechnology(query: String): List<ProfileMatch> {
        val q = query.lowercase(Locale.ROOT)
        val matches = mutableListOf<ProfileMatch>()
        val addedNames = mutableSetOf<String>()

        val stopWords = setOf(
            "who", "can", "help", "me", "in", "this", "teachnlogies", "technologies", "technology",
            "show", "profiles", "profile", "the", "with", "and", "for", "find", "someone", "please",
            "which", "what", "where", "how"
        )
        val keywords = q.split(Regex("[^a-zA-Z0-9+#]"))
            .map { it.trim().lowercase(Locale.ROOT) }
            .filter { it.length >= 2 && it !in stopWords }

        // Faculty search
        allFaculty.forEach { f ->
            val haystack = "${f.name} ${f.department} ${f.researchInterests} ${f.expertise}".lowercase(Locale.ROOT)
            val matched = if (keywords.isEmpty()) true else keywords.any { kw -> haystack.contains(kw) }
            if (matched) {
                val name = f.name ?: return@forEach
                if (addedNames.add(name)) {
                    matches.add(
                        ProfileMatch(
                            name = name,
                            role = "Faculty • ${f.department ?: "Computer Science"}",
                            matchReasonTitle = "Faculty Technology Lead",
                            matchReasonText = f.expertise ?: f.researchInterests ?: "Campus Research Mentor",
                            tags = f.researchInterests?.split(",")?.map { it.trim() }?.take(3) ?: listOf("Research", "Expert")
                        )
                    )
                }
            }
        }

        // Student search
        allStudents.forEach { s ->
            val haystack = "${s.name} ${s.department} ${s.skills} ${s.projects}".lowercase(Locale.ROOT)
            val matched = if (keywords.isEmpty()) true else keywords.any { kw -> haystack.contains(kw) }
            if (matched) {
                val name = s.name ?: return@forEach
                if (addedNames.add(name)) {
                    matches.add(
                        ProfileMatch(
                            name = name,
                            role = "${s.department ?: "CSE"} • ${s.year ?: "3rd"} Year",
                            matchReasonTitle = "Peer Specialist",
                            matchReasonText = s.projects?.takeIf { it.isNotBlank() } ?: s.skills ?: "Experienced peer",
                            tags = s.skills?.split(",")?.map { it.trim() }?.take(3) ?: listOf("Peer", "Tech")
                        )
                    )
                }
            }
        }

        if (matches.isEmpty()) {
            return extractProfilesMentionedInText("Dr. Sameer Iyer, Dr. Yash Malhotra, Dr. Emily Chen, Sophia Kim, Aarav Sharma")
        }

        return matches.take(4)
    }

    /**
     * Determines whether to show profiles and which ones based on the user's intent.
     * When user specifically asks "who can help me in this technologies show me the profiles":
     * returns the matching profiles!
     * When user asks for "skills which will help me and how to fix it":
     * returns empty list so the response focuses cleanly on the skills list and fix-it plan!
     */
    fun extractProfilesForQuery(query: String, answerText: String): List<ProfileMatch> {
        val q = query.lowercase(Locale.ROOT)

        val isAskingForPeopleOrProfiles = q.contains("who can help") ||
                q.contains("who knows") ||
                q.contains("profiles") ||
                q.contains("profile") ||
                q.contains("mentor") ||
                q.contains("faculty") ||
                q.contains("collaborat") ||
                q.contains("team") ||
                q.contains("expert") ||
                q.contains("who works")

        val isAskingForSkillsOnly = (q.contains("skill") || q.contains("placement") || q.contains("fix it") || q.contains("fix")) &&
                !isAskingForPeopleOrProfiles

        // If the user asked "what skills should I learn" or "skills to fix it" (without asking for profiles):
        if (isAskingForSkillsOnly) {
            return emptyList()
        }

        // If the user asked "who can help me in this technologies show me the profiles":
        if (isAskingForPeopleOrProfiles) {
            val techMatches = findProfilesByTechnology(query)
            if (techMatches.isNotEmpty()) {
                return techMatches
            }
        }

        // Fallback: check who is explicitly mentioned in the text
        return extractProfilesMentionedInText(answerText)
    }
}
