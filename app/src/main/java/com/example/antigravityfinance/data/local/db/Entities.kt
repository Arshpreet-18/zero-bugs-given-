package com.example.antigravityfinance.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.antigravityfinance.data.model.Budget
import com.example.antigravityfinance.data.model.BudgetPeriod
import com.example.antigravityfinance.data.model.Investment
import com.example.antigravityfinance.data.model.SavingsGoal
import com.example.antigravityfinance.data.model.Transaction
import com.example.antigravityfinance.data.model.TransactionStatus
import com.example.antigravityfinance.data.model.SplitShare

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val merchant: String,
    val date: Long,
    val category: String,
    val notes: String = "",
    val account: String = "Cash",
    val status: String = "CONFIRMED", // PENDING, CONFIRMED, CANCELLED
    val isIncome: Boolean = false,
    val isRecurring: Boolean = false,
    val detectedFromSms: Boolean = false
) {
    fun toDomain(): Transaction = Transaction(
        id = id,
        amount = amount,
        merchant = merchant,
        date = date,
        category = category,
        notes = notes,
        account = account,
        status = TransactionStatus.valueOf(status),
        isIncome = isIncome,
        isRecurring = isRecurring,
        detectedFromSms = detectedFromSms
    )

    companion object {
        fun fromDomain(domain: Transaction): TransactionEntity = TransactionEntity(
            id = domain.id,
            amount = domain.amount,
            merchant = domain.merchant,
            date = domain.date,
            category = domain.category,
            notes = domain.notes,
            account = domain.account,
            status = domain.status.name,
            isIncome = domain.isIncome,
            isRecurring = domain.isRecurring,
            detectedFromSms = domain.detectedFromSms
        )
    }
}

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val limitAmount: Double,
    val period: String = "MONTHLY", // WEEKLY, MONTHLY
    val spentAmount: Double = 0.0
) {
    fun toDomain(): Budget = Budget(
        id = id,
        category = category,
        limitAmount = limitAmount,
        period = BudgetPeriod.valueOf(period),
        spentAmount = spentAmount
    )

    companion object {
        fun fromDomain(domain: Budget): BudgetEntity = BudgetEntity(
            id = domain.id,
            category = domain.category,
            limitAmount = domain.limitAmount,
            period = domain.period.name,
            spentAmount = domain.spentAmount
        )
    }
}

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: Long,
    val isCompleted: Boolean = false,
    val streakCount: Int = 0,
    val lastUpdatedStreak: Long = 0L
) {
    fun toDomain(): SavingsGoal = SavingsGoal(
        id = id,
        name = name,
        targetAmount = targetAmount,
        currentAmount = currentAmount,
        deadline = deadline,
        isCompleted = isCompleted,
        streakCount = streakCount,
        lastUpdatedStreak = lastUpdatedStreak
    )

    companion object {
        fun fromDomain(domain: SavingsGoal): SavingsGoalEntity = SavingsGoalEntity(
            id = domain.id,
            name = domain.name,
            targetAmount = domain.targetAmount,
            currentAmount = domain.currentAmount,
            deadline = domain.deadline,
            isCompleted = domain.isCompleted,
            streakCount = domain.streakCount,
            lastUpdatedStreak = domain.lastUpdatedStreak
        )
    }
}

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String,
    val investedAmount: Double,
    val currentValuation: Double,
    val units: Double = 0.0,
    val averagePrice: Double = 0.0,
    val symbol: String = ""
) {
    fun toDomain(): Investment = Investment(
        id = id,
        name = name,
        type = type,
        investedAmount = investedAmount,
        currentValuation = currentValuation,
        units = units,
        averagePrice = averagePrice,
        symbol = symbol
    )

    companion object {
        fun fromDomain(domain: Investment): InvestmentEntity = InvestmentEntity(
            id = domain.id,
            name = domain.name,
            type = domain.type,
            investedAmount = domain.investedAmount,
            currentValuation = domain.currentValuation,
            units = domain.units,
            averagePrice = domain.averagePrice,
            symbol = domain.symbol
        )
    }
}

@Entity(tableName = "recurring_merchants")
data class RecurringMerchantEntity(
    @PrimaryKey val merchant: String,
    val isAutoConfirm: Boolean = false,
    val frequencyCount: Int = 1,
    val lastConfirmedAmount: Double = 0.0
)

@Entity(tableName = "splits")
data class SplitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transactionId: Int,
    val transactionAmount: Double,
    val transactionMerchant: String,
    val transactionDate: Long,
    val contactName: String,
    val shareAmount: Double,
    val isSettled: Boolean = false
) {
    fun toDomain(): SplitShare = SplitShare(
        id = id,
        transactionId = transactionId,
        transactionAmount = transactionAmount,
        transactionMerchant = transactionMerchant,
        transactionDate = transactionDate,
        contactName = contactName,
        shareAmount = shareAmount,
        isSettled = isSettled
    )

    companion object {
        fun fromDomain(domain: SplitShare): SplitEntity = SplitEntity(
            id = domain.id,
            transactionId = domain.transactionId,
            transactionAmount = domain.transactionAmount,
            transactionMerchant = domain.transactionMerchant,
            transactionDate = domain.transactionDate,
            contactName = domain.contactName,
            shareAmount = domain.shareAmount,
            isSettled = domain.isSettled
        )
    }
}

