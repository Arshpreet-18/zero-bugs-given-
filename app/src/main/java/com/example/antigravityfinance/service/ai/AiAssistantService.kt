package com.example.antigravityfinance.service.ai

import com.example.antigravityfinance.data.model.*
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import java.text.NumberFormat
import java.util.*

object AiAssistantService {

    suspend fun askAssistant(
        query: String,
        transactions: List<Transaction>,
        budgets: List<Budget>,
        goals: List<SavingsGoal>,
        investments: List<Investment>,
        apiKey: String?,
        currencySymbol: String = "₹"
    ): String {
        if (!apiKey.isNullOrBlank()) {
            return askGemini(query, transactions, budgets, goals, investments, apiKey)
        }
        return askLocalNlp(query, transactions, budgets, goals, investments, currencySymbol)
    }

    private suspend fun askGemini(
        query: String,
        transactions: List<Transaction>,
        budgets: List<Budget>,
        goals: List<SavingsGoal>,
        investments: List<Investment>,
        apiKey: String
    ): String {
        return try {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )
            
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

            val systemPrompt = """
                You are a premium AI personal finance assistant named Antigravity.
                Here is the user's financial profile data:
                
                [Budgets]
                $budgetContext
                
                [Recent Transactions]
                $txContext
                
                [Savings Goals]
                $goalContext
                
                [Investments Portfolio]
                $investmentContext
                
                Guidelines:
                1. Answer the user's queries accurately based on this data.
                2. Be concise, polite, and executive.
                3. Offer actual tips and spot abnormal spending or overruns when requested.
                4. Use the correct currency in responses.
                5. If you do not know or data is empty, mention that you're ready to help once transactions are logged.
            """.trimIndent()

            val response = model.generateContent(
                content {
                    text(systemPrompt)
                    text("User query: $query")
                }
            )
            response.text ?: "I am sorry, I couldn't formulate a response."
        } catch (e: Exception) {
            "Error connecting to Gemini: ${e.localizedMessage}. Falling back to local assistant."
        }
    }

    private fun askLocalNlp(
        query: String,
        transactions: List<Transaction>,
        budgets: List<Budget>,
        goals: List<SavingsGoal>,
        investments: List<Investment>,
        currency: String
    ): String {
        val text = query.lowercase()
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            val symbols = currency.trim()
            minimumFractionDigits = 0
        }
        
        val confirmed = transactions.filter { it.status == TransactionStatus.CONFIRMED }
        val expenses = confirmed.filter { !it.isIncome }
        val income = confirmed.filter { it.isIncome }

        // 1. Food and specific categories spend
        for (cat in TransactionCategory.values()) {
            if (text.contains(cat.name.lowercase()) || text.contains(cat.displayName.lowercase())) {
                val sum = expenses.filter { it.category.uppercase() == cat.name }.sumOf { it.amount }
                return "You have spent **$currency${String.format("%.2f", sum)}** on **${cat.displayName}** this month. " +
                       if (sum > 0) "This accounts for ${String.format("%.1f", (sum / expenses.sumOf { it.amount }) * 100)}% of your total monthly debits." else "Good job keeping this at zero!"
            }
        }
        
        if (text.contains("food") || text.contains("eat") || text.contains("dining")) {
            val sum = expenses.filter { it.category == "FOOD" }.sumOf { it.amount }
            return "You have spent **$currency${String.format("%.2f", sum)}** on Food & Dining this month."
        }

        // 2. Budget status
        if (text.contains("budget") || text.contains("left") || text.contains("remaining")) {
            val totalBudget = budgets.find { it.category == "All" }
            if (totalBudget == null) {
                return "You haven't set up a master budget yet. Go to the Budgets tab to set a monthly limit!"
            }
            val remaining = totalBudget.limitAmount - totalBudget.spentAmount
            return if (remaining >= 0) {
                "You have spent **$currency${String.format("%.2f", totalBudget.spentAmount)}** out of your **$currency${String.format("%.2f", totalBudget.limitAmount)}** budget. " +
                "You have **$currency${String.format("%.2f", remaining)}** left for the rest of the period."
            } else {
                "⚠️ **Overspending Alert!** You have exceeded your budget of **$currency${String.format("%.2f", totalBudget.limitAmount)}** by **$currency${String.format("%.2f", -remaining)}**."
            }
        }

        // 3. Biggest expenses
        if (text.contains("biggest") || text.contains("largest") || text.contains("highest") || text.contains("most expensive")) {
            if (expenses.isEmpty()) return "You have no debits logged yet."
            val sorted = expenses.sortedByDescending { it.amount }.take(3)
            val listText = sorted.mapIndexed { index, tx -> 
                "${index + 1}. **$currency${String.format("%.2f", tx.amount)}** at *${tx.merchant}* (${tx.category.lowercase().capitalize()})" 
            }.joinToString("\n")
            return "Here are your 3 biggest debits:\n$listText"
        }

        // 4. Savings calculations
        if (text.contains("save") || text.contains("saving") || text.contains("leftover")) {
            val totalIncome = income.sumOf { it.amount }
            val totalExpense = expenses.sumOf { it.amount }
            val netSavings = totalIncome - totalExpense
            val rate = if (totalIncome > 0) (netSavings / totalIncome) * 100 else 0.0
            
            return "This month, your credits were **$currency${String.format("%.2f", totalIncome)}** and debits were **$currency${String.format("%.2f", totalExpense)}**. " +
                   "Your net savings is **$currency${String.format("%.2f", netSavings)}** (Savings Rate: **${String.format("%.1f", rate)}%**)."
        }

        // 5. Forecast / Budget Overrun Predictor
        if (text.contains("forecast") || text.contains("predict") || text.contains("runway") || text.contains("overrun")) {
            val totalBudget = budgets.find { it.category == "All" } ?: return "Create a master budget first to calculate projections."
            val calendar = Calendar.getInstance()
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            if (dayOfMonth <= 1) return "Please wait a few days into the month for me to collect trend data to forecast."
            
            val dailyAvg = totalBudget.spentAmount / dayOfMonth
            val projectedSpend = dailyAvg * maxDays
            
            return if (projectedSpend > totalBudget.limitAmount) {
                "⚠️ **Budget Overrun Prediction:** Based on your current spending rate of **$currency${String.format("%.2f", dailyAvg)}/day**, " +
                "you are projected to spend **$currency${String.format("%.2f", projectedSpend)}** by the end of the month. " +
                "This will exceed your budget by **$currency${String.format("%.2f", projectedSpend - totalBudget.limitAmount)}**! I suggest reducing non-essential shopping."
            } else {
                "✅ **On Track:** At your current spending rate of **$currency${String.format("%.2f", dailyAvg)}/day**, " +
                "you are projected to finish the month at **$currency${String.format("%.2f", projectedSpend)}**, which is safely below your limit of **$currency${String.format("%.2f", totalBudget.limitAmount)}**."
            }
        }

        // 6. Insights & Savings Opportunities
        if (text.contains("insight") || text.contains("recommend") || text.contains("unusual") || text.contains("pattern")) {
            val insights = mutableListOf<String>()
            
            // Analyze categories
            val categoriesSum = expenses.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
            val topCategory = categoriesSum.maxByOrNull { it.value }
            
            if (topCategory != null) {
                insights.add("• Your highest spending category is **${topCategory.key.capitalize()}** at **$currency${String.format("%.2f", topCategory.value)}**.")
            }
            
            // Check recurring/subscriptions
            val recurringCount = expenses.filter { it.isRecurring }.size
            if (recurringCount > 0) {
                insights.add("• You have **$recurringCount subscription(s)** active. Regularly review if you still use them to save.")
            }
            
            // Check goal synergy
            val activeGoal = goals.find { !it.isCompleted }
            if (activeGoal != null && topCategory != null && topCategory.value > 1000) {
                val potentialSave = topCategory.value * 0.15
                val daysSaved = (potentialSave / 100).toInt() // arbitrary mapping
                insights.add("• **Savings Tip:** Cutting back **15%** on *${topCategory.key.lowercase()}* would save you **$currency${String.format("%.2f", potentialSave)}** this month. This could help you complete your **'${activeGoal.name}'** goal **$daysSaved days faster**!")
            }

            if (insights.isEmpty()) {
                return "I don't have enough spending history yet to generate deep insights. Try adding more transactions!"
            }
            return "Here are your AI Financial Insights:\n\n" + insights.joinToString("\n")
        }

        // 7. Investment Insights
        if (text.contains("invest") || text.contains("stock") || text.contains("portfolio")) {
            if (investments.isEmpty()) return "Your investment portfolio is currently empty. You can enable investments in settings and start tracking SIPs and Stocks!"
            
            val totalInvested = investments.sumOf { it.investedAmount }
            val currentVal = investments.sumOf { it.currentValuation }
            val netGain = currentVal - totalInvested
            val gainPct = if (totalInvested > 0) (netGain / totalInvested) * 100 else 0.0
            
            val stockWeight = investments.filter { it.type == "Stock" }.sumOf { it.currentValuation }
            val mfWeight = investments.filter { it.type == "Mutual Fund" || it.type == "SIP" }.sumOf { it.currentValuation }
            val totalVal = stockWeight + mfWeight
            
            val stockPct = if (totalVal > 0) (stockWeight / totalVal) * 100 else 0.0
            val mfPct = if (totalVal > 0) (mfWeight / totalVal) * 100 else 0.0

            // 10 year projection at 12%
            val projectedTenYears = currentVal * Math.pow(1.12, 10.0)

            return "📈 **Portfolio Analytics:**\n" +
                   "• Total Invested: **$currency${String.format("%.2f", totalInvested)}**\n" +
                   "• Current Valuation: **$currency${String.format("%.2f", currentVal)}** (${if (netGain >= 0) "+" else ""}${String.format("%.2f", gainPct)}%)\n" +
                   "• Asset Allocation: **Stocks: ${String.format("%.1f", stockPct)}%**, **Mutual Funds/SIPs: ${String.format("%.1f", mfPct)}%**\n" +
                   "• **Future Projection:** Compounding at a conservative **12% CAGR**, your current portfolio will grow to **$currency${String.format("%.2f", projectedTenYears)}** in 10 years!"
        }

        // Default response
        return "Hi! I am Antigravity, your finance assistant. Ask me questions like:\n" +
               "• *'How much did I spend on food?'*\n" +
               "• *'How much is left in my budget?'*\n" +
               "• *'Show my biggest debits'* or *'unusual spending'* \n" +
               "• *'How much did I save this month?'* or *'give me saving insights'*\n" +
               "• *'What is my investment growth?'*"
    }
}
