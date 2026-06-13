# Antigravity Finance

Antigravity Finance is a professional, offline-first personal finance and money management mobile application designed for secure, localized banking analysis. Built with Jetpack Compose, Material 3, and Kotlin, the application integrates SQLite encryption, automatic transaction detection via SMS parsing, Gemini-powered OCR receipt extraction, and an intelligent financial chatbot assistant.

---

## 🚀 Key Features

*   **🔒 Encrypted Local Storage (SQLCipher)**: All transactions, budgets, savings goals, and investments are persisted locally inside an encrypted SQLite database using **Room + SQLCipher** (256-bit passphrase key derived via secure Keystore providers).
*   **📱 Secure PIN Lock Gate**: An interactive, security-wrapped custom PIN pad shields the app workspace on startup.
*   **💬 Conversational AI Assistant**: An intelligent financial chatbot answers queries (*"how much did I spend on food this month?"*, *"predict budget overruns"*) using local rules and context mapping, or queries Gemini 1.5 Flash when developer keys are configured.
*   **📩 SMS Transaction Sync**: Auto-syncs transactions from device SMS notifications. The offline regex engine extracts amount, merchant, and income vs expense flags and stores bank balance entries.
*   **🧾 Smart OCR Receipt Scanner**: Scan receipt screenshots using the built-in Gemini Vision analyzer. It automatically detects merchant, amounts, and categories.
*   **⚠️ Smart Duplicate Warning Popup**: Runs an immediate similarity check (amount, merchant initials, and time delta) before inserting scanned receipts to prevent duplicate bookkeeping.
*   **🔥 Daily Savings Goal Streaks**: Track daily contributions on goal progress cards with a flame/fire icon indicator. Streaks are automatically calculated based on daily contribution intervals.
*   **📈 Investments commitments & SIP Alerts**: Manage mutual funds, stocks, and SIP portfolios. An active SIP low-balance monitor compares the synced bank balance to the monthly SIP total and triggers alerts to prevent failed transaction charges.
*   **🎨 Dynamic Custom Themes**: Instantly toggle between **Dynamic Theme** (vibrant gradients, rounded borders) and **Professional Theme** (clean slates, executive slate Navy) with custom accent colors and system Light/Dark modes.
*   **🌍 Restricted Localization**: Supported settings languages are strictly restricted to English and Hindi.

---

## 🛠️ Technology Stack

*   **UI/UX**: Jetpack Compose, Material Design 3 (M3)
*   **Database**: Android Room Persistence Library + SQLCipher (16 KB page-size aligned)
*   **Processor & Compilation**: KSP2 (Kotlin Symbol Processing), Kotlin 2.3+
*   **AI Models**: Gemini 1.5 Flash (via Google AI Client SDK)
*   **Architecture**: MVVM (Model-View-ViewModel), Repository Pattern, Offline-First, Flow Streams

---

## ⚙️ Setup and Installation

### Prerequisites
*   Android Studio Ladybug (or newer)
*   Android SDK (API 34+)
*   Java Development Kit (JDK) 17+

### Step-by-Step Guide
1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/your-username/AntigravityFinance.git
    cd AntigravityFinance
    ```
2.  **Open in Android Studio**:
    Select **File -> Open** and select the root directory of the project.
3.  **Configure API Keys (Optional)**:
    Open the app, navigate to **Settings**, and insert your Google Gemini API Key under Developer Options to enable advanced AI chatbot queries and real OCR scanning.
4.  **Build Project**:
    Press **Sync Project with Gradle Files** inside Android Studio, then build the APK using `Build -> Build Bundle(s) / APK(s) -> Build APK(s)`.

---

## 🧪 Testing

The repository contains unit tests validating the regular expression SMS parser, category sorting rules, and viewModel streams.

To run automated checks, use the Gradle commands:

*   **Compile Debug Sources**:
    ```bash
    ./gradlew compileDebugSources
    ```
*   **Run Unit Tests**:
    ```bash
    ./gradlew testDebugUnitTest
    ```

---

## 🔒 Security & Privacy Model

Antigravity Finance is designed with privacy as a priority:
1.  **Offline-First**: None of your financial records are uploaded to cloud servers. All analytics are computed on-device.
2.  **Passphrase Entropy**: Database decryption keys are generated using cryptographically secure random number generators and stored inside encrypted Android Shared Preferences.
3.  **No Unverified Outputs**: Scanned SMS logs serve strictly as a mirror. If an SMS is deleted from the device inbox, the application purges the corresponding transaction on the next sync, keeping records clean and verified.
