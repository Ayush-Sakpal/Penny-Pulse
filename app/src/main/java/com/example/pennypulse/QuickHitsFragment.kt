package com.example.pennypulse

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

// Constants for argument keys
private const val ARG_TOTAL_EXPENSE = "total_expense"

class QuickHitsFragment : Fragment() {

    private var totalExpense: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            totalExpense = it.getDouble(ARG_TOTAL_EXPENSE, 0.0) // Retrieve total expense, default to 0.0
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_quick_hits, container, false)

        // Set the user name and total expense in the TextViews
        val totalExpenseTextView = view.findViewById<TextView>(R.id.tExpense)
        totalExpenseTextView.text = "₹ ${totalExpense}" // Set total expense dynamically, formatting as integer

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance( totalExpense: Double) =
            QuickHitsFragment().apply {
                arguments = Bundle().apply {

                    putDouble(ARG_TOTAL_EXPENSE, totalExpense) // Pass the total expense
                }
            }
    }
}
