package com.example.antigravityfinance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.antigravityfinance.data.model.LanguageType

enum class PrivacyItemType {
    TITLE,
    SUBTITLE,
    HEADING,
    SUBHEADING,
    BODY,
    BULLET
}

data class PrivacyPolicyItem(
    val type: PrivacyItemType,
    val text: String
)

object PrivacyPolicyData {
    fun getItems(language: LanguageType): List<PrivacyPolicyItem> {
        return if (language == LanguageType.HINDI) {
            listOf(
                PrivacyPolicyItem(PrivacyItemType.TITLE, "FinKlar के लिए गोपनीयता नीति"),
                PrivacyPolicyItem(PrivacyItemType.SUBTITLE, "अंतिम अपडेट: 14 जून, 2026"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "FinKlar में आपका स्वागत है। आपकी गोपनीयता हमारे लिए महत्वपूर्ण है। यह गोपनीयता नीति बताती है कि जब आप हमारे एंड्रॉइड एप्लिकेशन का उपयोग करते हैं तो हम आपकी जानकारी को कैसे एकत्र, उपयोग, संग्रहीत और सुरक्षित करते हैं।"),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "जानकारी जो हम एकत्र करते हैं"),
                
                PrivacyPolicyItem(PrivacyItemType.SUBHEADING, "खाता जानकारी"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "जब आप एक खाता बनाते हैं, तो हम एकत्र कर सकते हैं:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "नाम"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "ईमेल पता"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "लॉगिन क्रेडेंशियल"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "आपके द्वारा प्रदान की गई प्रोफ़ाइल जानकारी"),
                
                PrivacyPolicyItem(PrivacyItemType.SUBHEADING, "एसएमएस डेटा (SMS Data)"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "आपकी स्पष्ट अनुमति से, FinKlar आपके डिवाइस पर वित्तीय एसएमएस संदेशों को पढ़ सकता है ताकि:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "लेन-देन का पता लगाया जा सके"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "लेन-देन के विवरण निकाले जा सकें"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "आय और खर्चों को वर्गीकृत किया जा सके"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "वित्तीय अंतर्दृष्टि का निर्माण किया जा सके"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "FinKlar केवल वित्तीय लेन-देन से संबंधित एसएमएस संदेशों को संसाधित करता है।"),
                
                PrivacyPolicyItem(PrivacyItemType.SUBHEADING, "वित्तीय जानकारी"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "हम एकत्र कर सकते हैं:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "लेन-देन की राशि"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "लेन-देन की तिथियां"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "व्यापारियों के नाम"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "व्यय श्रेणियां"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "उपयोगकर्ता द्वारा दर्ज किए गए नोट्स"),
                
                PrivacyPolicyItem(PrivacyItemType.SUBHEADING, "दस्तावेज़ और चित्र"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "यदि आप रसीदें, बिल, या लेन-देन के स्क्रीनशॉट स्कैन करना चुनते हैं, तो हम संसाधित कर सकते हैं:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "अपलोड की गई छवियां"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "निकाली गई पाठ्य सामग्री और लेन-देन का विवरण"),
                
                PrivacyPolicyItem(PrivacyItemType.SUBHEADING, "एआई चैट डेटा (AI Chat Data)"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "वित्तीय अंतर्दृष्टि, बजट सहायता और उपयोगकर्ता सहायता प्रदान करने के लिए एआई सहायक के माध्यम से भेजे गए संदेशों को संसाधित किया जा सकता है।"),
                
                PrivacyPolicyItem(PrivacyItemType.SUBHEADING, "डिवाइस की जानकारी"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "हम एकत्र कर सकते हैं:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "डिवाइस मॉडल"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "ऑपरेटिंग सिस्टम संस्करण"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "ऐप संस्करण"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "डायग्नोस्टिक और क्रैश जानकारी"),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "हम जानकारी का उपयोग कैसे करते हैं"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "हम आपकी जानकारी का उपयोग निम्न के लिए करते हैं:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "खाता पहुंच प्रदान करना"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "आय और व्यय को ट्रैक करना"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "वित्तीय रिपोर्ट और अंतर्दृष्टि उत्पन्न करना"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "एआई-संचालित सुविधाओं में सुधार करना"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "धोखाधड़ी और दुरुपयोग का पता लगाना"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "ऐप के प्रदर्शन और सुरक्षा में सुधार करना"),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "डेटा भंडारण और सुरक्षा"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "ट्रांसमिशन से पहले उपयोगकर्ता डेटा को एन्क्रिप्ट किया जाता है।"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "डेटा को एन्क्रिप्टेड क्लाउड इन्फ्रास्ट्रक्चर का उपयोग करके संग्रहीत किया जाता है।"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "उपयोगकर्ता डेटा तक पहुंच अधिकृत प्रणालियों और कर्मियों तक सीमित है।"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "जानकारी की सुरक्षा के लिए उद्योग-मानक सुरक्षा उपायों का उपयोग किया जाता है।"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "हालांकि, ट्रांसमिशन या स्टोरेज की कोई भी विधि पूरी तरह से सुरक्षित नहीं है, और पूर्ण सुरक्षा की गारंटी नहीं दी जा सकती है।"),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "डेटा साझा करना"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "हम आपकी व्यक्तिगत जानकारी को बेचते नहीं हैं।"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "हम जानकारी साझा कर सकते हैं:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "क्लाउड होस्टिंग प्रदाताओं के साथ"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "प्रमाणीकरण प्रदाताओं के साथ"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "एआई सेवा प्रदाताओं के साथ"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "कानूनी अधिकारियों के साथ जब कानून द्वारा आवश्यक हो"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "सभी तृतीय-पक्ष प्रदाताओं से उचित सुरक्षा मानकों को बनाए रखने की अपेक्षा की जाती है।"),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "एआई सेवाएं"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "FinKlar निम्न प्रदान करने के लिए तृतीय-पक्ष एआई सेवाओं का उपयोग कर सकता है:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "वित्तीय विश्लेषण"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "बजट सुझाव"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "लेन-देन का वर्गीकरण"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "उपयोगकर्ता सहायता"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "उपयोगकर्ताओं को वित्तीय निर्णयों के लिए पूरी तरह से एआई-जनरेटेड प्रतिक्रियाओं पर भरोसा नहीं करना चाहिए।"),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "डेटा प्रतिधारण (Data Retention)"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "हम सेवाओं को प्रदान करने या कानूनी दायित्वों का पालन करने के लिए आवश्यक समय तक डेटा बनाए रखते हैं।"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "उपयोगकर्ता अपने खाते और संबद्ध डेटा को हटाने का अनुरोध कर सकते हैं।"),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "उपयोगकर्ता के अधिकार"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "आप कर सकते हैं:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "अपनी जानकारी तक पहुँचें"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "गलत जानकारी को सही करें"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "अपना खाता हटाने का अनुरोध करें"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "एसएमएस एक्सेस जैसी अनुमतियां वापस लें"),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "बच्चों की गोपनीयता"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "FinKlar 13 वर्ष से कम उम्र के उपयोगकर्ताओं के लिए अभिप्रेत नहीं है।"),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "इस नीति में परिवर्तन"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "हम समय-समय पर इस गोपनीयता नीति को अपडेट कर सकते हैं। अपडेट के बाद FinKlar का निरंतर उपयोग संशोधित नीति की स्वीकृति माना जाएगा।"),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "संपर्क"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "गोपनीयता से संबंधित प्रश्नों के लिए, संपर्क करें:"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "ईमेल: support@finklar.com")
            )
        } else {
            listOf(
                PrivacyPolicyItem(PrivacyItemType.TITLE, "Privacy Policy for FinKlar"),
                PrivacyPolicyItem(PrivacyItemType.SUBTITLE, "Last Updated: June 14, 2026"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "Welcome to FinKlar. Your privacy is important to us. This Privacy Policy explains how we collect, use, store, and protect your information when you use our Android application."),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "Information We Collect"),
                
                PrivacyPolicyItem(PrivacyItemType.SUBHEADING, "Account Information"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "When you create an account, we may collect:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Name"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Email address"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Login credentials"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Profile information provided by you"),
                
                PrivacyPolicyItem(PrivacyItemType.SUBHEADING, "SMS Data"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "With your explicit permission, FinKlar may read financial SMS messages on your device to:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Detect transactions"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Extract transaction details"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Categorize income and expenses"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Build financial insights"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "FinKlar only processes SMS messages relevant to financial transactions."),
                
                PrivacyPolicyItem(PrivacyItemType.SUBHEADING, "Financial Information"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "We may collect:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Transaction amounts"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Transaction dates"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Merchant names"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Expense categories"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "User-entered notes"),
                
                PrivacyPolicyItem(PrivacyItemType.SUBHEADING, "Documents and Images"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "If you choose to scan receipts, bills, or transaction screenshots, we may process:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Uploaded images"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Extracted text and transaction details"),
                
                PrivacyPolicyItem(PrivacyItemType.SUBHEADING, "AI Chat Data"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "Messages sent through the AI assistant may be processed to provide financial insights, budgeting assistance, and user support."),
                
                PrivacyPolicyItem(PrivacyItemType.SUBHEADING, "Device Information"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "We may collect:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Device model"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Operating system version"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "App version"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Diagnostic and crash information"),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "How We Use Information"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "We use your information to:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Provide account access"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Track income and expenses"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Generate financial reports and insights"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Improve AI-powered features"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Detect fraud and abuse"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Improve app performance and security"),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "Data Storage and Security"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "User data is encrypted before transmission."),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Data is stored using encrypted cloud infrastructure."),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Access to user data is restricted to authorized systems and personnel."),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Industry-standard security measures are used to protect information."),
                PrivacyPolicyItem(PrivacyItemType.BODY, "However, no method of transmission or storage is completely secure, and absolute security cannot be guaranteed."),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "Data Sharing"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "We do not sell your personal information."),
                PrivacyPolicyItem(PrivacyItemType.BODY, "We may share information with:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Cloud hosting providers"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Authentication providers"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "AI service providers"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Legal authorities when required by law"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "All third-party providers are expected to maintain appropriate security standards."),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "AI Services"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "FinKlar may use third-party AI services to provide:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Financial analysis"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Budget suggestions"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Transaction categorization"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "User assistance"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "Users should not rely solely on AI-generated responses for financial decisions."),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "Data Retention"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "We retain data as long as necessary to provide services or comply with legal obligations."),
                PrivacyPolicyItem(PrivacyItemType.BODY, "Users may request deletion of their account and associated data."),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "User Rights"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "You may:"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Access your information"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Correct inaccurate information"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Request deletion of your account"),
                PrivacyPolicyItem(PrivacyItemType.BULLET, "Withdraw permissions such as SMS access"),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "Children's Privacy"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "FinKlar is not intended for users under 13 years of age."),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "Changes to This Policy"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "We may update this Privacy Policy periodically. Continued use of FinKlar after updates constitutes acceptance of the revised policy."),
                
                PrivacyPolicyItem(PrivacyItemType.HEADING, "Contact"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "For privacy-related questions, contact:"),
                PrivacyPolicyItem(PrivacyItemType.BODY, "Email: support@finklar.com")
            )
        }
    }
}

