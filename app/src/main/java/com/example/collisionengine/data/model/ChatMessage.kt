package com.example.collisionengine.data.model

import java.util.UUID

data class ProfileMatch(
    val name: String,
    val role: String,
    val matchReasonTitle: String,
    val matchReasonText: String,
    val tags: List<String>
)

data class DecisionAnalysis(
    val skillCoverage: String,
    val skillCoveragePercent: Int = 92,
    val projectFeasibility: String,
    val projectFeasibilityPercent: Int = 87,
    val missingSkill: String,
    val relevantFaculty: String,
    val relatedResearch: String,
    val potentialOutcomes: String,
    val suggestedProjectTitle: String? = null,
    val suggestedProjectDesc: String? = null
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isTopMatch: Boolean = false,
    val topMatches: List<ProfileMatch> = emptyList(),
    val decisionAnalysis: DecisionAnalysis? = null
)
