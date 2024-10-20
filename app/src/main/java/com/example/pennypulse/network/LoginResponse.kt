package com.example.pennypulse.network

data class LoginResponse(
    val message: String,
    val token: String, // Assuming the server returns a JWT token

)
