package com.example.engine

import com.example.data.model.LanguageType
import com.example.data.model.PlanStep
import com.example.data.model.PlatformType
import com.example.data.model.ProjectFileEntity
import com.example.data.model.ProjectPlan
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object ProjectSynthesizer {

    fun createPlanningPrompt(userPrompt: String): String {
        return """
You are KARAN AI APP BUILDER, an expert software architect.
The user provided this idea (which may be in Hindi, Hinglish, or English):
"$userPrompt"

Analyze the request and return ONLY a valid JSON object with this exact structure:
{
  "title": "Short descriptive app name",
  "platform": "ANDROID" or "WEB",
  "techStack": ["Kotlin", "Jetpack Compose", "Coroutines", "Room", etc.],
  "description": "2-3 sentence overview of the architecture and UX in English or Hinglish",
  "steps": [
    {"id": "step_1", "title": "Step title", "description": "Details of what will be implemented"}
  ],
  "estimatedFiles": [
    "app/src/main/AndroidManifest.xml",
    "app/src/main/java/com/app/MainActivity.kt",
    "app/src/main/java/com/app/ui/HomeScreen.kt",
    "app/build.gradle.kts"
  ]
}
Do not wrap in markdown or commentary, output valid JSON only.
        """.trimIndent()
    }

    fun createCodeGenerationPrompt(userPrompt: String, plan: ProjectPlan): String {
        val filesList = plan.estimatedFiles.joinToString(", ")
        return """
You are KARAN AI APP BUILDER, an elite autonomous software engineer.
User Request: "$userPrompt"
Project Plan: ${plan.title} (${plan.platform})
Target Files to Generate: $filesList

Generate the COMPLETE source code for ALL required files. Every file must be complete with real, working code (NO placeholders, NO '// TODO', NO mock comments).
Return ONLY a valid JSON array of file objects:
[
  {
    "path": "app/src/main/java/com/app/MainActivity.kt",
    "content": "...complete valid source code..."
  },
  ...
]
Do not output any surrounding text or markdown blocks, only the JSON array.
        """.trimIndent()
    }

    fun createModificationPrompt(
        modificationRequest: String,
        existingFiles: List<ProjectFileEntity>
    ): String {
        val fileSummaries = existingFiles.joinToString("\n") { file ->
            "--- File: ${file.path} ---\n${file.content.take(400)}..."
        }
        return """
You are KARAN AI APP BUILDER. The user wants to modify an existing project.
User Modification Request (in Hindi/Hinglish/English): "$modificationRequest"

Existing Project Files:
$fileSummaries

Analyze which files need changes. Return ONLY a JSON array containing the full updated code for the files that changed:
[
  {
    "path": "path/to/modified_or_new_file",
    "content": "...complete updated content..."
  }
]
Do NOT regenerate unchanged files. Return ONLY the JSON array.
        """.trimIndent()
    }

    fun parsePlan(jsonString: String, defaultName: String = "AI Generated App"): ProjectPlan {
        try {
            val cleanJson = cleanJsonString(jsonString)
            val obj = JSONObject(cleanJson)
            val title = obj.optString("title", defaultName)
            val platformStr = obj.optString("platform", "ANDROID")
            val platform = if (platformStr.contains("WEB", ignoreCase = true)) PlatformType.WEB else PlatformType.ANDROID
            val description = obj.optString("description", "Generated application by KARAN AI")

            val techStackList = mutableListOf<String>()
            val techArr = obj.optJSONArray("techStack")
            if (techArr != null) {
                for (i in 0 until techArr.length()) {
                    techStackList.add(techArr.getString(i))
                }
            }

            val stepsList = mutableListOf<PlanStep>()
            val stepsArr = obj.optJSONArray("steps")
            if (stepsArr != null) {
                for (i in 0 until stepsArr.length()) {
                    val sObj = stepsArr.getJSONObject(i)
                    stepsList.add(
                        PlanStep(
                            id = sObj.optString("id", "step_$i"),
                            title = sObj.optString("title", "Phase ${i + 1}"),
                            description = sObj.optString("description", "")
                        )
                    )
                }
            }

            val filesList = mutableListOf<String>()
            val filesArr = obj.optJSONArray("estimatedFiles")
            if (filesArr != null) {
                for (i in 0 until filesArr.length()) {
                    filesList.add(filesArr.getString(i))
                }
            }

            return ProjectPlan(
                title = title,
                platform = platform,
                techStack = if (techStackList.isNotEmpty()) techStackList else listOf("Kotlin", "Jetpack Compose", "Material 3"),
                description = description,
                steps = if (stepsList.isNotEmpty()) stepsList else defaultSteps(platform),
                estimatedFiles = if (filesList.isNotEmpty()) filesList else defaultFiles(platform)
            )
        } catch (e: Exception) {
            return generateLocalPlan(defaultName, PlatformType.ANDROID)
        }
    }

    fun parseGeneratedFiles(
        jsonString: String,
        projectId: String
    ): List<ProjectFileEntity> {
        val result = mutableListOf<ProjectFileEntity>()
        try {
            val cleanJson = cleanJsonString(jsonString)
            val array = JSONArray(cleanJson)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val path = item.optString("path", "file_$i.kt")
                val content = item.optString("content", "")
                val name = path.substringAfterLast('/')
                val ext = name.substringAfterLast('.', "")
                result.add(
                    ProjectFileEntity(
                        id = UUID.randomUUID().toString(),
                        projectId = projectId,
                        path = path,
                        name = name,
                        extension = ext,
                        content = content,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        return result
    }

    fun cleanJsonString(input: String): String {
        var trimmed = input.trim()
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.removePrefix("```json")
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.removePrefix("```")
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.removeSuffix("```")
        }
        return trimmed.trim()
    }

    fun defaultSteps(platform: PlatformType): List<PlanStep> {
        return listOf(
            PlanStep("step_1", "Architecture & Dependency Setup", "Configuring project structure, manifest, and build dependencies", true),
            PlanStep("step_2", "Data & Logic Layer", "Creating models, storage repositories, and state controllers"),
            PlanStep("step_3", "User Interface & Navigation", "Building modern responsive screens with Material 3"),
            PlanStep("step_4", "Hardware & Audio Integration", "Wiring up microphone, speech synthesis, and network connectors"),
            PlanStep("step_5", "Validation & Quality Check", "Conducting static syntax analysis and build verification")
        )
    }

    fun defaultFiles(platform: PlatformType): List<String> {
        return if (platform == PlatformType.ANDROID) {
            listOf(
                "app/src/main/AndroidManifest.xml",
                "app/src/main/java/com/app/MainActivity.kt",
                "app/src/main/java/com/app/ui/HomeScreen.kt",
                "app/src/main/java/com/app/ui/theme/Theme.kt",
                "app/src/main/java/com/app/data/AppModel.kt",
                "app/build.gradle.kts"
            )
        } else {
            listOf(
                "package.json",
                "src/index.html",
                "src/App.tsx",
                "src/main.tsx",
                "src/styles.css"
            )
        }
    }

    fun generateLocalPlan(idea: String, platform: PlatformType): ProjectPlan {
        val title = if (idea.isNotBlank()) idea.take(30) else "Karan Assistant App"
        return ProjectPlan(
            title = title,
            platform = platform,
            techStack = listOf("Kotlin", "Jetpack Compose", "Coroutines", "Room", "Material 3"),
            description = "High-performance native application engineered by KARAN AI with modern UI, robust error handling, and audio capabilities.",
            steps = defaultSteps(platform),
            estimatedFiles = defaultFiles(platform)
        )
    }
}
