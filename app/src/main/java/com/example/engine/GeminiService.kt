package com.example.engine

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService(
    private var customApiKey: String? = null
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun setCustomApiKey(key: String?) {
        customApiKey = key?.trim()
    }

    fun getEffectiveApiKey(): String {
        val custom = customApiKey
        if (!custom.isNullOrBlank()) return custom
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null,
        model: String = "gemini-3.5-flash"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add your key in Settings or AI Studio secrets.")
            )
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val requestPayload = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val textPart = JSONObject()
        textPart.put("text", prompt)
        partsArray.put(textPart)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        requestPayload.put("contents", contentsArray)

        if (!systemInstruction.isNullOrBlank()) {
            val sysInstructionObj = JSONObject()
            val sysPartsArray = JSONArray()
            val sysTextPart = JSONObject()
            sysTextPart.put("text", systemInstruction)
            sysPartsArray.put(sysTextPart)
            sysInstructionObj.put("parts", sysPartsArray)
            requestPayload.put("systemInstruction", sysInstructionObj)
        }

        // Configuration
        val genConfig = JSONObject()
        genConfig.put("temperature", 0.3)
        genConfig.put("maxOutputTokens", 8192)
        requestPayload.put("generationConfig", genConfig)

        val requestBody = requestPayload.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errMsg = try {
                        val errObj = JSONObject(bodyString).optJSONObject("error")
                        errObj?.optString("message") ?: "HTTP ${response.code}: $bodyString"
                    } catch (e: Exception) {
                        "HTTP ${response.code}: ${response.message}"
                    }
                    Log.e("GeminiService", "API Error: $errMsg")
                    return@withContext Result.failure(Exception(errMsg))
                }

                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        return@withContext Result.success(text)
                    }
                }
                Result.failure(Exception("Empty candidate response from Gemini API"))
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Call failed", e)
            Result.failure(e)
        }
    }
}
