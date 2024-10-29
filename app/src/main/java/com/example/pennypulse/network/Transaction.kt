package com.example.pennypulse.network

data class Transaction(
    val transactionid: String, // Unique identifier for the transaction
    val transactiontype: String, // Type of transaction (e.g., "debited", "credited")
    val amount: Double, // Amount of money involved in the transaction
    val date: String // Date of the transaction (e.g., "27/10/2024")
)