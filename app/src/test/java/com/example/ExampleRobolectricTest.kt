package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PlatformType
import com.example.data.model.ProjectFileEntity
import com.example.engine.ProjectSynthesizer
import com.example.validator.CodeValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("KARAN AI APP BUILDER", appName)
  }

  @Test
  fun `validate project files detects missing manifest`() {
    val files = listOf(
      ProjectFileEntity(
        id = "1",
        projectId = "p1",
        path = "app/src/main/java/MainActivity.kt",
        name = "MainActivity.kt",
        extension = "kt",
        content = "package com.example\nclass MainActivity"
      )
    )
    val errors = CodeValidator.validateProjectFiles(files)
    assertTrue(errors.any { it.message.contains("Missing AndroidManifest.xml") })
  }

  @Test
  fun `project synthesizer parses valid plan`() {
    val rawJson = """
      {
        "title": "Voice Assistant",
        "platform": "ANDROID",
        "techStack": ["Kotlin", "Compose"],
        "description": "Smart Voice Assistant in Hindi",
        "steps": [],
        "estimatedFiles": ["MainActivity.kt"]
      }
    """.trimIndent()
    val plan = ProjectSynthesizer.parsePlan(rawJson)
    assertEquals("Voice Assistant", plan.title)
    assertEquals(PlatformType.ANDROID, plan.platform)
  }
}
