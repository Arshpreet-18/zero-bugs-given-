package com.example.antigravityfinance

import com.example.antigravityfinance.service.sms.detection.SmsClassification
import com.example.antigravityfinance.service.sms.detection.SmsDetectionModule
import com.example.antigravityfinance.service.sms.detection.TransactionType
import org.junit.Assert.*
import org.junit.Test

class SmsDetectionModuleTest {

    @Test
    fun testRealDebitSms() = kotlinx.coroutines.runBlocking {
        val body = "Txn of Rs. 250 debited from HDFC A/c XX1234 towards Zomato. UPI Ref: 1234567890."
        val result = SmsDetectionModule.detect(body, "AD-HDFCBK", System.currentTimeMillis())
        
        assertEquals(SmsClassification.REAL_DEBIT_TRANSACTION, result.classification)
        assertEquals(TransactionType.DEBIT, result.transactionType)
        assertEquals(250.0, result.amount!!, 0.0)
        assertEquals("Zomato", result.merchantOrSender)
        assertEquals("1234", result.accountLastDigits)
        assertEquals("1234567890", result.transactionReferenceNumber)
        assertTrue(result.autoAdd)
        assertFalse(result.reviewRequired)
        assertTrue(result.confidenceScore >= 75)
    }

    @Test
    fun testRealCreditSms() = kotlinx.coroutines.runBlocking {
        val body = "Salary of INR 50,000 credited to SBI A/c XX5678."
        val result = SmsDetectionModule.detect(body, "VK-SBIBNK", System.currentTimeMillis())
        
        assertEquals(SmsClassification.REAL_CREDIT_TRANSACTION, result.classification)
        assertEquals(TransactionType.CREDIT, result.transactionType)
        assertEquals(50000.0, result.amount!!, 0.0)
        assertEquals("5678", result.accountLastDigits)
        assertTrue(result.autoAdd)
        assertFalse(result.reviewRequired)
        assertTrue(result.confidenceScore >= 75)
    }

    @Test
    fun testLoanAdWithAmount() = kotlinx.coroutines.runBlocking {
        val body = "Get pre-approved personal loan up to Rs. 5 Lakhs instantly. Apply at https://loans.com"
        val result = SmsDetectionModule.detect(body, "PROMOT", System.currentTimeMillis())
        
        assertEquals(SmsClassification.LOAN_AD, result.classification)
        assertFalse(result.autoAdd)
        assertFalse(result.reviewRequired)
        assertTrue(result.confidenceScore < 50)
    }

    @Test
    fun testCashbackSpam() = kotlinx.coroutines.runBlocking {
        val body = "Congratulations! You won Rs. 500 cashback reward. Click http://bit.ly/xxxx to claim."
        val result = SmsDetectionModule.detect(body, "PROMOT", System.currentTimeMillis())
        
        assertEquals(SmsClassification.SPAM_OR_AD, result.classification)
        assertFalse(result.autoAdd)
        assertFalse(result.reviewRequired)
        assertTrue(result.confidenceScore < 50)
    }

    @Test
    fun testFakePaymentSms() = kotlinx.coroutines.runBlocking {
        val body = "Payment of Rs. 1500 received successfully."
        val result = SmsDetectionModule.detect(body, "UnknownSender", System.currentTimeMillis())
        
        assertEquals(SmsClassification.FAKE_PAYMENT_MESSAGE, result.classification)
        assertFalse(result.autoAdd)
        assertFalse(result.reviewRequired)
    }

    @Test
    fun testOtpSms() = kotlinx.coroutines.runBlocking {
        val body = "Your OTP for NetBanking login is 987654. Do not share."
        val result = SmsDetectionModule.detect(body, "AD-BANKOT", System.currentTimeMillis())
        
        assertEquals(SmsClassification.OTP_OR_SECURITY_MESSAGE, result.classification)
        assertFalse(result.autoAdd)
        assertFalse(result.reviewRequired)
    }
}
