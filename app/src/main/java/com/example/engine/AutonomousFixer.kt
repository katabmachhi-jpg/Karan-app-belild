package com.example.engine

import com.example.data.model.ProjectFileEntity
import com.example.data.model.RepairAttempt
import com.example.data.model.ValidationError
import com.example.validator.CodeValidator

class AutonomousFixer(
    private val geminiService: GeminiService
) {
    suspend fun attemptRepair(
        errors: List<ValidationError>,
        files: List<ProjectFileEntity>,
        currentAttempt: Int
    ): Pair<List<ProjectFileEntity>, RepairAttempt> {
        val fixedFiles = files.toMutableList()
        val repairedPaths = mutableListOf<String>()

        for (err in errors) {
            val fileIdx = fixedFiles.indexOfFirst { it.path == err.filePath }
            if (fileIdx != -1) {
                val file = fixedFiles[fileIdx]
                val repairedContent = applyTargetedFix(file.content, err)
                if (repairedContent != file.content) {
                    fixedFiles[fileIdx] = file.copy(
                        content = repairedContent,
                        isModified = true,
                        updatedAt = System.currentTimeMillis()
                    )
                    repairedPaths.add(file.path)
                }
            }
        }

        // Check if there are still remaining errors and if Gemini can help repair
        val remainingErrors = CodeValidator.validateProjectFiles(fixedFiles)
        var status = if (remainingErrors.isEmpty()) "SUCCESS_FIXED_ALL" else "PARTIALLY_REPAIRED"

        if (remainingErrors.isNotEmpty() && currentAttempt <= 3) {
            // Attempt targeted Gemini prompt for complex errors
            for (err in remainingErrors.take(2)) {
                val fileIdx = fixedFiles.indexOfFirst { it.path == err.filePath }
                if (fileIdx != -1) {
                    val file = fixedFiles[fileIdx]
                    val prompt = """
Fix the following error in this source file:
Error: ${err.message} at line ${err.line}
File Path: ${file.path}
Current Content:
${file.content}

Return ONLY the complete fixed file content with no markdown or explanations.
                    """.trimIndent()
                    val result = geminiService.generateContent(prompt)
                    result.onSuccess { fixedCode ->
                        val cleanCode = ProjectSynthesizer.cleanJsonString(fixedCode)
                        if (cleanCode.isNotBlank() && cleanCode.length > 20) {
                            fixedFiles[fileIdx] = file.copy(
                                content = cleanCode,
                                isModified = true,
                                updatedAt = System.currentTimeMillis()
                            )
                            repairedPaths.add(file.path)
                        }
                    }
                }
            }
            val finalErrors = CodeValidator.validateProjectFiles(fixedFiles)
            status = if (finalErrors.isEmpty()) "SUCCESS_FIXED_ALL" else "FAILED_AFTER_ATTEMPT"
        }

        val attempt = RepairAttempt(
            attemptNumber = currentAttempt,
            errorSummary = errors.joinToString("; ") { "${it.filePath}: ${it.message}" },
            fixedFiles = repairedPaths.distinct(),
            status = status
        )

        return Pair(fixedFiles, attempt)
    }

    private fun applyTargetedFix(content: String, error: ValidationError): String {
        var updated = content

        // Fix missing imports
        if (error.message.contains("Missing import for @Composable")) {
            updated = insertImport(updated, "androidx.compose.runtime.Composable")
        }
        if (error.message.contains("Missing import for remember")) {
            updated = insertImport(updated, "androidx.compose.runtime.remember")
        }
        if (error.message.contains("Missing import for mutableStateOf")) {
            updated = insertImport(updated, "androidx.compose.runtime.mutableStateOf")
        }

        // Fix unbalanced braces
        if (error.message.contains("Unmatched curly braces")) {
            val countOpen = updated.count { it == '{' }
            val countClose = updated.count { it == '}' }
            if (countOpen > countClose) {
                updated += "\n" + "}".repeat(countOpen - countClose) + "\n"
            }
        }

        return updated
    }

    private fun insertImport(code: String, importPkg: String): String {
        val importLine = "import $importPkg\n"
        if (code.contains(importLine.trim())) return code

        val packageIdx = code.indexOf("package ")
        if (packageIdx != -1) {
            val endOfPackageLine = code.indexOf('\n', packageIdx)
            if (endOfPackageLine != -1) {
                return code.substring(0, endOfPackageLine + 1) + "\n" + importLine + code.substring(endOfPackageLine + 1)
            }
        }
        return importLine + "\n" + code
    }
}
