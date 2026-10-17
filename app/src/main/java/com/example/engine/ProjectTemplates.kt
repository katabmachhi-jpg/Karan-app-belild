package com.example.engine

import com.example.data.model.ProjectFileEntity
import java.util.UUID

object ProjectTemplates {

    fun getVoiceAssistantTemplate(projectId: String): List<ProjectFileEntity> {
        return listOf(
            ProjectFileEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                path = "app/src/main/AndroidManifest.xml",
                name = "AndroidManifest.xml",
                extension = "xml",
                content = """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="Karan Voice AI"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
                """.trimIndent()
            ),
            ProjectFileEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                path = "app/src/main/java/com/assistant/MainActivity.kt",
                name = "MainActivity.kt",
                extension = "kt",
                content = """
package com.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.assistant.ui.VoiceAssistantScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VoiceAssistantScreen()
                }
            }
        }
    }
}
                """.trimIndent()
            ),
            ProjectFileEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                path = "app/src/main/java/com/assistant/ui/VoiceAssistantScreen.kt",
                name = "VoiceAssistantScreen.kt",
                extension = "kt",
                content = """
package com.assistant.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VoiceAssistantScreen() {
    var isListening by remember { mutableStateOf(false) }
    var userSpeech by remember { mutableStateOf("") }
    var messages by remember { 
        mutableStateOf(listOf(
            "नमस्ते! मैं आपका करण एआई सहायक हूँ। पूछिए मैं आपकी क्या मदद कर सकता हूँ?",
            "Hello! I am your AI Voice Assistant. Speak or type below!"
        ))
    }

    val pulseScale by animateFloatAsState(
        targetValue = if (isListening) 1.25f else 1.0f,
        animationSpec = if (isListening) infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ) else spring()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "🎙️ करण AI वॉइस असिस्टेंट",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF00F5D4),
            fontSize = 22.sp
        )
        Text(
            text = "Hindi & English AI Voice Companion",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Message List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(14.dp),
                        color = Color(0xFFF1F5F9),
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big Mic Button with glow
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(90.dp)
                .scale(pulseScale)
                .background(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFA855F7), Color(0xFF6B21A8))
                    ),
                    shape = CircleShape
                )
        ) {
            IconButton(
                onClick = {
                    isListening = !isListening
                    if (isListening) {
                        messages = messages + "सुन रहा हूँ... (Listening in Hindi/English...)"
                    } else {
                        messages = messages + "उत्तर: आपका कार्य सफलतापूर्वक निष्पादित हो गया है।"
                    }
                },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Microphone",
                    tint = if (isListening) Color(0xFF00F5D4) else Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (isListening) "बोलिए, मैं सुन रहा हूँ... (Listening)" else "माइक दबाकर बोलें (Tap to Speak)",
            color = if (isListening) Color(0xFF00F5D4) else Color(0xFF94A3B8),
            fontSize = 13.sp
        )
    }
}
                """.trimIndent()
            ),
            ProjectFileEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                path = "app/src/main/java/com/assistant/data/AssistantData.kt",
                name = "AssistantData.kt",
                extension = "kt",
                content = """
package com.assistant.data

data class VoiceCommand(
    val id: String,
    val text: String,
    val language: String, // "hi-IN", "en-US", "hinglish"
    val timestamp: Long = System.currentTimeMillis(),
    val responseText: String
)
                """.trimIndent()
            ),
            ProjectFileEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                path = "app/build.gradle.kts",
                name = "build.gradle.kts",
                extension = "kts",
                content = """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.assistant"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.assistant.voiceai"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
}
                """.trimIndent()
            )
        )
    }

    fun getWebReactTemplate(projectId: String): List<ProjectFileEntity> {
        return listOf(
            ProjectFileEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                path = "package.json",
                name = "package.json",
                extension = "json",
                content = """
{
  "name": "karan-web-app",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "lucide-react": "^0.344.0"
  },
  "devDependencies": {
    "@types/react": "^18.3.3",
    "@types/react-dom": "^18.3.0",
    "@vitejs/plugin-react": "^4.3.1",
    "typescript": "^5.5.3",
    "vite": "^5.4.1"
  }
}
                """.trimIndent()
            ),
            ProjectFileEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                path = "src/App.tsx",
                name = "App.tsx",
                extension = "tsx",
                content = """
import React, { useState } from 'react';

export default function App() {
  const [tasks, setTasks] = useState([
    { id: 1, title: 'Karan AI Architecture Setup', completed: true },
    { id: 2, title: 'Multi-lingual Voice Interface', completed: false },
    { id: 3, title: 'Android APK Build Pipeline', completed: false }
  ]);
  const [input, setInput] = useState('');

  const addTask = () => {
    if (!input.trim()) return;
    setTasks([...tasks, { id: Date.now(), title: input, completed: false }]);
    setInput('');
  };

  return (
    <div style={{ background: '#0B0E17', color: '#F8FAFC', minHeight: '100vh', padding: '30px', fontFamily: 'sans-serif' }}>
      <header style={{ marginBottom: '24px' }}>
        <h1 style={{ color: '#00F5D4', margin: 0 }}>⚡ Karan AI Web Application</h1>
        <p style={{ color: '#94A3B8' }}>Autonomous Web Project Generated with React & TypeScript</p>
      </header>

      <div style={{ background: '#131D33', padding: '20px', borderRadius: '12px', maxWidth: '600px' }}>
        <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
          <input 
            type="text" 
            value={input} 
            onChange={(e) => setInput(e.target.value)} 
            placeholder="Add new item or idea..."
            style={{ flex: 1, padding: '12px', borderRadius: '8px', border: '1px solid #1E293B', background: '#0A0E17', color: '#fff' }}
          />
          <button 
            onClick={addTask}
            style={{ background: '#A855F7', color: '#fff', border: 'none', padding: '12px 20px', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' }}>
            Add
          </button>
        </div>

        <ul style={{ listStyle: 'none', padding: 0 }}>
          {tasks.map(t => (
            <li key={t.id} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 0', borderBottom: '1px solid #1E293B' }}>
              <input 
                type="checkbox" 
                checked={t.completed} 
                onChange={() => setTasks(tasks.map(x => x.id === t.id ? { ...x, completed: !x.completed } : x))} 
              />
              <span style={{ textDecoration: t.completed ? 'line-through' : 'none', color: t.completed ? '#64748B' : '#F8FAFC' }}>
                {t.title}
              </span>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
                """.trimIndent()
            ),
            ProjectFileEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                path = "src/main.tsx",
                name = "main.tsx",
                extension = "tsx",
                content = """
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
                """.trimIndent()
            ),
            ProjectFileEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                path = "index.html",
                name = "index.html",
                extension = "html",
                content = """
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Karan AI Web App</title>
  </head>
  <body style="margin: 0; background: #0B0E17;">
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
                """.trimIndent()
            )
        )
    }
}
