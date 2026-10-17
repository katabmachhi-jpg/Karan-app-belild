package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builder.GitHubWorkflowGenerator
import com.example.data.model.PlatformType
import com.example.ui.theme.*
import com.example.ui.viewmodel.BuilderViewModel

@Composable
fun SettingsDialog(
    viewModel: BuilderViewModel,
    onDismiss: () -> Unit
) {
    val currentApiKey by viewModel.customApiKey.collectAsState()
    val currentModel by viewModel.selectedModel.collectAsState()
    val speechLang by viewModel.speechLanguage.collectAsState()

    var apiKeyInput by remember { mutableStateOf(currentApiKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = CyberCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Karan AI Settings", color = TextPrimary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Gemini API Key config
                Text(
                    "Gemini API Key (Optional Override):",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    placeholder = { Text("Configured via AI Studio BuildConfig", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Text(
                    "Default key is injected securely via BuildConfig. Add your custom Gemini key here if desired.",
                    fontSize = 10.sp,
                    color = TextMuted
                )

                HorizontalDivider(color = SurfaceBorder)

                // Model Selection
                Text(
                    "AI Model Engine:",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Active: $currentModel (Recommended: gemini-3.5-flash / gemini-3.1-pro-preview)",
                    fontSize = 11.sp,
                    color = CyberCyan
                )

                HorizontalDivider(color = SurfaceBorder)

                // Voice Speech Language
                Text(
                    "Voice Recognition Language:",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = speechLang == "hi-IN",
                        onClick = { viewModel.setSpeechLanguage("hi-IN") },
                        label = { Text("🇮🇳 Hindi (hi-IN)") }
                    )
                    FilterChip(
                        selected = speechLang == "en-US",
                        onClick = { viewModel.setSpeechLanguage("en-US") },
                        label = { Text("🇺🇸 English (en-US)") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.setCustomApiKey(apiKeyInput)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberViolet)
            ) {
                Text("Save Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark
    )
}

@Composable
fun NewProjectDialog(
    viewModel: BuilderViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf(PlatformType.ANDROID) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddBox, contentDescription = null, tint = CyberCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Project", color = TextPrimary)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    placeholder = { Text("e.g. Hindi Voice Assistant") },
                    modifier = Modifier.fillMaxWidth().testTag("new_project_name_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("App Idea / Brief") },
                    placeholder = { Text("e.g. AI voice assistant with microphone...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Text("Target Platform:", fontSize = 12.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedPlatform == PlatformType.ANDROID,
                        onClick = { selectedPlatform = PlatformType.ANDROID },
                        label = { Text("🤖 Android Native") }
                    )
                    FilterChip(
                        selected = selectedPlatform == PlatformType.WEB,
                        onClick = { selectedPlatform = PlatformType.WEB },
                        label = { Text("🌐 Modern Web") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.createNewProject(name, description, selectedPlatform)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                modifier = Modifier.testTag("confirm_create_project_button")
            ) {
                Text("Create", color = Color(0xFF00382E), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark
    )
}

@Composable
fun GitHubCiModal(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val workflowYaml = remember { GitHubWorkflowGenerator.generateWorkflowFile() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudSync, contentDescription = null, tint = CyberViolet)
                Spacer(modifier = Modifier.width(8.dp))
                Text("GitHub Actions CI/CD APK Builder", color = TextPrimary, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "You can build signed APKs in the cloud automatically using GitHub Actions! Save this file to '.github/workflows/build-apk.yml':",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = CodeEditorBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = workflowYaml,
                        fontSize = 10.sp,
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("workflow", workflowYaml))
                    Toast.makeText(context, "Copied workflow YAML to clipboard!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberViolet)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy YAML")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark
    )
}
