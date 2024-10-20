package com.example.pennypulse
import android.util.Log
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.core.util.Pair
import com.example.pennypulse.network.ApiService
import com.google.android.material.datepicker.MaterialDatePicker
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import com.example.pennypulse.network.StatementRequest
import com.google.android.material.button.MaterialButton

private const val ARG_TOKEN = "param1"

class StatementFragment : Fragment() {
    private var token: String? = null
    private lateinit var selectedDate: TextView
    private lateinit var emailEditText: TextView // Use TextView for your EditText here
    private var selectedTransactionType: String? = null // Changed variable name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            token = it.getString(ARG_TOKEN)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statement, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedDate = view.findViewById(R.id.durationEditTextStatementForm)
        emailEditText = view.findViewById(R.id.emailEditTextStatementForm) // Initialize the email field
        val datePickerButton: ImageButton = view.findViewById(R.id.durationPickerButton)

        datePickerButton.setOnClickListener {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTheme(R.style.ThemeMaterialCalendar)
                .setTitleText("Select Date range")
                .setSelection(Pair(null, null))
                .build()

            picker.show(requireActivity().supportFragmentManager, "TAG")

            picker.addOnPositiveButtonClickListener {
                selectedDate.text = convertTimeToDate(it.first) + " - " + convertTimeToDate(it.second)
            }

            picker.addOnNegativeButtonClickListener {
                picker.dismiss()
            }
        }

        val transactionTypeSpinner = view.findViewById<Spinner>(R.id.transactionTypeSpinner)
        val transactionTypes = resources.getStringArray(R.array.transaction_types) // Changed to transaction_types
        val transactionAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, transactionTypes)
        transactionAdapter.setDropDownViewResource(R.layout.dropdown_item)
        transactionTypeSpinner.adapter = transactionAdapter

        transactionTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTransactionType = transactionTypes[position] // Changed variable name
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        view.findViewById<MaterialButton>(R.id.Download).setOnClickListener {
            val dateRange = selectedDate.text.toString()
            val email = emailEditText.text.toString().trim() // Capture email

            if (dateRange.isNotBlank() && !selectedTransactionType.isNullOrEmpty() && email.isNotBlank()) {
                sendFormDataToServer(token ?: "", selectedTransactionType!!, dateRange, email) // Changed variable name
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendFormDataToServer(token: String, transactionType: String, dateRange: String, email: String) { // Changed parameter name
        Log.d("hii",token)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.137.1:3000/api/") // Your backend's base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val statementRequest = StatementRequest(
            transactionType = transactionType, // Changed variable name
            dateRange = dateRange,
            email = email // Add email to your StatementRequest
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.submitStatement("Bearer $token", statementRequest)

                if (response.isSuccessful) {
                    // Handle successful response
                    val statementResponse = response.body()
                    activity?.runOnUiThread {
                        Toast.makeText(context, statementResponse?.message ?: "Statement submitted successfully!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle error response
                    val errorMessage = response.errorBody()?.string() ?: "Failed to submit statement"
                    activity?.runOnUiThread {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun convertTimeToDate(time: Long): String {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.timeInMillis = time
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(utc.time)
    }

    companion object {
        fun newInstance(token: String): StatementFragment {
            val fragment = StatementFragment()
            val args = Bundle().apply {
                putString(ARG_TOKEN, token)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
