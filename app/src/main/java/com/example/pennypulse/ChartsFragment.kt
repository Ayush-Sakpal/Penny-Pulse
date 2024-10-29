package com.example.pennypulse

import com.github.mikephil.charting.components.LimitLine
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.pennypulse.network.ApiService
import com.example.pennypulse.network.ExpenseDataPoint
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

private const val ARG_TOKEN = "param1"

class ChartsFragment : Fragment() {
    private var token: String? = null
    private lateinit var barChart: BarChart
    private lateinit var tipsContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_charts, container, false)
        arguments?.let {
            token = it.getString(ARG_TOKEN)
        }

        // Initialize the bar chart and tips container
        barChart = rootView.findViewById(R.id.barChart)
        tipsContainer = rootView.findViewById(R.id.tipsContainer)

        // Load data into the chart
        fetchChartData()

        return rootView
    }

    private fun fetchChartData() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.137.1:3000/api/") // Replace with your base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        token?.let {
            apiService.getWeeklyExpenses("Bearer $it").enqueue(object : Callback<List<ExpenseDataPoint>> {
                override fun onResponse(
                    call: Call<List<ExpenseDataPoint>>,
                    response: Response<List<ExpenseDataPoint>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { expenses ->
                            loadBarChart(expenses)
                        } ?: run {
                            showNoDataMessage() // Handle empty data
                        }
                    } else {
                        showNoDataMessage() // Handle API error
                    }
                }

                override fun onFailure(call: Call<List<ExpenseDataPoint>>, t: Throwable) {
                    t.printStackTrace()
                    showNoDataMessage() // Handle network error
                }
            })
        }
    }

    private fun loadBarChart(expenses: List<ExpenseDataPoint>) {
        val currentWeekData = MutableList(7) { 0f } // Initialize list for 7 days
        var totalExpenses = 0f
        var daysCounted = 0

        // Populate the list with actual data from the expenses
        expenses.forEach { expense ->
            val dayOfWeek = getDayOfWeekFromDate(expense.day) // Implement this method based on your data
            currentWeekData[dayOfWeek - 1] += expense.amount.toFloat() // Adjust index for 0-based list
            totalExpenses += expense.amount.toFloat()
            daysCounted++
        }

        // Calculate the average expense
        val averageExpense = if (daysCounted > 0) totalExpenses / daysCounted else 0f
        val tvTotalExpense = requireView().findViewById<TextView>(R.id.tvTotalExpense)
        tvTotalExpense.text = "Average Expense This Week: ₹ ${averageExpense}" // Set total expense dynamically, formatting as integer
        // Create entries for the chart
        val entries = currentWeekData.mapIndexed { index, value -> BarEntry((index + 1).toFloat(), value) }

        // Create a dataset and style it
        val dataSet = BarDataSet(entries, "Weekly Expenses").apply {
            color = resources.getColor(R.color.greenButton) // Set bar color
            valueTextColor = Color.WHITE
            valueTextSize = 16f
        }

        // Wrap dataset into BarData
        val barData = BarData(dataSet)
        barChart.data = barData

        // Customize the X-axis labels to show days of the week
        val xAxis = barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f // minimum axis-step (interval) is 1
            labelCount = 7
            textColor = Color.WHITE // Set X-axis text color to white
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return when (value.toInt()) {
                        1 -> "Mon"
                        2 -> "Tue"
                        3 -> "Wed"
                        4 -> "Thu"
                        5 -> "Fri"
                        6 -> "Sat"
                        7 -> "Sun"
                        else -> ""
                    }
                }
            }
        }

        // Add a horizontal line to represent the average expense
        val limitLine = LimitLine(averageExpense, "Average: ${averageExpense.toInt()}").apply {
            lineWidth = 2f
            enableDashedLine(10f, 10f, 0f)
            setLineColor(Color.WHITE) // Change Color.RED to your desired color
            textColor = Color.WHITE
        }

        // Add the limit line to the Y-axis
        barChart.axisLeft.addLimitLine(limitLine)

        // Customize the Y-axis text color to white
        barChart.axisLeft.textColor = Color.WHITE
        barChart.axisRight.textColor = Color.WHITE

        // Customize chart appearance
        barChart.description.isEnabled = false // Disable the description text
        barChart.setFitBars(true)
        barChart.animateY(1000)

        // Refresh the chart
        barChart.invalidate()

        // Display tips for better expense management
        displayTips(averageExpense)
    }

    private fun getDayOfWeekFromDate(dayString: String): Int {
        return when (dayString) {
            "Mon" -> 1
            "Tue" -> 2
            "Wed" -> 3
            "Thu" -> 4
            "Fri" -> 5
            "Sat" -> 6
            "Sun" -> 7
            else -> 1 // Default to Monday
        }
    }

    private fun showNoDataMessage() {
        barChart.setNoDataTextColor(Color.WHITE)
        barChart.setNoDataText("No expenses recorded.")
        barChart.invalidate() // Refresh the chart
    }

    private fun displayTips(averageExpense: Float) {
        // Initialize an empty list to hold the tips based on average expense
        val tips = mutableListOf<String>()

        // Determine tips based on averageExpense
        when {
            averageExpense < 100 -> {
                tips.add("Tip 1: You're doing great! Keep tracking your expenses to maintain this habit.")
            }
            averageExpense in 100.0..300.0 -> {
                tips.add("Tip 1: Good job! Review your expenses to see where you can save more.")

            }
            averageExpense > 300 -> {
                tips.add("Tip 2: Think about ways to cut down on non-essential expenses.")
            }
        }

        tipsContainer.removeAllViews() // Clear any existing tips

        for (tip in tips) {
            // Inflate the tip item layout
            val tipView = LayoutInflater.from(context).inflate(R.layout.tip_item, tipsContainer, false)
            val suggestionsTextView = tipView.findViewById<TextView>(R.id.suggestionsTextView)
            suggestionsTextView.text = tip
            tipsContainer.addView(tipView)
        }
    }

    companion object {
        fun newInstance(token: String): ChartsFragment {
            return ChartsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TOKEN, token)
                }
            }
        }
    }
}
