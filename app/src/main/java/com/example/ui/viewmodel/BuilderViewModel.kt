package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.builder.BuildResult
import com.example.builder.ZipExporter
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.ProjectRepository
import com.example.engine.GeminiService
import com.example.voice.SpeechManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

enum class NavigationTab {
    STUDIO_CHAT,
    FILE_EXPLORER,
    BUILD_PIPELINE,
    APP_SIMULATOR
}

class BuilderViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val geminiService = GeminiService()
    val repository = ProjectRepository(application, db, geminiService)
    val speechManager = SpeechManager(application)

    // Active navigation tab
    private val _currentTab = MutableStateFlow(NavigationTab.STUDIO_CHAT)
    val currentTab = _currentTab.asStateFlow()

    // Projects
    val allProjects: StateFlow<List<ProjectEntity>> = repository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeProject = MutableStateFlow<ProjectEntity?>(null)
    val activeProject = _activeProject.asStateFlow()

    // Files for active project
    private val _projectFiles = MutableStateFlow<List<ProjectFileEntity>>(emptyList())
    val projectFiles = _projectFiles.asStateFlow()

    // Active file in editor
    private val _selectedFile = MutableStateFlow<ProjectFileEntity?>(null)
    val selectedFile = _selectedFile.asStateFlow()

    private val _editorContent = MutableStateFlow("")
    val editorContent = _editorContent.asStateFlow()

    // Chat messages
    private val _chatMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    // Build logs
    private val _buildLogs = MutableStateFlow<List<BuildLog>>(emptyList())
    val buildLogs = _buildLogs.asStateFlow()

    private val _isBuilding = MutableStateFlow(false)
    val isBuilding = _isBuilding.asStateFlow()

    private val _lastBuildResult = MutableStateFlow<BuildResult?>(null)
    val lastBuildResult = _lastBuildResult.asStateFlow()

    // Generation states
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    private val _activePlan = MutableStateFlow<ProjectPlan?>(null)
    val activePlan = _activePlan.asStateFlow()

    // Settings
    private val _customApiKey = MutableStateFlow("")
    val customApiKey = _customApiKey.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedModel = _selectedModel.asStateFlow()

    private val _speechLanguage = MutableStateFlow("hi-IN")
    val speechLanguage = _speechLanguage.asStateFlow()

    // Dialog flags
    val showNewProjectDialog = MutableStateFlow(false)
    val showSettingsDialog = MutableStateFlow(false)
    val showAddFileDialog = MutableStateFlow(false)
    val showGitHubModal = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            allProjects.collect { projects ->
                if (projects.isNotEmpty() && _activeProject.value == null) {
                    selectProject(projects.first())
                } else if (projects.isEmpty()) {
                    // Create default first project: Karan Voice AI Assistant
                    createDefaultProject()
                }
            }
        }
    }

    private suspend fun createDefaultProject() {
        val proj = repository.createProject(
            name = "Karan Voice Assistant",
            description = "Android AI voice assistant with microphone, Hindi/English responses, and modern UI",
            platform = PlatformType.ANDROID
        )
        selectProject(proj)
    }

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun selectProject(project: ProjectEntity) {
        _activeProject.value = project
        viewModelScope.launch {
            repository.getFilesForProject(project.id).collect { files ->
                _projectFiles.value = files
                if (_selectedFile.value == null || !files.any { it.id == _selectedFile.value?.id }) {
                    _selectedFile.value = files.firstOrNull()
                    _editorContent.value = files.firstOrNull()?.content ?: ""
                }
            }
        }
        viewModelScope.launch {
            repository.getMessagesForProject(project.id).collect { msgs ->
                _chatMessages.value = msgs
            }
        }
    }

    fun createNewProject(name: String, description: String, platform: PlatformType) {
        viewModelScope.launch {
            val proj = repository.createProject(name, description, platform)
            selectProject(proj)
            showNewProjectDialog.value = false
        }
    }

    fun selectFile(file: ProjectFileEntity) {
        _selectedFile.value = file
        _editorContent.value = file.content
    }

    fun updateEditorContent(newContent: String) {
        _editorContent.value = newContent
    }

    fun saveCurrentFile() {
        val file = _selectedFile.value ?: return
        val updated = file.copy(
            content = _editorContent.value,
            isModified = true,
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.saveFile(updated)
            _selectedFile.value = updated
        }
    }

    fun addNewFile(path: String, content: String) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            repository.addFile(proj.id, path, content)
            showAddFileDialog.value = false
        }
    }

    fun deleteFile(fileId: String) {
        viewModelScope.launch {
            repository.deleteFile(fileId)
        }
    }

    fun sendPrompt(userPrompt: String) {
        val proj = _activeProject.value ?: return
        if (userPrompt.isBlank()) return

        viewModelScope.launch {
            // Add user message
            repository.addMessage(proj.id, "USER", userPrompt)

            _isGenerating.value = true

            // Check if this is a modification to an existing project or a new creation
            val currentFiles = _projectFiles.value
            val isModification = currentFiles.isNotEmpty() && (
                userPrompt.contains("change", ignoreCase = true) ||
                userPrompt.contains("add", ignoreCase = true) ||
                userPrompt.contains("theme", ignoreCase = true) ||
                userPrompt.contains("fix", ignoreCase = true) ||
                userPrompt.contains("karo", ignoreCase = true) ||
                userPrompt.contains("badal", ignoreCase = true)
            )

            if (isModification) {
                repository.addMessage(
                    proj.id,
                    "KARAN_AI",
                    "समीक्षा कर रहा हूँ (Inspecting existing files)... Applying targeted modifications for: '$userPrompt'"
                )
                val modResult = repository.modifyProjectFiles(proj.id, userPrompt)
                if (modResult.isSuccess) {
                    repository.addMessage(
                        proj.id,
                        "KARAN_AI",
                        "सफलतापूर्वक प्रोजेक्ट अपडेट हो गया! (Files updated successfully). You can review the files in the Explorer or run the Build Pipeline."
                    )
                }
            } else {
                // STEP 1: Plan
                repository.addMessage(
                    proj.id,
                    "KARAN_AI",
                    "योजना तैयार की जा रही है... (Generating architectural plan for your app)..."
                )
                val planResult = repository.generateProjectPlan(proj.id, userPrompt)
                if (planResult.isSuccess) {
                    val plan = planResult.getOrThrow()
                    _activePlan.value = plan
                    val planDesc = "🎯 योजना स्वीकृत: ${plan.title}\nArchitecture: ${plan.techStack.joinToString(", ")}\nEstimated Files: ${plan.estimatedFiles.size}\nSteps:\n" +
                            plan.steps.joinToString("\n") { "• ${it.title}: ${it.description}" }

                    repository.addMessage(proj.id, "KARAN_AI", planDesc, hasPlan = true)

                    // Speak out plan title
                    speechManager.speak("ऐप की योजना तैयार है: ${plan.title}", isHindi = true)

                    // STEP 2: Generate multi-file code
                    repository.addMessage(
                        proj.id,
                        "KARAN_AI",
                        "सोर्स कोड फाइलें लिखी जा रही हैं... (Generating multi-file code for ${plan.estimatedFiles.size} components)..."
                    )
                    val filesResult = repository.generateProjectFiles(proj.id, userPrompt, plan)
                    if (filesResult.isSuccess) {
                        repository.addMessage(
                            proj.id,
                            "KARAN_AI",
                            "सभी फाइलें सफलतापूर्वक जनरेट हो गई हैं! ${filesResult.getOrThrow().size} files ready. आप फाइल एक्सप्लोरर में कोड देख सकते हैं या सीधा APK बिल्ड चला सकते हैं।"
                        )
                    }
                }
            }

            _isGenerating.value = false
        }
    }

    fun runBuild() {
        val proj = _activeProject.value ?: return
        if (_isBuilding.value) return

        viewModelScope.launch {
            _isBuilding.value = true
            _buildLogs.value = emptyList()
            setTab(NavigationTab.BUILD_PIPELINE)

            val result = repository.executeBuild(proj.id) { log ->
                _buildLogs.value = _buildLogs.value + log
            }
            _lastBuildResult.value = result
            _isBuilding.value = false

            if (result.isSuccess) {
                speechManager.speak("एपीके बिल्ड सफल रहा!", isHindi = true)
                repository.addMessage(
                    proj.id,
                    "SYSTEM",
                    "बिल्ड सफल रहा! APK Artifact: ${result.apkFileName} (${result.apkFile?.length()?.div(1024) ?: 0} KB) is ready for download."
                )
            } else {
                speechManager.speak("बिल्ड में त्रुटि आई।", isHindi = true)
                repository.addMessage(
                    proj.id,
                    "SYSTEM",
                    "बिल्ड असफल: ${result.errorMessage}. Autonomous repair attempts exhausted."
                )
            }
        }
    }

    fun startVoiceInput() {
        speechManager.startListening(_speechLanguage.value) { recognizedText ->
            if (recognizedText.isNotBlank()) {
                sendPrompt(recognizedText)
            }
        }
    }

    fun stopVoiceInput() {
        speechManager.stopListening()
    }

    fun exportProjectZip(onExported: (File?) -> Unit) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            val file = repository.exportZip(proj.id)
            onExported(file)
        }
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
        geminiService.setCustomApiKey(key)
    }

    fun setSpeechLanguage(lang: String) {
        _speechLanguage.value = lang
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.release()
    }
}
