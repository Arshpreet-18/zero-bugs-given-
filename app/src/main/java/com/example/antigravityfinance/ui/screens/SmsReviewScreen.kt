package com.example.antigravityfinance.ui.screens

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravityfinance.data.model.Transaction
import com.example.antigravityfinance.data.model.TransactionStatus
import com.example.antigravityfinance.service.sms.detection.SmsClassification
import com.example.antigravityfinance.service.sms.detection.SmsDetectionModule
import com.example.antigravityfinance.service.sms.detection.SmsDetectionResult
import com.example.antigravityfinance.theme.*
import com.example.antigravityfinance.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SmsReviewScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) }
    val tabs = listOf("Auto-Added History", "Bank Verifier")
    val isScanning by viewModel.isSmsScanning.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SMS Transaction Hub",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(
                onClick = {
                    viewModel.scanSmsInbox { addedCount, balance ->
                        android.widget.Toast.makeText(
                            context,
                            "Sync complete! Scanned all transactions till last.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                },
                enabled = !isScanning,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync SMS Inbox",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (activeTab) {
            0 -> AutoAddedTab(viewModel)
            1 -> BankVerifierTab(viewModel)
        }
    }
}

@Composable
fun AutoAddedTab(viewModel: FinanceViewModel) {
    val transactions by viewModel.allTransactions.collectAsState()
    val currency = viewModel.currency.collectAsState().value
    val smsTransactions = remember(transactions) {
        transactions.filter { it.detectedFromSms && it.status == TransactionStatus.CONFIRMED }
    }

    if (smsTransactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No transactions have been automatically added from SMS yet.", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(smsTransactions) { tx ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tx.merchant, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(tx.date)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("Account: ${tx.account}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = "${if (tx.isIncome) "+" else "-"}${currency.symbol}${tx.amount}",
                            fontWeight = FontWeight.Bold,
                            color = if (tx.isIncome) AccentEmerald else AccentCrimson,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BankVerifierTab(viewModel: FinanceViewModel) {
    var codeText by remember { mutableStateOf("") }
    var verificationResult by remember { mutableStateOf<String?>(null) }
    val trustedSenders by viewModel.trustedSenders.collectAsState()
    var newTrustedSender by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Verify Bank SMS Sender Code",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Enter the 3-letter prefix code of the SMS sender (e.g. HDF, SBI, ICI) to verify its bank registration locally.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = codeText,
                onValueChange = { 
                    if (it.length <= 3) {
                        codeText = it 
                        verificationResult = null
                    }
                },
                label = { Text("Code (3 letters)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    val list = SmsDetectionModule.BankVerifier.resolveBankCode(codeText)
                    verificationResult = if (list.isNotEmpty()) {
                        if (list.size == 1) "Verified: ${list.first()}" else "Ambiguous: matches ${list.joinToString(" / ")}"
                    } else {
                        "Not found in local bank dictionary."
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Verify")
            }
        }

        verificationResult?.let { res ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (res.startsWith("Verified")) AccentEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = res,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (res.startsWith("Verified")) AccentEmerald else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Manage Trusted SMS Senders",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Add specific sender IDs (e.g. AD-HDFCBK) to bypass standard sender filters and gain bonus confidence scoring points.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTrustedSender,
                onValueChange = { newTrustedSender = it },
                label = { Text("Sender ID") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (newTrustedSender.isNotBlank()) {
                        viewModel.addTrustedSender(newTrustedSender.trim())
                        newTrustedSender = ""
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Trust")
            }
        }

        Text("Current Trusted Senders:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        if (trustedSenders.isEmpty()) {
            Text("No custom trusted senders added yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            for (sender in trustedSenders) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(sender, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Icon(
                        Icons.Default.Verified, 
                        contentDescription = "Trusted", 
                        tint = AccentEmerald, 
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.logoutUser()
                // notify user
                android.widget.Toast.makeText(context, "Cleared complete storage and logged out successfully.", android.widget.Toast.LENGTH_LONG).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.DeleteForever, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear Storage & Logout")
        }
    }
}
