package com.example.pennypulse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
    private lateinit var signupTitleText: TextView
    private lateinit var signupSubTitleText: TextView
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var signupToLoginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup_screen)

        // Handle window insets for edge-to-edge support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI components
        nameEditText = findViewById(R.id.signupNameEditText)
        emailEditText = findViewById(R.id.signupEmailEditText)
        passwordEditText = findViewById(R.id.signupPasswordEditText)
        signupButton = findViewById(R.id.signupButton)
        signupToLoginButton = findViewById(R.id.signupToLoginButton)

        // Signup button click listener
        signupButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Check if all fields are filled
            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                signupUser(name, email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Go to Login screen click listener
        signupToLoginButton.setOnClickListener {
            val goToLoginIntent = Intent(this, LoginScreen::class.java)
            startActivity(goToLoginIntent)
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

                    // Handle successful signup
                    runOnUiThread {
                        Toast.makeText(this@SignupScreen, signupResponse?.toString() ?: "Signup successful!", Toast.LENGTH_SHORT).show()
                        // Navigate to MainActivity
                        val goToMainIntent = Intent(this@SignupScreen, LoginScreen::class.java)
                        startActivity(goToMainIntent)
                        finish() // Close SignupScreen after successful signup
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
}
