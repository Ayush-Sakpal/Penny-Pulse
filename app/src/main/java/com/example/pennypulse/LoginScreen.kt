package com.example.pennypulse
import java.math.BigDecimal
import java.math.RoundingMode
import android.util.Log
import com.auth0.android.jwt.JWT
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pennypulse.network.ApiService
import com.example.pennypulse.network.LoginRequest
import com.example.pennypulse.network.LoginResponse
import com.example.pennypulse.network.SmsData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class LoginScreen : AppCompatActivity() {
    private lateinit var loginEmailEditText: EditText
    private lateinit var loginPasswordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var goToSignUpButton: Button
    private val SMS_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loginEmailEditText = findViewById(R.id.loginEmailEditText)
        loginPasswordEditText = findViewById(R.id.loginPasswordEditText)
        loginButton = findViewById(R.id.loginButton)
        goToSignUpButton = findViewById(R.id.goToSignupButton)

        loginButton.setOnClickListener {
            val email = loginEmailEditText.text.toString()
            val password = loginPasswordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }

        goToSignUpButton.setOnClickListener {
            startActivity(Intent(this, SignupScreen::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.137.1:3000/api/") // Replace with your server's IP address
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val loginRequest = LoginRequest(email = email, password = password)
                val response = apiService.loginUser(loginRequest)

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val token = loginResponse?.token
                    val userId = token?.let {
                        JWT(it).getClaim("userId").asString()?.toIntOrNull()
                    }
                    saveToken(token)
                    saveUserId(userId) // Save user ID


                    if (ContextCompat.checkSelfPermission(this@LoginScreen, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this@LoginScreen, arrayOf(Manifest.permission.READ_SMS), SMS_PERMISSION_CODE)
                    } else {Log.d("deep",token.toString())
                        readSmsAndNavigate(apiService)
                        val intent = Intent(this@LoginScreen, MainActivity::class.java).apply {
                            putExtra("EXTRA_TOKEN", token) // Add token as an extra
                        }
// Start MainActivity and finish LoginScreen
                        startActivity(intent)
                        finish()
                    }


                } else {
                    runOnUiThread {
                        Toast.makeText(this@LoginScreen, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@LoginScreen, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveToken(token: String?) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("jwt_token", token)
            apply()
        }
    }

    private fun saveUserId(userId: Int?) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("user_id", userId ?: 0) // Save the user ID, default to 0 if null
            apply()
        }
    }

    private fun readSmsAndNavigate(apiService: ApiService) {
        val cursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)
        cursor?.let {
            while (it.moveToNext()) {
                val smsBody = it.getString(it.getColumnIndex("body"))
                val smsDate = it.getLong(it.getColumnIndex("date")) // Get the date in milliseconds

                // Convert milliseconds to a readable date format
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                val transactionDate = dateFormat.format(Date(smsDate))

                // Extract relevant SMS data only if it matches your criteria
                val smsData = extractSmsData(smsBody, transactionDate) // Pass the date to the method
                smsData?.let { data ->
                    sendSmsToServer(apiService, data)
                }
            }
            it.close()
        }

    }
    private fun formatAmount(amount: String): String {
        return try {
            // Convert to BigDecimal and format to 2 decimal places
            val bigDecimalAmount = BigDecimal(amount.replace(",", ".")).setScale(2, RoundingMode.HALF_UP)
            bigDecimalAmount.toString()
        } catch (e: NumberFormatException) {
            "0.00" // Fallback if parsing fails
        }
    }


    private fun extractSmsData(smsBody: String, transactionDate: String): SmsData? {
        // Check if the SMS is related to UPI

        // Expanded regex to capture amounts, debited/credited, and transaction ID for various formats
        val regex = """(?i)(?:(\d+[,.]\d+)[^\d]*(?:debited|credited|spent|withdrawn|added|received)|(?:debited|credited|spent|withdrawn|added|received)[^\d]*(\d+[,.]\d+))[^\d]*(?:on date)?[^\d]*(?:\d{1,2}[A-Za-z]{3}\d{2,4})?[^\d]*(?:txn(?: id)?|ref(?:no|erence)?|transaction)?[:\s-]*([A-Za-z0-9]+)""".toRegex()

        val matchResult = regex.find(smsBody)

        // Retrieve userId from shared preferences
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", 0)

        return matchResult?.let {
            // Extract the amount
            val amount = it.groups[1]?.value ?: it.groups[2]?.value ?: "0.00"
            val formattedAmount = formatAmount(amount) // Format the amount to 2 decimal places

            // Determine the transaction type
            val transactionType = if (smsBody.contains("debited", ignoreCase = true)) "debited" else "credited"

            // Extract the transaction ID or reference
            val transactionId = it.groups[3]?.value ?: "unknown"

            // Return the extracted data
            SmsData(userId, formattedAmount, transactionType, transactionId, transactionDate)
        }
    }





    private fun sendSmsToServer(apiService: ApiService, smsData: SmsData) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.sendSms(smsData) // Sending SmsData with userId
                if (response.isSuccessful) {
                    println("SMS data sent successfully: $smsData")
                } else {
                    println("Failed to send SMS data: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.137.1:3000/api/") // Replace with your server's IP address
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readSmsAndNavigate(apiService)
            } else {
                Toast.makeText(this, "Permission denied to read SMS", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
