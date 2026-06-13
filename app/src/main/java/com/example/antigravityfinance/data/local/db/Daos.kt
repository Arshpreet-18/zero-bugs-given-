package com.example.antigravityfinance.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY date DESC")
    fun getTransactionsByStatus(status: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE amount = :amount AND merchant = :merchant AND ABS(date - :date) < :timeThreshold")
    suspend fun findPotentialDuplicates(amount: Double, merchant: String, date: Long, timeThreshold: Long): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity): Unit

    @Delete
    suspend fun delete(transaction: TransactionEntity): Unit
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE category = :category LIMIT 1")
    suspend fun getBudgetByCategory(category: String): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity): Unit

    @Delete
    suspend fun delete(budget: BudgetEntity): Unit
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY deadline ASC")
    fun getAllGoals(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getGoalById(id: Int): SavingsGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SavingsGoalEntity): Long

    @Update
    suspend fun update(goal: SavingsGoalEntity): Unit

    @Delete
    suspend fun delete(goal: SavingsGoalEntity): Unit
}

@Dao
interface InvestmentDao {
    @Query("SELECT * FROM investments")
    fun getAllInvestments(): Flow<List<InvestmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(investment: InvestmentEntity): Long

    @Update
    suspend fun update(investment: InvestmentEntity): Unit

    @Delete
    suspend fun delete(investment: InvestmentEntity): Unit
}

@Dao
interface RecurringMerchantDao {
    @Query("SELECT * FROM recurring_merchants")
    suspend fun getAllRecurringMerchants(): List<RecurringMerchantEntity>

    @Query("SELECT * FROM recurring_merchants WHERE merchant = :merchant LIMIT 1")
    suspend fun getRecurringMerchant(merchant: String): RecurringMerchantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurringMerchant: RecurringMerchantEntity): Unit
}

@Dao
interface SplitDao {
    @Query("SELECT * FROM splits ORDER BY transactionDate DESC")
    fun getAllSplits(): Flow<List<SplitEntity>>

    @Query("SELECT * FROM splits WHERE id = :id LIMIT 1")
    suspend fun getSplitById(id: Int): SplitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(split: SplitEntity): Long

    @Update
    suspend fun update(split: SplitEntity): Unit

    @Delete
    suspend fun delete(split: SplitEntity): Unit
}

