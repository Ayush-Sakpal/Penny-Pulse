package com.example.pennypulse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignupScreen : AppCompatActivity() {

    lateinit var signupTitleText: TextView
    lateinit var signupSubTitleText: TextView
    lateinit var emailEditText: TextView
    lateinit var passwordEditText: EditText
    lateinit var signupButton: Button
    lateinit var signupToLoginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        signupButton = findViewById(R.id.signupButton)
        signupToLoginButton = findViewById(R.id.signupToLoginButton)

        signupButton.setOnClickListener {
            val goToMainIntent = Intent(this, MainActivity::class.java)
            startActivity(goToMainIntent)
        }

        signupToLoginButton.setOnClickListener {
            val goToLoginIntent = Intent(this, LoginScreen::class.java)
            startActivity(goToLoginIntent)
        }
    }
}