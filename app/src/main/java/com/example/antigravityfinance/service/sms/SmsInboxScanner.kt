package com.example.antigravityfinance.service.sms

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.antigravityfinance.data.model.Transaction
import com.example.antigravityfinance.data.model.TransactionStatus
import com.example.antigravityfinance.data.repository.TransactionRepository
import kotlinx.coroutines.flow.first

object SmsInboxScanner {
    
    data class SyncSmsResult(
        val addedCount: Int,
        val latestBalance: Double?,
        val maxTimestamp: Long
    )

    fun reconcileInbox(
        context: Context, 
        repository: TransactionRepository
    ): SyncSmsResult {
        val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_SMS
        )
        if (permissionCheck != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.d("SmsInboxScanner", "SMS Permission not granted. Skipping reconciliation.")
            return SyncSmsResult(0, null, 0L)
        }

        var addedCount = 0
        var latestBalance: Double? = null
        var latestBalanceTimestamp: Long = 0
        var maxTimestamp: Long = 0
        
        val parsedSmsList = mutableListOf<Transaction>()
        
        try {
            val inboxUri = Uri.parse("content://sms/inbox")
            val projection = arrayOf("_id", "address", "body", "date")
            
            // Query all historical SMS messages to build the source of truth
            val cursor = context.contentResolver.query(
                inboxUri, 
                projection, 
                null, 
                null, 
                "date DESC"
            )
            
            cursor?.use { c ->
                val bodyIndex = c.getColumnIndexOrThrow("body")
                val dateIndex = c.getColumnIndexOrThrow("date")
                val addressIndex = c.getColumnIndexOrThrow("address")
                
                while (c.moveToNext()) {
                    val body = c.getString(bodyIndex) ?: continue
                    val date = c.getLong(dateIndex)
                    val address = c.getString(addressIndex) ?: ""
                    
                    val parsedResult = SmsParser.parse(body)
                    if (parsedResult != null) {
                        val transaction = parsedResult.transaction.copy(
                            date = date,
                            notes = "Synced from SMS Inbox (${address})",
                            status = TransactionStatus.CONFIRMED
                        )
                        parsedSmsList.add(transaction)
                        
                        if (date > maxTimestamp) {
                            maxTimestamp = date
                        }
                        
                        val balance = parsedResult.balance
                        if (balance != null && date > latestBalanceTimestamp) {
                            latestBalance = balance
                            latestBalanceTimestamp = date
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SmsInboxScanner", "Error scanning SMS inbox for reconciliation", e)
        }
        
        // Fetch existing database transactions
        val dbTransactions = kotlinx.coroutines.runBlocking {
            repository.allTransactions.first()
        }
        
        // 1. Insert transactions that are in SMS but not in DB
        for (smsTx in parsedSmsList) {
            val exists = dbTransactions.any { dbTx ->
                dbTx.amount == smsTx.amount &&
                dbTx.isIncome == smsTx.isIncome &&
                dbTx.merchant.equals(smsTx.merchant, ignoreCase = true) &&
                Math.abs(dbTx.date - smsTx.date) < 120000 // within 2 minutes
            }
            if (!exists) {
                kotlinx.coroutines.runBlocking {
                    val isAutoConfirm = repository.isMerchantAutoConfirm(smsTx.merchant)
                    val finalStatus = TransactionStatus.CONFIRMED
                    repository.insertTransaction(smsTx.copy(status = finalStatus))
                }
                addedCount++
            }
        }
        
        // 2. Delete only SMS-derived transactions that are no longer found in SMS.
        // Manual and OCR transactions should remain in the history.
        for (dbTx in dbTransactions.filter { it.detectedFromSms }) {
            val foundInSms = parsedSmsList.any { smsTx ->
                smsTx.amount == dbTx.amount &&
                smsTx.isIncome == dbTx.isIncome &&
                smsTx.merchant.equals(dbTx.merchant, ignoreCase = true) &&
                Math.abs(smsTx.date - dbTx.date) < 120000 // within 2 minutes
            }
            if (!foundInSms) {
                Log.d("SmsInboxScanner", "Deleting unverified transaction: ${dbTx.merchant} - ${dbTx.amount}")
                kotlinx.coroutines.runBlocking {
                    repository.deleteTransaction(dbTx)
                }
            }
        }
        
        return SyncSmsResult(addedCount, latestBalance, maxTimestamp)
    }
}
