package com.example.collisionengine.ui.simulator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.collisionengine.data.models.Faculty
import com.example.collisionengine.data.models.Project
import com.example.collisionengine.data.models.Research
import com.example.collisionengine.data.models.Student
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStreamReader

class WhatIfSimulatorViewModel(application: Application) : AndroidViewModel(application) {

    private val gson = Gson()

    private var allStudents: List<Student> = emptyList()
    private var allFaculty: List<Faculty> = emptyList()
    private var allResearch: List<Research> = emptyList()
    private var allProjects: List<Project> = emptyList()

    private val _promptQuery = MutableStateFlow("What if I collaborate with Aarav + Sophia to build an AI powered campus traffic system?")
    val promptQuery: StateFlow<String> = _promptQuery.asStateFlow()

    private val _selectedCollaborators = MutableStateFlow<List<SimulatorCollaborator>>(emptyList())
    val selectedCollaborators: StateFlow<List<SimulatorCollaborator>> = _selectedCollaborators.asStateFlow()

    private val _availableStudents = MutableStateFlow<List<Student>>(emptyList())
    val availableStudents: StateFlow<List<Student>> = _availableStudents.asStateFlow()

    private val _searchStudentQuery = MutableStateFlow("")
    val searchStudentQuery: StateFlow<String> = _searchStudentQuery.asStateFlow()

    private val _analysisResult = MutableStateFlow<SimulationAnalysisResult?>(null)
    val analysisResult: StateFlow<SimulationAnalysisResult?> = _analysisResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _showAddPersonSheet = MutableStateFlow(false)
    val showAddPersonSheet: StateFlow<Boolean> = _showAddPersonSheet.asStateFlow()

    private val _showFullSimulationDialog = MutableStateFlow(false)
    val showFullSimulationDialog: StateFlow<Boolean> = _showFullSimulationDialog.asStateFlow()

    init {
        loadDataFromAssets()
        initDefaultCollaborators()
        runSimulation()
    }

