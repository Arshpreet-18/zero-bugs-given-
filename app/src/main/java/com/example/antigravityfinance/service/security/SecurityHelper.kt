package com.example.antigravityfinance.service.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.antigravityfinance.data.model.CurrencyType
import com.example.antigravityfinance.data.model.LanguageType
import com.example.antigravityfinance.data.model.ThemeType
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecurityHelper(private val context: Context) {
    private val sharedPrefs = context.getSharedPreferences("finance_app_prefs", Context.MODE_PRIVATE)
    private val keyAlias = "AntigravityFinanceKey"

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
        if (entry != null) {
            return entry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun encrypt(data: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv
            
            val combined = ByteArray(iv.size + encrypted.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
            
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun decrypt(encryptedBase64: String): String {
        if (encryptedBase64.isNullOrEmpty()) return ""
        return try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            val iv = ByteArray(12)
            val encrypted = ByteArray(combined.size - 12)
            System.arraycopy(combined, 0, iv, 0, 12)
            System.arraycopy(combined, 12, encrypted, 0, encrypted.size)
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)
            
            val decryptedBytes = cipher.doFinal(encrypted)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun getDatabasePassphrase(): String {
        var encryptedKey = sharedPrefs.getString("db_key_enc", null)
        if (encryptedKey == null) {
            val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray()
            val sb = StringBuilder()
            val random = SecureRandom()
            for (i in 0 until 32) {
                sb.append(chars[random.nextInt(chars.size)])
            }
            val rawKey = sb.toString()
            encryptedKey = encrypt(rawKey)
            sharedPrefs.edit().putString("db_key_enc", encryptedKey).apply()
            return rawKey
        }
        return decrypt(encryptedKey)
    }

    // PIN Lock Management
    fun savePin(pin: String) {
        val encryptedPin = encrypt(pin)
        sharedPrefs.edit().putString("app_pin", encryptedPin).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val savedEncryptedPin = sharedPrefs.getString("app_pin", null) ?: return false
        val savedPin = decrypt(savedEncryptedPin)
        return savedPin == pin
    }

    fun isPinSet(): Boolean {
        return sharedPrefs.getString("app_pin", null) != null
    }

    fun clearPin() {
        sharedPrefs.edit().remove("app_pin").apply()
    }

    fun setBiometricsEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("biometrics_enabled", enabled).apply()
    }

    fun isBiometricsEnabled(): Boolean {
        return sharedPrefs.getBoolean("biometrics_enabled", false)
    }

    // Theme Settings
    fun saveTheme(theme: ThemeType) {
        sharedPrefs.edit().putString("theme_mode", theme.name).apply()
    }

    fun getTheme(): ThemeType {
        val name = sharedPrefs.getString("theme_mode", ThemeType.DYNAMIC.name)
        return try { ThemeType.valueOf(name!!) } catch (e: Exception) { ThemeType.DYNAMIC }
    }

    fun saveDarkMode(isDark: Boolean?) {
        if (isDark == null) {
            sharedPrefs.edit().remove("dark_mode").apply()
        } else {
            sharedPrefs.edit().putBoolean("dark_mode", isDark).apply()
        }
    }

    fun getDarkMode(): Boolean? {
        if (!sharedPrefs.contains("dark_mode")) return null
        return sharedPrefs.getBoolean("dark_mode", false)
    }

    fun saveAccentIndex(index: Int) {
        sharedPrefs.edit().putInt("accent_color_index", index).apply()
    }

    fun getAccentIndex(): Int {
        return sharedPrefs.getInt("accent_color_index", -1)
    }

    // Currency Settings
    fun saveCurrency(currency: CurrencyType) {
        sharedPrefs.edit().putString("currency_type", currency.name).apply()
    }

    fun getCurrency(): CurrencyType {
        return CurrencyType.INR
    }

    // Language Settings
    fun saveLanguage(language: LanguageType) {
        sharedPrefs.edit().putString("language_type", language.name).apply()
    }

    fun getLanguage(): LanguageType {
        val name = sharedPrefs.getString("language_type", LanguageType.ENGLISH.name)
        return try { LanguageType.valueOf(name!!) } catch (e: Exception) { LanguageType.ENGLISH }
    }

    // Investments module enable/disable
    fun saveInvestmentsEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("investments_enabled", enabled).apply()
    }

    fun isInvestmentsEnabled(): Boolean {
        return sharedPrefs.getBoolean("investments_enabled", true)
    }

    // API Keys (For developer mode)
    fun saveGeminiApiKey(key: String) {
        sharedPrefs.edit().putString("gemini_api_key", encrypt(key)).apply()
    }

    fun getGeminiApiKey(): String {
        val enc = sharedPrefs.getString("gemini_api_key", "") ?: ""
        val dec = decrypt(enc)
        return if (dec.isNotBlank()) dec else ""
    }

    fun saveElevenLabsKey(key: String) {
        sharedPrefs.edit().putString("eleven_labs_key", encrypt(key)).apply()
    }

    fun getElevenLabsKey(): String {
        val enc = sharedPrefs.getString("eleven_labs_key", "") ?: ""
        return decrypt(enc)
    }

    fun saveSarvamKey(key: String) {
        sharedPrefs.edit().putString("sarvam_key", encrypt(key)).apply()
    }

    fun getSarvamKey(): String {
        val enc = sharedPrefs.getString("sarvam_key", "") ?: ""
        if (enc.isBlank()) {
            return "sk_hffq66ua_3qucYR1Zpdn8vjsFj8VT4kHo"
        }
        val decrypted = decrypt(enc)
        if (decrypted.isBlank()) {
            return "sk_hffq66ua_3qucYR1Zpdn8vjsFj8VT4kHo"
        }
        return decrypted
    }

    fun saveSyncedBalance(balance: Double) {
        sharedPrefs.edit().putString("synced_sms_balance", encrypt(balance.toString())).apply()
    }

    fun getSyncedBalance(): Double? {
        val enc = sharedPrefs.getString("synced_sms_balance", null) ?: return null
        val dec = decrypt(enc)
        return dec.toDoubleOrNull()
    }

    fun getLastSmsScanTimestamp(): Long {
        return sharedPrefs.getLong("last_sms_scan_timestamp", 0L)
    }

    fun saveLastSmsScanTimestamp(timestamp: Long) {
        sharedPrefs.edit().putLong("last_sms_scan_timestamp", timestamp).apply()
    }

    fun isInitialIncomeSet(): Boolean {
        return sharedPrefs.getBoolean("initial_income_set", false)
    }

    fun setInitialIncomeSet(set: Boolean) {
        sharedPrefs.edit().putBoolean("initial_income_set", set).apply()
    }

    fun saveRentAmount(amount: Double) {
        sharedPrefs.edit().putFloat("rent_amount", amount.toFloat()).apply()
    }
    fun getRentAmount(): Double {
        return sharedPrefs.getFloat("rent_amount", 0.0f).toDouble()
    }

    fun saveEmiAmount(amount: Double) {
        sharedPrefs.edit().putFloat("emi_amount", amount.toFloat()).apply()
    }
    fun getEmiAmount(): Double {
        return sharedPrefs.getFloat("emi_amount", 0.0f).toDouble()
    }

    fun saveEmiDay(day: Int) {
        sharedPrefs.edit().putInt("emi_day", day).apply()
    }
    fun getEmiDay(): Int {
        return sharedPrefs.getInt("emi_day", 1)
    }

    fun saveSipAmount(amount: Double) {
        sharedPrefs.edit().putFloat("sip_amount", amount.toFloat()).apply()
    }
    fun getSipAmount(): Double {
        return sharedPrefs.getFloat("sip_amount", 0.0f).toDouble()
    }

    fun saveSipDay(day: Int) {
        sharedPrefs.edit().putInt("sip_day", day).apply()
    }
    fun getSipDay(): Int {
        return sharedPrefs.getInt("sip_day", 1)
    }

    fun saveOtherMandatory(amount: Double) {
        sharedPrefs.edit().putFloat("other_mandatory", amount.toFloat()).apply()
    }
    fun getOtherMandatory(): Double {
        return sharedPrefs.getFloat("other_mandatory", 0.0f).toDouble()
    }

    fun saveUserIncomeOverride(income: Double) {
        sharedPrefs.edit().putFloat("user_income_override", income.toFloat()).apply()
    }
    fun getUserIncomeOverride(): Double {
        return sharedPrefs.getFloat("user_income_override", 0.0f).toDouble()
    }

    fun saveLastWalletClearTimestamp(timestamp: Long) {
        sharedPrefs.edit().putLong("last_wallet_clear_timestamp", timestamp).apply()
    }
    fun getLastWalletClearTimestamp(): Long {
        return sharedPrefs.getLong("last_wallet_clear_timestamp", 0L)
    }

    // Trusted SMS Senders
    fun saveTrustedSender(senderId: String) {
        val current = getTrustedSenders().toMutableSet()
        current.add(senderId.uppercase())
        sharedPrefs.edit().putStringSet("trusted_sms_senders", current).apply()
    }

    fun removeTrustedSender(senderId: String) {
        val current = getTrustedSenders().toMutableSet()
        current.remove(senderId.uppercase())
        sharedPrefs.edit().putStringSet("trusted_sms_senders", current).apply()
    }

    fun isTrustedSender(senderId: String): Boolean {
        return getTrustedSenders().contains(senderId.uppercase())
    }

    fun getTrustedSenders(): Set<String> {
        return sharedPrefs.getStringSet("trusted_sms_senders", emptySet()) ?: emptySet()
    }

    fun getReviewNeededQueue(): List<com.example.antigravityfinance.service.sms.detection.SmsDetectionResult> {
        val jsonStr = sharedPrefs.getString("review_needed_sms_queue", null) ?: return emptyList()
        return try {
            Json.decodeFromString<List<com.example.antigravityfinance.service.sms.detection.SmsDetectionResult>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveReviewNeededQueue(list: List<com.example.antigravityfinance.service.sms.detection.SmsDetectionResult>) {
        try {
            val jsonStr = Json.encodeToString(list)
            sharedPrefs.edit().putString("review_needed_sms_queue", jsonStr).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getIgnoredSmsList(): List<com.example.antigravityfinance.service.sms.detection.SmsDetectionResult> {
        val jsonStr = sharedPrefs.getString("ignored_sms_list", null) ?: return emptyList()
        return try {
            Json.decodeFromString<List<com.example.antigravityfinance.service.sms.detection.SmsDetectionResult>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveIgnoredSmsList(list: List<com.example.antigravityfinance.service.sms.detection.SmsDetectionResult>) {
        try {
            val jsonStr = Json.encodeToString(list)
            sharedPrefs.edit().putString("ignored_sms_list", jsonStr).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
