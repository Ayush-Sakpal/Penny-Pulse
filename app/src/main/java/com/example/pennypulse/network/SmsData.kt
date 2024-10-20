package com.example.pennypulse.network

data class SmsData(
    val userId: Int,                // User ID field
    val amount: String,
    val transactionType: String,
    val transactionId: String,
    val transactionDate: String      // Transaction date field
)