    private fun loadDataFromAssets() {
        val context = getApplication<Application>().applicationContext
        try {
            allStudents = loadJson("Students.json", object : TypeToken<List<Student>>() {})
            allFaculty = loadJson("Faculty.json", object : TypeToken<List<Faculty>>() {})
            allResearch = loadJson("Research.json", object : TypeToken<List<Research>>() {})
            allProjects = loadJson("Projects.json", object : TypeToken<List<Project>>() {})
            _availableStudents.value = allStudents
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun <T> loadJson(filename: String, typeToken: TypeToken<List<T>>): List<T> {
        val context = getApplication<Application>().applicationContext
        return try {
            val inputStream = context.assets.open(filename)
            val reader = InputStreamReader(inputStream)
            val list = gson.fromJson<List<T>>(reader, typeToken.type)
            reader.close()
            list ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun initDefaultCollaborators() {
        _selectedCollaborators.value = listOf(
            SimulatorCollaborator(
                id = "c_aarav",
                name = "Aarav Sharma",
                department = "3rd Year ECE",
                year = "3rd Year",
                roleTitle = "IoT • 3rd Year ECE",
                primarySkillTag = "IoT • Sensors",
                tagColorHex = 0xFF10B981, // Green
                skills = listOf("IoT Protocols (MQTT/Zigbee)", "Sensor Integration", "Hardware Prototyping", "Embedded Systems"),
                isOnline = true,
                avatarInitials = "AS"
            ),
            SimulatorCollaborator(
                id = "c_sophia",
                name = "Sophia Kim",
                department = "2nd Year CSE",
                year = "2nd Year",
                roleTitle = "CV • 2nd Year CSE",
                primarySkillTag = "Computer Vision",
                tagColorHex = 0xFFA855F7, // Purple
                skills = listOf("Computer Vision", "Deep Learning", "PyTorch", "OpenCV", "Edge AI"),
                isOnline = true,
                avatarInitials = "SK"
            )
        )
    }

    fun onPromptChange(newText: String) {
        _promptQuery.value = newText
    }

    fun onSearchStudentChange(query: String) {
        _searchStudentQuery.value = query
        if (query.isBlank()) {
            _availableStudents.value = allStudents
        } else {
            val q = query.lowercase().trim()
            _availableStudents.value = allStudents.filter { s ->
                val name = s.name?.lowercase() ?: ""
                val skills = s.skills?.lowercase() ?: ""
                val dept = s.department?.lowercase() ?: ""
                name.contains(q) || skills.contains(q) || dept.contains(q)
            }
        }
    }

    fun addCollaboratorFromStudent(student: Student) {
        val name = student.name ?: "Unknown Student"
        if (_selectedCollaborators.value.any { it.name.equals(name, ignoreCase = true) }) {
            _showAddPersonSheet.value = false
            return
        }

        val skillsList = student.skills?.split(",")?.map { it.trim() } ?: listOf("General Tech")
        val primaryTag = skillsList.firstOrNull() ?: "General"
        val deptShort = student.department?.replace("Department of", "")?.trim() ?: "Student"
        val yearShort = if (!student.year.isNullOrBlank()) "${student.year} Year" else "Student"
        val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()

        val isMobileDev = skillsList.any { it.contains("Mobile", true) || it.contains("Android", true) || it.contains("React", true) || it.contains("Flutter", true) }

        val newCollaborator = SimulatorCollaborator(
            id = student.studentId ?: "student_${System.currentTimeMillis()}",
            name = name,
            department = "$yearShort $deptShort",
            year = yearShort,
            roleTitle = "${deptShort.take(12)} • $yearShort",
            primarySkillTag = primaryTag,
            tagColorHex = if (isMobileDev) 0xFF38BDF8 else 0xFF8B5CF6,
            skills = skillsList,
            isOnline = true,
            avatarInitials = initials.ifBlank { "ST" }
        )

        _selectedCollaborators.value = _selectedCollaborators.value + newCollaborator
        _showAddPersonSheet.value = false

        updatePromptWithCollaborators()
        runSimulation()
    }

    fun removeCollaborator(collaboratorId: String) {
        _selectedCollaborators.value = _selectedCollaborators.value.filter { it.id != collaboratorId }
        updatePromptWithCollaborators()
        runSimulation()
    }

    private fun updatePromptWithCollaborators() {
        val names = _selectedCollaborators.value.map { it.name.split(" ").first() }
        if (names.isNotEmpty()) {
            val namesStr = names.joinToString(" + ")
            val current = _promptQuery.value
            if (current.contains("What if I collaborate with", ignoreCase = true) && current.contains("to build", ignoreCase = true)) {
                val after = current.substringAfter("to build", "").trim()
                if (after.isNotBlank()) {
                    _promptQuery.value = "What if I collaborate with $namesStr to build $after"
                }
            }
        }
    }

    fun runSimulation() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            
            // Sync mentioned names from prompt if user typed new names
            val prompt = _promptQuery.value
            syncCollaboratorsFromPrompt(prompt)

            delay(150)
            val result = WhatIfSimulatorEngine.simulate(
                prompt = _promptQuery.value,
                collaborators = _selectedCollaborators.value,
                allStudents = allStudents,
                allFaculty = allFaculty,
                allResearch = allResearch,
                allProjects = allProjects
            )
            _analysisResult.value = result
            _isAnalyzing.value = false
        }
    }

    private fun syncCollaboratorsFromPrompt(prompt: String) {
        val pLower = prompt.lowercase()
        // If prompt specifically mentions Aarav + Sophia and collaborators are empty or desynced
        if (pLower.contains("aarav") && pLower.contains("sophia") && _selectedCollaborators.value.size < 2) {
            initDefaultCollaborators()
        }
    }

    fun setShowAddPersonSheet(show: Boolean) {
        _showAddPersonSheet.value = show
        if (show) {
            _searchStudentQuery.value = ""
            _availableStudents.value = allStudents
        }
    }

    fun setShowFullSimulationDialog(show: Boolean) {
        _showFullSimulationDialog.value = show
    }
}
