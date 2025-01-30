package com.example.pennypulse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pennypulse.network.ApiService
import com.example.pennypulse.network.SignupRequest
import com.example.pennypulse.network.SignupResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SignupScreen : AppCompatActivity() {

    // UI components
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var signupToLoginButton: Button
    private lateinit var otpEditText: EditText // Field for OTP
    private lateinit var verifyOtpButton: Button // Button for OTP verification

    private var generatedOtp: String? = null // Variable to store the generated OTP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup_screen)

        // Initialize UI components
        nameEditText = findViewById(R.id.signupNameEditText)
        emailEditText = findViewById(R.id.signupEmailEditText)
        passwordEditText = findViewById(R.id.signupPasswordEditText)
        confirmPasswordEditText = findViewById(R.id.signupConfirmPasswordEditText)
        signupButton = findViewById(R.id.signupButton)
        signupToLoginButton = findViewById(R.id.signupToLoginButton)
        otpEditText = findViewById(R.id.otpEditText)
        verifyOtpButton = findViewById(R.id.verifyOtpButton)

        // Initially hide the OTP field and verify button
        otpEditText.visibility = View.GONE
        verifyOtpButton.visibility = View.GONE

        // Signup button click listener
        signupButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    signupUser(name, email, password)
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Go to Login screen click listener
        signupToLoginButton.setOnClickListener {
            val goToLoginIntent = Intent(this, LoginScreen::class.java)
            startActivity(goToLoginIntent)
        }

        // Verify OTP button click listener
        verifyOtpButton.setOnClickListener {
            val otp = otpEditText.text.toString().trim()
            if (otp.isNotEmpty() && otp == generatedOtp) {
                // Proceed to register the user after OTP verification
                val name = nameEditText.text.toString().trim()
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()
                registerUser(name, email, password)
            } else {
                Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show()
                val goToSignIntent = Intent(this@SignupScreen, SignupScreen::class.java)
                startActivity(goToSignIntent)
                finish()
            }
        }
    }

    // Function to sign up a new user
    private fun signupUser(name: String, email: String, password: String) {
        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.137.1:3000/api/") // Replace with your server's IP
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Create ApiService instance
        val apiService = retrofit.create(ApiService::class.java)

        // Coroutine to handle network request asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create signup request with name, email, and password
                val signupRequest = SignupRequest(name, email, password)
                val response = apiService.signupUser(signupRequest)

                if (response.isSuccessful) {
                    val signupResponse = response.body()
                    generatedOtp = signupResponse?.otp.toString()// Store the OTP sent by the server

                    // Handle successful signup
                    runOnUiThread {
                        Toast.makeText(this@SignupScreen, signupResponse?.message ?: "OTP sent to your email.", Toast.LENGTH_SHORT).show()
                        // Show OTP input field and button
                        otpEditText.visibility = View.VISIBLE
                        verifyOtpButton.visibility = View.VISIBLE
                        signupButton.visibility=View.GONE
                    }
                } else {
                    // Handle signup failure
                    val errorMessage = response.errorBody()?.string() ?: "Signup failed. Please try again."
                    runOnUiThread {
                        Toast.makeText(this@SignupScreen, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Handle network or other exceptions
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@SignupScreen, "Signup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Function to register user after OTP verification
    private fun registerUser(name: String, email: String, password: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.137.1:3000/api/") // Replace with your server's IP
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        // Coroutine to handle network request asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create registration request with name, email, and password
                val registrationRequest = SignupRequest(name, email, password) // Adjust if needed
                val response = apiService.registerUser(registrationRequest) // Make sure to implement this in ApiService

                if (response.isSuccessful) {
                    val registrationResponse = response.body()
                    runOnUiThread {
                        Toast.makeText(this@SignupScreen, registrationResponse?.message ?: "Registration successful!", Toast.LENGTH_SHORT).show()
                        // Navigate to the login screen or main activity
                        val goToLoginIntent = Intent(this@SignupScreen, LoginScreen::class.java)
                        startActivity(goToLoginIntent)
                        finish()
                    }
                } else {
                    // Handle registration failure
                    val errorMessage = response.errorBody()?.string() ?: "Registration failed. Please try again."
                    runOnUiThread {
                        Toast.makeText(this@SignupScreen, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Handle network or other exceptions
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@SignupScreen, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
