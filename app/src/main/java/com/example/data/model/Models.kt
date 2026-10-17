package com.example.data.model

enum class PlatformType {
    ANDROID,
    WEB,
    HYBRID
}

enum class LanguageType {
    KOTLIN_COMPOSE,
    REACT_TS,
    HTML_JS
}

enum class ProjectStatus {
    IDLE,
    PLANNING,
    GENERATING,
    VALIDATING,
    READY,
    BUILDING,
    BUILT,
    ERROR
}

data class PlanStep(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val isCurrent: Boolean = false
)

data class ProjectPlan(
    val title: String,
    val platform: PlatformType,
    val techStack: List<String>,
    val description: String,
    val steps: List<PlanStep>,
    val estimatedFiles: List<String>
)

enum class LogLevel {
    INFO,
    CMD,
    WARN,
    ERROR,
    SUCCESS
}

data class BuildLog(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val message: String,
    val details: String? = null
)

data class ValidationError(
    val filePath: String,
    val line: Int = 1,
    val message: String,
    val severity: String = "ERROR",
    val fixSuggestion: String? = null
)

data class RepairAttempt(
    val attemptNumber: Int,
    val errorSummary: String,
    val fixedFiles: List<String>,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)
