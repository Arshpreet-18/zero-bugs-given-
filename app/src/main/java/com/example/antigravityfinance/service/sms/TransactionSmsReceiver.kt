package com.example.antigravityfinance.service.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.antigravityfinance.data.local.db.FinanceDatabase
import com.example.antigravityfinance.data.model.Transaction
import com.example.antigravityfinance.data.model.TransactionStatus
import com.example.antigravityfinance.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

data class ParsedSmsResult(
    val transaction: Transaction,
    val balance: Double?
)

class TransactionSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in messages) {
                val body = message.messageBody
                val sender = message.originatingAddress ?: "Unknown"
                val timestamp = message.timestampMillis
                parseAndProcessSms(context, body, sender, timestamp)
            }
        }
    }

    fun parseAndProcessSms(
        context: Context, 
        body: String, 
        senderId: String = "Unknown", 
        timestamp: Long = System.currentTimeMillis()
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val securityHelper = com.example.antigravityfinance.service.security.SecurityHelper(context.applicationContext)
            val trustedSenders = securityHelper.getTrustedSenders()
            val result = com.example.antigravityfinance.service.sms.detection.SmsDetectionModule.detect(body, senderId, timestamp, trustedSenders)

            val db = FinanceDatabase.getDatabase(context)
            val repo = TransactionRepository(db.transactionDao(), db.recurringMerchantDao(), db.budgetDao())

            if (result.availableBalance != null) {
                securityHelper.saveSyncedBalance(result.availableBalance)
            }

            if (result.autoAdd && result.amount != null) {
                var resolvedMerchant = result.merchantOrSender
                if (resolvedMerchant == "Unknown Merchant" && senderId.isNotBlank() && senderId != "Unknown") {
                    val contactName = SmsInboxScanner.getContactName(context, senderId)
                    if (contactName != null) {
                        resolvedMerchant = contactName
                    }
                }
                val duplicate = repo.checkForDuplicate(result.amount, resolvedMerchant, result.smsTimestamp)
                if (duplicate == null) {
                    val category = AutoCategorizer.categorize(resolvedMerchant)
                    val tx = Transaction(
                        amount = result.amount,
                        merchant = resolvedMerchant,
                        date = result.smsTimestamp,
                        category = category,
                        notes = result.rawSms,
                        account = result.bankName.ifBlank { "Bank SMS" },
                        status = TransactionStatus.CONFIRMED,
                        isIncome = result.transactionType == com.example.antigravityfinance.service.sms.detection.TransactionType.CREDIT,
                        detectedFromSms = true
                    )
                    repo.insertTransaction(tx)
                } else {
                    Log.d("SMSReceiver", "Duplicate transaction detected: #${duplicate.id}")
                }
            }
        }
    }
}

object SmsParser {
    fun parse(body: String): ParsedSmsResult? {
        val text = body.lowercase()

        val debitKeywords = listOf(
            "debited", "spent", "charged", "withdrawn", "withdrawal", "paid",
            "payment", "sent", "purchase", "dr", "debit"
        )
        val creditKeywords = listOf(
            "credited", "received", "deposited", "deposit", "refund", "salary",
            "cashback", "reversed", "reversal", "cr"
        )
        val isCredit = creditKeywords.any { keyword -> text.containsWord(keyword) }
        val isDebit = debitKeywords.any { keyword -> text.containsWord(keyword) } ||
            (text.containsWord("transferred") && !isCredit) ||
            (text.containsWord("transfer") && !isCredit)

        if (!isCredit && !isDebit) return null

        val amountRegexes = listOf(
            "(?:debited|credited|spent|paid|received|sent|charged|withdrawn|deposited|refund(?:ed)?|payment\\s+of|purchase\\s+of|txn\\s+(?:of|for)|transaction\\s+(?:of|for))\\s*(?:by|with|for|of)?\\s*(?:rs\\.?|inr|\\p{Sc})?\\s*([\\d,]+(?:\\.\\d{1,2})?)",
            "(?:rs\\.?|inr|\\p{Sc})\\s*([\\d,]+(?:\\.\\d{1,2})?)\\s*(?:has\\s+been\\s+)?(?:debited|credited|spent|paid|received|sent|charged|withdrawn|deposited|refund(?:ed)?)",
            "(?:rs\\.?|inr|\\p{Sc})\\s*([\\d,]+(?:\\.\\d{1,2})?)"
        )
        var amount: Double? = null
        for (regex in amountRegexes) {
            val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val amountStr = matcher.group(1)?.replace(",", "")
                amount = amountStr?.toDoubleOrNull()
                if (amount != null) break
            }
        }
        if (amount == null) return null

        val isIncome = isCredit && !isDebit

