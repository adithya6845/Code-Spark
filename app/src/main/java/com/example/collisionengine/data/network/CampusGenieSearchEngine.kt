package com.example.collisionengine.data.network

import android.content.Context
import com.example.collisionengine.data.model.DecisionAnalysis
import com.example.collisionengine.data.model.ProfileMatch
import com.example.collisionengine.data.models.Faculty
import com.example.collisionengine.data.models.Project
import com.example.collisionengine.data.models.Research
import com.example.collisionengine.data.models.Student
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.util.Locale

object CampusGenieSearchEngine {

    private val gson = Gson()
    private var allStudents: List<Student> = emptyList()
    private var allFaculty: List<Faculty> = emptyList()
    private var allResearch: List<Research> = emptyList()
    private var allProjects: List<Project> = emptyList()
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        try {
            allStudents = loadJson(context, "Students.json", object : TypeToken<List<Student>>() {})
            allFaculty = loadJson(context, "Faculty.json", object : TypeToken<List<Faculty>>() {})
            allResearch = loadJson(context, "Research.json", object : TypeToken<List<Research>>() {})
            allProjects = loadJson(context, "Projects.json", object : TypeToken<List<Project>>() {})
            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun <T> loadJson(context: Context, filename: String, typeToken: TypeToken<List<T>>): List<T> {
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

    data class GenieAnswer(
        val text: String,
        val decisionAnalysis: DecisionAnalysis,
        val matchedProfiles: List<ProfileMatch>
    )

    fun answerQuery(query: String, liveDatabricksNotes: String = ""): GenieAnswer {
        val q = query.lowercase(Locale.ROOT).trim()

        // 1. Scenario: Aarav + Sophia / Campus Traffic / IoT + CV
        if ((q.contains("aarav") && q.contains("sophia")) || (q.contains("traffic") && (q.contains("ai") || q.contains("iot") || q.contains("collaborate")))) {
            val profiles = listOf(
                ProfileMatch(
                    name = "Aarav Sharma",
                    role = "ECE • 3rd Year",
                    matchReasonTitle = "IoT & Hardware Prototyping",
                    matchReasonText = "MQTT, Sensor Nodes, Embedded Systems, Zigbee",
                    tags = listOf("IoT & Sensors", "Hardware Prototyping", "MQTT")
                ),
                ProfileMatch(
                    name = "Sophia Kim",
                    role = "CSE • 2nd Year",
                    matchReasonTitle = "Computer Vision & Edge AI",
                    matchReasonText = "YOLOv8, Deep Learning, PyTorch Edge Inference, OpenCV",
                    tags = listOf("Computer Vision", "Deep Learning", "PyTorch")
                ),
                ProfileMatch(
                    name = "Dr. Emily Chen",
                    role = "Faculty • Computer Science",
                    matchReasonTitle = "Relevant Faculty Mentor",
                    matchReasonText = "Edge AI, IoT Video Telemetry, Real-time Campus Analytics",
                    tags = listOf("Edge AI", "IoT Research", "Computer Vision")
                )
            )

            val decision = DecisionAnalysis(
                skillCoverage = "92% (4 / 5 Skills Covered)",
                skillCoveragePercent = 92,
                projectFeasibility = "87% (High Chance)",
                projectFeasibilityPercent = 87,
                missingSkill = "Mobile Development",
                relevantFaculty = "Dr. Emily Chen (Edge AI • IoT Research)",
                relatedResearch = "2 Papers match your project idea",
                potentialOutcomes = "Hackathon (High) • Research Project (High) • Startup Idea (Good)",
                suggestedProjectTitle = "AI Powered Campus Traffic Intelligence System",
                suggestedProjectDesc = "Real-time traffic prediction using IoT sensors and Computer Vision for smart campus mobility."
            )

            val text = "• Synergy (91%): Aarav provides IoT sensors & MQTT; Sophia handles Computer Vision & YOLO.\n" +
                    "• Mentor: Dr. Emily Chen (Edge AI Lab) has 2 matching campus publications.\n" +
                    "• Advice: Add 1 student for mobile/cloud dashboard deployment."

            return GenieAnswer(text = text, decisionAnalysis = decision, matchedProfiles = profiles)
        }

        // 2. Scenario: Drone / Robotics / Autonomous Surveillance
        if (q.contains("drone") || q.contains("robot") || q.contains("ros") || q.contains("uav") || q.contains("surveillance")) {
            val profiles = listOf(
                ProfileMatch(
                    name = "Dr. Ethan Liu",
                    role = "Faculty • Computer Engineering",
                    matchReasonTitle = "Robotics & Computer Vision Lead",
                    matchReasonText = "Robotics, Computer Vision, Edge Computing, ROS 2",
                    tags = listOf("Robotics", "Computer Vision", "Edge Computing")
                ),
                ProfileMatch(
                    name = "Dr. Anjali Verma",
                    role = "Faculty • Computer Science",
                    matchReasonTitle = "Embedded Robotics",
                    matchReasonText = "Embedded Systems, Robotics, Control Algorithms",
                    tags = listOf("Embedded Systems", "Robotics", "Control")
                ),
                ProfileMatch(
                    name = "Sophia Kim",
                    role = "CSE • 2nd Year",
                    matchReasonTitle = "Computer Vision & Edge AI",
                    matchReasonText = "YOLOv8, Object Detection, Real-time Stream Analytics",
                    tags = listOf("Computer Vision", "Deep Learning", "PyTorch")
                )
            )

            val decision = DecisionAnalysis(
                skillCoverage = "85% (3 / 4 Skills Covered)",
                skillCoveragePercent = 85,
                projectFeasibility = "83% (High Chance)",
                projectFeasibilityPercent = 83,
                missingSkill = "Telemetry & Flight Controller Firmware",
                relevantFaculty = "Dr. Ethan Liu (Robotics • Edge AI)",
                relatedResearch = "2 Papers match your project idea",
                potentialOutcomes = "Hackathon (High) • Research Project (High) • Startup Idea (Moderate)",
                suggestedProjectTitle = "Autonomous Campus Patrol & Mapping Drone",
                suggestedProjectDesc = "Vision-guided autonomous navigation mesh for campus safety surveillance and thermal anomaly inspection."
            )

            val text = "• Feasibility (85%): Requires ROS 2 for flight control, YOLOv8 for real-time person/threat detection, and telemetry streaming.\n" +
                    "• Mentors: Dr. Ethan Liu (Robotics Lab) and Dr. Anjali Verma (Embedded Systems).\n" +
                    "• Recommended Peers: Team up with Sophia Kim for Computer Vision perception models.\n" +
                    "• Opportunities: High hackathon potential and eligible for BMSCE Innovation & Incubation Center funding."

            return GenieAnswer(text = text, decisionAnalysis = decision, matchedProfiles = profiles)
        }

        // 3. Scenario: Cybersecurity vs Full Stack / Security
        if (q.contains("cyber") || q.contains("security")) {
            val profiles = listOf(
                ProfileMatch(
                    name = "Dr. Sameer Iyer",
                    role = "Faculty • Computer Science",
                    matchReasonTitle = "Relevant Faculty Mentor",
                    matchReasonText = "Cybersecurity, Network Security, Hardware Security & IoT Systems",
                    tags = listOf("Cybersecurity", "Network Security", "Software Engineering")
                ),
                ProfileMatch(
                    name = "Dr. Harsh Mehta",
                    role = "Faculty • Electronics & Communication",
                    matchReasonTitle = "Hardware Defense & FPGA",
                    matchReasonText = "FPGA Acceleration, Cybersecurity, Cloud Infrastructure",
                    tags = listOf("Cybersecurity", "Cloud Computing", "FPGA")
                ),
                ProfileMatch(
                    name = "Akash Joshi",
                    role = "IT • 3rd Year",
                    matchReasonTitle = "Peer Collaborator",
                    matchReasonText = "Penetration Testing, Linux Kernel, Wireshark, Zero-Trust",
                    tags = listOf("Cybersecurity", "Linux", "Python")
                )
            )

            val decision = DecisionAnalysis(
                skillCoverage = "88% (4 / 5 Skills Covered)",
                skillCoveragePercent = 88,
                projectFeasibility = "89% (High Chance)",
                projectFeasibilityPercent = 89,
                missingSkill = "Cloud Security / DevOps",
                relevantFaculty = "Dr. Sameer Iyer (Cybersecurity • CS)",
                relatedResearch = "3 Papers match your project idea",
                potentialOutcomes = "Hackathon (High) • Research Project (High) • Startup Idea (Good)",
                suggestedProjectTitle = "Zero-Trust Campus Security & Vulnerability Mesh",
                suggestedProjectDesc = "Automated threat intelligence and zero-trust authentication framework securing campus services."
            )

            val text = "• Demand: Cybersecurity roles offer ~25% higher placement packages (Cisco, Palo Alto, AWS).\n" +
                    "• Mentors: Active security research under Dr. Sameer Iyer and Dr. Harsh Mehta.\n" +
                    "• Recommended Peer: Akash Joshi for penetration testing and network defence."

            return GenieAnswer(text = text, decisionAnalysis = decision, matchedProfiles = profiles)
        }

        // 4. Scenario: VLSI / Chip Design
        if (q.contains("vlsi") || q.contains("chip") || q.contains("fpga") || q.contains("hardware")) {
            val profiles = listOf(
                ProfileMatch(
                    name = "Dr. Arjun Menon",
                    role = "Faculty • VLSI Design",
                    matchReasonTitle = "VLSI & Edge Hardware Lead",
                    matchReasonText = "IoT Systems, Embedded Systems, Hardware Security, Edge Computing",
                    tags = listOf("Edge Computing", "Hardware Security", "VLSI")
                ),
                ProfileMatch(
                    name = "Dr. Rahul Verma",
                    role = "Faculty • VLSI Design",
                    matchReasonTitle = "Chip Architecture Mentor",
                    matchReasonText = "Cybersecurity, Embedded Systems, SoC Design",
                    tags = listOf("VLSI Design", "Embedded Systems")
                )
            )

            val decision = DecisionAnalysis(
                skillCoverage = "86% (4 / 5 Skills Covered)",
                skillCoveragePercent = 86,
                projectFeasibility = "88% (High Chance)",
                projectFeasibilityPercent = 88,
                missingSkill = "FPGA Timing Closure / Verilog Verification",
                relevantFaculty = "Dr. Arjun Menon (VLSI Design • Edge)",
                relatedResearch = "3 Papers match your project idea",
                potentialOutcomes = "Hackathon (Good) • Research Project (High) • Startup Idea (Moderate)",
                suggestedProjectTitle = "Custom RISC-V Neural Accelerator on FPGA",
                suggestedProjectDesc = "Energy-efficient hardware accelerator for edge intelligence inference on custom RISC-V core."
            )

            val text = "• Value: High semiconductor placement demand (Intel, Qualcomm, Texas Instruments).\n" +
                    "• Mentors: Dr. Arjun Menon provides FPGA synthesis and EDA tools access."

            return GenieAnswer(text = text, decisionAnalysis = decision, matchedProfiles = profiles)
        }

        // 5. Dynamic Query Fallback
        val matchedFaculty = allFaculty.filter { f ->
            val text = "${f.name} ${f.researchInterests} ${f.expertise} ${f.department}".lowercase()
            q.split(" ", ",", "+").any { word -> word.length > 3 && text.contains(word) }
        }.take(2)

        val matchedStudents = allStudents.filter { s ->
            val text = "${s.name} ${s.skills} ${s.projects} ${s.department}".lowercase()
            q.split(" ", ",", "+").any { word -> word.length > 3 && text.contains(word) }
        }.take(2)

        val profiles = mutableListOf<ProfileMatch>()
        matchedFaculty.forEach { fac ->
            profiles.add(
                ProfileMatch(
                    name = fac.name ?: "Dr. Sameer Iyer",
                    role = "Faculty • ${fac.department ?: "Computer Science"}",
                    matchReasonTitle = "Relevant Faculty Mentor",
                    matchReasonText = fac.expertise ?: fac.researchInterests ?: "Campus Research Lead",
                    tags = fac.researchInterests?.split(",")?.map { it.trim() }?.take(3) ?: listOf("Research", "AI")
                )
            )
        }
        matchedStudents.forEach { s ->
            profiles.add(
                ProfileMatch(
                    name = s.name ?: "Peer Student",
                    role = "${s.department ?: "CSE"} • ${s.year ?: "3rd"} Year",
                    matchReasonTitle = "Peer Collaborator Match",
                    matchReasonText = s.projects?.takeIf { it.isNotBlank() } ?: s.skills ?: "Specialized student",
                    tags = s.skills?.split(",")?.map { it.trim() }?.take(3) ?: listOf("Student", "Tech")
                )
            )
        }

        if (profiles.isEmpty()) {
            profiles.add(
                ProfileMatch(
                    name = "Dr. Emily Chen",
                    role = "Faculty • Computer Science",
                    matchReasonTitle = "Campus Mentor",
                    matchReasonText = "Edge AI, IoT Systems, Smart Campus Analytics",
                    tags = listOf("Edge AI", "IoT Research", "Mentorship")
                )
            )
            profiles.add(
                ProfileMatch(
                    name = "Aarav Sharma",
                    role = "ECE • 3rd Year",
                    matchReasonTitle = "Peer Collaborator",
                    matchReasonText = "IoT Protocols, Microcontrollers, Embedded Hardware",
                    tags = listOf("IoT", "Hardware", "Sensors")
                )
            )
        }

        val topFacultyName = profiles.firstOrNull { it.role.startsWith("Faculty") }?.name ?: "Dr. Emily Chen"

        val decision = DecisionAnalysis(
            skillCoverage = "90% (4 / 5 Skills Covered)",
            skillCoveragePercent = 90,
            projectFeasibility = "86% (High Chance)",
            projectFeasibilityPercent = 86,
            missingSkill = "Mobile / Cloud Deployment",
            relevantFaculty = "$topFacultyName (Campus Research)",
            relatedResearch = "2 Papers match your project idea",
            potentialOutcomes = "Hackathon (High) • Research Project (High) • Startup Idea (Good)",
            suggestedProjectTitle = "Intelligent Campus Synergy Pipeline",
            suggestedProjectDesc = "Cross-domain collaborative initiative applying machine intelligence to campus problems."
        )

        val topNames = profiles.joinToString(", ") { it.name }
        val text = "• Feasibility & Synergy (86%): Aligns with active campus initiatives in AI and embedded systems.\n" +
                "• Mentors & Peers: Explore collaboration with $topNames."

        return GenieAnswer(text = text, decisionAnalysis = decision, matchedProfiles = profiles)
    }
}
