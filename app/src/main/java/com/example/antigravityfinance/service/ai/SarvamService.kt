package com.example.antigravityfinance.service.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL

object SarvamService {

    suspend fun transcribeAudio(file: File, apiKey: String): String? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext null
        val boundary = "Boundary-${System.currentTimeMillis()}"
        val lineEnd = "\r\n"
        val twoHyphens = "--"

        try {
            val url = URL("https://api.sarvam.ai/speech-to-text")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("api-subscription-key", apiKey)
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            conn.doOutput = true
            conn.useCaches = false

            DataOutputStream(conn.outputStream).use { outputStream ->
                // File Parameter
                outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"${file.name}\"$lineEnd")
                outputStream.writeBytes("Content-Type: audio/wav$lineEnd")
                outputStream.writeBytes(lineEnd)

                FileInputStream(file).use { fileInputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
                outputStream.writeBytes(lineEnd)

                // Model Parameter
                outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                outputStream.writeBytes("Content-Disposition: form-data; name=\"model\"$lineEnd")
                outputStream.writeBytes(lineEnd)
                outputStream.writeBytes("saaras:v3")
                outputStream.writeBytes(lineEnd)

                // Language Code Parameter
                outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                outputStream.writeBytes("Content-Disposition: form-data; name=\"language_code\"$lineEnd")
                outputStream.writeBytes(lineEnd)
                outputStream.writeBytes("en-IN")
                outputStream.writeBytes(lineEnd)

                // End of multipart
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
                outputStream.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                Log.d("SarvamService", "Response: $responseText")
                val json = JSONObject(responseText)
                return@withContext json.optString("transcript", null)
            } else {
                val errText = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Log.e("SarvamService", "Failed to transcribe: response code $responseCode, error: $errText")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("SarvamService", "Error in Sarvam STT: ${e.localizedMessage}", e)
            return@withContext null
        }
    }
}
