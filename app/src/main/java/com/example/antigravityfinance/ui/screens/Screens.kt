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

object Translator {
    private val translations = mapOf(
        "en" to mapOf(
            "Dashboard" to "Dashboard",
            "Wallet" to "Wallet",
            "AI Chat" to "AI Chat",
            "Financial Tools" to "Financial Tools",
            "Settings" to "Settings",
            
            "Good Morning ☀️" to "Good Morning ☀️",
            "Good Afternoon 🌤️" to "Good Afternoon 🌤️",
            "Good Evening 🌙" to "Good Evening 🌙",
            
            "Select App Language" to "Select App Language",
            "Choose your preferred language for the interface and AI chatbot assistant." to "Choose your preferred language for the interface and AI chatbot assistant.",
            "Send OTP & Continue" to "Send OTP & Continue",
            "Back" to "Back",
            "Enter Verification Code" to "Enter Verification Code",
            "We sent a 6-digit OTP code to" to "We sent a 6-digit OTP code to",
            "Please enter it below to confirm your phone number." to "Please enter it below to confirm your phone number.",
            "6-Digit OTP Code" to "6-Digit OTP Code",
            "Resend Code" to "Resend Code",
            "Verify & Continue" to "Verify & Continue",
            "OTP Sent successfully!" to "OTP Sent successfully!",
            "OTP Resent successfully" to "OTP Resent successfully",
            "Registration Verified!" to "Registration Verified!",
            "Error:" to "Error:",
            "Try bypass code '123456'." to "Try bypass code '123456'.",
            "OTP must be exactly 6 digits" to "OTP must be exactly 6 digits",
            
            "Net Balance" to "Net Balance",
            "Monthly Income" to "Monthly Income",
            "Monthly Expense" to "Monthly Expense",
            "Recent Transactions" to "Recent Transactions",
            "See All" to "See All",
            "Low Balance Warning" to "Low Balance Warning",
            "Your balance is below the minimum required for SIP!" to "Your balance is below the minimum required for SIP!",
            "Total Credit" to "Total Credit",
            "Total Debit" to "Total Debit",
            "Category Breakdown" to "Category Breakdown",
            "No confirmed transactions recorded yet.\nGo to Wallet tab or simulate SMS alerts!" to "No confirmed transactions recorded yet.\nGo to Wallet tab or simulate SMS alerts!",
            
            "Visual Themes" to "Visual Themes",
            "Dynamic Theme" to "Dynamic Theme",
            "Vibrant gradients, rounded cards & playful transitions." to "Vibrant gradients, rounded cards & playful transitions.",
            "Professional Theme" to "Professional Theme",
            "Elegant borders, dark charcoal & executive dashboard designs." to "Elegant borders, dark charcoal & executive dashboard designs.",
            "Accent Color" to "Accent Color",
            "Display Mode" to "Display Mode",
            "System Theme" to "System Theme",
            "Light Mode" to "Light Mode",
            "Dark Mode" to "Dark Mode",
            "Security Gates" to "Security Gates",
            "Lock App with PIN" to "Lock App with PIN",
            "Secure local wallet with secure PIN keyboard" to "Secure local wallet with secure PIN keyboard",
            "Localization" to "Localization",
            "Language" to "Language",
            "Enable Investments Module" to "Enable Investments Module",
            
            "AI Simulators & Tools" to "AI Simulators & Tools",
            "Scan Receipt" to "Scan Receipt",
            "Simulate SMS" to "Simulate SMS",
            "Add Manual" to "Add Manual",
            "Scan SMS Inbox" to "Scan SMS Inbox",
            "Search transactions..." to "Search transactions...",
            "Sort by:" to "Sort by:",
            "No transactions synced yet. Tap Scan SMS Inbox or Simulate SMS!" to "No transactions synced yet. Tap Scan SMS Inbox or Simulate SMS!",
            
            "AI Finance Assistant" to "AI Finance Assistant",
            "Type a message..." to "Type a message...",
            "Send" to "Send",
            
            "Budgets" to "Budgets",
            "Savings Goals" to "Savings Goals",
            "Investments" to "Investments",
            "Splitwise" to "Splitwise",
            
            "Confirm" to "Confirm",
            "Cancel" to "Cancel",
            "Ignore" to "Ignore",
            "Save" to "Save",
            "Title" to "Title",
            "Amount" to "Amount",
            "Category" to "Category",
            "Type" to "Type",
            "Income" to "Income",
            "Expense" to "Expense",
            "Delete" to "Delete",
            "Add Transaction" to "Add Transaction",
            "Transaction Details" to "Transaction Details",
            
            "Date" to "Date",
            "Merchant" to "Merchant",
            "Last Synced Bank Balance" to "Last Synced Bank Balance",
            "Voice Input" to "Voice Input",
            "Camera Scan" to "Camera Scan",
            "Simulate Mock Receipt" to "Simulate Mock Receipt",
            "Choose scanning method:" to "Choose scanning method:",
            "Voice transaction recorded!" to "Voice transaction recorded!",
            "Failed to parse voice command." to "Failed to parse voice command.",
            "Tap microphone to speak" to "Tap microphone to speak",
            "Spoken Text / Translation" to "Spoken Text / Translation",
            "Presets for Testing:" to "Presets for Testing:",
            "Speak your transaction, and Sarvam AI will automatically parse and record it." to "Speak your transaction, and Sarvam AI will automatically parse and record it.",
            "Safe Spend" to "Safe Spend",
            "Safe Spend Calculator" to "Safe Spend Calculator",
            "Save Financial Setup" to "Save Financial Setup",
            "Monthly Income Override (0 = auto)" to "Monthly Income Override (0 = auto)",
            "Monthly Rent (₹)" to "Monthly Rent (₹)",
            "EMI Amount (₹)" to "EMI Amount (₹)",
            "EMI Due Day (1-31)" to "EMI Due Day (1-31)",
            "SIP Amount (₹)" to "SIP Amount (₹)",
            "SIP Due Day (1-31)" to "SIP Due Day (1-31)",
            "Other Mandatory Bills (₹)" to "Other Mandatory Bills (₹)",
            "Safe Spend Remaining" to "Safe Spend Remaining",
            "Daily Safe Limit" to "Daily Safe Limit",
            "Clear Wallet Transactions" to "Clear Wallet Transactions",
            "Clear Wallet" to "Clear Wallet",
            "Maintenance & Reset" to "Maintenance & Reset",
            "Hides transactions from wallet view. Preserves data for charts and calculators." to "Hides transactions from wallet view. Preserves data for charts and calculators.",
            "Wallet transactions cleared!" to "Wallet transactions cleared!",
            "API Keys updated successfully!" to "API Keys updated successfully!",
            "Low Balance Alert: EMI" to "Low Balance Alert: EMI",
            "Low Balance Alert: SIP" to "Low Balance Alert: SIP",
            "Domain Spend Percentage" to "Domain Spend Percentage"
        ),
        "hi" to mapOf(
            "Dashboard" to "डैशबोर्ड",
            "Wallet" to "बटुआ",
            "AI Chat" to "एआई चैट",
            "Financial Tools" to "वित्तीय साधन",
            "Settings" to "सेटिंग्स",
            
            "Good Morning ☀️" to "सुप्रभात ☀️",
            "Good Afternoon 🌤️" to "शुभ दोपहर 🌤️",
            "Good Evening 🌙" to "शुभ संध्या 🌙",
            
            "Select App Language" to "ऐप की भाषा चुनें",
            "Choose your preferred language for the interface and AI chatbot assistant." to "इंटरफ़ेस और एआई चैटबॉट सहायक के लिए अपनी पसंदीदा भाषा चुनें।",
            "Send OTP & Continue" to "ओटीपी भेजें और जारी रखें",
            "Back" to "पीछे",
            "Enter Verification Code" to "सत्यापन कोड दर्ज करें",
            "We sent a 6-digit OTP code to" to "हमने 6 अंकों का ओटीपी कोड भेजा है",
            "Please enter it below to confirm your phone number." to "अपना फोन नंबर सत्यापित करने के लिए कृपया इसे नीचे दर्ज करें।",
            "6-Digit OTP Code" to "6 अंकों का ओटीपी कोड",
            "Resend Code" to "कोड पुनः भेजें",
            "Verify & Continue" to "सत्यापित करें और जारी रखें",
            "OTP Sent successfully!" to "ओटीपी सफलतापूर्वक भेजा गया!",
            "OTP Resent successfully" to "ओटीपी सफलतापूर्वक पुनः भेजा गया",
            "Registration Verified!" to "पंजीकरण सत्यापित हो गया!",
            "Error:" to "त्रुटि:",
            "Try bypass code '123456'." to "बायपास कोड '123456' आज़माएं।",
            "OTP must be exactly 6 digits" to "ओटीपी ठीक 6 अंकों का होना चाहिए",
            
            "Net Balance" to "कुल जमा राशि",
            "Monthly Income" to "मासिक आय",
            "Monthly Expense" to "मासिक खर्च",
            "Recent Transactions" to "हाल के लेन-देन",
            "See All" to "सभी देखें",
            "Low Balance Warning" to "कम बैलेंस की चेतावनी",
            "Your balance is below the minimum required for SIP!" to "काफी कम बैलेंस",
            "Total Credit" to "कुल क्रेडिट",
            "Total Debit" to "कुल डेबिट",
            "Category Breakdown" to "श्रेणी विवरण",
            "No confirmed transactions recorded yet.\nGo to Wallet tab or simulate SMS alerts!" to "अभी तक कोई पुष्टि नहीं हुई लेन-देन दर्ज नहीं है।\nवॉलेट टैब पर जाएं या एसएमएस अलर्ट सिमुलेट करें!",
            
            "Visual Themes" to "दृश्य थीम्स",
            "Dynamic Theme" to "डायनेमिक थीम",
            "Vibrant gradients, rounded cards & playful transitions." to "वाइब्रेंट ग्रेडिएंट्स, गोल कार्ड और प्लेफुल ट्रांजिशन।",
            "Professional Theme" to "प्रोफेशनल थीम",
            "Elegant borders, dark charcoal & executive dashboard designs." to "सुरुचिपूर्ण बॉर्डर, डार्क चारकोल और कार्यकारी डैशबोर्ड डिज़ाइन।",
            "Accent Color" to "एक्सेंट रंग",
            "Display Mode" to "प्रदर्शन मोड",
            "System Theme" to "सिस्टम थीम",
            "Light Mode" to "लाइट मोड",
            "Dark Mode" to "डार्क मोड",
            "Security Gates" to "सुरक्षा द्वार",
            "Lock App with PIN" to "पिन के साथ ऐप लॉक करें",
            "Secure local wallet with secure PIN keyboard" to "सुरक्षित पिन कीबोर्ड के साथ स्थानीय वॉलेट सुरक्षित करें",
            "Localization" to "स्थानीयकरण",
            "Language" to "भाषा",
            "Enable Investments Module" to "निवेश मॉड्यूल सक्षम करें",
            
            "AI Simulators & Tools" to "एआई सिमुलेटर और उपकरण",
            "Scan Receipt" to "रसीद स्कैन करें",
            "Simulate SMS" to "एसएमएस सिमुलेट करें",
            "Add Manual" to "मैन्युअल जोड़ें",
            "Scan SMS Inbox" to "एसएमएस इनबॉक्स स्कैन करें",
            "Search transactions..." to "लेन-देन खोजें...",
            "Sort by:" to "क्रमबद्ध करें:",
            "No transactions synced yet. Tap Scan SMS Inbox or Simulate SMS!" to "अभी तक कोई लेन-देन सिंक नहीं हुआ है। एसएमएस इनबॉक्स स्कैन करें या एसएमएस सिमुलेट करें!",
            
            "AI Finance Assistant" to "एआई वित्त सहायक",
            "Type a message..." to "एक संदेश टाइप करें...",
            "Send" to "भेजें",
            
            "Budgets" to "बजट",
            "Savings Goals" to "बचत लक्ष्य",
            "Investments" to "निवेश",
            "Splitwise" to "स्प्लिटवाइज़",
            
            "Confirm" to "पुष्टि करें",
            "Cancel" to "रद्द करें",
            "Ignore" to "अनदेखा करें",
            "Save" to "सहेजें",
            "Title" to "शीर्षक",
            "Amount" to "राशि",
            "Category" to "श्रेणी",
            "Type" to "प्रकार",
            "Income" to "आय",
            "Expense" to "खर्च",
            "Delete" to "हटाएं",
            "Add Transaction" to "लेन-देन जोड़ें",
            "Transaction Details" to "लेन-देन विवरण",
            
            "Date" to "दिनांक",
            "Merchant" to "व्यापारी",
            "Last Synced Bank Balance" to "अंतिम सिंक किया गया बैंक बैलेंस",
            "Voice Input" to "आवाज इनपुट",
            "Camera Scan" to "कैमरा स्कैन",
            "Simulate Mock Receipt" to "नकली रसीद सिमुलेट करें",
            "Choose scanning method:" to "स्कैनिंग विधि चुनें:",
            "Voice transaction recorded!" to "आवाज लेन-देन दर्ज हो गया!",
            "Failed to parse voice command." to "वॉयस कमांड पार्स करने में विफल।",
            "Tap microphone to speak" to "बोलने के लिए माइक्रोफ़ोन टैप करें",
            "Spoken Text / Translation" to "बोला गया पाठ / अनुवाद",
            "Presets for Testing:" to "परीक्षण के लिए प्रीसेट:",
            "Speak your transaction, and Sarvam AI will automatically parse and record it." to "अपना लेन-देन बोलें, और सर्वम एआई इसे स्वचालित रूप से पार्स और दर्ज करेगा।",
            "Safe Spend" to "सुरक्षित खर्च",
            "Safe Spend Calculator" to "सुरक्षित खर्च कैलकुलेटर",
            "Save Financial Setup" to "वित्तीय सेटअप सहेजें",
            "Monthly Income Override (0 = auto)" to "मासिक आय अधिभावी (0 = ऑटो)",
            "Monthly Rent (₹)" to "मासिक किराया (₹)",
            "EMI Amount (₹)" to "ईएमआई राशि (₹)",
            "EMI Due Day (1-31)" to "ईएमआई देय दिन (1-31)",
            "SIP Amount (₹)" to "एसआईपी राशि (₹)",
            "SIP Due Day (1-31)" to "एसआईपी देय दिन (1-31)",
            "Other Mandatory Bills (₹)" to "अन्य अनिवार्य बिल (₹)",
            "Safe Spend Remaining" to "सुरक्षित खर्च शेष",
            "Daily Safe Limit" to "दैनिक सुरक्षित सीमा",
            "Clear Wallet Transactions" to "वॉलेट लेन-देन साफ करें",
            "Clear Wallet" to "वॉलेट साफ करें",
            "Maintenance & Reset" to "रखरखाव और रीसेट",
            "Hides transactions from wallet view. Preserves data for charts and calculators." to "वॉलेट दृश्य से लेन-देन छुपाता है। चार्ट और कैलकुलेटर के लिए डेटा रखता है।",
            "Wallet transactions cleared!" to "वॉलेट लेन-देन साफ हो गए!",
            "API Keys updated successfully!" to "एपीआई कुंजी सफलतापूर्वक अपडेट की गई!",
            "Low Balance Alert: EMI" to "कम बैलेंस अलर्ट: ईएमआई",
            "Low Balance Alert: SIP" to "कम बैलेंस अलर्ट: एसआईपी",
            "Domain Spend Percentage" to "डोमेन खर्च प्रतिशत"
        )
    )

