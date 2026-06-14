package com.example.antigravityfinance.service.sms.detection

import java.util.regex.Pattern
import kotlin.math.abs

object SmsDetectionModule {

    // Bank Verifier dictionary and matching logic
    object BankVerifier {
        private val bankDict = mapOf(
            "HDF" to listOf("HDFC Bank"),
            "SBI" to listOf("State Bank of India"),
            "ICI" to listOf("ICICI Bank"),
            "AXI" to listOf("Axis Bank"),
            "KOT" to listOf("Kotak Mahindra Bank"),
            "PNB" to listOf("Punjab National Bank"),
            "BOB" to listOf("Bank of Baroda"),
            "CAN" to listOf("Canara Bank"),
            "IDF" to listOf("IDFC First Bank"),
            "IND" to listOf("IndusInd Bank", "Indian Bank") // Ambiguous
        )

        fun resolveBankCode(code: String): List<String> =
            bankDict[code.uppercase()] ?: emptyList()

        fun getPotentialBankFromSender(senderId: String): String {
            val normalized = senderId.uppercase()
            for ((code, bankList) in bankDict) {
                if (normalized.contains(code)) {
                    return if (bankList.size == 1) bankList.first() else "Ambiguous Bank (${bankList.joinToString(" / ")})"
                }
            }
            return "Unknown Bank"
        }

        fun isStandardBankSenderPattern(senderId: String): Boolean {
            // Standard bank DLT header: e.g. "VK-HDFCBK", "AD-SBICRD", "BP-PAYTMB"
            val dltPattern = Pattern.compile("^[a-zA-Z]{2}-[a-zA-Z0-9]{4,10}$")
            if (dltPattern.matcher(senderId).matches()) return true
            // Shortcodes: e.g. GPAY, PAYTM, PHONEPE, AMAZON
            val shortNames = listOf("GPAY", "PAYTM", "PHONEPE", "BHARPE", "AMAZON", "FLIPKRT",
                "HDFCBK", "SBIPSG", "ICICIB", "AXISBK", "KOTAKB", "IDFCBK", "BOBINF",
                "CANBNK", "PNBSMS", "INDUSB", "YESBNK")
            val upper = senderId.uppercase()
            return shortNames.any { upper.contains(it) }
        }
    }

    // Spam and Loan Ad checks
    object SpamFilter {
        private val loanKeywords = listOf(
            "loan", "pre-approved", "instant cash", "apply now", "limited offer", 
            "emi offer", "credit card offer"
        )
        
        private val promoKeywords = listOf(
            "cashback offer", "reward", "win", "scratch card", "claim now", 
            "congratulations", "voucher", "coupon"
        )

        private val linkPatterns = listOf(
            "http://", "https://", "bit.ly", "t.co"
        )

        private val securityKeywords = listOf(
            "otp", "one-time password", "verification code", "security code", "netbanking password"
        )

        fun containsLoanIntent(text: String): Boolean =
            loanKeywords.any { text.contains(it, ignoreCase = true) }

        fun containsPromoIntent(text: String): Boolean =
            promoKeywords.any { text.contains(it, ignoreCase = true) }

        fun containsLink(text: String): Boolean =
            linkPatterns.any { text.contains(it, ignoreCase = true) } || text.contains("download app", ignoreCase = true)

        fun containsSecurityOtp(text: String): Boolean =
            securityKeywords.any { text.contains(it, ignoreCase = true) }
    }

    // Extraction rules
    object TransactionExtractor {
        fun extractAmount(text: String): Double? {
            val amountRegexes = listOf(
                "(?:debited|credited|spent|paid|received|sent|charged|withdrawn|deposited|refund(?:ed)?|payment\\s+of|purchase\\s+of|txn\\s+(?:of|for)|transaction\\s+(?:of|for))\\s*(?:by|with|for|of)?\\s*(?:rs\\.?|inr|₹|\\p{Sc})?\\s*([\\d,]+(?:\\.\\d{1,2})?)",
                "(?:rs\\.?|inr|₹|\\p{Sc})\\s*([\\d,]+(?:\\.\\d{1,2})?)\\s*(?:has\\s+been\\s+)?(?:debited|credited|spent|paid|received|sent|charged|withdrawn|deposited|refund(?:ed)?)",
                "(?:rs\\.?|inr|₹|\\p{Sc})\\s*([\\d,]+(?:\\.\\d{1,2})?)"
            )
            for (regex in amountRegexes) {
                val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                val matcher = pattern.matcher(text)
                if (matcher.find()) {
                    val amountStr = matcher.group(1).replace(",", "")
                    val amt = amountStr.toDoubleOrNull()
                    if (amt != null && amt > 0.0) return amt
                }
            }
            return null
        }

