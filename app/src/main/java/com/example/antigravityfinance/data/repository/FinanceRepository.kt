package com.example.antigravityfinance.data.repository

import com.example.antigravityfinance.data.local.db.*
import com.example.antigravityfinance.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.math.abs

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val recurringMerchantDao: RecurringMerchantDao,
    private val budgetDao: BudgetDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions().map { list ->
        list.map { it.toDomain() }
    }

    val pendingTransactions: Flow<List<Transaction>> = transactionDao.getTransactionsByStatus("PENDING").map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getTransactionById(id: Int): Transaction? {
        return transactionDao.getTransactionById(id)?.toDomain()
    }

    suspend fun insertTransaction(transaction: Transaction): Int {
        val entity = TransactionEntity.fromDomain(transaction)
        val id = transactionDao.insert(entity).toInt()
        
        // If confirmed, update budgets
        if (transaction.status == TransactionStatus.CONFIRMED) {
            updateBudgetsForTransaction(transaction.category, transaction.amount, isIncome = transaction.isIncome)
            learnRecurringMerchant(transaction.merchant, transaction.amount)
        }
        return id
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(TransactionEntity.fromDomain(transaction))
        if (transaction.status == TransactionStatus.CONFIRMED) {
            updateBudgetsForTransaction(transaction.category, -transaction.amount, isIncome = transaction.isIncome)
        }
    }

    suspend fun confirmTransaction(id: Int) {
        val entity = transactionDao.getTransactionById(id)
        if (entity != null && entity.status != "CONFIRMED") {
            val updated = entity.copy(status = "CONFIRMED")
            transactionDao.update(updated)
            
            val domain = updated.toDomain()
            updateBudgetsForTransaction(domain.category, domain.amount, isIncome = domain.isIncome)
            learnRecurringMerchant(domain.merchant, domain.amount)
        }
    }

    suspend fun cancelTransaction(id: Int) {
        val entity = transactionDao.getTransactionById(id)
        if (entity != null) {
            val wasConfirmed = entity.status == "CONFIRMED"
            val updated = entity.copy(status = "CANCELLED")
            transactionDao.update(updated)
            
            if (wasConfirmed) {
                // Deduct from budgets
                val domain = entity.toDomain()
                updateBudgetsForTransaction(domain.category, -domain.amount, isIncome = domain.isIncome)
            }
        }
    }

    suspend fun restoreTransaction(id: Int) {
        val entity = transactionDao.getTransactionById(id)
        if (entity != null && entity.status == "CANCELLED") {
            val updated = entity.copy(status = "CONFIRMED")
            transactionDao.update(updated)
            
            val domain = updated.toDomain()
            updateBudgetsForTransaction(domain.category, domain.amount, isIncome = domain.isIncome)
        }
    }

    suspend fun updateTransactionCategory(id: Int, newCategory: String) {
        val entity = transactionDao.getTransactionById(id)
        if (entity != null) {
            val oldCategory = entity.category
            if (oldCategory != newCategory) {
                val updated = entity.copy(category = newCategory)
                transactionDao.update(updated)
                
                if (entity.status == "CONFIRMED") {
                    val domain = entity.toDomain()
                    updateBudgetsForTransaction(oldCategory, -domain.amount, isIncome = domain.isIncome)
                    updateBudgetsForTransaction(newCategory, domain.amount, isIncome = domain.isIncome)
                }
            }
        }
    }

    suspend fun checkForDuplicate(amount: Double, merchant: String, date: Long): Transaction? {
        // Time threshold: 2 hours (7200000 ms)
        val timeThreshold = 7200000L
        val potentialDuplicates = transactionDao.findPotentialDuplicates(amount, merchant, date, timeThreshold)
        if (potentialDuplicates.isNotEmpty()) {
            return potentialDuplicates.first().toDomain()
        }
        
        // Secondary checks: check if any transaction in the database has exact amount, same day, and matching merchant initials
        // But for performance, the database query is best. Let's return the first potential matching one.
        return null
    }

    suspend fun isMerchantAutoConfirm(merchant: String): Boolean {
        val merchantName = merchant.trim().uppercase()
        val record = recurringMerchantDao.getRecurringMerchant(merchantName)
        return record?.isAutoConfirm ?: false
    }

    private suspend fun learnRecurringMerchant(merchant: String, amount: Double) {
        val merchantName = merchant.trim().uppercase()
        val record = recurringMerchantDao.getRecurringMerchant(merchantName)
        if (record != null) {
            val newCount = record.frequencyCount + 1
            // Auto confirm after 3 confirmed transactions
            val autoConfirm = newCount >= 3
            recurringMerchantDao.insert(
                RecurringMerchantEntity(
                    merchant = merchantName,
                    isAutoConfirm = autoConfirm,
                    frequencyCount = newCount,
                    lastConfirmedAmount = amount
                )
            )
        } else {
            recurringMerchantDao.insert(
                RecurringMerchantEntity(
                    merchant = merchantName,
                    isAutoConfirm = false,
                    frequencyCount = 1,
                    lastConfirmedAmount = amount
                )
            )
        }
    }

    private suspend fun updateBudgetsForTransaction(category: String, amount: Double, isIncome: Boolean) {
        if (isIncome) return // Budgets only track expenses
        
        // Update category specific budget
        val catBudget = budgetDao.getBudgetByCategory(category)
        if (catBudget != null) {
            budgetDao.update(catBudget.copy(spentAmount = catBudget.spentAmount + amount))
        }

        // Update overall budget
        val allBudget = budgetDao.getBudgetByCategory("All")
        if (allBudget != null) {
            budgetDao.update(allBudget.copy(spentAmount = allBudget.spentAmount + amount))
        }
    }
}

