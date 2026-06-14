const functions = require("firebase-functions");
const admin = require("firebase-admin");
const twilio = require("twilio");

admin.initializeApp();

const db = admin.firestore();

// Retrieve Twilio credentials from environment configuration or environment variables
const getTwilioConfig = () => {
  const accountSid = process.env.TWILIO_ACCOUNT_SID || functions.config().twilio?.sid;
  const authToken = process.env.TWILIO_AUTH_TOKEN || functions.config().twilio?.token;
  const phoneNumber = process.env.TWILIO_PHONE_NUMBER || functions.config().twilio?.phone;

  return { accountSid, authToken, phoneNumber };
};

/**
 * HTTP Function: Send OTP via Twilio SMS
 * Request Body: { "phoneNumber": "+1234567890", "name": "John Doe", "email": "john@example.com" }
 */
exports.sendOTP = functions.https.onRequest(async (req, res) => {
  // Handle CORS
  res.set("Access-Control-Allow-Origin", "*");
  if (req.method === "OPTIONS") {
    res.set("Access-Control-Allow-Methods", "POST");
    res.set("Access-Control-Allow-Headers", "Content-Type");
    res.status(204).send("");
    return;
  }

  try {
    const { phoneNumber, name, email } = req.body;
    if (!phoneNumber) {
      res.status(400).json({ error: "Phone number is required" });
      return;
    }

    // Generate a secure 6-digit OTP
    const otp = Math.floor(100000 + Math.random() * 900000).toString();

    // Store OTP in Firestore with server timestamp (expires in 5 minutes)
    await db.collection("otps").doc(phoneNumber).set({
      otp: otp,
      name: name || "",
      email: email || "",
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });

    const { accountSid, authToken, phoneNumber: twilioPhone } = getTwilioConfig();

    if (!accountSid || !authToken || !twilioPhone) {
      console.warn("Twilio credentials are not configured. Logging OTP for debugging:", otp);
      // If Twilio is not configured, we return success with a warning so the user knows it's working in mock mode
      res.status(200).json({ 
        success: true, 
        message: "Twilio not configured on backend. Use code displayed in server logs for testing.", 
        mockOtp: otp 
      });
      return;
    }

    // Send SMS via Twilio
    const client = twilio(accountSid, authToken);
    await client.messages.create({
      body: `Your FinKlar verification code is ${otp}. It is valid for 5 minutes.`,
      from: twilioPhone,
      to: phoneNumber
    });

    res.status(200).json({ success: true, message: "OTP sent successfully" });
  } catch (error) {
    console.error("Error sending OTP:", error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * HTTP Function: Verify OTP
 * Request Body: { "phoneNumber": "+1234567890", "otp": "123456" }
 */
exports.verifyOTP = functions.https.onRequest(async (req, res) => {
  // Handle CORS
  res.set("Access-Control-Allow-Origin", "*");
  if (req.method === "OPTIONS") {
    res.set("Access-Control-Allow-Methods", "POST");
    res.set("Access-Control-Allow-Headers", "Content-Type");
    res.status(204).send("");
    return;
  }

  try {
    const { phoneNumber, otp } = req.body;
    if (!phoneNumber || !otp) {
      res.status(400).json({ error: "Phone number and OTP are required" });
      return;
    }

    const docRef = db.collection("otps").doc(phoneNumber);
    const doc = await docRef.get();

    if (!doc.exists) {
      res.status(400).json({ success: false, error: "No OTP request found for this phone number" });
      return;
    }

    const data = doc.data();
    const storedOtp = data.otp;
    const createdAt = data.createdAt ? data.createdAt.toDate() : null;

    if (storedOtp !== otp) {
      res.status(400).json({ success: false, error: "Invalid OTP code" });
      return;
    }

    // Check expiration (5 minutes = 300,000 milliseconds)
    if (createdAt && (Date.now() - createdAt.getTime() > 300000)) {
      res.status(400).json({ success: false, error: "OTP has expired. Please request a new one." });
      return;
    }

    // OTP is valid. Clean up the document
    await docRef.delete();

    // Store verified user profile in user database (optional metadata store)
    await db.collection("users").doc(phoneNumber).set({
      name: data.name,
      email: data.email,
      verifiedAt: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    res.status(200).json({ 
      success: true, 
      message: "OTP verified successfully",
      profile: {
        name: data.name,
        email: data.email,
        phoneNumber: phoneNumber
      }
    });
  } catch (error) {
    console.error("Error verifying OTP:", error);
    res.status(500).json({ error: error.message });
  }
});
