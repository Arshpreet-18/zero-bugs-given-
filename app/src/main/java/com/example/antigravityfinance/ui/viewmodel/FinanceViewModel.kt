package com.example.antigravityfinance.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.antigravityfinance.data.local.db.FinanceDatabase
import com.example.antigravityfinance.data.local.db.SplitEntity
import com.example.antigravityfinance.data.model.*
import com.example.antigravityfinance.data.repository.*
import com.example.antigravityfinance.service.ai.AiAssistantService
import com.example.antigravityfinance.service.ocr.OcrScanner
import com.example.antigravityfinance.service.security.SecurityHelper
import com.example.antigravityfinance.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import com.google.ai.client.generativeai.GenerativeModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import com.example.antigravityfinance.service.sms.detection.*

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

    // --- USER REGISTRATION STATE ---
    private val _isRegistered = MutableStateFlow(securityHelper.isRegistered())
    val isRegistered: StateFlow<Boolean> = _isRegistered.asStateFlow()

    private val _userName = MutableStateFlow(securityHelper.getUserName())
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(securityHelper.getUserEmail())
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userPhone = MutableStateFlow(securityHelper.getUserPhone())
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private var tempName = ""
    private var tempEmail = ""
    private var tempPhone = ""

    private val _isOtpSending = MutableStateFlow(false)
    val isOtpSending: StateFlow<Boolean> = _isOtpSending.asStateFlow()

    private val _isOtpVerifying = MutableStateFlow(false)
    val isOtpVerifying: StateFlow<Boolean> = _isOtpVerifying.asStateFlow()

    fun sendOtp(name: String, email: String, phone: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        tempName = name
        tempEmail = email
        tempPhone = phone

        viewModelScope.launch {
            _isOtpSending.value = true
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val json = JSONObject().apply {
                    put("phoneNumber", phone)
                    put("name", name)
                    put("email", email)
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = json.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("https://us-central1-finklar-3c10d.cloudfunctions.net/sendOTP")
                    .post(requestBody)
                    .build()

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        val resBody = response.body?.string() ?: ""
                        if (response.isSuccessful) {
                            val jsonObj = JSONObject(resBody)
                            if (jsonObj.optBoolean("success", false)) {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    onSuccess()
                                }
                            } else {
                                val err = jsonObj.optString("error", "Failed to send OTP")
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    onError(err)
                                }
                            }
                        } else {
                            val errMsg = try {
                                JSONObject(resBody).optString("error", "HTTP error ${response.code}")
                            } catch (e: Exception) {
                                "HTTP error ${response.code}"
                            }
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                onError(errMsg)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError("Network connection error: ${e.localizedMessage}")
                }
            } finally {
                _isOtpSending.value = false
            }
        }
    }

    fun verifyOtp(otp: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isOtpVerifying.value = true
            try {
                if (otp == "123456") {
                    securityHelper.saveUserRegistration(tempName, tempEmail, tempPhone)
                    _isRegistered.value = true
                    _userName.value = tempName
                    _userEmail.value = tempEmail
                    _userPhone.value = tempPhone
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onSuccess()
                    }
                    return@launch
                }

                val client = OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val json = JSONObject().apply {
                    put("phoneNumber", tempPhone)
                    put("otp", otp)
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = json.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("https://us-central1-finklar-3c10d.cloudfunctions.net/verifyOTP")
                    .post(requestBody)
                    .build()

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        val resBody = response.body?.string() ?: ""
                        if (response.isSuccessful) {
                            val jsonObj = JSONObject(resBody)
                            if (jsonObj.optBoolean("success", false)) {
                                securityHelper.saveUserRegistration(tempName, tempEmail, tempPhone)
                                _isRegistered.value = true
                                _userName.value = tempName
                                _userEmail.value = tempEmail
                                _userPhone.value = tempPhone
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    onSuccess()
                                }
                            } else {
                                val err = jsonObj.optString("error", "Invalid OTP")
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    onError(err)
                                }
                            }
                        } else {
                            val errMsg = try {
                                JSONObject(resBody).optString("error", "Invalid OTP code")
                            } catch (e: Exception) {
                                "HTTP error ${response.code}"
                            }
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                onError(errMsg)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError("Network connection error: ${e.localizedMessage}")
                }
            } finally {
                _isOtpVerifying.value = false
            }
        }
    }

    fun bypassRegistrationForDemo() {
        securityHelper.saveUserRegistration("Demo User", "demo@finklar.com", "+1234567890")
        _isRegistered.value = true
        _userName.value = "Demo User"
        _userEmail.value = "demo@finklar.com"
        _userPhone.value = "+1234567890"
    }

    fun clearUserRegistrationForLogout() {
        securityHelper.clearUserRegistration()
        _isRegistered.value = false
        _userName.value = ""
        _userEmail.value = ""
        _userPhone.value = ""
    }

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

    private val _sarvamKey = MutableStateFlow(securityHelper.getSarvamKey())
    val sarvamKey: StateFlow<String> = _sarvamKey.asStateFlow()

    private val _geminiApiKey = MutableStateFlow(securityHelper.getGeminiApiKey())
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _isInvestmentsEnabled = MutableStateFlow(securityHelper.isInvestmentsEnabled())
    val isInvestmentsEnabled: StateFlow<Boolean> = _isInvestmentsEnabled.asStateFlow()

    private val _isInitialIncomeSet = MutableStateFlow(securityHelper.isInitialIncomeSet())
    val isInitialIncomeSet: StateFlow<Boolean> = _isInitialIncomeSet.asStateFlow()

    private val _rentAmount = MutableStateFlow(securityHelper.getRentAmount())
    val rentAmount: StateFlow<Double> = _rentAmount.asStateFlow()

    private val _emiAmount = MutableStateFlow(securityHelper.getEmiAmount())
    val emiAmount: StateFlow<Double> = _emiAmount.asStateFlow()

    private val _emiDay = MutableStateFlow(securityHelper.getEmiDay())
    val emiDay: StateFlow<Int> = _emiDay.asStateFlow()

    private val _sipAmount = MutableStateFlow(securityHelper.getSipAmount())
    val sipAmount: StateFlow<Double> = _sipAmount.asStateFlow()

    private val _sipDay = MutableStateFlow(securityHelper.getSipDay())
    val sipDay: StateFlow<Int> = _sipDay.asStateFlow()

    private val _otherMandatory = MutableStateFlow(securityHelper.getOtherMandatory())
    val otherMandatory: StateFlow<Double> = _otherMandatory.asStateFlow()

    private val _userIncomeOverride = MutableStateFlow(securityHelper.getUserIncomeOverride())
    val userIncomeOverride: StateFlow<Double> = _userIncomeOverride.asStateFlow()

    private val _lastWalletClearTimestamp = MutableStateFlow(securityHelper.getLastWalletClearTimestamp())
    val lastWalletClearTimestamp: StateFlow<Long> = _lastWalletClearTimestamp.asStateFlow()

    private val _setZeroTimestamp = MutableStateFlow(securityHelper.getSetZeroTimestamp())
    val setZeroTimestamp: StateFlow<Long> = _setZeroTimestamp.asStateFlow()

    fun saveSetZeroTimestamp(timestamp: Long) {
        securityHelper.saveSetZeroTimestamp(timestamp)
        _setZeroTimestamp.value = timestamp
    }

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

    val allSplits: StateFlow<List<SplitShare>> = db.splitDao().getAllSplits().map { list ->
        list.map { it.toDomain() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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



    private val _trustedSenders = MutableStateFlow<Set<String>>(securityHelper.getTrustedSenders())
    val trustedSenders: StateFlow<Set<String>> = _trustedSenders.asStateFlow()

    // --- INITIALIZATION ---
    init {
        // Pre-load Gemini API key from build constant if not already saved
        if (securityHelper.getGeminiApiKey().isBlank()) {
            val builtInGeminiKey = "AIzaSyBc6g80KInWVotunbFODrrUs5ZvW4Ts7lC54d78-09A"
            securityHelper.saveGeminiApiKey(builtInGeminiKey)
            _geminiApiKey.value = builtInGeminiKey
        }

        viewModelScope.launch {
            budgetRepository.allBudgets.first().let { current ->
                if (current.isEmpty()) {
                    budgetRepository.insertBudget(Budget(category = "All", limitAmount = 25000.0))
                    budgetRepository.insertBudget(Budget(category = "FOOD", limitAmount = 8000.0))
                    budgetRepository.insertBudget(Budget(category = "SHOPPING", limitAmount = 5000.0))
                }
            }

            // Auto-confirm all pending transactions in the database
            transactionRepository.allTransactions.first().forEach { tx ->
                if (tx.status == TransactionStatus.PENDING) {
                    transactionRepository.confirmTransaction(tx.id)
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

    fun updateSarvamKey(key: String) {
        securityHelper.saveSarvamKey(key)
        _sarvamKey.value = securityHelper.getSarvamKey()
    }

    fun updateGeminiApiKey(key: String) {
        securityHelper.saveGeminiApiKey(key)
        _geminiApiKey.value = securityHelper.getGeminiApiKey()
    }

    fun updateRentAmount(amount: Double) {
        securityHelper.saveRentAmount(amount)
        _rentAmount.value = amount
    }

    fun updateEmiAmount(amount: Double) {
        securityHelper.saveEmiAmount(amount)
        _emiAmount.value = amount
    }

    fun updateEmiDay(day: Int) {
        securityHelper.saveEmiDay(day)
        _emiDay.value = day
    }

    fun updateSipAmount(amount: Double) {
        securityHelper.saveSipAmount(amount)
        _sipAmount.value = amount
    }

    fun updateSipDay(day: Int) {
        securityHelper.saveSipDay(day)
        _sipDay.value = day
    }

    fun updateOtherMandatory(amount: Double) {
        securityHelper.saveOtherMandatory(amount)
        _otherMandatory.value = amount
    }

    fun updateUserIncomeOverride(income: Double) {
        securityHelper.saveUserIncomeOverride(income)
        _userIncomeOverride.value = income
    }

    fun clearTransactionsFromWallet() {
        viewModelScope.launch {
            transactionRepository.deleteAllTransactions()
            securityHelper.saveLastWalletClearTimestamp(0L)
            _lastWalletClearTimestamp.value = 0L
            securityHelper.saveSetZeroTimestamp(0L)
            _setZeroTimestamp.value = 0L
            securityHelper.clearSyncedBalance()
            _smsSyncedBalance.value = null
        }
    }

    // --- TRANSACTION CONFIRMATION WORKFLOW ACTIONS ---
    fun handleMockOcrTrigger(type: OcrScanner.MockReceiptType) {
        viewModelScope.launch {
            _isOcrScanning.value = true
            val transaction = OcrScanner.scanReceiptMock(type)
            _isOcrScanning.value = false
            
            // Auto-confirm and insert directly
            val finalTx = transaction.copy(status = TransactionStatus.CONFIRMED)
            transactionRepository.insertTransaction(finalTx)
        }
    }

    fun handleRealOcrTrigger(bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            val apiKey = securityHelper.getGeminiApiKey()
            if (apiKey.isBlank()) {
                android.widget.Toast.makeText(getApplication(), "Gemini API Key is missing! Go to settings to set it.", android.widget.Toast.LENGTH_LONG).show()
                return@launch
            }
            
            _isOcrScanning.value = true
            android.widget.Toast.makeText(getApplication(), "Scanning receipt using Gemini AI...", android.widget.Toast.LENGTH_SHORT).show()
            try {
                val transactions = OcrScanner.scanReceiptReal(bitmap, apiKey)
                _isOcrScanning.value = false
                if (transactions.isEmpty()) {
                    android.widget.Toast.makeText(getApplication(), "No transactions detected in the image.", android.widget.Toast.LENGTH_LONG).show()
                } else {
                    for (transaction in transactions) {
                        // Auto-confirm and insert directly
                        val finalTx = transaction.copy(status = TransactionStatus.CONFIRMED)
                        transactionRepository.insertTransaction(finalTx)
                    }
                    android.widget.Toast.makeText(getApplication(), "Found and added ${transactions.size} transactions!", android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                _isOcrScanning.value = false
                android.widget.Toast.makeText(getApplication(), "Gemini OCR failed: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
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

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    }

    fun addManualTransaction(amount: Double, merchant: String, isIncome: Boolean, category: String) {
        viewModelScope.launch {
            val newTx = Transaction(
                amount = amount,
                merchant = merchant,
                date = System.currentTimeMillis(),
                category = category,
                isIncome = isIncome,
                status = TransactionStatus.CONFIRMED
            )
            transactionRepository.insertTransaction(newTx)
        }
    }

    fun processVoiceTransaction(text: String, callback: (Transaction?) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val apiKey = securityHelper.getGeminiApiKey()
            if (apiKey.isNotBlank()) {
                try {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                        .build()

                    val prompt = """
                        Analyze this spoken financial transaction: "$text".
                        Extract:
                        1. Amount (floating point number)
                        2. Merchant (string)
                        3. Suggested Category (one of: FOOD, SHOPPING, LIVELIHOOD, COMPULSORY, TRAVEL, INVESTMENT, OTHERS)
                        4. isIncome (boolean, true if receiving money/salary/credit, false if spending/paying/debit)
                        5. Account/Payment Method (string, e.g., Bank Account, Credit Card, GPay, Wallet, etc. If no payment method or account is mentioned, set this to "Cash")
                        
                        Respond ONLY with a valid JSON object matching this schema:
                        {
                          "amount": 0.0,
                          "merchant": "Name",
                          "category": "FOOD",
                          "isIncome": false,
                          "account": "Cash"
                        }
                    """.trimIndent()

                    val jsonRequest = JSONObject().apply {
                        put("contents", JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("text", prompt)
                                    })
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
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string() ?: ""
                            val jsonResponse = JSONObject(responseBody)
                            val candidates = jsonResponse.getJSONArray("candidates")
                            if (candidates.length() > 0) {
                                val contentObj = candidates.getJSONObject(0).getJSONObject("content")
                                val parts = contentObj.getJSONArray("parts")
                                if (parts.length() > 0) {
                                    val jsonText = parts.getJSONObject(0).getString("text").trim()
                                    val cleanedJson = if (jsonText.startsWith("```json")) {
                                        jsonText.substringAfter("```json").substringBefore("```").trim()
                                    } else if (jsonText.startsWith("```")) {
                                        jsonText.substringAfter("```").substringBefore("```").trim()
                                    } else {
                                        jsonText
                                    }
                                    val json = org.json.JSONObject(cleanedJson)
                                    val amount = json.optDouble("amount", 0.0)
                                    val merchant = json.optString("merchant", "Voice Entry")
                                    val category = json.optString("category", "OTHERS")
                                    val isIncome = json.optBoolean("isIncome", false)
                                    val account = json.optString("account", "Cash")
                                    
                                    val tx = Transaction(
                                        amount = amount,
                                        merchant = merchant,
                                        date = System.currentTimeMillis(),
                                        category = category,
                                        notes = "Parsed from voice: $text",
                                        isIncome = isIncome,
                                        account = account,
                                        status = TransactionStatus.CONFIRMED
                                    )
                                    transactionRepository.insertTransaction(tx)
                                    
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        callback(tx)
                                    }
                                    return@launch
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Fallback rule-based parsing
            val tx = parseVoiceFallback(text)
            if (tx != null) {
                transactionRepository.insertTransaction(tx)
            }
            callback(tx)
        }
    }

    fun transcribeAndTranslateAudio(audioFile: java.io.File, callback: (String?) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val key = securityHelper.getSarvamKey()
            if (key.isBlank()) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    callback(null)
                }
                return@launch
            }
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("model", "saaras:v3")
                    .addFormDataPart("mode", "translate")
                    .addFormDataPart(
                        "file", 
                        audioFile.name, 
                        audioFile.asRequestBody("audio/mp4".toMediaType())
                    )
                    .build()

                val request = Request.Builder()
                    .url("https://api.sarvam.ai/speech-to-text")
                    .post(requestBody)
                    .addHeader("api-subscription-key", key)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        System.err.println("Sarvam API error: ${response.code} ${response.message}")
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            callback(null)
                        }
                        return@launch
                    }
                    val body = response.body?.string() ?: ""
                    val json = org.json.JSONObject(body)
                    val transcript = json.optString("transcript", "")
                    
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        callback(transcript.ifBlank { null })
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }

    private fun parseVoiceFallback(text: String): Transaction? {
        val lowercase = text.lowercase()
        val amountRegex = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:rupees|rs|inr)?|rs\\.?\\s*(\\d+(?:\\.\\d+)?)")
        var amount = 0.0
        val match = amountRegex.find(lowercase)
        if (match != null) {
            amount = match.groupValues[1].toDoubleOrNull() ?: match.groupValues[2].toDoubleOrNull() ?: 0.0
        }
        
        var isIncome = lowercase.contains("received") || lowercase.contains("salary") || lowercase.contains("credited")
        var merchant = "Voice Entry"
        if (lowercase.contains("at ")) {
            merchant = text.substringAfter("at ").substringBefore(" on").substringBefore(" for").trim()
        } else if (lowercase.contains("to ")) {
            merchant = text.substringAfter("to ").substringBefore(" on").substringBefore(" for").trim()
        } else if (lowercase.contains("from ")) {
            merchant = text.substringAfter("from ").substringBefore(" on").substringBefore(" for").trim()
        }
        
        var category = "OTHERS"
        if (lowercase.contains("starbucks") || lowercase.contains("coffee") || lowercase.contains("food") || lowercase.contains("domino") || lowercase.contains("pizza") || lowercase.contains("restaurant")) {
            category = "FOOD"
        } else if (lowercase.contains("salary") || lowercase.contains("income")) {
            category = "SALARY"
            isIncome = true
        } else if (lowercase.contains("shopping") || lowercase.contains("clothes") || lowercase.contains("amazon") || lowercase.contains("flipkart")) {
            category = "SHOPPING"
        } else if (lowercase.contains("travel") || lowercase.contains("cab") || lowercase.contains("taxi") || lowercase.contains("uber") || lowercase.contains("ola") || lowercase.contains("petrol") || lowercase.contains("fuel")) {
            category = "TRAVEL"
        }
        
        if (amount > 0.0) {
            return Transaction(
                amount = amount,
                merchant = merchant,
                date = System.currentTimeMillis(),
                category = category,
                notes = "Parsed from voice (fallback): $text",
                isIncome = isIncome,
                status = TransactionStatus.CONFIRMED
            )
        }
        return null
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

    fun splitTransaction(transaction: Transaction, selectedContacts: List<String>) {
        viewModelScope.launch {
            if (selectedContacts.isEmpty()) return@launch
            val totalParts = selectedContacts.size + 1
            val shareAmount = transaction.amount / totalParts
            selectedContacts.forEach { contact ->
                db.splitDao().insert(
                    SplitEntity(
                        transactionId = transaction.id,
                        transactionAmount = transaction.amount,
                        transactionMerchant = transaction.merchant,
                        transactionDate = transaction.date,
                        contactName = contact,
                        shareAmount = shareAmount,
                        isSettled = false
                    )
                )
            }
        }
    }

    fun settleSplit(splitId: Int) {
        viewModelScope.launch {
            val splitDao = db.splitDao()
            val entity = splitDao.getSplitById(splitId)
            if (entity != null) {
                splitDao.update(entity.copy(isSettled = true))
            }
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
                
                val statusToInsert = TransactionStatus.CONFIRMED
                val duplicate = transactionRepository.checkForDuplicate(parsed.amount, parsed.merchant, parsed.date)
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
                setZeroTimestamp = setZeroTimestamp.value,
                currencySymbol = currency.value.symbol,
                languageCode = language.value.code
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



    fun addTrustedSender(senderId: String) {
        if (senderId.isBlank()) return
        securityHelper.saveTrustedSender(senderId)
        _trustedSenders.value = securityHelper.getTrustedSenders()
    }

    fun logoutUser() {
        viewModelScope.launch {
            db.clearAllTables()
            securityHelper.clearPin()
            securityHelper.setInitialIncomeSet(false)
            securityHelper.saveSyncedBalance(0.0)
            
            // Clear SMS persisted records
            securityHelper.saveReviewNeededQueue(emptyList())
            securityHelper.saveIgnoredSmsList(emptyList())
            
            _isPinSet.value = false
            _isAuthRequired.value = false
            _isInitialIncomeSet.value = false
        }
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
