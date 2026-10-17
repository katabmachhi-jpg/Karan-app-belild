package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.BuilderViewModel
import com.example.ui.viewmodel.NavigationTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: BuilderViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val activeProject by viewModel.activeProject.collectAsState()

    val showNewProject by viewModel.showNewProjectDialog.collectAsState()
    val showSettings by viewModel.showSettingsDialog.collectAsState()
    val showAddFile by viewModel.showAddFileDialog.collectAsState()
    val showGitHub by viewModel.showGitHubModal.collectAsState()

    var projectDropdownExpanded by remember { mutableStateOf(false) }

    // Handle system back navigation: return to Studio tab if on a sub-screen
    BackHandler(enabled = currentTab != NavigationTab.STUDIO_CHAT) {
        viewModel.setTab(NavigationTab.STUDIO_CHAT)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { projectDropdownExpanded = true }
                    ) {
                        Column {
                            Text(
                                text = "KARAN AI APP BUILDER",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = activeProject?.name ?: "Select Project",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Project Switcher Dropdown
                        DropdownMenu(
                            expanded = projectDropdownExpanded,
                            onDismissRequest = { projectDropdownExpanded = false },
                            modifier = Modifier.background(SurfaceDark)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = CyberCyan)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("+ Create New Project", color = CyberCyan, fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    projectDropdownExpanded = false
                                    viewModel.showNewProjectDialog.value = true
                                }
                            )
                            HorizontalDivider(color = SurfaceBorder)

                            allProjects.forEach { proj ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = proj.name,
                                                color = if (proj.id == activeProject?.id) CyberViolet else TextPrimary,
                                                fontWeight = if (proj.id == activeProject?.id) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                text = "${proj.platform} • ${proj.status}",
                                                fontSize = 10.sp,
                                                color = TextMuted
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectProject(proj)
                                        projectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.showSettingsDialog.value = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = TextPrimary,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == NavigationTab.STUDIO_CHAT,
                    onClick = { viewModel.setTab(NavigationTab.STUDIO_CHAT) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Studio Chat") },
                    label = { Text("Studio", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyanContainer,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_studio")
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.FILE_EXPLORER,
                    onClick = { viewModel.setTab(NavigationTab.FILE_EXPLORER) },
                    icon = { Icon(Icons.Default.Folder, contentDescription = "Files") },
                    label = { Text("Explorer", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyanContainer,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_explorer")
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.BUILD_PIPELINE,
                    onClick = { viewModel.setTab(NavigationTab.BUILD_PIPELINE) },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Build APK") },
                    label = { Text("Build APK", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyanContainer,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_build")
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.APP_SIMULATOR,
                    onClick = { viewModel.setTab(NavigationTab.APP_SIMULATOR) },
                    icon = { Icon(Icons.Default.PlayCircle, contentDescription = "App Simulator") },
                    label = { Text("Preview", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyanContainer,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_simulator")
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.STUDIO_CHAT -> ChatStudioScreen(viewModel)
                NavigationTab.FILE_EXPLORER -> FileExplorerScreen(viewModel)
                NavigationTab.BUILD_PIPELINE -> BuildPipelineScreen(viewModel)
                NavigationTab.APP_SIMULATOR -> AppSimulatorScreen(viewModel)
            }
        }
    }

    // Dialogs
    if (showSettings) {
        SettingsDialog(viewModel = viewModel, onDismiss = { viewModel.showSettingsDialog.value = false })
    }
    if (showNewProject) {
        NewProjectDialog(viewModel = viewModel, onDismiss = { viewModel.showNewProjectDialog.value = false })
    }
    if (showAddFile) {
        AddFileDialog(viewModel = viewModel, onDismiss = { viewModel.showAddFileDialog.value = false })
    }
    if (showGitHub) {
        GitHubCiModal(onDismiss = { viewModel.showGitHubModal.value = false })
    }
}
