package com.example.collisionengine.ui.simulator

import com.example.collisionengine.data.models.Faculty
import com.example.collisionengine.data.models.Research
import kotlinx.serialization.Serializable

@Serializable
data class SimulatorCollaborator(
    val id: String,
    val name: String,
    val department: String,
    val year: String,
    val roleTitle: String, // e.g. "IoT • 3rd Year ECE"
    val primarySkillTag: String, // e.g. "IoT • Sensors"
    val tagColorHex: Long = 0xFF10B981,
    val skills: List<String> = emptyList(),
    val isOnline: Boolean = true,
    val avatarInitials: String = name.take(2).uppercase()
)

data class MetricItem(
    val title: String,
    val value: String,
    val subtitle: String,
    val valueColorHex: Long,
    val iconType: MetricIconType
)

enum class MetricIconType {
    SKILL_COVERAGE,
    FEASIBILITY,
    COLLABORATION,
    MISSING_SKILL,
    FACULTY,
    RESEARCH
}

data class OutcomeItem(
    val title: String,
    val potentialText: String,
    val colorHex: Long,
    val iconType: OutcomeIconType
)

enum class OutcomeIconType {
    HACKATHON,
    RESEARCH_PROJECT,
    STARTUP_IDEA
}

data class ProjectIdea(
    val title: String,
    val description: String,
    val domain: String,
    val keyTechnologies: List<String> = emptyList()
)

data class SkillCoverageDetail(
    val skillName: String,
    val isCovered: Boolean,
    val coveredBy: String? = null,
    val importance: String = "Required"
)

data class RoadmapPhase(
    val phaseNumber: Int,
    val title: String,
    val duration: String,
    val milestone: String,
    val assignedMembers: List<String>
)

data class SimulationAnalysisResult(
    val prompt: String,
    val skillCoveragePercent: Int,
    val coveredSkillsCount: Int,
    val totalSkillsCount: Int,
    val projectFeasibilityPercent: Int,
    val feasibilityLabel: String,
    val collaborationStrengthPercent: Int,
    val collaborationLabel: String,
    val missingSkill: String,
    val missingSkillRecommendation: String,
    val relevantFaculty: String,
    val facultyDepartment: String,
    val relatedResearchCount: Int,
    val researchLabel: String,
    val potentialOutcomes: List<OutcomeItem>,
    val suggestedProject: ProjectIdea,
    val skillDetails: List<SkillCoverageDetail>,
    val roadmap: List<RoadmapPhase>,
    val matchedFacultyMembers: List<Faculty> = emptyList(),
    val matchedPapers: List<Research> = emptyList(),
    val genieExplanation: String = ""
)
