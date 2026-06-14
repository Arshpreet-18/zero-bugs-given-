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

    private const val MODEL = "gemini-2.5-flash"
    private const val ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Builds the shared financial-profile context block injected into every Gemini request, so the
     * chatbot and the savings-insights generator are grounded in the same data.
     */
    private fun buildFinancialContext(
        transactions: List<Transaction>,
        budgets: List<Budget>,
        goals: List<SavingsGoal>,
        investments: List<Investment>,
        setZeroTimestamp: Long,
        currencySymbol: String
    ): String {
        // Only confirmed transactions after the reset cutoff count toward spending sums (mirrors
        // StatisticsScreen). Earlier transactions still appear in history for reference.
        val confirmedTx = transactions.filter { it.status == TransactionStatus.CONFIRMED }
        val countedTx = confirmedTx.filter { it.date > setZeroTimestamp }

        val txContext = confirmedTx.joinToString("\n") {
            "${if (it.isIncome) "Credit" else "Debit"}: $currencySymbol${it.amount} at ${it.merchant} on ${Date(it.date)} [Category: ${it.category}]"
        }.ifBlank { "No transactions logged yet." }

        val totalDebit = countedTx.filter { !it.isIncome }.sumOf { it.amount }
        val totalCredit = countedTx.filter { it.isIncome }.sumOf { it.amount }
        val categorySpend = countedTx.filter { !it.isIncome }
            .groupBy { it.category }
            .map { (cat, list) -> cat to list.sumOf { it.amount } }
            .sortedByDescending { it.second }
        val categoryContext = categorySpend.joinToString("\n") { (cat, sum) ->
            "Category: $cat — spent $currencySymbol$sum"
        }.ifBlank { "No spending recorded yet." }

        val budgetContext = budgets.joinToString("\n") {
            "Category: ${it.category}, Limit: $currencySymbol${it.limitAmount}, Spent: $currencySymbol${it.spentAmount}"
        }.ifBlank { "No budgets set." }
        val goalContext = goals.joinToString("\n") {
            "Goal: ${it.name}, Target: $currencySymbol${it.targetAmount}, Current: $currencySymbol${it.currentAmount}, Target Date: ${Date(it.deadline)}"
        }.ifBlank { "No savings goals set." }
        val investmentContext = investments.joinToString("\n") {
            "${it.type}: ${it.name}, Symbol: ${it.symbol}, Invested: $currencySymbol${it.investedAmount}, Value: $currencySymbol${it.currentValuation}"
        }.ifBlank { "No investments tracked." }

        val resetInfo = if (setZeroTimestamp > 0L) {
            "Note: The user reset the debit/credit start date to ${Date(setZeroTimestamp)}. Totals above only include transactions after that date; earlier ones remain in history but are excluded from sums."
        } else {
            ""
        }

        return """
            [Spending Summary]
            Total spent (debits): $currencySymbol$totalDebit
            Total received (credits): $currencySymbol$totalCredit

            [Spending by Category]
            $categoryContext

            [Budgets]
            $budgetContext

            [Recent Transactions]
            $txContext

            [Savings Goals]
            $goalContext

            [Investments Portfolio]
            $investmentContext

            $resetInfo
        """.trimIndent()
    }

    private fun languageInstruction(languageCode: String): String =
        if (languageCode == "hi") {
            "RESPOND ENTIRELY IN HINDI (हिंदी). All financial tips, analysis, explanations, greetings, and calculations must be written in the Hindi language only."
        } else {
            "Respond in English."
        }

    /** Issues a single Gemini text request and returns the first candidate's text, or [onError]. */
    private fun callGemini(systemPrompt: String, userText: String, apiKey: String, onError: (String) -> String): String {
        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userText) })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonRequest.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$ENDPOINT?key=$apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                return onError("Gemini API request failed with code ${response.code}: $errBody")
            }
            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return onError("No response generated by Gemini.")
            }
            val contentObj = candidates.getJSONObject(0).optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")
            if (parts == null || parts.length() == 0) {
                return onError("No response content generated by Gemini.")
            }
            return parts.getJSONObject(0).optString("text", "Empty response from Gemini.")
        }
    }

    suspend fun askAssistant(
        query: String,
        transactions: List<Transaction>,
        budgets: List<Budget>,
        goals: List<SavingsGoal>,
        investments: List<Investment>,
        apiKey: String?,
        setZeroTimestamp: Long,
        currencySymbol: String = "₹",
        languageCode: String = "en"
    ): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val cleanKey = apiKey?.trim() ?: ""
        if (cleanKey.isBlank()) {
            return@withContext "Gemini API key is not configured. Please go to Settings and set a valid Gemini API Key."
        }

        try {
            val context = buildFinancialContext(
                transactions, budgets, goals, investments, setZeroTimestamp, currencySymbol
            )
            val systemPrompt = """
                You are FinKlar, a premium AI personal finance advisor focused on helping the user
                save money and optimize their expenses.

                Here is the user's financial profile data:

                $context

                Guidelines:
                1. Answer the user's queries accurately and only from the data above.
                2. Act as a savings coach: identify the user's top spending categories, flag budget
                   overruns and unusually high or recurring debits, and explain where money is leaking.
                3. Give concrete, prioritized, quantified suggestions (e.g. "cut dining out by
                   ${currencySymbol}1500/month") rather than generic advice. Tie tips to real numbers above.
                4. Be concise, polite, and executive. Use $currencySymbol for all amounts.
                5. If data is empty, say you're ready to help once transactions are logged.
                6. ${languageInstruction(languageCode)}
            """.trimIndent()

            callGemini(systemPrompt, "User query: $query", cleanKey) { it }
        } catch (e: Exception) {
            e.printStackTrace()
            "Error connecting to Gemini API: ${e.localizedMessage}"
        }
    }

    /**
     * Generates a short list of proactive, data-grounded money-saving tips for the insights card.
     * Returns an empty list on any error or missing key (the UI degrades gracefully).
     */
    suspend fun generateSavingsInsights(
        transactions: List<Transaction>,
        budgets: List<Budget>,
        goals: List<SavingsGoal>,
        investments: List<Investment>,
        apiKey: String?,
        setZeroTimestamp: Long,
        currencySymbol: String = "₹",
        languageCode: String = "en"
    ): List<String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val cleanKey = apiKey?.trim() ?: ""
        if (cleanKey.isBlank()) return@withContext emptyList<String>()

        try {
            val context = buildFinancialContext(
                transactions, budgets, goals, investments, setZeroTimestamp, currencySymbol
            )
            val systemPrompt = """
                You are FinKlar, an AI savings advisor. Based on the user's financial data below,
                produce exactly 3 short, actionable, quantified money-saving tips tailored to their
                actual spending. Reference real categories and amounts. Each tip must be one sentence,
                use $currencySymbol for amounts, and avoid generic filler.

                $context

                ${languageInstruction(languageCode)}

                Respond ONLY with a valid JSON array of 3 strings, e.g.:
                ["Tip one.", "Tip two.", "Tip three."]
            """.trimIndent()

            val text = callGemini(systemPrompt, "Generate my savings insights.", cleanKey) { "" }
            if (text.isBlank()) return@withContext emptyList<String>()

            val cleaned = when {
                text.trim().startsWith("```json") -> text.substringAfter("```json").substringBefore("```").trim()
                text.trim().startsWith("```") -> text.substringAfter("```").substringBefore("```").trim()
                else -> text.trim()
            }
            val arr = JSONArray(cleaned)
            (0 until arr.length()).map { arr.getString(it) }.filter { it.isNotBlank() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
