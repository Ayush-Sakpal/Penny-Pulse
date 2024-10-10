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

class LoginScreen : AppCompatActivity() {
    lateinit var loginTitleText: TextView
    lateinit var loginSubTitleText: TextView
    lateinit var emailEditText: TextView
    lateinit var passwordEditText: EditText
    lateinit var loginButton: Button
    lateinit var goToSignUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loginButton = findViewById(R.id.loginButton)
        goToSignUpButton = findViewById(R.id.goToSignupButton)

        loginButton.setOnClickListener {
            val goToMainIntent = Intent(this, MainActivity::class.java)
            startActivity(goToMainIntent)
        }

        goToSignUpButton.setOnClickListener {
            val goToSignupIntent = Intent(this, SignupScreen::class.java)
            startActivity(goToSignupIntent)
        }
    }
}