    fun translate(text: String, language: LanguageType): String {
        return translations[language.code]?.get(text) ?: text
    }
}

fun String.translate(language: LanguageType): String {
    return Translator.translate(this, language)
}

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
                    text = "Welcome to FinKlar",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceTransactionDialog(
    language: LanguageType,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var spokenText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var isTranslating by remember { mutableStateOf(false) }
    var recordingTimer by remember { mutableStateOf(15) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val audioFile = remember { java.io.File(context.cacheDir, "sarvam_voice.m4a") }
    val audioRecorder = remember { com.example.antigravityfinance.service.audio.AudioRecorder(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isRecording = true
            recordingTimer = 15
            try {
                audioRecorder.startRecording(audioFile)
                scope.launch {
                    while (recordingTimer > 0 && isRecording) {
                        delay(1000)
                        recordingTimer--
                    }
                    if (isRecording) {
                        isRecording = false
                        audioRecorder.stopRecording()
                        isTranslating = true
                        viewModel.transcribeAndTranslateAudio(audioFile) { transcript ->
                            isTranslating = false
                            if (transcript != null) {
                                spokenText = transcript
                            } else {
                                android.widget.Toast.makeText(context, "Failed to translate voice audio.".translate(language), android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                isRecording = false
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Error starting recorder: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            android.widget.Toast.makeText(context, "Microphone permission is required to record audio.".translate(language), android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val startRecordingFlow = {
        val hasMicPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasMicPermission) {
            isRecording = true
            recordingTimer = 15
            try {
                audioRecorder.startRecording(audioFile)
                scope.launch {
                    while (recordingTimer > 0 && isRecording) {
                        delay(1000)
                        recordingTimer--
                    }
                    if (isRecording) {
                        isRecording = false
                        audioRecorder.stopRecording()
                        isTranslating = true
                        viewModel.transcribeAndTranslateAudio(audioFile) { transcript ->
                            isTranslating = false
                            if (transcript != null) {
                                spokenText = transcript
                            } else {
                                android.widget.Toast.makeText(context, "Failed to translate voice audio.".translate(language), android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                isRecording = false
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Error starting recorder: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    val stopRecordingFlow = {
        isRecording = false
        audioRecorder.stopRecording()
        isTranslating = true
        viewModel.transcribeAndTranslateAudio(audioFile) { transcript ->
            isTranslating = false
            if (transcript != null) {
                spokenText = transcript
            } else {
                android.widget.Toast.makeText(context, "Failed to translate voice audio.".translate(language), android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Sarvam AI Voice Input".translate(language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = "Speak your transaction, and Sarvam AI will automatically parse and record it.".translate(language),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isTranslating) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Text(
                        text = "Translating speech with Sarvam AI...".translate(language),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (isRecording) {
                    val waveTransition = rememberInfiniteTransition(label = "mic_wave")
                    val waveScale by waveTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 1.4f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "wave_scale"
                    )
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                            .clickable { stopRecordingFlow() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp * waveScale)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                    Text(
                        text = "${"Recording...".translate(language)} (${recordingTimer}s)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    IconButton(
                        onClick = { startRecordingFlow() },
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Start Recording",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text("Tap microphone to speak".translate(language), style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = spokenText,
                    onValueChange = { spokenText = it },
                    label = { Text("Spoken Text / Translation".translate(language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Presets for Testing:".translate(language),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Start)
                )
                
                val presets = listOf(
                    "Spent 350 rupees at Starbucks on Coffee",
                    "Received salary of 65000 rupees from company",
                    "Paid 1250 rupees to Domino's Pizza for party"
                )
                
                presets.forEach { preset ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { spokenText = preset },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = preset,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel".translate(language))
                    }
                    Button(
                        onClick = {
                            if (spokenText.isNotBlank()) {
                                onSave(spokenText)
                            }
                        },
                        enabled = spokenText.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Process".translate(language))
                    }
                }
            }
        }
    }
}

@Composable
fun SipAlertCard(
    smsSyncedBalance: Double,
    totalSipAmount: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = modifier.fillMaxWidth()
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
                    text = "Your synced bank balance ($currencySymbol${String.format("%,.2f", smsSyncedBalance)}) is lower than your active monthly SIP commitments ($currencySymbol${String.format("%,.0f", totalSipAmount)}). Please maintain sufficient balance to avoid failed transaction charges.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
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
    val language by viewModel.language.collectAsState()
    val smsSyncedBalance by viewModel.smsSyncedBalance.collectAsState()
    val investments by viewModel.investments.collectAsState()
    val userName by viewModel.userName.collectAsState()

    var showCreditDialog by remember { mutableStateOf(false) }
    var showDebitDialog by remember { mutableStateOf(false) }

    val totalSipAmount = remember(investments) {
        investments.filter { it.type == "SIP" }.sumOf { it.investedAmount }
    }
    
    val setZeroTimestamp by viewModel.setZeroTimestamp.collectAsState()
    val confirmedTx = transactions.filter { it.status == TransactionStatus.CONFIRMED && it.date > setZeroTimestamp }
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

    val now = remember { java.util.Calendar.getInstance() }
    val greeting = remember {
        val hour = now.get(java.util.Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good Morning ☀️"
            hour < 17 -> "Good Afternoon 🌤️"
            else      -> "Good Evening 🌙"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greeting.translate(language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = userName.ifBlank { "FinKlar" },
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.AccountBalance,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        if (smsSyncedBalance != null && totalSipAmount > 0.0 && smsSyncedBalance!! < totalSipAmount) {
            SipAlertCard(
                smsSyncedBalance = smsSyncedBalance!!,
                totalSipAmount = totalSipAmount,
                currencySymbol = currency.symbol
            )
        }

        // ── Net Balance card ──────────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Net Balance".translate(language),
                            color = Color.Black.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${currency.symbol}${String.format("%,.2f", netBalance)}",
                        color = Color.Black,
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    // Credit / Debit stat row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Credit chip
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showCreditDialog = true }
                                .background(Color.White.copy(alpha = 0.45f))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(Color(0xFF34A853), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Credit", color = Color.Black.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${currency.symbol}${String.format("%,.0f", creditSum)}",
                                    color = Color.Black,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        // Debit chip
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showDebitDialog = true }
                                .background(Color.White.copy(alpha = 0.45f))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(Color(0xFFEA4335), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Debit", color = Color.Black.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${currency.symbol}${String.format("%,.0f", debitSum)}",
                                    color = Color.Black,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (confirmedTx.isEmpty()) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Inbox, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Text("No transactions yet", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tap Scan SMS to import from your bank messages", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            val recentTx = remember(confirmedTx) {
                confirmedTx.sortedByDescending { it.date }.take(5)
            }

            Text(
                text = "Recent Transactions".translate(language),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            recentTx.forEach { tx ->
                TransactionRow(
                    tx = tx,
                    currency = currency,
                    onClick = { }
                )
            }
        }

        if (showCreditDialog) {
            FilteredTransactionListDialog(
                title = "Credit Transactions".translate(language),
                isIncome = true,
                transactions = transactions,
                currency = currency,
                language = language,
                onDismiss = { showCreditDialog = false }
            )
        }

        if (showDebitDialog) {
            FilteredTransactionListDialog(
                title = "Debit Transactions".translate(language),
                isIncome = false,
                transactions = transactions,
                currency = currency,
                language = language,
                onDismiss = { showDebitDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilteredTransactionListDialog(
    title: String,
    isIncome: Boolean,
    transactions: List<Transaction>,
    currency: CurrencyType,
    language: LanguageType,
    onDismiss: () -> Unit
) {
    var filterAllTime by remember { mutableStateOf(false) }
    
    val currentCalendar = remember { java.util.Calendar.getInstance() }
    val currentYear = currentCalendar.get(java.util.Calendar.YEAR)
    val currentMonth = currentCalendar.get(java.util.Calendar.MONTH)

    val filteredTransactions = remember(transactions, filterAllTime) {
        transactions.filter { tx ->
            if (tx.isIncome == isIncome && tx.status == TransactionStatus.CONFIRMED) {
                if (filterAllTime) {
                    true
                } else {
                    val txCal = java.util.Calendar.getInstance().apply { timeInMillis = tx.date }
                    txCal.get(java.util.Calendar.YEAR) == currentYear && txCal.get(java.util.Calendar.MONTH) == currentMonth
                }
            } else {
                false
            }
        }.sortedByDescending { it.date }
    }

    val monthName = remember {
        java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close".translate(language))
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (filterAllTime) title else "$title - $monthName",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = filterAllTime,
                    onClick = { filterAllTime = !filterAllTime },
                    label = { Text("All Time".translate(language), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        text = {
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (filterAllTime) "No transactions found.".translate(language) else "No transactions in $monthName.".translate(language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTransactions) { tx ->
                        TransactionRow(
                            tx = tx,
                            currency = currency,
                            onClick = {}
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val language by viewModel.language.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val isSmsScanning by viewModel.isSmsScanning.collectAsState()
    val smsSyncedBalance by viewModel.smsSyncedBalance.collectAsState()
    
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
    
    var showScanChoiceDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.handleRealOcrTrigger(bitmap)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    viewModel.handleRealOcrTrigger(bitmap)
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Failed to load image: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
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

    Column(modifier = modifier.fillMaxSize()) {
        // Header row with title and Scan SMS button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = {
                    val permission = android.Manifest.permission.READ_SMS
                    val granted = androidx.core.content.ContextCompat.checkSelfPermission(context, permission) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (granted) {
                        viewModel.scanSmsInbox { count, _ ->
                            scanStatusMessage = "Scan complete. Synced $count new transactions."
                        }
                    } else {
                        smsPermissionLauncher.launch(permission)
                    }
                },
                enabled = !isSmsScanning,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                if (isSmsScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scanning...", fontSize = 13.sp)
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Sms,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan SMS", fontSize = 13.sp)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
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
                                text = "Last Synced Bank Balance".translate(language),
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

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search transactions...".translate(language)) },
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
                    text = "Sort by:".translate(language),
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
                            Text(sortBy.translate(language))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            listOf("Date", "Amount", "Merchant").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.translate(language)) },
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
                    Text("No transactions synced yet. Tap Scan SMS Inbox or Simulate SMS!".translate(language))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredTransactions,
                        key = { tx -> tx.id }
                    ) { tx ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.StartToEnd) {
                                    viewModel.deleteTransaction(tx)
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = true,
                            enableDismissFromEndToStart = false,
                            backgroundContent = {
                                val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                                    Color.Red
                                } else {
                                    Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color, shape = RoundedCornerShape(16.dp))
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
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
        }

        // Expandable Speed Dial FAB in bottom-right
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            AnimatedVisibility(
                visible = isFabExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    // Scan Receipt Option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable { 
                            isFabExpanded = false
                            showScanChoiceDialog = true 
                        }
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = "Scan Receipt".translate(language),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        FloatingActionButton(
                            onClick = { 
                                isFabExpanded = false
                                showScanChoiceDialog = true 
                            },
                            modifier = Modifier.size(48.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Scan Receipt", modifier = Modifier.size(24.dp))
                        }
                    }

                    // Add Manual Option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable { 
                            isFabExpanded = false
                            showManualDialog = true 
                        }
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = "Add Manual".translate(language),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        FloatingActionButton(
                            onClick = { 
                                isFabExpanded = false
                                showManualDialog = true 
                            },
                            modifier = Modifier.size(48.dp),
                            containerColor = AccentEmerald
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Manual", modifier = Modifier.size(24.dp), tint = Color.White)
                        }
                    }

                    // Voice Input Option (Sarvam AI)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable { 
                            isFabExpanded = false
                            showVoiceDialog = true 
                        }
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = "Voice Input".translate(language),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        FloatingActionButton(
                            onClick = { 
                                isFabExpanded = false
                                showVoiceDialog = true 
                            },
                            modifier = Modifier.size(48.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Input", modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            // Primary Toggle FAB
            FloatingActionButton(
                onClick = { isFabExpanded = !isFabExpanded },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = if (isFabExpanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Menu"
                )
            }
            }
        }
    }

    if (showScanChoiceDialog) {
        Dialog(onDismissRequest = { showScanChoiceDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Scan Receipt".translate(language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Choose scanning method:".translate(language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            showScanChoiceDialog = false
                            cameraLauncher.launch(null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Camera Scan".translate(language))
                    }
                    Button(
                        onClick = {
                            showScanChoiceDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gallery Scan".translate(language))
                    }
                    Button(
                        onClick = {
                            showScanChoiceDialog = false
                            viewModel.handleMockOcrTrigger(OcrScanner.MockReceiptType.STARBUCKS)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simulate Mock Receipt".translate(language))
                    }
                    OutlinedButton(
                        onClick = { showScanChoiceDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel".translate(language))
                    }
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

    if (showVoiceDialog) {
        VoiceTransactionDialog(
            language = language,
            viewModel = viewModel,
            onDismiss = { showVoiceDialog = false },
            onSave = { spokenText ->
                showVoiceDialog = false
                viewModel.processVoiceTransaction(spokenText) { tx ->
                    if (tx != null) {
                        android.widget.Toast.makeText(context, "Voice transaction recorded!".translate(language), android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Failed to parse voice command.".translate(language), android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
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
    val accentColor = if (tx.isIncome) Color(0xFF34A853) else Color(0xFFEA4335)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Colored left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        color = if (tx.status == TransactionStatus.PENDING) MaterialTheme.colorScheme.primary
                                else accentColor,
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )
        Column(modifier = Modifier.padding(12.dp).weight(1f)) {
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
                            .size(42.dp)
                            .background(getCategoryColor(tx.category).copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(tx.category),
                            contentDescription = null,
                            tint = getCategoryColor(tx.category),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tx.merchant,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
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
                            text = SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(tx.date)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (tx.isIncome) "+" else "-"}${currency.symbol}${String.format("%,.2f", tx.amount)}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = accentColor
                    )
                    Text(
                        text = if (tx.isIncome) "Credit" else "Debit",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor.copy(alpha = 0.7f)
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
        } // end colored-bar Row
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

        val suggestionQueries = listOf(
            "How much did I spend on food?",
            "How much is left in my budget?",
            "Show my biggest debits",
            "Give me saving insights",
            "What is my investment growth?"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            suggestionQueries.forEach { query ->
                SuggestionChip(
                    onClick = {
                        viewModel.sendMessageToAssistant(query)
                    },
                    label = { Text(query, fontSize = 12.sp) },
                    shape = RoundedCornerShape(16.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Ask FinKlar AI...") },
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
                color = if (isUser) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
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

// --- SAFE SPEND SCREEN ---
@Composable
fun SafeSpendScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val rentAmount by viewModel.rentAmount.collectAsState()
    val emiAmount by viewModel.emiAmount.collectAsState()
    val emiDay by viewModel.emiDay.collectAsState()
    val sipAmount by viewModel.sipAmount.collectAsState()
    val sipDay by viewModel.sipDay.collectAsState()
    val otherMandatory by viewModel.otherMandatory.collectAsState()
    
    val currency by viewModel.currency.collectAsState()
    val language by viewModel.language.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val smsSyncedBalance by viewModel.smsSyncedBalance.collectAsState()
    val setZeroTimestamp by viewModel.setZeroTimestamp.collectAsState()

    var isSetupExpanded by remember { mutableStateOf(false) }

    var editRent by remember(rentAmount) { mutableStateOf(if (rentAmount > 0.0) rentAmount.toInt().toString() else "") }
    var editEmi by remember(emiAmount) { mutableStateOf(if (emiAmount > 0.0) emiAmount.toInt().toString() else "") }
    var editEmiDay by remember(emiDay) { mutableStateOf(emiDay.toString()) }
    var editSip by remember(sipAmount) { mutableStateOf(if (sipAmount > 0.0) sipAmount.toInt().toString() else "") }
    var editSipDay by remember(sipDay) { mutableStateOf(sipDay.toString()) }
    var editOther by remember(otherMandatory) { mutableStateOf(if (otherMandatory > 0.0) otherMandatory.toInt().toString() else "") }

    val calendar = remember(transactions) { java.util.Calendar.getInstance() }
    val currentMonth = calendar.get(java.util.Calendar.MONTH)
    val currentYear = calendar.get(java.util.Calendar.YEAR)
    val todayDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val totalDaysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    val daysRemaining = totalDaysInMonth - todayDay + 1

    val confirmedTx = remember(transactions, setZeroTimestamp) {
        transactions.filter { it.status == TransactionStatus.CONFIRMED && it.date > setZeroTimestamp }
    }

    val calculatedIncome = remember(transactions) {
        val monthCredits = confirmedTx.filter { 
            if (it.isIncome) {
                val txCal = java.util.Calendar.getInstance().apply { timeInMillis = it.date }
                txCal.get(java.util.Calendar.MONTH) == currentMonth &&
                txCal.get(java.util.Calendar.YEAR) == currentYear
            } else {
                false
            }
        }.sumOf { it.amount }
        if (monthCredits > 0.0) monthCredits else 65000.0
    }
    val totalIncome = calculatedIncome

    val totalCommitments = rentAmount + emiAmount + sipAmount + otherMandatory
    val disposableIncome = (totalIncome - totalCommitments).coerceAtLeast(0.0)

    val variableSpent = remember(transactions) {
        confirmedTx.filter { 
            if (!it.isIncome) {
                val txCal = java.util.Calendar.getInstance().apply { timeInMillis = it.date }
                txCal.get(java.util.Calendar.MONTH) == currentMonth &&
                txCal.get(java.util.Calendar.YEAR) == currentYear
            } else {
                false
            }
        }.sumOf { it.amount }
    }

    val safeSpendRemaining = (disposableIncome - variableSpent).coerceAtLeast(0.0)
    val safeDailyLimit = if (daysRemaining > 0) safeSpendRemaining / daysRemaining else safeSpendRemaining

    val walletBalance = remember(transactions) {
        val confirmed = transactions.filter { it.status == TransactionStatus.CONFIRMED }
        confirmed.filter { it.isIncome }.sumOf { it.amount } - confirmed.filter { !it.isIncome }.sumOf { it.amount }
    }
    val currentBalance = smsSyncedBalance ?: walletBalance

    fun isDueSoon(targetDay: Int, currentDay: Int, maxDays: Int): Boolean {
        if (targetDay >= currentDay) {
            return (targetDay - currentDay) in 0..3
        }
        return (targetDay + maxDays - currentDay) in 0..3
    }
    val isEmiAlert = emiAmount > 0.0 && isDueSoon(emiDay, todayDay, totalDaysInMonth) && currentBalance < emiAmount
    val isSipAlert = sipAmount > 0.0 && isDueSoon(sipDay, todayDay, totalDaysInMonth) && currentBalance < sipAmount

    val categorySpentPcts = remember(transactions, totalIncome) {
        confirmedTx.filter { !it.isIncome }
            .groupBy { it.category }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                val pct = if (totalIncome > 0.0) (sum / totalIncome) * 100.0 else 0.0
                cat to pct
            }.sortedByDescending { it.second }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isEmiAlert) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Low Balance Alert: EMI".translate(language),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Your balance (${currency.symbol}${String.format("%.2f", currentBalance)}) is below your upcoming EMI of ${currency.symbol}${String.format("%.2f", emiAmount)} due on day ${emiDay}.".translate(language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        if (isSipAlert) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Low Balance Alert: SIP".translate(language),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Your balance (${currency.symbol}${String.format("%.2f", currentBalance)}) is below your upcoming SIP of ${currency.symbol}${String.format("%.2f", sipAmount)} due on day ${sipDay}.".translate(language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Safe Spend main display
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Safe Spend Remaining".translate(language),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${currency.symbol}${String.format("%,.2f", safeSpendRemaining)}",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Income".translate(language), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text("${currency.symbol}${totalIncome.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bills/Commitments".translate(language), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text("${currency.symbol}${totalCommitments.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Daily Safe Limit".translate(language), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text("${currency.symbol}${safeDailyLimit.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        // Commitments expander setup
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isSetupExpanded = !isSetupExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Financial Setup & Commitments".translate(language),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Icon(
                        imageVector = if (isSetupExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Expand/Collapse"
                    )
                }

                if (isSetupExpanded) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        OutlinedTextField(
                            value = editRent,
                            onValueChange = { editRent = it },
                            label = { Text("Monthly Rent (₹)".translate(language)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editEmi,
                                onValueChange = { editEmi = it },
                                label = { Text("EMI Amount (₹)".translate(language)) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = editEmiDay,
                                onValueChange = { editEmiDay = it },
                                label = { Text("EMI Due Day (1-31)".translate(language)) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editSip,
                                onValueChange = { editSip = it },
                                label = { Text("SIP Amount (₹)".translate(language)) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = editSipDay,
                                onValueChange = { editSipDay = it },
                                label = { Text("SIP Due Day (1-31)".translate(language)) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        OutlinedTextField(
                            value = editOther,
                            onValueChange = { editOther = it },
                            label = { Text("Other Mandatory Bills (₹)".translate(language)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = {
                                viewModel.updateRentAmount(editRent.toDoubleOrNull() ?: 0.0)
                                viewModel.updateEmiAmount(editEmi.toDoubleOrNull() ?: 0.0)
                                viewModel.updateEmiDay(editEmiDay.toIntOrNull() ?: 1)
                                viewModel.updateSipAmount(editSip.toDoubleOrNull() ?: 0.0)
                                viewModel.updateSipDay(editSipDay.toIntOrNull() ?: 1)
                                viewModel.updateOtherMandatory(editOther.toDoubleOrNull() ?: 0.0)
                                isSetupExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save Financial Setup".translate(language))
                        }
                    }
                }
            }
        }

        // Domain Analysis display
        Text("Domain Spend Percentage".translate(language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        if (categorySpentPcts.isEmpty()) {
            Text("No variable expense records found for this month.".translate(language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categorySpentPcts.forEach { (cat, pct) ->
                        val catDisplayName = TransactionCategory.values().find { it.name == cat }?.displayName ?: cat
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(catDisplayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(String.format("%.1f%%", pct), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = (pct / 100.0).toFloat().coerceIn(0f, 1f),
                                color = getCategoryColor(cat),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                            )
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
    val sarvamKey by viewModel.sarvamKey.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
 
    var showPinSetDialog by remember { mutableStateOf(false) }
    val isPinSet by viewModel.isPinSet.collectAsState()
 
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

 
        Text("Display Mode".translate(language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = isDark == false,
                onClick = { viewModel.updateDarkMode(false) },
                label = { Text("Light Mode".translate(language)) }
            )
            FilterChip(
                selected = isDark == true,
                onClick = { viewModel.updateDarkMode(true) },
                label = { Text("Dark Mode".translate(language)) }
            )
        }
 
        Divider()
 
        Text("Security Gates".translate(language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Lock App with PIN".translate(language), fontWeight = FontWeight.Bold)
                Text("Secure local wallet with secure PIN keyboard".translate(language), style = MaterialTheme.typography.bodySmall)
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
 
        Text("Localization".translate(language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
 
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Language".translate(language), fontWeight = FontWeight.Bold)
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
                Text("Enable Investments Module".translate(language), fontWeight = FontWeight.Bold)
                Text("SIPs, mutual funds watchlists, allocation charts", style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = isInvestmentsEnabled,
                onCheckedChange = { viewModel.updateInvestmentsEnabled(it) }
            )
        }

        Divider()

        Text("Maintenance & Reset".translate(language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text("Clear Wallet Transactions".translate(language), fontWeight = FontWeight.Bold)
                Text("Permanently deletes all transactions from the wallet. This resets your balance and data.".translate(language), style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(
                onClick = {
                    viewModel.clearTransactionsFromWallet()
                    android.widget.Toast.makeText(context, "Wallet transactions cleared!".translate(language), android.widget.Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Clear Wallet".translate(language))
            }
        }

        Divider()

        val setZeroTimestamp by viewModel.setZeroTimestamp.collectAsState()
        val formattedZeroDate = remember(setZeroTimestamp) {
            if (setZeroTimestamp > 0L) {
                val date = java.util.Date(setZeroTimestamp)
                val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                sdf.format(date)
            } else {
                "Not Set".translate(language)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text("Set Zero Point".translate(language), fontWeight = FontWeight.Bold)
                Text("Start a new count of credit/debit from a chosen date, discarding earlier sums but keeping transaction history.".translate(language), style = MaterialTheme.typography.bodySmall)
                if (setZeroTimestamp > 0L) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Current Zero Point: $formattedZeroDate".translate(language),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (setZeroTimestamp > 0L) {
                    OutlinedButton(
                        onClick = {
                            viewModel.saveSetZeroTimestamp(0L)
                            android.widget.Toast.makeText(context, "Zero point reset successfully!".translate(language), android.widget.Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset".translate(language))
                    }
                }
                Button(
                    onClick = {
                        val calendar = java.util.Calendar.getInstance()
                        if (setZeroTimestamp > 0L) {
                            calendar.timeInMillis = setZeroTimestamp
                        }
                        android.app.DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedCal = java.util.Calendar.getInstance().apply {
                                    set(java.util.Calendar.YEAR, year)
                                    set(java.util.Calendar.MONTH, month)
                                    set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                                    set(java.util.Calendar.MINUTE, 0)
                                    set(java.util.Calendar.SECOND, 0)
                                    set(java.util.Calendar.MILLISECOND, 0)
                                }
                                viewModel.saveSetZeroTimestamp(selectedCal.timeInMillis)
                                android.widget.Toast.makeText(context, "Zero point set successfully!".translate(language), android.widget.Toast.LENGTH_SHORT).show()
                            },
                            calendar.get(java.util.Calendar.YEAR),
                            calendar.get(java.util.Calendar.MONTH),
                            calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Set 0".translate(language))
                }
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        onClick = if (enabled) onClick else ({}),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            1.dp,
            if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(135.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(36.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// --- FINANCIAL TOOLS SCREEN ---
@Composable
fun FinancialToolsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    var activeTool by remember { mutableStateOf<String?>(null) }
    val isInvestmentsEnabled by viewModel.isInvestmentsEnabled.collectAsState()
    
    val investments by viewModel.investments.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val smsSyncedBalance by viewModel.smsSyncedBalance.collectAsState()

    val emiAmount by viewModel.emiAmount.collectAsState()
    val emiDay by viewModel.emiDay.collectAsState()
    val sipAmount by viewModel.sipAmount.collectAsState()
    val sipDay by viewModel.sipDay.collectAsState()
    val language by viewModel.language.collectAsState()

    // Get current calendar info
    val calendar = remember { java.util.Calendar.getInstance() }
    val todayDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val maxDays = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

    val setZeroTimestamp by viewModel.setZeroTimestamp.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val walletBalance = remember(transactions, setZeroTimestamp) {
        val confirmed = transactions.filter { it.status == TransactionStatus.CONFIRMED && it.date > setZeroTimestamp }
        confirmed.filter { it.isIncome }.sumOf { it.amount } - confirmed.filter { !it.isIncome }.sumOf { it.amount }
    }
    val currentBalance = smsSyncedBalance ?: walletBalance
    
    val totalSipAmount = remember(investments) {
        investments.filter { it.type == "SIP" }.sumOf { it.investedAmount }
    }

    // Helper to check if date is within 3 days
    fun isDueSoon(targetDay: Int, currentDay: Int, maxDays: Int): Boolean {
        if (targetDay >= currentDay) {
            return (targetDay - currentDay) in 0..3
        }
        return (targetDay + maxDays - currentDay) in 0..3
    }

    val isEmiAlert = emiAmount > 0.0 && isDueSoon(emiDay, todayDay, maxDays) && currentBalance < emiAmount
    val isSipAlert = sipAmount > 0.0 && isDueSoon(sipDay, todayDay, maxDays) && currentBalance < sipAmount

    if (activeTool == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Financial Tools".translate(language),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isEmiAlert) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Low Balance Alert: EMI".translate(language),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Your balance (${currency.symbol}${String.format("%.2f", currentBalance)}) is below your upcoming EMI of ${currency.symbol}${String.format("%.2f", emiAmount)} due on day ${emiDay}.".translate(language),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            if (isSipAlert) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Low Balance Alert: SIP".translate(language),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Your balance (${currency.symbol}${String.format("%.2f", currentBalance)}) is below your upcoming SIP of ${currency.symbol}${String.format("%.2f", sipAmount)} due on day ${sipDay}.".translate(language),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            if (smsSyncedBalance != null && totalSipAmount > 0.0 && smsSyncedBalance!! < totalSipAmount && !isSipAlert) {
                SipAlertCard(
                    smsSyncedBalance = smsSyncedBalance!!,
                    totalSipAmount = totalSipAmount,
                    currencySymbol = currency.symbol,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Grid Layout (2 columns per row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ToolCard(
                    title = "Safe Spend",
                    subtitle = "Analyze spent %, commit bills & save",
                    icon = Icons.Rounded.AccountBalance,
                    onClick = { activeTool = "Safe Spend" },
                    modifier = Modifier.weight(1f)
                )
                ToolCard(
                    title = "Savings Goals",
                    subtitle = "Track contributions and streaks",
                    icon = Icons.Rounded.Flag,
                    onClick = { activeTool = "Goals" },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ToolCard(
                    title = "Splitwise",
                    subtitle = "Split bills and settle balances",
                    icon = Icons.Rounded.Group,
                    onClick = { activeTool = "Splitwise" },
                    modifier = Modifier.weight(1f)
                )
                if (isInvestmentsEnabled) {
                    ToolCard(
                        title = "Investments",
                        subtitle = "Mutual funds, stocks & SIP tracker",
                        icon = Icons.Rounded.TrendingUp,
                        onClick = { activeTool = "Investments" },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    ToolCard(
                        title = "Investments (Locked)",
                        subtitle = "Enable in settings to track stocks & SIPs",
                        icon = Icons.Rounded.Lock,
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ToolCard(
                    title = "Statistics",
                    subtitle = "View bar graph and pie chart analysis",
                    icon = Icons.Rounded.BarChart,
                    onClick = { activeTool = "Statistics" },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activeTool = null }) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (activeTool) {
                        "Safe Spend" -> "Safe Spend".translate(language)
                        "Goals" -> "Savings Goals".translate(language)
                        "Investments" -> "Investment Portfolio".translate(language)
                        "Splitwise" -> "Splitwise".translate(language)
                        "Statistics" -> "Statistics".translate(language)
                        else -> ""
                    },
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
            
            Box(modifier = Modifier.weight(1f)) {
                when (activeTool) {
                    "Safe Spend" -> SafeSpendScreen(viewModel = viewModel)
                    "Goals" -> GoalsScreen(viewModel = viewModel)
                    "Investments" -> InvestmentsScreen(viewModel = viewModel)
                    "Splitwise" -> SplitwiseScreen(viewModel = viewModel)
                    "Statistics" -> StatisticsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun StatisticsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val language by viewModel.language.collectAsState()
    val setZeroTimestamp by viewModel.setZeroTimestamp.collectAsState()

    val confirmedTx = remember(transactions, setZeroTimestamp) {
        transactions.filter { it.status == TransactionStatus.CONFIRMED && it.date > setZeroTimestamp }
    }
    val creditSum = remember(confirmedTx) { confirmedTx.filter { it.isIncome }.sumOf { it.amount } }
    val debitSum = remember(confirmedTx) { confirmedTx.filter { !it.isIncome }.sumOf { it.amount } }

    val categoryTotals = remember(confirmedTx, language) {
        confirmedTx.filter { !it.isIncome }
            .groupBy { it.category }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                PieChartInput(
                    color = getCategoryColor(cat),
                    value = sum,
                    description = TransactionCategory.values().find { it.name == cat }?.displayName?.translate(language) ?: cat.translate(language)
                )
            }.filter { it.value > 0 }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (confirmedTx.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transaction data available to show statistics.".translate(language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Spending Overview".translate(language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    InteractiveBarChart(
                        creditValues = listOf(creditSum * 0.9, creditSum, creditSum * 0.95),
                        debitValues = listOf(debitSum * 0.8, debitSum * 0.9, debitSum),
                        labels = listOf("April", "May", "June"),
                        currencySymbol = currency.symbol,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (categoryTotals.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Category Breakdown".translate(language),
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

// --- SPLITWISE SCREEN ---
@Composable
fun SplitwiseScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val splits by viewModel.allSplits.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val themeType by viewModel.themeType.collectAsState()
    
    val mockContacts = remember { listOf("Alice", "Bob", "Charlie", "Dave", "Eve") }
    var showSplitDialog by remember { mutableStateOf(false) }
    
    val unsettledSplits = splits.filter { !it.isSettled }
    val totalOwed = unsettledSplits.sumOf { it.shareAmount }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance Owed Card
        val cardBrush = if (themeType == ThemeType.DYNAMIC) {
            Brush.horizontalGradient(
                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
            )
        } else {
            Brush.horizontalGradient(
                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.surfaceVariant)
            )
        }
        
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .background(cardBrush)
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Group,
                            contentDescription = "Splitwise",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            "Splitwise Balances",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Total You Are Owed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                    Text(
                        "${currency.symbol}${String.format("%,.2f", totalOwed)}",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        
        Button(
            onClick = { showSplitDialog = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Split a Transaction", fontWeight = FontWeight.Bold)
        }
        
        Text("Split History", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        
        if (splits.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No split transactions yet.\nClick above to split your first bill!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            splits.forEach { split ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = split.transactionMerchant,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Paid: ${currency.symbol}${split.transactionAmount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${split.contactName} owes you ${currency.symbol}${String.format("%.2f", split.shareAmount)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (split.isSettled) Color.Gray else MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        if (split.isSettled) {
                            Text(
                                text = "Settled",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        } else {
                            Button(
                                onClick = { viewModel.settleSplit(split.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Text("Settle Up", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showSplitDialog) {
        SplitTransactionDialog(
            transactions = transactions.filter { !it.isIncome && it.status == TransactionStatus.CONFIRMED },
            mockContacts = mockContacts,
            currencySymbol = currency.symbol,
            onDismiss = { showSplitDialog = false },
            onConfirmSplit = { transaction, contacts ->
                viewModel.splitTransaction(transaction, contacts)
                showSplitDialog = false
            }
        )
    }
}

@Composable
fun SplitTransactionDialog(
    transactions: List<Transaction>,
    mockContacts: List<String>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirmSplit: (Transaction, List<String>) -> Unit
) {
    var selectedTx by remember { mutableStateOf<Transaction?>(null) }
    var selectedContacts by remember { mutableStateOf(setOf<String>()) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Split a Bill",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                if (selectedTx == null) {
                    Text("Select a transaction:", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (transactions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No recent expenses to split.")
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            transactions.forEach { tx ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { selectedTx = tx },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(tx.merchant, fontWeight = FontWeight.SemiBold)
                                            Text(tx.category, style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text(
                                            "${currencySymbol}${tx.amount}",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                    }
                } else {
                    val tx = selectedTx!!
                    Text(
                        "Split ${tx.merchant} (${currencySymbol}${tx.amount})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Select friends to split with:", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        mockContacts.forEach { contact ->
                            val isChecked = selectedContacts.contains(contact)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedContacts = if (isChecked) {
                                            selectedContacts - contact
                                        } else {
                                            selectedContacts + contact
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedContacts = if (checked) {
                                            selectedContacts + contact
                                        } else {
                                            selectedContacts - contact
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(contact, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    
                    if (selectedContacts.isNotEmpty()) {
                        val totalPeople = selectedContacts.size + 1
                        val share = tx.amount / totalPeople
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Split: You + ${selectedContacts.size} friends",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Each share: $currencySymbol${String.format("%,.2f", share)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { selectedTx = null; selectedContacts = emptySet() }) {
                            Text("Back")
                        }
                        Row {
                            TextButton(onClick = onDismiss) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onConfirmSplit(tx, selectedContacts.toList()) },
                                enabled = selectedContacts.isNotEmpty()
                            ) {
                                Text("Split")
                            }
                        }
                    }
                }
            }
        }
    }
}
