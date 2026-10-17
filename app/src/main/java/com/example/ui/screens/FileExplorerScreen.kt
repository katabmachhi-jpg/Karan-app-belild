package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectFileEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.BuilderViewModel

@Composable
fun FileExplorerScreen(
    viewModel: BuilderViewModel,
    modifier: Modifier = Modifier
) {
    val files by viewModel.projectFiles.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val editorContent by viewModel.editorContent.collectAsState()

    var showExplorerDrawer by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CodeEditorBg)
    ) {
        // Top Toolbar: File Name & Action Buttons
        Surface(
            color = SurfaceDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // File selector dropdown button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showExplorerDrawer = !showExplorerDrawer }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (showExplorerDrawer) Icons.Default.FolderOpen else Icons.Default.Folder,
                        contentDescription = "Toggle Files",
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedFile?.name ?: "No file selected",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // New file button
                    IconButton(
                        onClick = { viewModel.showAddFileDialog.value = true },
                        modifier = Modifier
                            .testTag("add_file_button")
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                            contentDescription = "New File",
                            tint = CyberCyan
                        )
                    }

                    // Save file button
                    Button(
                        onClick = { viewModel.saveCurrentFile() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberVioletContainer
                        ),
                        modifier = Modifier
                            .testTag("save_file_button")
                            .height(38.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = CyberViolet,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", color = CyberViolet, fontSize = 12.sp)
                    }
                }
            }
        }

        // Horizontal File Tabs Bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(files) { file ->
                val isSelected = file.id == selectedFile?.id
                Surface(
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    color = if (isSelected) CodeEditorBg else SurfaceDark,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)) else null,
                    modifier = Modifier
                        .clickable { viewModel.selectFile(file) }
                        .padding(horizontal = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getFileIcon(file.extension),
                            contentDescription = null,
                            tint = if (isSelected) CyberCyan else TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = file.name,
                            fontSize = 12.sp,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Dropdown Collapsible Project File Explorer List
        if (showExplorerDrawer) {
            Surface(
                color = SurfaceDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .testTag("file_tree")
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    items(files) { file ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectFile(file)
                                    showExplorerDrawer = false
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = getFileIcon(file.extension),
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = file.name,
                                        fontSize = 13.sp,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = file.path,
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteFile(file.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = StatusError,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)
                    }
                }
            }
        }

        // Code Editor View with line numbers
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            if (selectedFile != null) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CodeEditorBg)
                ) {
                    // Line numbers gutter
                    val lineCount = remember(editorContent) {
                        editorContent.lines().size.coerceAtLeast(1)
                    }

                    Column(
                        modifier = Modifier
                            .width(36.dp)
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1..lineCount.coerceAtMost(200)) {
                            Text(
                                text = "$i",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    VerticalDivider(color = SurfaceBorder, thickness = 1.dp)

                    // Text Field Editor
                    OutlinedTextField(
                        value = editorContent,
                        onValueChange = { viewModel.updateEditorContent(it) },
                        modifier = Modifier
                            .testTag("code_editor")
                            .fillMaxSize()
                            .padding(horizontal = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CodeEditorBg,
                            unfocusedContainerColor = CodeEditorBg,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Select a file to preview and edit source code", color = TextSecondary)
                }
            }
        }
    }
}

fun getFileIcon(extension: String) = when (extension.lowercase()) {
    "kt" -> Icons.Default.Code
    "xml" -> Icons.Default.IntegrationInstructions
    "kts" -> Icons.Default.Build
    "tsx", "ts", "js" -> Icons.Default.Javascript
    "json" -> Icons.Default.DataObject
    "html" -> Icons.Default.Html
    else -> Icons.Default.Description
}
