package com.example.pennypulse

import android.util.Log
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.pennypulse.network.ApiService
import com.example.pennypulse.network.Transaction
import com.google.android.material.button.MaterialButton

// Constants for argument keys
private const val ARG_TOTAL_EXPENSE = "total_expense"
private const val ARG_TOTAL_CREDIT = "total_credit"
private const val ARG_TOKEN = "token"

class QuickHitsFragment : Fragment() {

    private var totalExpense: Double = 0.0
    private var totalCredit: Double = 0.0
    private var token: String? = null
    private lateinit var transactionContainer: LinearLayout // Layout to hold transaction views

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            totalExpense = it.getDouble(ARG_TOTAL_EXPENSE, 0.0) // Retrieve total expense, default to 0.0
            totalCredit = it.getDouble(ARG_TOTAL_CREDIT, 0.0) // Retrieve total expense, default to 0.0
            token = it.getString(ARG_TOKEN) // Retrieve token
            Log.d("QuickHitsFragment", "Total Expense: $totalExpense, Token: $token")
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_quick_hits, container, false)

        // Set the total expense in the TextView
        val totalExpenseTextView = view.findViewById<TextView>(R.id.tExpense)
        totalExpenseTextView.visibility = View.VISIBLE
        totalExpenseTextView.text = "₹ ${totalExpense}" // Set total expense dynamically
        val totalCreditTextView = view.findViewById<TextView>(R.id.tCredit)
        totalCreditTextView.visibility = View.VISIBLE
        totalCreditTextView.text = "₹ ${totalCredit}" // Set total expense dynamically

        // Initialize the transaction container
        transactionContainer = view.findViewById(R.id.transactionContainer)

        // Fetch transactions
        fetchTransactions(token)



        return view
    }

    private fun fetchTransactions(token: String?) {
        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.137.1:3000/api/") // Replace with your API base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.getTransactions("Bearer $token") // Assuming your API requires Bearer token

        call.enqueue(object : Callback<List<Transaction>> {
            override fun onResponse(call: Call<List<Transaction>>, response: Response<List<Transaction>>) {
                if (response.isSuccessful) {
                    response.body()?.let { transactions ->
                        displayTransactions(transactions)
                    }
                } else {
                    Log.e("QuickHitsFragment", "Failed to fetch transactions: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<Transaction>>, t: Throwable) {
                Log.e("QuickHitsFragment", "Error: ${t.message}")
            }
        })
    }



    private fun displayTransactions(transactions: List<Transaction>) {
        for (transaction in transactions) {
            val transactionView = LayoutInflater.from(context).inflate(R.layout.transaction_container, transactionContainer, false)
            val transIdTextView = transactionView.findViewById<TextView>(R.id.transactionId) // Adjust this ID according to your layout
            val transDateTextView = transactionView.findViewById<TextView>(R.id.transactionDate) // Adjust this ID
            val transAmountTextView = transactionView.findViewById<TextView>(R.id.transactionAmount) // Adjust this ID
            val transTypeTextView = transactionView.findViewById<TextView>(R.id.transactionTitleView) // Use the correct ID

            // Set transaction data
            transIdTextView.text = transaction.transactionid // Adjust according to your Transaction model
            transDateTextView.text = transaction.date // Adjust according to your Transaction model
            transAmountTextView.text = "₹ ${transaction.amount}" // Adjust according to your Transaction model
            transTypeTextView.text = transaction.transactiontype

            // Change the color of the amount based on transaction type
            if (transaction.transactiontype.equals("credited", ignoreCase = true)) {
                transAmountTextView.setTextColor(resources.getColor(R.color.green)) // Change to green for credited
            } else {
                transAmountTextView.setTextColor(resources.getColor(R.color.red)) // Change to red for debited
            }

            // Add the view to the container
            transactionContainer.addView(transactionView)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(totalExpense: Double, totalCredit:Double, token: String) =
            QuickHitsFragment().apply {
                arguments = Bundle().apply {
                    putDouble(ARG_TOTAL_EXPENSE, totalExpense) // Pass the total expense
                    putDouble(ARG_TOTAL_CREDIT, totalCredit) // Pass the total expense
                    putString(ARG_TOKEN, token) // Pass the token
                }
            }
    }
}
