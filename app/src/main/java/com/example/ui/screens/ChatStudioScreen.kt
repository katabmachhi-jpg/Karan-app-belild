package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessageEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.BuilderViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatStudioScreen(
    viewModel: BuilderViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isListening by viewModel.speechManager.isListening.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val speechLang by viewModel.speechLanguage.collectAsState()
    val activeProject by viewModel.activeProject.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "🎙️ Hindi Voice Assistant App" to "Make an Android AI voice assistant with a microphone, Hindi voice responses, a modern interface, and Gemini AI integration.",
        "📊 Crypto Portfolio Tracker" to "Make an Android crypto portfolio tracker with live price charts, dark cyberpunk UI, and Room database.",
        "🏋️ Gym & Workout Logger" to "Make a fitness workout logger with exercises, sets, reps, timer, and room persistence.",
        "🌐 React & TypeScript Web App" to "Make a modern responsive React TypeScript web application with task dashboard and dark theme.",
        "🎨 Change Theme to Neon Cyan" to "Change the app theme to electric cyan and black with glowing cards."
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Active Project Info Banner
        Surface(
            color = SurfaceDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isGenerating) StatusWarning else CyberCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activeProject?.name ?: "KARAN AI",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Badge(containerColor = CyberVioletContainer) {
                        Text(
                            text = activeProject?.platform ?: "ANDROID",
                            color = CyberCyan,
                            fontSize = 10.sp
                        )
                    }
                }

                // Language toggle pill
                AssistChip(
                    onClick = {
                        val next = if (speechLang == "hi-IN") "en-US" else "hi-IN"
                        viewModel.setSpeechLanguage(next)
                    },
                    label = {
                        Text(
                            text = if (speechLang == "hi-IN") "🇮🇳 हिंदी" else "🇺🇸 English",
                            fontSize = 11.sp,
                            color = CyberCyan
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = SurfaceCard
                    ),
                    modifier = Modifier.height(30.dp)
                )
            }
        }

        // Chat message timeline
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg = msg)
            }

            if (isGenerating) {
                item {
                    GeneratingIndicator()
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { (label, prompt) ->
                SuggestionChip(
                    onClick = {
                        inputText = prompt
                    },
                    label = {
                        Text(text = label, fontSize = 11.sp, color = TextPrimary)
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = SurfaceCard
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = SurfaceBorder
                    )
                )
            }
        }

        // Bottom Input Area
        Surface(
            color = SurfaceDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Microphone button
                val pulseScale by animateFloatAsState(
                    targetValue = if (isListening) 1.25f else 1.0f,
                    animationSpec = if (isListening) infiniteRepeatable(
                        animation = tween(500),
                        repeatMode = RepeatMode.Reverse
                    ) else spring()
                )

                IconButton(
                    onClick = {
                        if (isListening) viewModel.stopVoiceInput() else viewModel.startVoiceInput()
                    },
                    modifier = Modifier
                        .testTag("mic_button")
                        .size(48.dp)
                        .scale(pulseScale)
                        .background(
                            brush = if (isListening) Brush.radialGradient(listOf(StatusError, CyberViolet))
                            else Brush.linearGradient(listOf(CyberViolet, CyberVioletDark)),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Input Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (isListening) "बोलिए, सुन रहा हूँ..." else "Ask in Hindi, Hinglish, or English...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .testTag("chat_input")
                        .weight(1f)
                        .heightIn(min = 48.dp, max = 120.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isGenerating) {
                            viewModel.sendPrompt(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .testTag("send_button")
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(CyberCyan, CyberCyanDark)),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color(0xFF00382E),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessageEntity) {
    val isUser = msg.sender == "USER"
    val isSystem = msg.sender == "SYSTEM"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            if (!isUser) {
                Icon(
                    imageVector = if (isSystem) Icons.Default.Build else Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = if (isSystem) StatusWarning else CyberCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = when (msg.sender) {
                    "USER" -> "You"
                    "KARAN_AI" -> "KARAN AI"
                    else -> "Build Engine"
                },
                fontSize = 11.sp,
                color = if (isUser) CyberViolet else CyberCyan,
                fontWeight = FontWeight.SemiBold
            )
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            color = if (isUser) CyberVioletContainer else if (isSystem) SurfaceBorder else SurfaceCard,
            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder) else null,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = msg.content,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun GeneratingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = CyberCyan
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Karan AI is thinking, planning, and coding...",
            fontSize = 12.sp,
            color = CyberCyan
        )
    }
}
