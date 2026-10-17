package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builder.ZipExporter
import com.example.data.model.BuildLog
import com.example.data.model.LogLevel
import com.example.ui.theme.*
import com.example.ui.viewmodel.BuilderViewModel

@Composable
fun BuildPipelineScreen(
    viewModel: BuilderViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBuilding by viewModel.isBuilding.collectAsState()
    val buildLogs by viewModel.buildLogs.collectAsState()
    val lastResult by viewModel.lastBuildResult.collectAsState()
    val activeProject by viewModel.activeProject.collectAsState()

    val logListState = rememberLazyListState()

    LaunchedEffect(buildLogs.size) {
        if (buildLogs.isNotEmpty()) {
            logListState.animateScrollToItem(buildLogs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(12.dp)
    ) {
        // Build Pipeline Header Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "⚡ APK Build & Repair Engine",
                            style = MaterialTheme.typography.titleMedium,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Multi-stage compiler, auto-repair & APK packaging",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    // Build Status Pill
                    val statusColor = when {
                        isBuilding -> StatusWarning
                        lastResult?.isSuccess == true -> StatusSuccess
                        lastResult?.isSuccess == false -> StatusError
                        else -> TextMuted
                    }
                    val statusText = when {
                        isBuilding -> "BUILDING..."
                        lastResult?.isSuccess == true -> "APK READY"
                        lastResult?.isSuccess == false -> "FAILED"
                        else -> "IDLE"
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusColor.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                    ) {
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Build Stages Progress Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StageStepItem("1. Validate", isComplete = buildLogs.any { it.message.contains("Static checks PASSED") })
                    StageStepItem("2. Repair (≤3)", isComplete = buildLogs.any { it.message.contains("Repair attempt") || it.message.contains("0 syntax") })
                    StageStepItem("3. Compile", isComplete = buildLogs.any { it.message.contains("kspDebugKotlin") })
                    StageStepItem("4. Package", isComplete = lastResult?.isSuccess == true)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: Run Build, Download APK, Export ZIP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.runBuild() },
                        enabled = !isBuilding,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberViolet),
                        modifier = Modifier
                            .testTag("run_build_button")
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        if (isBuilding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Building...", fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Build APK", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (lastResult?.isSuccess == true && lastResult?.apkFile != null) {
                        Button(
                            onClick = {
                                lastResult?.apkFile?.let { file ->
                                    ZipExporter.installApk(context, file)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            modifier = Modifier
                                .testTag("install_apk_button")
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(
                                Icons.Default.InstallMobile,
                                contentDescription = null,
                                tint = Color(0xFF00382E),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Install APK",
                                color = Color(0xFF00382E),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            viewModel.exportProjectZip { zipFile ->
                                if (zipFile != null) {
                                    ZipExporter.shareFile(context, zipFile, "application/zip")
                                } else {
                                    Toast.makeText(context, "Exporting ZIP...", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .testTag("export_zip_button")
                            .size(48.dp)
                            .background(SurfaceCard, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Archive, contentDescription = "Export ZIP", tint = CyberCyan)
                    }

                    IconButton(
                        onClick = { viewModel.showGitHubModal.value = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(SurfaceCard, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = "GitHub CI", tint = CyberViolet)
                    }
                }

                if (lastResult?.isSuccess == true && lastResult?.apkFile != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Android,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = lastResult?.apkFileName ?: "app-debug.apk",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Size: ${(lastResult?.apkFile?.length() ?: 0) / 1024} KB • Ready to install",
                                        fontSize = 11.sp,
                                        color = CyberCyan
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    lastResult?.apkFile?.let { file ->
                                        ZipExporter.shareFile(context, file, "application/vnd.android.package-archive")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberVioletContainer),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = null,
                                    tint = CyberViolet,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download", color = CyberViolet, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Terminal Build Log Console
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CodeEditorBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Console top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyberCyan))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Build Output & Error Diagnostics",
                            fontSize = 11.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "${buildLogs.size} logs",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                // Log entries stream
                LazyColumn(
                    state = logListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (buildLogs.isEmpty()) {
                        item {
                            Text(
                                text = "Press 'Build APK' to run validation, error diagnosis, and packaging...",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else {
                        items(buildLogs) { log ->
                            BuildLogItem(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StageStepItem(title: String, isComplete: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(if (isComplete) StatusSuccess else SurfaceBorder),
            contentAlignment = Alignment.Center
        ) {
            if (isComplete) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 9.sp,
            color = if (isComplete) TextPrimary else TextMuted
        )
    }
}

@Composable
fun BuildLogItem(log: BuildLog) {
    val color = when (log.level) {
        LogLevel.CMD -> CyberCyan
        LogLevel.SUCCESS -> StatusSuccess
        LogLevel.ERROR -> StatusError
        LogLevel.WARN -> StatusWarning
        LogLevel.INFO -> TextPrimary
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = when (log.level) {
                LogLevel.CMD -> "[$] "
                LogLevel.SUCCESS -> "[✔] "
                LogLevel.ERROR -> "[✖] "
                LogLevel.WARN -> "[!] "
                LogLevel.INFO -> "[i] "
            },
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = log.message,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}