class BudgetRepository(private val budgetDao: BudgetDao) {
    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgets().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun insertBudget(budget: Budget) {
        budgetDao.insert(BudgetEntity.fromDomain(budget))
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.update(BudgetEntity.fromDomain(budget))
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.delete(BudgetEntity.fromDomain(budget))
    }

    suspend fun getBudgetByCategory(category: String): Budget? {
        return budgetDao.getBudgetByCategory(category)?.toDomain()
    }

    suspend fun recalculateSpent(transactions: List<Transaction>) {
        val expenses = transactions.filter { it.status == TransactionStatus.CONFIRMED && !it.isIncome }
        val budgets = budgetDao.getAllBudgets().first()
        for (budget in budgets) {
            val category = budget.category
            val spent = if (category == "All") {
                expenses.sumOf { it.amount }
            } else {
                expenses.filter { it.category.uppercase() == category.uppercase() }.sumOf { it.amount }
            }
            budgetDao.update(budget.copy(spentAmount = spent))
        }
    }
}

class GoalRepository(private val savingsGoalDao: SavingsGoalDao) {
    val allGoals: Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoals().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun insertGoal(goal: SavingsGoal) {
        savingsGoalDao.insert(SavingsGoalEntity.fromDomain(goal))
    }

    suspend fun updateGoal(goal: SavingsGoal) {
        val existing = savingsGoalDao.getGoalById(goal.id)?.toDomain()
        var updatedStreakCount = goal.streakCount
        var updatedLastUpdatedStreak = goal.lastUpdatedStreak
        val now = System.currentTimeMillis()

        if (existing != null) {
            if (goal.currentAmount > existing.currentAmount) {
                val lastUpdate = existing.lastUpdatedStreak
                if (lastUpdate == 0L) {
                    updatedStreakCount = 1
                    updatedLastUpdatedStreak = now
                } else {
                    val tz = java.util.TimeZone.getDefault()
                    val dayNow = (now + tz.getOffset(now)) / (24 * 60 * 60 * 1000L)
                    val dayLast = (lastUpdate + tz.getOffset(lastUpdate)) / (24 * 60 * 60 * 1000L)
                    
                    if (dayNow == dayLast + 1L) {
                        updatedStreakCount = existing.streakCount + 1
                        updatedLastUpdatedStreak = now
                    } else if (dayNow > dayLast + 1L) {
                        updatedStreakCount = 1
                        updatedLastUpdatedStreak = now
                    } else {
                        updatedStreakCount = existing.streakCount
                        if (updatedStreakCount == 0) {
                            updatedStreakCount = 1
                            updatedLastUpdatedStreak = now
                        }
                    }
                }
            }
        } else {
            if (goal.currentAmount > 0) {
                updatedStreakCount = 1
                updatedLastUpdatedStreak = now
            }
        }
        val completed = goal.currentAmount >= goal.targetAmount
        val finalGoal = goal.copy(
            isCompleted = completed,
            streakCount = updatedStreakCount,
            lastUpdatedStreak = updatedLastUpdatedStreak
        )
        savingsGoalDao.update(SavingsGoalEntity.fromDomain(finalGoal))
    }

    suspend fun deleteGoal(goal: SavingsGoal) {
        savingsGoalDao.delete(SavingsGoalEntity.fromDomain(goal))
    }
}

class InvestmentRepository(private val investmentDao: InvestmentDao) {
    val allInvestments: Flow<List<Investment>> = investmentDao.getAllInvestments().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun insertInvestment(investment: Investment) {
        investmentDao.insert(InvestmentEntity.fromDomain(investment))
    }

    suspend fun updateInvestment(investment: Investment) {
        investmentDao.update(InvestmentEntity.fromDomain(investment))
    }

    suspend fun deleteInvestment(investment: Investment) {
        investmentDao.delete(InvestmentEntity.fromDomain(investment))
    }
}