        fun extractMaskedAccount(text: String): String? {
            val patterns = listOf(
                "a/c\\s*(?:ending|no|x+)*\\s*([0-9]*x+[0-9]+|[0-9]{3,})",
                "account\\s*(?:ending|no|x+)*\\s*([0-9]*x+[0-9]+|[0-9]{3,})",
                "ending\\s+in\\s+([0-9]{3,})",
                "card\\s*(?:ending|no|x+)*\\s*([0-9]*x+[0-9]+|[0-9]{3,})"
            )
            for (regex in patterns) {
                val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                val matcher = pattern.matcher(text)
                if (matcher.find()) {
                    val match = matcher.group(1)
                    if (match.any { it.isDigit() }) {
                        return match.filter { it.isDigit() }.takeLast(4)
                    }
                }
            }
            return null
        }

        fun extractUpiId(text: String): String? {
            val pattern = Pattern.compile("[a-zA-Z0-9.\\-_]+@[a-zA-Z]{3,}", Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                return matcher.group()
            }
            return null
        }

        fun extractRefNumber(text: String): String? {
            val patterns = listOf(
                "(?:upi\\s+ref\\s+no|ref\\s+no|txn\\s+id|upi\\s+ref|ref)\\s*[:\\-\\s#]*([a-zA-Z0-9]{8,16})",
                "txn\\s+([a-zA-Z0-9]{8,16})"
            )
            for (regex in patterns) {
                val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                val matcher = pattern.matcher(text)
                if (matcher.find()) {
                    return matcher.group(1)
                }
            }
            return null
        }

        fun extractBalance(text: String): Double? {
            val balanceRegex = "(?:bal(?:ance)?|avl(?:\\.?\\s+bal(?:ance)?)?|available\\s+balance|avail\\s+bal|bal:)\\s*(?:is)?\\s*(?:rs\\.?|inr|₹|\\p{Sc})?\\s*([\\d,]+(?:\\.\\d{1,2})?)"
            val balancePattern = Pattern.compile(balanceRegex, Pattern.CASE_INSENSITIVE)
            val balanceMatcher = balancePattern.matcher(text)
            if (balanceMatcher.find()) {
                val balStr = balanceMatcher.group(1).replace(",", "")
                return balStr.toDoubleOrNull()
            }
            return null
        }

        fun extractMerchantOrSender(text: String): String {
            var merchant = "Unknown Merchant"
            val merchantRegexes = listOf(
                "at\\s+([a-zA-Z0-9\\s\\.\\-_@]+?)(?:\\s+on|\\s+for|\\s+using|\\s+ending|\\.|$)",
                "paid\\s+to\\s+([a-zA-Z0-9\\s\\.\\-_@]+?)(?:\\s+on|\\s+for|\\s+using|\\.|$)",
                "to\\s+([a-zA-Z0-9\\s\\.\\-_@]+?)(?:\\s+on|\\s+for|\\s+using|\\.|$)",
                "from\\s+([a-zA-Z0-9\\s\\.\\-_@]+?)(?:\\s+on|\\s+for|\\s+using|\\.|$)",
                "towards\\s+([a-zA-Z0-9\\s\\.\\-_@]+?)(?:\\s+on|\\s+for|\\s+using|\\.|$)"
            )
            for (regex in merchantRegexes) {
                val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                val matcher = pattern.matcher(text)
                if (matcher.find()) {
                    var match = matcher.group(1).trim()
                    if (match.contains("@")) {
                        match = match.split("@")[0].trim()
                    }
                    val normalizedMatch = match.lowercase()
                    if (
                        match.isNotEmpty() &&
                        !normalizedMatch.contains("account") &&
                        !normalizedMatch.contains("card") &&
                        !normalizedMatch.contains("avl bal") &&
                        !normalizedMatch.contains("balance")
                    ) {
                        merchant = match
                        break
                    }
                }
            }
            return merchant
        }
    }

