package com.example.validator

import com.example.data.model.ProjectFileEntity
import com.example.data.model.ValidationError

object CodeValidator {

    fun validateProjectFiles(files: List<ProjectFileEntity>): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        var hasManifest = false
        var hasMainActivity = false

        for (file in files) {
            when {
                file.path.endsWith("AndroidManifest.xml") -> {
                    hasManifest = true
                    errors.addAll(validateManifest(file))
                }
                file.path.endsWith(".kt") -> {
                    if (file.name.contains("MainActivity")) hasMainActivity = true
                    errors.addAll(validateKotlinFile(file))
                }
                file.path.endsWith(".kts") -> {
                    errors.addAll(validateGradleFile(file))
                }
                file.path.endsWith(".tsx") || file.path.endsWith(".ts") -> {
                    errors.addAll(validateTypeScriptFile(file))
                }
            }
        }

        // Project level check for Android projects
        val isAndroid = files.any { it.path.startsWith("app/") || it.path.endsWith(".kt") }
        if (isAndroid) {
            if (!hasManifest) {
                errors.add(
                    ValidationError(
                        filePath = "app/src/main/AndroidManifest.xml",
                        line = 1,
                        message = "Missing AndroidManifest.xml configuration",
                        fixSuggestion = "Generate standard AndroidManifest with Launcher activity"
                    )
                )
            }
            if (!hasMainActivity) {
                errors.add(
                    ValidationError(
                        filePath = "app/src/main/java/MainActivity.kt",
                        line = 1,
                        message = "Missing MainActivity entry point",
                        fixSuggestion = "Create MainActivity extending ComponentActivity"
                    )
                )
            }
        }

        return errors
    }

    private fun validateKotlinFile(file: ProjectFileEntity): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val content = file.content

        // 1. Bracket & parenthesis balance check
        var braceCount = 0
        var parenCount = 0
        var lineNum = 1
        for (line in content.lines()) {
            braceCount += line.count { it == '{' } - line.count { it == '}' }
            parenCount += line.count { it == '(' } - line.count { it == ')' }
            lineNum++
        }
        if (braceCount != 0) {
            errors.add(
                ValidationError(
                    filePath = file.path,
                    line = lineNum,
                    message = "Unmatched curly braces { } (imbalance: $braceCount)",
                    fixSuggestion = if (braceCount > 0) "Add missing closing brace '}'" else "Remove extra closing brace"
                )
            )
        }
        if (parenCount != 0) {
            errors.add(
                ValidationError(
                    filePath = file.path,
                    line = lineNum,
                    message = "Unmatched parenthesis ( ) (imbalance: $parenCount)",
                    fixSuggestion = "Verify function call or parameter closing parenthesis"
                )
            )
        }

        // 2. Missing imports check
        if (content.contains("@Composable") && !content.contains("import androidx.compose.runtime.Composable")) {
            errors.add(
                ValidationError(
                    filePath = file.path,
                    line = 1,
                    message = "Missing import for @Composable",
                    fixSuggestion = "Add: import androidx.compose.runtime.Composable"
                )
            )
        }
        if (content.contains("remember {") && !content.contains("import androidx.compose.runtime.remember")) {
            errors.add(
                ValidationError(
                    filePath = file.path,
                    line = 1,
                    message = "Missing import for remember",
                    fixSuggestion = "Add: import androidx.compose.runtime.remember"
                )
            )
        }
        if (content.contains("mutableStateOf(") && !content.contains("import androidx.compose.runtime.mutableStateOf")) {
            errors.add(
                ValidationError(
                    filePath = file.path,
                    line = 1,
                    message = "Missing import for mutableStateOf",
                    fixSuggestion = "Add: import androidx.compose.runtime.mutableStateOf"
                )
            )
        }

        // 3. Package statement check
        if (!content.trim().startsWith("package ") && !content.contains("\npackage ")) {
            errors.add(
                ValidationError(
                    filePath = file.path,
                    line = 1,
                    message = "Missing package declaration",
                    fixSuggestion = "Add appropriate package declaration at top of file"
                )
            )
        }

        return errors
    }

    private fun validateManifest(file: ProjectFileEntity): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val content = file.content
        if (!content.contains("<manifest")) {
            errors.add(ValidationError(file.path, 1, "Missing root <manifest> tag"))
        }
        if (!content.contains("<application")) {
            errors.add(ValidationError(file.path, 1, "Missing <application> tag"))
        }
        if (!content.contains("<activity")) {
            errors.add(ValidationError(file.path, 1, "Missing <activity> entry point"))
        }
        return errors
    }

    private fun validateGradleFile(file: ProjectFileEntity): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val content = file.content
        if (!content.contains("plugins {") && !content.contains("apply plugin:")) {
            errors.add(ValidationError(file.path, 1, "Missing plugins configuration block in gradle file"))
        }
        return errors
    }

    private fun validateTypeScriptFile(file: ProjectFileEntity): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val content = file.content
        var braceCount = 0
        for (ch in content) {
            if (ch == '{') braceCount++
            if (ch == '}') braceCount--
        }
        if (braceCount != 0) {
            errors.add(ValidationError(file.path, 1, "Unbalanced braces { } in TypeScript file (diff: $braceCount)"))
        }
        return errors
    }
}