        val balanceRegex = "(?:bal(?:ance)?|avl(?:\\.?\\s+bal(?:ance)?)?|available\\s+balance|avail\\s+bal|bal:)\\s*(?:is)?\\s*(?:rs\\.?|inr|\\p{Sc})?\\s*([\\d,]+(?:\\.\\d{1,2})?)"
        val balancePattern = Pattern.compile(balanceRegex, Pattern.CASE_INSENSITIVE)
        val balanceMatcher = balancePattern.matcher(text)
        var balance: Double? = null
        if (balanceMatcher.find()) {
            val balStr = balanceMatcher.group(1)?.replace(",", "")
            balance = balStr?.toDoubleOrNull()
        }

        var merchant = "Unknown Merchant"
        val merchantRegexes = listOf(
            "at\\s+([a-zA-Z0-9\\s\\.\\-_@]+?)(?:\\s+on|\\s+for|\\s+using|\\s+ending|\\.|$)",
            "paid\\s+to\\s+([a-zA-Z0-9\\s\\.\\-_@]+?)(?:\\s+on|\\s+for|\\s+using|\\.|$)",
            "to\\s+([a-zA-Z0-9\\s\\.\\-_@]+?)(?:\\s+on|\\s+for|\\s+using|\\.|$)",
            "from\\s+([a-zA-Z0-9\\s\\.\\-_@]+?)(?:\\s+on|\\s+for|\\s+using|\\.|$)",
            "towards\\s+([a-zA-Z0-9\\s\\.\\-_@]+?)(?:\\s+on|\\s+for|\\s+using|\\.|$)",
            "on\\s+([a-zA-Z0-9\\s\\.\\-_@]+?)(?:\\s+using|\\.|$)",
            "info:\\s*([a-zA-Z0-9\\s\\.\\-_@/]+?)(?:\\s+on|\\s+for|\\.|$)"
        )
        for (regex in merchantRegexes) {
            val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                var match = matcher.group(1)?.trim() ?: ""
                if (match.contains("@")) {
                    match = match.split("@")[0].trim()
                }
                val normalizedMatch = match.lowercase()
                if (
                    match.isNotEmpty() &&
                    !normalizedMatch.contains("account") &&
                    !normalizedMatch.contains("card") &&
                    !normalizedMatch.contains("avl bal")
                ) {
                    merchant = match
                    break
                }
            }
        }

        val category = AutoCategorizer.categorize(merchant)

        val transaction = Transaction(
            amount = amount,
            merchant = merchant,
            date = System.currentTimeMillis(),
            category = category,
            notes = "Parsed from SMS",
            account = if (text.contains("card")) "Credit Card" else "Bank Account",
            status = TransactionStatus.CONFIRMED,
            isIncome = isIncome,
            isRecurring = false,
            detectedFromSms = true
        )

        return ParsedSmsResult(transaction, balance)
    }

    private fun String.containsWord(keyword: String): Boolean {
        return Pattern.compile("\\b${Pattern.quote(keyword)}\\b", Pattern.CASE_INSENSITIVE)
            .matcher(this)
            .find()
    }
}

object AutoCategorizer {
    fun categorize(merchant: String): String {
        val m = merchant.lowercase()
        return when {
            m.contains("starbucks") || m.contains("cafe") || m.contains("restaurant") || m.contains("zomato") || m.contains("swiggy") || m.contains("food") || m.contains("canteen") -> "FOOD"
            m.contains("amazon") || m.contains("flipkart") || m.contains("myntra") || m.contains("shopping") || m.contains("mall") || m.contains("store") || m.contains("clothing") -> "SHOPPING"
            m.contains("grocery") || m.contains("supermarket") || m.contains("blinkit") || m.contains("zepto") || m.contains("bigbasket") || m.contains("mart") || m.contains("netflix") || m.contains("spotify") || m.contains("hotstar") || m.contains("cinema") || m.contains("theater") || m.contains("bookmyshow") || m.contains("entertainment") -> "LIVELIHOOD"
            m.contains("uber") || m.contains("ola") || m.contains("metro") || m.contains("petrol") || m.contains("fuel") || m.contains("travel") || m.contains("cab") || m.contains("train") -> "TRAVEL"
            m.contains("electricity") || m.contains("water") || m.contains("recharge") || m.contains("jio") || m.contains("airtel") || m.contains("bill") || m.contains("gas") || m.contains("rent") || m.contains("house") || m.contains("landlord") -> "COMPULSORY"
            m.contains("zerodha") || m.contains("groww") || m.contains("stock") || m.contains("mutual fund") || m.contains("sip") || m.contains("invest") -> "INVESTMENT"
            m.contains("salary") || m.contains("paycheck") || m.contains("employer") || m.contains("dividend") -> "SALARY"
            else -> "OTHERS"
        }
    }
}
