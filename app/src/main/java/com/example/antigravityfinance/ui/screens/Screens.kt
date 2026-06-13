package com.example.antigravityfinance.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.antigravityfinance.data.model.*
import com.example.antigravityfinance.service.ai.AiAssistantService
import com.example.antigravityfinance.service.ocr.OcrScanner
import com.example.antigravityfinance.theme.*
import com.example.antigravityfinance.ui.components.*
import com.example.antigravityfinance.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SetupInitialIncomeDialog(
    currencySymbol: String,
    onSave: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                
                Text(
                    text = "Welcome to Antigravity Finance",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Before we begin, please enter details about your current initial income/balance to initialize your wallet. All future credit transactions detected from SMS will be optional.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { 
                        amountText = it 
                        errorText = null
                    },
                    label = { Text("Initial Income / Balance ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorText != null,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount != null && amount > 0.0) {
                            onSave(amount)
                        } else {
                            errorText = "Please enter a valid amount greater than 0"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Initialize Wallet")
                }
            }
        }
    }
}

// --- DASHBOARD SCREEN ---
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val themeType by viewModel.themeType.collectAsState()
    val isInitialIncomeSet by viewModel.isInitialIncomeSet.collectAsState()

    if (!isInitialIncomeSet) {
        SetupInitialIncomeDialog(
            currencySymbol = currency.symbol,
            onSave = { amount -> viewModel.addInitialIncome(amount) }
        )
    }
    
    val confirmedTx = transactions.filter { it.status == TransactionStatus.CONFIRMED }
    val creditSum = confirmedTx.filter { it.isIncome }.sumOf { it.amount }
    val debitSum = confirmedTx.filter { !it.isIncome }.sumOf { it.amount }
    val netBalance = creditSum - debitSum

    val categoryTotals = confirmedTx.filter { !it.isIncome }
        .groupBy { it.category }
        .map { (cat, list) ->
            val sum = list.sumOf { it.amount }
            PieChartInput(
                color = getCategoryColor(cat),
                value = sum,
                description = TransactionCategory.values().find { it.name == cat }?.displayName ?: cat
            )
        }.filter { it.value > 0 }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        ) {
            val bgBrush = if (themeType == ThemeType.DYNAMIC) {
                Brush.horizontalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
            } else {
                Brush.horizontalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            
            Column(
                modifier = Modifier
                    .background(bgBrush)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Net Balance",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${currency.symbol}${String.format("%,.2f", netBalance)}",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Total Credit", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Credit", tint = AccentEmerald, modifier = Modifier.size(16.dp))
                            Text(text = "${currency.symbol}${String.format("%,.0f", creditSum)}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Column {
                        Text(text = "Total Debit", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Debit", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                            Text(text = "${currency.symbol}${String.format("%,.0f", debitSum)}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (confirmedTx.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No confirmed transactions recorded yet.\nGo to Wallet tab or simulate SMS alerts!", textAlign = TextAlign.Center)
            }
        } else {
            InteractiveBarChart(
                creditValues = listOf(creditSum * 0.9, creditSum, creditSum * 0.95),
                debitValues = listOf(debitSum * 0.8, debitSum * 0.9, debitSum),
                labels = listOf("April", "May", "June"),
                currencySymbol = currency.symbol,
                modifier = Modifier.fillMaxWidth()
            )

            if (categoryTotals.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Category Breakdown",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
                        )
                        
                        AnimatedDonutChart(
                            inputs = categoryTotals,
                            currencySymbol = currency.symbol,
                            modifier = Modifier.fillMaxWidth().height(220.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            mainAxisSpacing = 8.dp,
                            crossAxisSpacing = 8.dp
                        ) {
                            categoryTotals.forEach { cat ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Box(modifier = Modifier.size(12.dp).background(cat.color, CircleShape))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = cat.description, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val currency by viewModel.currency.collectAsState()
    
    val scannedTx by viewModel.scannedTransaction.collectAsState()
    val showDupWarning by viewModel.showDuplicateWarning.collectAsState()
    val dupMatchTx by viewModel.duplicateMatchingTransaction.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val isSmsScanning by viewModel.isSmsScanning.collectAsState()
    val smsSyncedBalance by viewModel.smsSyncedBalance.collectAsState()
    val isInitialIncomeSet by viewModel.isInitialIncomeSet.collectAsState()
    
    var scanStatusMessage by remember { mutableStateOf<String?>(null) }
    var selectedTxForDetails by remember { mutableStateOf<Transaction?>(null) }
    
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scanSmsInbox { count, _ ->
                scanStatusMessage = "Scan complete. Synced $count new transactions."
            }
        } else {
            scanStatusMessage = "SMS Permission denied."
        }
    }
    
    LaunchedEffect(scanStatusMessage) {
        scanStatusMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            scanStatusMessage = null
        }
    }

    var showManualDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Date") }
    var sortAscending by remember { mutableStateOf(false) }
    
    if (!isInitialIncomeSet) {
        SetupInitialIncomeDialog(
            currencySymbol = currency.symbol,
            onSave = { amount -> viewModel.addInitialIncome(amount) }
        )
    }

    if (showDupWarning && scannedTx != null && dupMatchTx != null) {
        DuplicateWarningDialog(
            newTx = scannedTx!!,
            existingTx = dupMatchTx!!,
            onConfirmAnyway = { viewModel.forceConfirmDuplicate() },
            onCancel = { viewModel.dismissDuplicateWarning() },
            currencySymbol = currency.symbol
        )
    }

    val filteredTransactions = remember(transactions, searchQuery, selectedStatusFilter, sortBy, sortAscending) {
        var list = transactions

        // 1. Status Filter
        if (selectedStatusFilter != "All") {
            list = list.filter { tx ->
                when (selectedStatusFilter) {
                    "Pending" -> tx.status == TransactionStatus.PENDING
                    "Confirmed" -> tx.status == TransactionStatus.CONFIRMED
                    "Cancelled" -> tx.status == TransactionStatus.CANCELLED
                    else -> true
                }
            }
        }

        // 2. Search query (merchant name, notes, banking/account details)
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter { tx ->
                tx.merchant.lowercase().contains(q) ||
                tx.notes.lowercase().contains(q) ||
                tx.account.lowercase().contains(q) ||
                tx.category.lowercase().contains(q)
            }
        }

        // 3. Sorting
        list = when (sortBy) {
            "Amount" -> if (sortAscending) list.sortedBy { it.amount } else list.sortedByDescending { it.amount }
            "Merchant" -> if (sortAscending) list.sortedBy { it.merchant.lowercase() } else list.sortedByDescending { it.merchant.lowercase() }
            else -> if (sortAscending) list.sortedBy { it.date } else list.sortedByDescending { it.date } // Date
        }

        list
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (smsSyncedBalance != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Last Synced Bank Balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${currency.symbol}${String.format("%,.2f", smsSyncedBalance)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "AI Simulators & Tools",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.handleMockOcrTrigger(OcrScanner.MockReceiptType.STARBUCKS) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan Receipt", fontSize = 11.sp, maxLines = 1)
                    }

                    Button(
                        onClick = { viewModel.simulateIncomingSms("Transaction Alert: INR 450.00 spent on HDFC Card ending 1234 at Starbucks Coffee on 13-06-26. Avl Bal Rs. 15,230.00") },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate SMS", fontSize = 11.sp, maxLines = 1)
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showManualDialog = true },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Manual", fontSize = 11.sp, maxLines = 1)
                    }
                    
                    Button(
                        onClick = {
                            val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.READ_SMS
                            )
                            if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                viewModel.scanSmsInbox { count, _ ->
                                    scanStatusMessage = "Scan complete. Synced $count new transactions."
                                }
                            } else {
                                smsPermissionLauncher.launch(android.Manifest.permission.READ_SMS)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isSmsScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan SMS Inbox", fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }

        if (scannedTx != null && !showDupWarning) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Scanned Transaction Extracted",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Merchant: ${scannedTx!!.merchant}", style = MaterialTheme.typography.bodyMedium)
                    Text("Amount: ${currency.symbol}${scannedTx!!.amount}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("Category: ${scannedTx!!.category}", style = MaterialTheme.typography.bodySmall)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.confirmScannedTransaction() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm")
                        }
                        Button(
                            onClick = { viewModel.cancelScannedTransaction() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        OutlinedButton(
                            onClick = { viewModel.ignoreScannedTransaction() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ignore")
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search transactions...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Pending", "Confirmed", "Cancelled").forEach { status ->
                val isSelected = selectedStatusFilter == status
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedStatusFilter = status },
                    label = { Text(status) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sort by:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var showSortMenu by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { showSortMenu = true }) {
                        Text(sortBy)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        listOf("Date", "Amount", "Merchant").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    sortBy = option
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
                IconButton(onClick = { sortAscending = !sortAscending }) {
                    Icon(
                        imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = "Sort direction"
                    )
                }
            }
        }

        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No transactions synced yet. Tap Scan SMS Inbox or Simulate SMS!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTransactions) { tx ->
                    TransactionRow(
                        tx = tx,
                        currency = currency,
                        onClick = { selectedTxForDetails = tx },
                        onConfirm = { viewModel.confirmPendingTransaction(tx.id) },
                        onReject = { viewModel.cancelPendingTransaction(tx.id) }
                    )
                }
            }
        }
    }

    if (showManualDialog) {
        ManualTransactionDialog(
            currencySymbol = currency.symbol,
            onDismiss = { showManualDialog = false },
            onSave = { amount, merchant, isIncome, category ->
                viewModel.addManualTransaction(amount, merchant, isIncome, category)
                showManualDialog = false
            }
        )
    }

    if (selectedTxForDetails != null) {
        TransactionDetailsDialog(
            tx = selectedTxForDetails!!,
            currencySymbol = currency.symbol,
            onDismiss = { selectedTxForDetails = null },
            onUpdateCategory = { newCat ->
                viewModel.updateTransactionCategory(selectedTxForDetails!!.id, newCat)
                selectedTxForDetails = null
            },
            onConfirm = { 
                viewModel.confirmPendingTransaction(selectedTxForDetails!!.id)
                selectedTxForDetails = null
            },
            onReject = { 
                viewModel.cancelPendingTransaction(selectedTxForDetails!!.id)
                selectedTxForDetails = null
            }
        )
    }
}

