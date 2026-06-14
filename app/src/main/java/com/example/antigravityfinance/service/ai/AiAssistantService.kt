package com.example.antigravityfinance.service.ai

import com.example.antigravityfinance.data.model.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

object AiAssistantService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun askAssistant(
        query: String,
        transactions: List<Transaction>,
        budgets: List<Budget>,
        goals: List<SavingsGoal>,
        investments: List<Investment>,
        apiKey: String?,
        setZeroTimestamp: Long,
        currencySymbol: String = "₹"
    ): String {
        val cleanKey = apiKey?.trim() ?: ""
        if (cleanKey.isBlank()) {
            return "Gemini API key is not configured. Please go to Settings and set a valid Gemini API Key."
        }
        
        return askGeminiDirect(query, transactions, budgets, goals, investments, cleanKey, setZeroTimestamp, currencySymbol)
    }

    private suspend fun askGeminiDirect(
        query: String,
        transactions: List<Transaction>,
        budgets: List<Budget>,
        goals: List<SavingsGoal>,
        investments: List<Investment>,
        apiKey: String,
        setZeroTimestamp: Long,
        currencySymbol: String
    ): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            // Format context
            val confirmedTx = transactions.filter { it.status == TransactionStatus.CONFIRMED }
            val txContext = confirmedTx.joinToString("\n") { 
                "${if (it.isIncome) "Credit" else "Debit"}: ${it.amount} at ${it.merchant} on ${Date(it.date)} [Category: ${it.category}]"
            }
            val budgetContext = budgets.joinToString("\n") { 
                "Category: ${it.category}, Limit: ${it.limitAmount}, Spent: ${it.spentAmount}" 
            }
            val goalContext = goals.joinToString("\n") { 
                "Goal: ${it.name}, Target: ${it.targetAmount}, Current: ${it.currentAmount}, Target Date: ${Date(it.deadline)}" 
            }
            val investmentContext = investments.joinToString("\n") { 
                "${it.type}: ${it.name}, Symbol: ${it.symbol}, Invested: ${it.investedAmount}, Value: ${it.currentValuation}" 
            }

            val resetInfo = if (setZeroTimestamp > 0L) {
                "Note: The user has reset the debit/credit start date to ${Date(setZeroTimestamp)}. Any calculations of total balance, total debits, or total credits must only sum transactions dated after ${Date(setZeroTimestamp)}. Discard earlier transactions for sums, but note they still appear in history."
            } else {
                ""
            }

            val systemPrompt = """
                You are a premium AI personal finance assistant named FinKlar.
                Here is the user's financial profile data:
                
                [Budgets]
                $budgetContext
                
                [Recent Transactions]
                $txContext
                
                [Savings Goals]
                $goalContext
                
                [Investments Portfolio]
                $investmentContext
                
                $resetInfo
                
                Guidelines:
                1. Answer the user's queries accurately based on this data.
                2. Be concise, polite, and executive.
                3. Offer actual tips and spot abnormal spending or overruns when requested.
                4. Use the correct currency in responses.
                5. If you do not know or data is empty, mention that you're ready to help once transactions are logged.
            """.trimIndent()

            val jsonRequest = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "User query: $query")
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonRequest.toString().toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    return@withContext "Gemini API request failed with code ${response.code}: $errBody"
                }
                
                val responseBody = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext "No response generated by Gemini."
                }
                val contentObj = candidates.getJSONObject(0).optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                if (parts == null || parts.length() == 0) {
                    return@withContext "No response content generated by Gemini."
                }
                parts.getJSONObject(0).optString("text", "Empty response from Gemini.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Error connecting to Gemini API: ${e.localizedMessage}"
        }
    }
}
