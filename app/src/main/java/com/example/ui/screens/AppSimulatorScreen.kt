package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlatformType
import com.example.ui.theme.*
import com.example.ui.viewmodel.BuilderViewModel

@Composable
fun AppSimulatorScreen(
    viewModel: BuilderViewModel,
    modifier: Modifier = Modifier
) {
    val activeProject by viewModel.activeProject.collectAsState()
    val isWeb = activeProject?.platform == PlatformType.WEB.name

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Device Frame Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isWeb) Icons.Default.Language else Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isWeb) "Interactive Web Sandbox Preview" else "Interactive Android Device Preview",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Badge(containerColor = CyberVioletContainer) {
                Text("LIVE RUNNING", color = CyberCyan, fontSize = 9.sp)
            }
        }

        // Mock Phone Frame
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(3.dp, SurfaceBorder),
            modifier = Modifier
                .testTag("app_simulator_preview")
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Phone Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0B0F19))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("12:00", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                        Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                    }
                }

                // Inner App Screen
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (isWeb) {
                        WebPreviewMock()
                    } else {
                        VoiceAssistantSimulator(viewModel)
                    }
                }

                // Phone Home Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0B0F19))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF334155))
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceAssistantSimulator(viewModel: BuilderViewModel) {
    var isListening by remember { mutableStateOf(false) }
    var responses by remember {
        mutableStateOf(
            listOf(
                "नमस्ते! मैं आपका करण एआई सहायक हूँ। पूछिए मैं आपकी क्या मदद कर सकता हूँ?",
                "Hello! Tap the mic button to speak in Hindi or English."
            )
        )
    }

    val pulseScale by animateFloatAsState(
        targetValue = if (isListening) 1.2f else 1.0f,
        animationSpec = if (isListening) infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ) else spring()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = CyberCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Karan Voice AI",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Message List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(responses) { resp ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = resp,
                        color = Color(0xFFF1F5F9),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big Mic Button with glow
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .scale(pulseScale)
                .background(
                    brush = Brush.radialGradient(listOf(CyberViolet, CyberVioletDark)),
                    shape = CircleShape
                )
        ) {
            IconButton(
                onClick = {
                    isListening = !isListening
                    if (isListening) {
                        responses = responses + "सुन रहा हूँ... (Listening in Hindi/English...)"
                        viewModel.speechManager.speak("सुन रहा हूँ, बताइए क्या काम है?", isHindi = true)
                    } else {
                        responses = responses + "उत्तर: आपका कार्य पूरा हो गया है। AI मॉडल से कनेक्टेड है!"
                    }
                },
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Simulated Mic",
                    tint = if (isListening) CyberCyan else Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (isListening) "बोलिए, सुन रहा हूँ... (Listening)" else "माइक दबाकर बोलें (Tap to test)",
            color = if (isListening) CyberCyan else TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
fun WebPreviewMock() {
    var items by remember {
        mutableStateOf(listOf("Build Android APK Pipeline", "Hindi Speech Recognition", "React Dashboard UI"))
    }
    var newText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("⚡ Web App Dashboard", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("React + TypeScript Component Preview", color = TextSecondary, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = newText,
                onValueChange = { newText = it },
                placeholder = { Text("New task...", fontSize = 12.sp) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = {
                    if (newText.isNotBlank()) {
                        items = items + newText
                        newText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberViolet),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Add", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { item ->
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item, color = TextPrimary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
