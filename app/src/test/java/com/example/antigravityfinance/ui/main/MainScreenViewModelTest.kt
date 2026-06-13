package com.example.antigravityfinance.ui.main

import com.example.antigravityfinance.data.model.TransactionStatus
import com.example.antigravityfinance.service.sms.AutoCategorizer
import com.example.antigravityfinance.service.sms.SmsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MainScreenViewModelTest {

    @Test
    fun testSmsParser_debitTransaction() {
        val sms = "Dear Customer, your A/C ending X1234 has been debited by Rs. 550.00 at Starbucks on 13-06-26."
        val result = SmsParser.parse(sms)
        
        assertNotNull(result)
        val transaction = result!!.transaction
        assertEquals(550.00, transaction.amount, 0.0)
        assertEquals("Starbucks", transaction.merchant)
        assertEquals("FOOD", transaction.category)
        assertEquals(TransactionStatus.CONFIRMED, transaction.status)
        assertEquals(false, transaction.isIncome)
        assertEquals(true, transaction.detectedFromSms)
    }

    @Test
    fun testSmsParser_creditTransaction() {
        val sms = "Dear Customer, Rs. 15,000.00 credited to account X5678 from Salary."
        val result = SmsParser.parse(sms)
        
        assertNotNull(result)
        val transaction = result!!.transaction
        assertEquals(15000.00, transaction.amount, 0.0)
        assertEquals("Salary", transaction.merchant)
        assertEquals("SALARY", transaction.category)
        assertEquals(TransactionStatus.CONFIRMED, transaction.status)
        assertEquals(true, transaction.isIncome)
    }

    @Test
    fun testSmsParser_upiDebitTransaction() {
        val sms = "UPI transaction of INR 1,250.50 paid to Blinkit on 13-06-26. Avl Bal INR 8,749.50"
        val result = SmsParser.parse(sms)

        assertNotNull(result)
        val transaction = result!!.transaction
        assertEquals(1250.50, transaction.amount, 0.0)
        assertEquals("Blinkit", transaction.merchant)
        assertEquals("LIVELIHOOD", transaction.category)
        assertEquals(false, transaction.isIncome)
        assertEquals(8749.50, result.balance!!, 0.0)
    }

    @Test
    fun testSmsParser_invalidMessage() {
        val sms = "Hey, did you finish that report?"
        val transaction = SmsParser.parse(sms)
        assertNull(transaction)
    }

    @Test
    fun testAutoCategorizer() {
        assertEquals("FOOD", AutoCategorizer.categorize("Starbucks"))
        assertEquals("LIVELIHOOD", AutoCategorizer.categorize("Blinkit"))
        assertEquals("SHOPPING", AutoCategorizer.categorize("Amazon India"))
        assertEquals("TRAVEL", AutoCategorizer.categorize("Uber Ride"))
        assertEquals("INVESTMENT", AutoCategorizer.categorize("Zerodha"))
        assertEquals("OTHERS", AutoCategorizer.categorize("Unknown Vendor"))
    }
}
