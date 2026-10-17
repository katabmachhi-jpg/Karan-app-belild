package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val platform: String = PlatformType.ANDROID.name,
    val language: String = LanguageType.KOTLIN_COMPOSE.name,
    val status: String = ProjectStatus.READY.name,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val repairCount: Int = 0,
    val apkBuilt: Boolean = false,
    val planSummaryJson: String? = null
)

@Entity(tableName = "project_files")
data class ProjectFileEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val path: String,
    val name: String,
    val extension: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val isModified: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val sender: String, // "USER", "KARAN_AI", "SYSTEM", "TOOL"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val audioTranscription: String? = null,
    val hasPlan: Boolean = false,
    val rawPlanJson: String? = null
)

@Entity(tableName = "build_runs")
data class BuildRunEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val status: String, // "SUCCESS", "FAILED", "BUILDING", "REPAIRED"
    val timestamp: Long = System.currentTimeMillis(),
    val logsJson: String = "",
    val apkFileName: String? = null,
    val apkFileSize: Long? = null,
    val repairAttemptsCount: Int = 0
)
