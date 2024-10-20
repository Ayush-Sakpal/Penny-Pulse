package com.example.pennypulse.network

data class SignupRequest(
    var name:String,
    val email: String,
    val password: String
)
