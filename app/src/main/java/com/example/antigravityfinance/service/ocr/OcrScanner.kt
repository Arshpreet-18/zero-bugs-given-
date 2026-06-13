package com.example.antigravityfinance.service.ocr

import android.graphics.Bitmap
import com.example.antigravityfinance.data.model.Transaction
import com.example.antigravityfinance.data.model.TransactionCategory
import com.example.antigravityfinance.data.model.TransactionStatus
import com.example.antigravityfinance.service.sms.AutoCategorizer
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.util.Calendar

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

    suspend fun scanReceiptReal(bitmap: Bitmap, apiKey: String): List<Transaction> {
        return try {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )
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

            val response = model.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )
            val jsonText = response.text?.trim() ?: return emptyList()
            // Extract json if wrapped in ```json ... ```
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
                        val parts = dateStr.split("-")
                        val cal = Calendar.getInstance()
                        cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
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
        } catch (e: Exception) {
            android.util.Log.e("OcrScanner", "Gemini OCR failed", e)
            throw e
        }
    }
}
