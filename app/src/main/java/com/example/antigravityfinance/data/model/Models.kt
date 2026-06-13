package com.example.antigravityfinance.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}

@Serializable
enum class TransactionCategory(val displayName: String) {
    TRAVEL("Travel"),
    FOOD("Food"),
    LIVELIHOOD("Livelihood"),
    COMPULSORY("Compulsory Expenses"),
    SHOPPING("Shopping"),
    INVESTMENT("Investments"),
    SALARY("Salary & Income"),
    OTHERS("Others")
}

@Serializable
enum class BudgetPeriod {
    WEEKLY,
    MONTHLY
}

@Serializable
enum class ThemeType {
    DYNAMIC,
    PROFESSIONAL
}

@Serializable
enum class LanguageType(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "Hindi")
}

@Serializable
enum class CurrencyType(val symbol: String, val code: String) {
    INR("₹", "INR"),
    USD("$", "USD"),
    EUR("€", "EUR")
}

@Serializable
data class Transaction(
    val id: Int = 0,
    val amount: Double,
    val merchant: String,
    val date: Long,
    val category: String,
    val notes: String = "",
    val account: String = "Cash",
    val status: TransactionStatus = TransactionStatus.CONFIRMED,
    val isIncome: Boolean = false,
    val isRecurring: Boolean = false,
    val detectedFromSms: Boolean = false
)

@Serializable
data class Budget(
    val id: Int = 0,
    val category: String, // "All" or a specific TransactionCategory name
    val limitAmount: Double,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val spentAmount: Double = 0.0
)

@Serializable
data class SavingsGoal(
    val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: Long,
    val isCompleted: Boolean = false,
    val streakCount: Int = 0,
    val lastUpdatedStreak: Long = 0L
)

@Serializable
data class Investment(
    val id: Int = 0,
    val name: String,
    val type: String, // "Stock", "Mutual Fund", "SIP"
    val investedAmount: Double,
    val currentValuation: Double,
    val units: Double = 0.0,
    val averagePrice: Double = 0.0,
    val symbol: String = "" // e.g. "TCS", "RELIANCE"
)

@Serializable
data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class SplitShare(
    val id: Int = 0,
    val transactionId: Int,
    val transactionAmount: Double,
    val transactionMerchant: String,
    val transactionDate: Long,
    val contactName: String,
    val shareAmount: Double,
    val isSettled: Boolean = false
)

