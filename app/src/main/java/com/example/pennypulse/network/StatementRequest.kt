package com.example.pennypulse.network

data class StatementRequest(
    val transactionType: String,
    val dateRange: String,
    val email:String // Add email to your StatementRequest
)
