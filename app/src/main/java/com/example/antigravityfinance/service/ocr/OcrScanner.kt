package com.example.antigravityfinance.service.ocr

import android.graphics.Bitmap
import com.example.antigravityfinance.data.model.Transaction
import com.example.antigravityfinance.data.model.TransactionCategory
import com.example.antigravityfinance.data.model.TransactionStatus
import com.example.antigravityfinance.service.sms.AutoCategorizer
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.TimeUnit

object OcrScanner {

    // Mock receipt templates for demonstration
    enum class MockReceiptType(
        val merchant: String,
        val amount: Double,
        val category: String,
        val account: String,
        val notes: String
    ) {
        STARBUCKS("Starbucks Coffee", 450.00, "FOOD", "Credit Card", "Mocha and Croissant"),
        BLINKIT("Blinkit Delivery", 1250.0, "LIVELIHOOD", "Bank Account", "Weekly vegetables and dairy"),
        UBER("Uber India", 380.0, "TRAVEL", "Cash", "Ride to office"),
        RELIANCE_DIGITAL("Reliance Digital", 8999.0, "SHOPPING", "Bank Account", "Bluetooth headphones purchase")
    }

    suspend fun scanReceiptMock(type: MockReceiptType): Transaction {
        delay(2000) // Simulate processing time
        return Transaction(
            amount = type.amount,
            merchant = type.merchant,
            date = System.currentTimeMillis(),
            category = type.category,
            notes = type.notes,
            account = type.account,
            status = TransactionStatus.CONFIRMED,
            isIncome = false,
            isRecurring = false,
            detectedFromSms = false
        )
    }

    suspend fun scanReceiptReal(bitmap: Bitmap, apiKey: String): List<Transaction> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val outputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val imageBytes = outputStream.toByteArray()
            val base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)

            val prompt = """
                Analyze this receipt or transaction screenshot. It may contain one or multiple transaction records.
                Detect all records. For each transaction record, extract:
                1. Total Amount (as a floating point number)
                2. Merchant/Store Name (as string)
                3. Date of transaction (if found, format as YYYY-MM-DD, otherwise return current date)
                4. Suggested Category (one of: FOOD, SHOPPING, LIVELIHOOD, COMPULSORY, TRAVEL, INVESTMENT, OTHERS)
                5. Brief note summarizing items or transaction.
                
                Respond ONLY with a valid JSON array of objects matching this schema:
                [
                  {
                    "amount": 0.0,
                    "merchant": "Name",
                    "date": "2026-06-13",
                    "category": "FOOD",
                    "notes": "Croissant and coffee"
                  }
                ]
            """.trimIndent()

            val jsonRequest = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        })
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonRequest.toString().toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    throw Exception("Gemini OCR API failed: ${response.code} $errBody")
                }
                
                val responseBody = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.getJSONArray("candidates")
                if (candidates.length() == 0) return@withContext emptyList<Transaction>()
                val contentObj = candidates.getJSONObject(0).getJSONObject("content")
                val parts = contentObj.getJSONArray("parts")
                if (parts.length() == 0) return@withContext emptyList<Transaction>()
                
                val jsonText = parts.getJSONObject(0).getString("text").trim()
                
                val cleanedJson = if (jsonText.startsWith("```json")) {
                    jsonText.substringAfter("```json").substringBefore("```").trim()
                } else if (jsonText.startsWith("```")) {
                    jsonText.substringAfter("```").substringBefore("```").trim()
                } else {
                    jsonText
                }

                val jsonArray = org.json.JSONArray(cleanedJson)
                val list = mutableListOf<Transaction>()
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val amount = json.optDouble("amount", 0.0)
                    val merchant = json.optString("merchant", "Unknown Merchant")
                    val categoryStr = json.optString("category", "OTHERS")
                    val notes = json.optString("notes", "Scanned Receipt")
                    val dateStr = json.optString("date", "")
                    
                    val dateMs = if (dateStr.isNotEmpty()) {
                        try {
                            val partsDate = dateStr.split("-")
                            val cal = Calendar.getInstance()
                            cal.set(partsDate[0].toInt(), partsDate[1].toInt() - 1, partsDate[2].toInt())
                            cal.timeInMillis
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }
                    } else {
                        System.currentTimeMillis()
                    }

                    list.add(
                        Transaction(
                            amount = amount,
                            merchant = merchant,
                            date = dateMs,
                            category = categoryStr,
                            notes = notes,
                            account = "Scanned Receipt",
                            status = TransactionStatus.CONFIRMED,
                            isIncome = false,
                            isRecurring = false,
                            detectedFromSms = false
                        )
                    )
                }
                list
            }
        } catch (e: Exception) {
            android.util.Log.e("OcrScanner", "Gemini OCR failed", e)
            throw e
        }
    }
}
