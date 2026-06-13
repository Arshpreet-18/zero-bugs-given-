package com.example.antigravityfinance.service.sms.detection

import kotlinx.serialization.Serializable

@Serializable
enum class SmsClassification {
    REAL_DEBIT_TRANSACTION,
    REAL_CREDIT_TRANSACTION,
    POSSIBLE_TRANSACTION_NEEDS_REVIEW,
    SPAM_OR_AD,
    LOAN_AD,
    FAKE_PAYMENT_MESSAGE,
    OTP_OR_SECURITY_MESSAGE,
    NON_TRANSACTION
}

@Serializable
enum class TransactionType {
    CREDIT, DEBIT, NONE
}

@Serializable
data class SmsDetectionResult(
    val classification: SmsClassification,
    val transactionType: TransactionType,
    val amount: Double?,
    val currency: String,
    val merchantOrSender: String,
    val bankName: String,
    val accountLastDigits: String?,
    val cardLastDigits: String?,
    val upiId: String?,
    val transactionReferenceNumber: String?,
    val availableBalance: Double?,
    val smsSenderId: String,
    val smsTimestamp: Long,
    val confidenceScore: Int,
    val autoAdd: Boolean,
    val reviewRequired: Boolean,
    val classificationReason: List<String>,
    val rawSms: String
)