    // Orchestrator method
    suspend fun detect(
        body: String,
        senderId: String,
        timestamp: Long,
        trustedSenders: Set<String> = emptySet()
    ): SmsDetectionResult {

        val text = body.lowercase()
        val reason = mutableListOf<String>()

        val isTrusted = trustedSenders.contains(senderId.uppercase()) || 
                trustedSenders.contains(senderId)

        val isStandardBank = BankVerifier.isStandardBankSenderPattern(senderId)
        val extractedBank = BankVerifier.getPotentialBankFromSender(senderId)
        
        // 1. Extractions
        val amount = TransactionExtractor.extractAmount(body)
        val maskedAcc = TransactionExtractor.extractMaskedAccount(body)
        val upiId = TransactionExtractor.extractUpiId(body)
        val refNo = TransactionExtractor.extractRefNumber(body)
        val balance = TransactionExtractor.extractBalance(body)
        val merchant = TransactionExtractor.extractMerchantOrSender(body)

        // 2. Identify transaction keywords
        val creditKeywords = listOf("credited", "credited to", "deposited", "deposit", "received", "salary", "refund", "cashback", "reversed", "reversal",
            "money received", "amount received", "upi cr", "cr.", "cr ")
        val debitKeywords = listOf("debited", "debited from", "spent", "charged", "withdrawn", "withdrawal", "paid", "payment", "sent", "purchase",
            "deducted", "upi dr", "dr.", "dr ", "transaction successful", "txn successful", "transfer successful")

        val hasCreditWord = creditKeywords.any { text.contains(it) } || text.contains("cr")
        val hasDebitWord = debitKeywords.any { text.contains(it) } || text.contains("dr")

        var detectedType = TransactionType.NONE
        if (hasCreditWord && !hasDebitWord) {
            detectedType = TransactionType.CREDIT
        } else if (hasDebitWord && !hasCreditWord) {
            detectedType = TransactionType.DEBIT
        } else if (hasDebitWord && hasCreditWord) {
            // Double check order or keyword priority
            if (text.indexOf("debited") < text.indexOf("credited") && text.indexOf("debited") != -1) {
                detectedType = TransactionType.DEBIT
            } else {
                detectedType = TransactionType.CREDIT
            }
        }

        // 3. Scoring System
        var score = 0

        // Trusted sender
        if (isTrusted || isStandardBank) {
            score += 25
            reason.add("Trusted sender ID matched (+25)")
        }

        // Keywords
        if (hasCreditWord || hasDebitWord) {
            score += 20
            reason.add("Debit/Credit transaction keyword found (+20)")
        }

        // Amount
        if (amount != null) {
            score += 15
            reason.add("Amount detected (+15)")
        }

        // Masked Account/Card
        if (maskedAcc != null) {
            score += 15
            reason.add("Masked account/card number detected (+15)")
        }

        // Balance
        if (balance != null) {
            score += 10
            reason.add("Available balance extracted (+10)")
        }

        // Reference number
        if (refNo != null || upiId != null) {
            score += 10
            reason.add("UPI/Bank reference details detected (+10)")
        }

        // Merchant name
        if (merchant != "Unknown Merchant") {
            score += 10
            reason.add("Merchant/Person name extracted (+10)")
        }

        // Negative scoring
        var isLoanSpam = false
        var isPromoSpam = false
        var isOtp = false
        var isFakePayment = false

        if (SpamFilter.containsLoanIntent(body)) {
            score -= 50
            isLoanSpam = true
            reason.add("Loan/Lending intent keywords detected (-50)")
        }

        if (SpamFilter.containsLink(body)) {
            score -= 50
            isPromoSpam = true
            reason.add("Suspicious link/Download app keywords detected (-50)")
        }

        if (SpamFilter.containsPromoIntent(body)) {
            score -= 40
            isPromoSpam = true
            reason.add("Cashback/Reward/Claim promo language detected (-40)")
        }

        if (SpamFilter.containsSecurityOtp(body)) {
            score -= 20
            isOtp = true
            reason.add("OTP or login security details detected (-20)")
        }

        // Fake payment warning checklist
        // Generic wording like "Payment received successfully" but no masked bank or UPI ref details
        val isGenericReceived = text.contains("received successfully") || text.contains("payment received")
        if (isGenericReceived && maskedAcc == null && refNo == null) {
            score -= 40
            isFakePayment = true
            reason.add("Fake payment pattern: Generic received claims but no reference detail (-40)")
        }

        if (maskedAcc == null && refNo == null && upiId == null && !isOtp) {
            score -= 15
            reason.add("No account/UPI/reference detail found (-15)")
        }

        // Clamp score between 0 and 100
        val finalScore = score.coerceIn(0, 100)

        // 4. Decision and Classification logic
        var classification = SmsClassification.NON_TRANSACTION
        var autoAdd = false
        val reviewRequired = false

        if (isOtp) {
            classification = SmsClassification.OTP_OR_SECURITY_MESSAGE
        } else if (isLoanSpam) {
            classification = SmsClassification.LOAN_AD
        } else if (isPromoSpam) {
            classification = SmsClassification.SPAM_OR_AD
        } else if (isFakePayment) {
            classification = SmsClassification.FAKE_PAYMENT_MESSAGE
        } else if (detectedType != TransactionType.NONE && amount != null) {
            if (finalScore >= 60 && !isPromoSpam && !isLoanSpam && !isFakePayment) {
                classification = if (detectedType == TransactionType.DEBIT) {
                    SmsClassification.REAL_DEBIT_TRANSACTION
                } else {
                    SmsClassification.REAL_CREDIT_TRANSACTION
                }
                autoAdd = true
            } else if (finalScore >= 40) {
                classification = SmsClassification.POSSIBLE_TRANSACTION_NEEDS_REVIEW
            } else {
                classification = SmsClassification.NON_TRANSACTION
            }
        }

        // Strict override: if any loan, promo, or OTP is active, override autoAdd
        if (isOtp || isLoanSpam || isPromoSpam) {
            autoAdd = false
        }

        return SmsDetectionResult(
            classification = classification,
            transactionType = detectedType,
            amount = amount,
            currency = "INR", // Default currency INR
            merchantOrSender = merchant,
            bankName = extractedBank,
            accountLastDigits = maskedAcc,
            cardLastDigits = if (text.contains("card")) maskedAcc else null,
            upiId = upiId,
            transactionReferenceNumber = refNo,
            availableBalance = balance,
            smsSenderId = senderId,
            smsTimestamp = timestamp,
            confidenceScore = finalScore,
            autoAdd = autoAdd,
            reviewRequired = reviewRequired,
            classificationReason = reason,
            rawSms = body
        )
    }
}