@Composable
fun TransactionRow(
    tx: Transaction,
    currency: CurrencyType,
    onClick: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tx.status == TransactionStatus.PENDING) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp, 
            color = if (tx.status == TransactionStatus.PENDING) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) 
            else 
                MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1.5f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                getCategoryColor(tx.category).copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(tx.category),
                            contentDescription = null,
                            tint = getCategoryColor(tx.category),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tx.merchant,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (tx.status == TransactionStatus.PENDING) {
                                Spacer(modifier = Modifier.width(6.dp))
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("PENDING", fontSize = 9.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                        labelColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = null
                                )
                            } else if (tx.status == TransactionStatus.CANCELLED) {
                                Spacer(modifier = Modifier.width(6.dp))
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("CANCELLED", fontSize = 9.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = null
                                )
                            }
                        }
                        Text(
                            text = tx.notes.ifEmpty { TransactionCategory.values().find { it.name == tx.category }?.displayName ?: tx.category },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${if (tx.isIncome) "Credit " else "Debit "}${currency.symbol}${tx.amount}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (tx.isIncome) AccentEmerald else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (tx.status == TransactionStatus.PENDING && onConfirm != null && onReject != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Confirm", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionDetailsDialog(
    tx: Transaction,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onUpdateCategory: (String) -> Unit,
    onConfirm: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val timeStr = timeFormat.format(Date(tx.date))
    val dateStr = dateFormat.format(Date(tx.date))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction Details",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (tx.isIncome) "Income" else "Expense",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${if (tx.isIncome) "+" else "-"}$currencySymbol${tx.amount}",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (tx.isIncome) AccentEmerald else MaterialTheme.colorScheme.error
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                DetailRow(label = "Date", value = dateStr, icon = Icons.Rounded.Event)
                DetailRow(label = "Time", value = timeStr, icon = Icons.Rounded.Schedule)
                DetailRow(label = "From", value = tx.account, icon = Icons.Rounded.CreditCard)
                DetailRow(label = "To", value = tx.merchant, icon = Icons.Rounded.Storefront)
                
                if (tx.notes.isNotEmpty()) {
                    DetailRow(label = "Notes", value = tx.notes, icon = Icons.Rounded.Notes)
                }

                if (tx.status == TransactionStatus.PENDING && onConfirm != null && onReject != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Confirm")
                        }
                        OutlinedButton(
                            onClick = onReject,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject")
                        }
                    }
                }

                if (!tx.isIncome) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    
                    Text(
                        text = "Recategorize Transaction",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FlowRow(
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TransactionCategory.values().forEach { cat ->
                            val isSelected = tx.category.uppercase() == cat.name.uppercase()
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdateCategory(cat.name) },
                                label = { Text(cat.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = getCategoryColor(cat.name).copy(alpha = 0.2f),
                                    selectedLabelColor = getCategoryColor(cat.name),
                                    selectedLeadingIconColor = getCategoryColor(cat.name)
                                ),
                                leadingIcon = {
                                    Icon(
                                        imageVector = getCategoryIcon(cat.name),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// --- MANUAL TRANSACTION ENTRY DIALOG ---
@Composable
fun ManualTransactionDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (amount: Double, merchant: String, isIncome: Boolean, category: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf("FOOD") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Transaction",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { isIncome = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Debit", color = if (!isIncome) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                    Button(
                        onClick = { isIncome = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isIncome) AccentEmerald else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Credit", color = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant / Source") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("Category", style = MaterialTheme.typography.bodySmall)
                    FlowRow(
                        mainAxisSpacing = 4.dp,
                        crossAxisSpacing = 4.dp,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        TransactionCategory.values().forEach { cat ->
                            val selected = category == cat.name
                            InputChip(
                                selected = selected,
                                onClick = { category = cat.name },
                                label = { Text(cat.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0 && merchant.isNotBlank()) {
                                onSave(amount, merchant, isIncome, category)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

// --- AI ASSISTANT CHAT SCREEN WITH VOICE SIMULATOR ---
@Composable
fun AssistantScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isThinking by viewModel.isAiThinking.collectAsState()
    var inputText by remember { mutableStateOf("") }
    
    var isRecordingSimulated by remember { mutableStateOf(false) }
    var recordingTimer by remember { mutableStateOf(3) }
    val scope = rememberCoroutineScope()

    if (isRecordingSimulated) {
        Dialog(onDismissRequest = { isRecordingSimulated = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("AI Voice Commands", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    
                    val waveTransition = rememberInfiniteTransition(label = "voice_wave")
                    val waveScale by waveTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1.5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "wave_scale"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp * waveScale)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                        )
                        Icon(Icons.Default.Mic, contentDescription = "Mic", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                    
                    Text("Listening... (simulating voice analysis in $recordingTimer s)")
                    Text(
                        text = "\"How much money is left in my budget?\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatHistory) { msg ->
                ChatBubble(msg = msg)
            }
            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI is processing stats...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Ask Antigravity AI...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessageToAssistant(inputText)
                            inputText = ""
                        }
                    }
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessageToAssistant(inputText)
                                inputText = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable {
                        isRecordingSimulated = true
                        recordingTimer = 3
                        scope.launch {
                            while (recordingTimer > 0) {
                                delay(1000)
                                recordingTimer--
                            }
                            isRecordingSimulated = false
                            viewModel.sendMessageToAssistant("How much money is left in my budget?")
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voice Assistant", tint = Color.White)
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bg = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(bg, shape)
                .padding(12.dp)
        ) {
            Text(
                text = msg.text,
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(msg.timestamp)),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}

// --- BUDGET SCREEN ---
@Composable
fun BudgetScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val budgets by viewModel.budgets.collectAsState()
    val currency by viewModel.currency.collectAsState()
    
    var showAiForecast by remember { mutableStateOf(false) }
    var aiForecastResult by remember { mutableStateOf("") }
    var isThinkingForecast by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("AI Budget Overrun Predictor", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Analyze daily spending patterns and forecast monthly limit overruns instantly.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = {
                        isThinkingForecast = true
                        scope.launch {
                            delay(1500)
                            isThinkingForecast = false
                            val txs = viewModel.allTransactions.value
                            aiForecastResult = AiAssistantService.askAssistant(
                                query = "forecast budget overruns",
                                transactions = txs,
                                budgets = budgets,
                                goals = emptyList(),
                                investments = emptyList(),
                                apiKey = null,
                                currencySymbol = currency.symbol
                            )
                            showAiForecast = true
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isThinkingForecast) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Calculate AI Projection")
                    }
                }
                
                if (showAiForecast) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = aiForecastResult,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                }
            }
        }

        Text("Active Budgets", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        if (budgets.isEmpty()) {
            Text("No budgets found. Please add budgets.")
        } else {
            budgets.forEach { budget ->
                val progress = if (budget.limitAmount > 0) (budget.spentAmount / budget.limitAmount).toFloat() else 0f
                val isOverspent = budget.spentAmount > budget.limitAmount
                val catName = TransactionCategory.values().find { it.name == budget.category }?.displayName ?: if (budget.category == "All") "Master Budget" else budget.category

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = catName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "${currency.symbol}${budget.spentAmount.toInt()} / ${currency.symbol}${budget.limitAmount.toInt()}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LinearProgressIndicator(
                            progress = progress.coerceAtMost(1f),
                            color = if (isOverspent) MaterialTheme.colorScheme.error else if (progress > 0.8f) AccentOrange else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                        )
                        
                        if (isOverspent) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Budget overrun by ${currency.symbol}${(budget.spentAmount - budget.limitAmount).toInt()}!",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SAVINGS GOALS SCREEN ---
@Composable
fun GoalsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.goals.collectAsState()
    val currency by viewModel.currency.collectAsState()

    var showAddGoalDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Savings Goals", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            IconButton(
                onClick = { showAddGoalDialog = true },
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        if (goals.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No savings goals active yet.\nClick '+' to add one!", textAlign = TextAlign.Center)
            }
        } else {
            goals.forEach { goal ->
                val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
                val percent = (progress * 100).toInt()
                
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = goal.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    if (goal.streakCount > 0) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Rounded.Whatshot,
                                            contentDescription = "Contribution Streak",
                                            tint = AccentOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${goal.streakCount} d",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = AccentOrange
                                        )
                                    }
                                }
                                val deadlineDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(goal.deadline))
                                Text(text = "Target Date: $deadlineDate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                            Text(
                                text = "$percent%",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LinearProgressIndicator(
                            progress = progress.coerceAtMost(1f),
                            color = AccentIndigo,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Saved: ${currency.symbol}${goal.currentAmount.toInt()} / ${currency.symbol}${goal.targetAmount.toInt()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            var showDepositDialog by remember { mutableStateOf(false) }
                            Button(
                                onClick = { showDepositDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Deposit", fontSize = 12.sp)
                            }
                            
                            if (showDepositDialog) {
                                var depositAmountStr by remember { mutableStateOf("") }
                                Dialog(onDismissRequest = { showDepositDialog = false }) {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Text("Deposit to ${goal.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            OutlinedTextField(
                                                value = depositAmountStr,
                                                onValueChange = { depositAmountStr = it },
                                                label = { Text("Amount (${currency.symbol})") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(onClick = { showDepositDialog = false }, modifier = Modifier.weight(1f)) {
                                                    Text("Cancel")
                                                }
                                                Button(
                                                    onClick = {
                                                        val amt = depositAmountStr.toDoubleOrNull() ?: 0.0
                                                        if (amt > 0) {
                                                            viewModel.depositToGoal(goal, amt)
                                                            showDepositDialog = false
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Confirm")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddGoalDialog) {
        var name by remember { mutableStateOf("") }
        var targetText by remember { mutableStateOf("") }
        
        Dialog(onDismissRequest = { showAddGoalDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("New Savings Goal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Goal Name (e.g. New Laptop)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        label = { Text("Target Amount (${currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { showAddGoalDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val target = targetText.toDoubleOrNull() ?: 0.0
                                if (name.isNotBlank() && target > 0) {
                                    viewModel.addGoal(name, target)
                                    showAddGoalDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}

// --- INVESTMENTS PORTFOLIO SCREEN ---
@Composable
fun InvestmentsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val investments by viewModel.investments.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val smsSyncedBalance by viewModel.smsSyncedBalance.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val totalSipAmount = remember(investments) {
        investments.filter { it.type == "SIP" }.sumOf { it.investedAmount }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Investment Portfolio", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Investment", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        if (smsSyncedBalance != null && totalSipAmount > 0.0 && smsSyncedBalance!! < totalSipAmount) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Low Balance Alert for Active SIPs",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your synced bank balance (${currency.symbol}${String.format("%,.2f", smsSyncedBalance)}) is lower than your active monthly SIP commitments (${currency.symbol}${String.format("%,.0f", totalSipAmount)}). Please maintain sufficient balance to avoid failed transaction charges.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        if (investments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Your portfolio is empty.\nClick '+' to add Mutual Funds or Stocks!", textAlign = TextAlign.Center)
            }
        } else {
            val totalInvested = investments.sumOf { it.investedAmount }
            val currentVal = investments.sumOf { it.currentValuation }
            val netGain = currentVal - totalInvested
            val gainPct = if (totalInvested > 0) (netGain / totalInvested) * 100 else 0.0

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Value", style = MaterialTheme.typography.bodySmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${currency.symbol}${String.format("%,.2f", currentVal)}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${if (netGain >= 0) "▲ +" else "▼ "}${String.format("%.1f", gainPct)}%",
                            color = if (netGain >= 0) AccentEmerald else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Invested: ${currency.symbol}${totalInvested.toInt()}", style = MaterialTheme.typography.bodySmall)
                        Text("Gains: ${currency.symbol}${netGain.toInt()}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            SleekLineChart(
                dataPoints = listOf(currentVal, currentVal * 1.12, currentVal * 1.25, currentVal * 1.40, currentVal * 1.57),
                labels = listOf("Now", "Year 1", "Year 2", "Year 3", "Year 4"),
                currencySymbol = currency.symbol,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Holdings", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

            investments.forEach { asset ->
                val assetGain = asset.currentValuation - asset.investedAmount
                val assetGainPct = if (asset.investedAmount > 0) (assetGain / asset.investedAmount) * 100 else 0.0
                
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(asset.name, fontWeight = FontWeight.Bold)
                            Text("${asset.type} • ${asset.symbol}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${currency.symbol}${asset.currentValuation.toInt()}", fontWeight = FontWeight.Bold)
                            Text(
                                text = "${if (assetGain >= 0) "+" else ""}${String.format("%.1f", assetGainPct)}%",
                                color = if (assetGain >= 0) AccentEmerald else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var symbol by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("Stock") }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Add Investment", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Stock", "Mutual Fund", "SIP").forEach { t ->
                            FilterChip(
                                selected = type == t,
                                onClick = { type = t },
                                label = { Text(t) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name (e.g. Tata Consultancy)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = symbol,
                        onValueChange = { symbol = it },
                        label = { Text("Symbol / Code (e.g. TCS)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Invested Amount (${currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { showAddDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val amount = amountText.toDoubleOrNull() ?: 0.0
                                if (name.isNotBlank() && amount > 0) {
                                    viewModel.addInvestment(name, symbol, type, amount)
                                    showAddDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}

// --- SETTINGS & CUSTOMIZATION SCREEN ---
@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val themeType by viewModel.themeType.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val accentIndex by viewModel.accentIndex.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val language by viewModel.language.collectAsState()
    val isInvestmentsEnabled by viewModel.isInvestmentsEnabled.collectAsState()

    var showPinSetDialog by remember { mutableStateOf(false) }
    val isPinSet by viewModel.isPinSet.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Visual Themes", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = if (themeType == ThemeType.DYNAMIC) 2.dp else 1.dp,
                    color = if (themeType == ThemeType.DYNAMIC) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.updateTheme(ThemeType.DYNAMIC) }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Box(modifier = Modifier.size(16.dp).background(DynamicLightPrimary, CircleShape))
                        Box(modifier = Modifier.size(16.dp).background(DynamicLightSecondary, CircleShape))
                        Box(modifier = Modifier.size(16.dp).background(DynamicLightTertiary, CircleShape))
                    }
                    Text("Dynamic Theme", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("Vibrant gradients, rounded cards & playful transitions.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = if (themeType == ThemeType.PROFESSIONAL) 2.dp else 1.dp,
                    color = if (themeType == ThemeType.PROFESSIONAL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.updateTheme(ThemeType.PROFESSIONAL) }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Box(modifier = Modifier.size(16.dp).background(ProfLightPrimary, CircleShape))
                        Box(modifier = Modifier.size(16.dp).background(ProfLightSecondary, CircleShape))
                        Box(modifier = Modifier.size(16.dp).background(ProfLightTertiary, CircleShape))
                    }
                    Text("Professional Theme", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("Elegant borders, dark charcoal & executive dashboard designs.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        }

        Text("Accent Color", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val colors = listOf(AccentEmerald, AccentOrange, AccentIndigo, AccentCrimson, AccentSky)
            colors.forEachIndexed { index, color ->
                val selected = accentIndex == index
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color, CircleShape)
                        .border(
                            width = if (selected) 3.dp else 0.dp,
                            color = MaterialTheme.colorScheme.onBackground,
                            shape = CircleShape
                        )
                        .clickable {
                            viewModel.updateAccentIndex(if (selected) -1 else index)
                        }
                )
            }
        }

        Divider()

        Text("Display Mode", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = isDark == null,
                onClick = { viewModel.updateDarkMode(null) },
                label = { Text("System Theme") }
            )
            FilterChip(
                selected = isDark == false,
                onClick = { viewModel.updateDarkMode(false) },
                label = { Text("Light Mode") }
            )
            FilterChip(
                selected = isDark == true,
                onClick = { viewModel.updateDarkMode(true) },
                label = { Text("Dark Mode") }
            )
        }

        Divider()

        Text("Security Gates", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Lock App with PIN", fontWeight = FontWeight.Bold)
                Text("Secure local wallet with secure PIN keyboard", style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = isPinSet,
                onCheckedChange = { checked ->
                    if (checked) {
                        showPinSetDialog = true
                    } else {
                        viewModel.removePin()
                    }
                }
            )
        }

        Divider()

        Text("Localization", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Preferred Currency", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CurrencyType.values().forEach { cur ->
                    FilterChip(
                        selected = currency == cur,
                        onClick = { viewModel.updateCurrency(cur) },
                        label = { Text("${cur.symbol} (${cur.code})") }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Language", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                LanguageType.values().forEach { lang ->
                    FilterChip(
                        selected = language == lang,
                        onClick = { viewModel.updateLanguage(lang) },
                        label = { Text(lang.displayName) }
                    )
                }
            }
        }

        Divider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Enable Investments Module", fontWeight = FontWeight.Bold)
                Text("SIPs, mutual funds watchlists, allocation charts", style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = isInvestmentsEnabled,
                onCheckedChange = { viewModel.updateInvestmentsEnabled(it) }
            )
        }
    }

    if (showPinSetDialog) {
        Dialog(onDismissRequest = { showPinSetDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                    PinKeyboardGate(
                        isPinSet = false,
                        pinError = false,
                        onPinSubmitted = { pin ->
                            viewModel.setPin(pin)
                            showPinSetDialog = false
                        }
                    )
                }
            }
        }
    }
}

// --- COLOR AND ICON UTILS ---
fun getCategoryColor(category: String): Color {
    return when (category.uppercase()) {
        "TRAVEL" -> AccentIndigo
        "FOOD" -> AccentOrange
        "LIVELIHOOD" -> AccentEmerald
        "COMPULSORY" -> AccentSky
        "SHOPPING" -> AccentCrimson
        "INVESTMENT" -> Color(0xFF00A86B)
        "SALARY" -> Color(0xFF32CD32)
        else -> Color.Gray
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.uppercase()) {
        "TRAVEL" -> Icons.Rounded.DirectionsCar
        "FOOD" -> Icons.Rounded.Restaurant
        "LIVELIHOOD" -> Icons.Rounded.Favorite
        "COMPULSORY" -> Icons.Rounded.ReceiptLong
        "SHOPPING" -> Icons.Rounded.ShoppingBag
        "INVESTMENT" -> Icons.Rounded.TrendingUp
        "SALARY" -> Icons.Rounded.AccountBalanceWallet
        else -> Icons.Rounded.Help
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val spacingPx = mainAxisSpacing.roundToPx()
        val crossSpacingPx = crossAxisSpacing.roundToPx()
        
        val lines = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val lineHeights = mutableListOf<Int>()
        
        var currentLine = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentWidth = 0
        var currentHeight = 0
        
        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            if (currentWidth + placeable.width + spacingPx > constraints.maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                lineHeights.add(currentHeight)
                currentLine = mutableListOf()
                currentWidth = 0
                currentHeight = 0
            }
            currentLine.add(placeable)
            currentWidth += placeable.width + spacingPx
            currentHeight = maxOf(currentHeight, placeable.height)
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
            lineHeights.add(currentHeight)
        }
        
        val totalHeight = lineHeights.sum() + (lineHeights.size - 1).coerceAtLeast(0) * crossSpacingPx
        val width = constraints.maxWidth
        
        layout(width, totalHeight.coerceAtMost(constraints.maxHeight)) {
            var y = 0
            lines.forEachIndexed { lineIndex, line ->
                var x = 0
                val lineHeight = lineHeights[lineIndex]
                line.forEach { placeable ->
                    placeable.place(x, y)
                    x += placeable.width + spacingPx
                }
                y += lineHeight + crossSpacingPx
            }
        }
    }
}
