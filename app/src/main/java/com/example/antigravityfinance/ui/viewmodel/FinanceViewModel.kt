package com.example.antigravityfinance.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.antigravityfinance.data.local.db.FinanceDatabase
import com.example.antigravityfinance.data.model.*
import com.example.antigravityfinance.data.repository.*
import com.example.antigravityfinance.service.ai.AiAssistantService
import com.example.antigravityfinance.service.ocr.OcrScanner
import com.example.antigravityfinance.service.security.SecurityHelper
import com.example.antigravityfinance.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FinanceDatabase.getDatabase(application)
    private val securityHelper = SecurityHelper(application)
    
    val transactionRepository = TransactionRepository(db.transactionDao(), db.recurringMerchantDao(), db.budgetDao())
    val budgetRepository = BudgetRepository(db.budgetDao())
    val goalRepository = GoalRepository(db.savingsGoalDao())
    val investmentRepository = InvestmentRepository(db.investmentDao())

    // --- SECURITY FLOW STATE ---
    private val _isPinSet = MutableStateFlow(securityHelper.isPinSet())
    val isPinSet: StateFlow<Boolean> = _isPinSet.asStateFlow()

    private val _isAuthRequired = MutableStateFlow(securityHelper.isPinSet())
    val isAuthRequired: StateFlow<Boolean> = _isAuthRequired.asStateFlow()

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError.asStateFlow()

    // --- THEME & SETTINGS STATE ---
    private val _themeType = MutableStateFlow(securityHelper.getTheme())
    val themeType: StateFlow<ThemeType> = _themeType.asStateFlow()

    private val _isDarkMode = MutableStateFlow(securityHelper.getDarkMode())
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    private val _accentIndex = MutableStateFlow(securityHelper.getAccentIndex())
    val accentIndex: StateFlow<Int> = _accentIndex.asStateFlow()

    val customAccent: StateFlow<Color?> = _accentIndex.map { index ->
        getAccentColorFromIndex(index)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currency = MutableStateFlow(securityHelper.getCurrency())
    val currency: StateFlow<CurrencyType> = _currency.asStateFlow()

    private val _language = MutableStateFlow(securityHelper.getLanguage())
    val language: StateFlow<LanguageType> = _language.asStateFlow()

    private val _isInvestmentsEnabled = MutableStateFlow(securityHelper.isInvestmentsEnabled())
    val isInvestmentsEnabled: StateFlow<Boolean> = _isInvestmentsEnabled.asStateFlow()

    private val _isInitialIncomeSet = MutableStateFlow(securityHelper.isInitialIncomeSet())
    val isInitialIncomeSet: StateFlow<Boolean> = _isInitialIncomeSet.asStateFlow()

    // --- REPOSITORIES DATA STREAMS ---
    val allTransactions: StateFlow<List<Transaction>> = transactionRepository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTransactions: StateFlow<List<Transaction>> = transactionRepository.pendingTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<Budget>> = budgetRepository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<SavingsGoal>> = goalRepository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val investments: StateFlow<List<Investment>> = investmentRepository.allInvestments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- OCR SCANNING WORKFLOW STATE ---
    private val _isOcrScanning = MutableStateFlow(false)
    val isOcrScanning: StateFlow<Boolean> = _isOcrScanning.asStateFlow()

    private val _scannedTransaction = MutableStateFlow<Transaction?>(null)
    val scannedTransaction: StateFlow<Transaction?> = _scannedTransaction.asStateFlow()

    private val _showDuplicateWarning = MutableStateFlow(false)
    val showDuplicateWarning: StateFlow<Boolean> = _showDuplicateWarning.asStateFlow()

    private val _duplicateMatchingTransaction = MutableStateFlow<Transaction?>(null)
    val duplicateMatchingTransaction: StateFlow<Transaction?> = _duplicateMatchingTransaction.asStateFlow()

    // --- AI CHATBOT STATE ---
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("welcome", "Hello! I am FinKlar, your AI finance assistant. Ask me anything about your budgets, transactions, or investments.", false)
        )
    )
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // --- SMS SYNC STATE ---
    private val _smsSyncedBalance = MutableStateFlow<Double?>(securityHelper.getSyncedBalance())
    val smsSyncedBalance: StateFlow<Double?> = _smsSyncedBalance.asStateFlow()
    
    private val _isSmsScanning = MutableStateFlow(false)
    val isSmsScanning: StateFlow<Boolean> = _isSmsScanning.asStateFlow()

    // --- INITIALIZATION ---
    init {
        viewModelScope.launch {
            budgetRepository.allBudgets.first().let { current ->
                if (current.isEmpty()) {
                    budgetRepository.insertBudget(Budget(category = "All", limitAmount = 25000.0))
                    budgetRepository.insertBudget(Budget(category = "FOOD", limitAmount = 8000.0))
                    budgetRepository.insertBudget(Budget(category = "SHOPPING", limitAmount = 5000.0))
                }
            }
            allTransactions.first().let { current ->
                if (current.isEmpty()) {
                    transactionRepository.insertTransaction(
                        Transaction(amount = 3500.0, merchant = "HDFC Bank", date = System.currentTimeMillis() - 86400000 * 2, category = "SALARY", isIncome = true, status = TransactionStatus.CONFIRMED)
                    )
                    transactionRepository.insertTransaction(
                        Transaction(amount = 750.0, merchant = "Zomato Food", date = System.currentTimeMillis() - 86400000, category = "FOOD", isIncome = false, status = TransactionStatus.CONFIRMED)
                    )
                    transactionRepository.insertTransaction(
                        Transaction(amount = 2500.0, merchant = "Amazon India", date = System.currentTimeMillis(), category = "SHOPPING", isIncome = false, status = TransactionStatus.CONFIRMED)
                    )
                }
            }
            reconcileSmsInbox()
        }
    }

    // --- SECURITY ACTIONS ---
    fun setPin(pin: String) {
        securityHelper.savePin(pin)
        _isPinSet.value = true
        _isAuthRequired.value = false
    }

    fun submitPin(pin: String): Boolean {
        return if (securityHelper.verifyPin(pin)) {
            _isAuthRequired.value = false
            _pinError.value = false
            true
        } else {
            _pinError.value = true
            false
        }
    }

    fun logout() {
        if (securityHelper.isPinSet()) {
            _isAuthRequired.value = true
        }
    }

    fun removePin() {
        securityHelper.clearPin()
        securityHelper.setBiometricsEnabled(false)
        _isPinSet.value = false
        _isAuthRequired.value = false
    }

    // --- SETTINGS ACTIONS ---
    fun updateTheme(theme: ThemeType) {
        securityHelper.saveTheme(theme)
        _themeType.value = theme
    }

    fun updateDarkMode(isDark: Boolean?) {
        securityHelper.saveDarkMode(isDark)
        _isDarkMode.value = isDark
    }

    fun updateAccentIndex(index: Int) {
        securityHelper.saveAccentIndex(index)
        _accentIndex.value = index
    }

    fun updateCurrency(currency: CurrencyType) {
        securityHelper.saveCurrency(currency)
        _currency.value = currency
    }

    fun updateLanguage(language: LanguageType) {
        securityHelper.saveLanguage(language)
        _language.value = language
    }

    fun updateInvestmentsEnabled(enabled: Boolean) {
        securityHelper.saveInvestmentsEnabled(enabled)
        _isInvestmentsEnabled.value = enabled
    }

    // --- TRANSACTION CONFIRMATION WORKFLOW ACTIONS ---
    fun handleMockOcrTrigger(type: OcrScanner.MockReceiptType) {
        viewModelScope.launch {
            _isOcrScanning.value = true
            val transaction = OcrScanner.scanReceiptMock(type)
            _isOcrScanning.value = false
            
            val duplicate = transactionRepository.checkForDuplicate(transaction.amount, transaction.merchant, transaction.date)
            if (duplicate != null) {
                _scannedTransaction.value = transaction
                _duplicateMatchingTransaction.value = duplicate
                _showDuplicateWarning.value = true
            } else {
                transactionRepository.insertTransaction(transaction)
            }
        }
    }

    fun handleRealOcrTrigger(bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            val apiKey = securityHelper.getGeminiApiKey()
            if (apiKey.isBlank()) return@launch
            
            _isOcrScanning.value = true
            val transaction = OcrScanner.scanReceiptReal(bitmap, apiKey)
            _isOcrScanning.value = false
            
            if (transaction != null) {
                val duplicate = transactionRepository.checkForDuplicate(transaction.amount, transaction.merchant, transaction.date)
                if (duplicate != null) {
                    _scannedTransaction.value = transaction
                    _duplicateMatchingTransaction.value = duplicate
                    _showDuplicateWarning.value = true
                } else {
                    transactionRepository.insertTransaction(transaction)
                }
            }
        }
    }

    fun confirmScannedTransaction() {
        val tx = _scannedTransaction.value ?: return
        viewModelScope.launch {
            transactionRepository.insertTransaction(tx.copy(status = TransactionStatus.CONFIRMED))
            _scannedTransaction.value = null
            _showDuplicateWarning.value = false
            _duplicateMatchingTransaction.value = null
        }
    }

    fun cancelScannedTransaction() {
        val tx = _scannedTransaction.value ?: return
        viewModelScope.launch {
            transactionRepository.insertTransaction(tx.copy(status = TransactionStatus.CANCELLED))
            _scannedTransaction.value = null
            _showDuplicateWarning.value = false
            _duplicateMatchingTransaction.value = null
        }
    }

    fun ignoreScannedTransaction() {
        val tx = _scannedTransaction.value ?: return
        viewModelScope.launch {
            transactionRepository.insertTransaction(tx.copy(status = TransactionStatus.PENDING))
            _scannedTransaction.value = null
            _showDuplicateWarning.value = false
            _duplicateMatchingTransaction.value = null
        }
    }

    fun forceConfirmDuplicate() {
        confirmScannedTransaction()
    }

    fun dismissDuplicateWarning() {
        _scannedTransaction.value = null
        _showDuplicateWarning.value = false
        _duplicateMatchingTransaction.value = null
    }

    fun confirmPendingTransaction(id: Int) {
        viewModelScope.launch {
            transactionRepository.confirmTransaction(id)
        }
    }

    fun cancelPendingTransaction(id: Int) {
        viewModelScope.launch {
            transactionRepository.cancelTransaction(id)
        }
    }

    fun updateTransactionCategory(id: Int, newCategory: String) {
        viewModelScope.launch {
            transactionRepository.updateTransactionCategory(id, newCategory)
        }
    }

    // --- DATABASE WRITE WRAPPERS FOR UI COMPATIBILITY ---
    fun addManualTransaction(amount: Double, merchant: String, isIncome: Boolean, category: String) {
        viewModelScope.launch {
            val isIncomeOptional = isIncome && securityHelper.isInitialIncomeSet()
            val status = if (isIncomeOptional) TransactionStatus.PENDING else TransactionStatus.CONFIRMED
            val newTx = Transaction(
                amount = amount,
                merchant = merchant,
                date = System.currentTimeMillis(),
                category = category,
                isIncome = isIncome,
                status = status
            )
            transactionRepository.insertTransaction(newTx)
        }
    }

    fun addInitialIncome(amount: Double) {
        viewModelScope.launch {
            val initialIncomeTx = Transaction(
                amount = amount,
                merchant = "Initial Income Setup",
                date = System.currentTimeMillis(),
                category = "SALARY",
                isIncome = true,
                status = TransactionStatus.CONFIRMED,
                notes = "Initial bank balance setup"
            )
            transactionRepository.insertTransaction(initialIncomeTx)
            securityHelper.setInitialIncomeSet(true)
            _isInitialIncomeSet.value = true
        }
    }

    fun addGoal(name: String, target: Double) {
        viewModelScope.launch {
            goalRepository.insertGoal(
                SavingsGoal(
                    name = name,
                    targetAmount = target,
                    deadline = System.currentTimeMillis() + 86400000L * 90
                )
            )
        }
    }

    fun depositToGoal(goal: SavingsGoal, amount: Double) {
        viewModelScope.launch {
            goalRepository.updateGoal(goal.copy(currentAmount = goal.currentAmount + amount))
        }
    }

    fun addInvestment(name: String, symbol: String, type: String, amount: Double) {
        viewModelScope.launch {
            investmentRepository.insertInvestment(
                Investment(
                    name = name,
                    symbol = symbol.uppercase(),
                    type = type,
                    investedAmount = amount,
                    currentValuation = amount * 1.05
                )
            )
        }
    }

    // --- SMS SIMULATION TRIGGER ---
    fun simulateIncomingSms(body: String) {
        viewModelScope.launch {
            val parsedResult = com.example.antigravityfinance.service.sms.SmsParser.parse(body)
            if (parsedResult != null) {
                val parsed = parsedResult.transaction
                val balance = parsedResult.balance
                
                if (balance != null) {
                    securityHelper.saveSyncedBalance(balance)
                    _smsSyncedBalance.value = balance
                }
                
                val isAutoConfirm = transactionRepository.isMerchantAutoConfirm(parsed.merchant)
                val isIncomeOptional = parsed.isIncome && securityHelper.isInitialIncomeSet()
                val finalStatus = if (isAutoConfirm && !isIncomeOptional) TransactionStatus.CONFIRMED else TransactionStatus.PENDING
                
                val duplicate = transactionRepository.checkForDuplicate(parsed.amount, parsed.merchant, parsed.date)
                val statusToInsert = if (duplicate != null) TransactionStatus.PENDING else finalStatus
                val notesToInsert = if (duplicate != null) "SMS matches Transaction #${duplicate.id} (${duplicate.merchant})" else parsed.notes
                
                val finalTx = parsed.copy(status = statusToInsert, notes = notesToInsert)
                transactionRepository.insertTransaction(finalTx)
            }
        }
    }

    // --- SMS INBOX SCANNING TRIGGER ---
    fun scanSmsInbox(onComplete: (addedCount: Int, balance: Double?) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _isSmsScanning.value = true
            val result = com.example.antigravityfinance.service.sms.SmsInboxScanner.reconcileInbox(getApplication(), transactionRepository)
            _isSmsScanning.value = false
            
            if (result.latestBalance != null) {
                securityHelper.saveSyncedBalance(result.latestBalance)
                _smsSyncedBalance.value = result.latestBalance
            }
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onComplete(result.addedCount, result.latestBalance)
            }
        }
    }

    fun reconcileSmsInbox() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _isSmsScanning.value = true
            val result = com.example.antigravityfinance.service.sms.SmsInboxScanner.reconcileInbox(getApplication(), transactionRepository)
            _isSmsScanning.value = false
            
            if (result.latestBalance != null) {
                securityHelper.saveSyncedBalance(result.latestBalance)
                _smsSyncedBalance.value = result.latestBalance
            }
        }
    }

    // --- AI CHAT CONVERSATION ---
    fun sendMessageToAssistant(text: String) {
        if (text.isBlank()) return
        
        val userMsg = ChatMessage(UUID.randomUUID().toString(), text, true)
        _chatHistory.value = _chatHistory.value + userMsg
        
        viewModelScope.launch {
            _isAiThinking.value = true
            val response = AiAssistantService.askAssistant(
                query = text,
                transactions = allTransactions.value,
                budgets = budgets.value,
                goals = goals.value,
                investments = investments.value,
                apiKey = securityHelper.getGeminiApiKey(),
                currencySymbol = currency.value.symbol
            )
            _isAiThinking.value = false
            val aiMsg = ChatMessage(UUID.randomUUID().toString(), response, false)
            _chatHistory.value = _chatHistory.value + aiMsg
        }
    }

    fun clearChat() {
        _chatHistory.value = listOf(
            ChatMessage("welcome", "Chat log cleared. Ask me a new financial query!", false)
        )
    }

    // --- HELPERS ---
    private fun getAccentColorFromIndex(index: Int): Color? {
        return when (index) {
            0 -> AccentEmerald
            1 -> AccentOrange
            2 -> AccentIndigo
            3 -> AccentCrimson
            4 -> AccentSky
            else -> null
        }
    }
}