@Composable
fun PrivacyPolicyContent(
    language: LanguageType,
    useThemeColors: Boolean,
    modifier: Modifier = Modifier
) {
    val items = remember(language) { PrivacyPolicyData.getItems(language) }
    
    val titleColor = if (useThemeColors) MaterialTheme.colorScheme.onSurface else Color.Black
    val subtitleColor = if (useThemeColors) MaterialTheme.colorScheme.onSurfaceVariant else Color.DarkGray
    val headingColor = if (useThemeColors) MaterialTheme.colorScheme.primary else Color.Black
    val subheadingColor = if (useThemeColors) MaterialTheme.colorScheme.secondary else Color.DarkGray
    val bodyColor = if (useThemeColors) MaterialTheme.colorScheme.onSurface else Color.Black
    val bulletColor = if (useThemeColors) MaterialTheme.colorScheme.onSurface else Color.Black

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            when (item.type) {
                PrivacyItemType.TITLE -> {
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = titleColor
                    )
                }
                PrivacyItemType.SUBTITLE -> {
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = subtitleColor
                    )
                }
                PrivacyItemType.HEADING -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = headingColor
                    )
                }
                PrivacyItemType.SUBHEADING -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = subheadingColor
                    )
                }
                PrivacyItemType.BODY -> {
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = bodyColor
                    )
                }
                PrivacyItemType.BULLET -> {
                    Row(
                        modifier = Modifier.padding(start = 12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = bulletColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = bodyColor
                        )
                    }
                }
            }
        }
    }
}
