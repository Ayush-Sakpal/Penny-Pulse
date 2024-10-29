package com.example.pennypulse

import android.util.Log
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pennypulse.network.ApiService
import com.example.pennypulse.network.SmsData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class ManualAdd : AppCompatActivity() {
    lateinit var addButton: Button
    lateinit var cancelButton: Button
    lateinit var dateEditTextManAdd: TextView
    lateinit var timeEditTextManAdd: TextView
    lateinit var titleEditTextManAdd: EditText
    lateinit var amountEditTextManAdd: EditText
    lateinit var transactionTypeSpinner: Spinner
    private var selectedTransactionType: String? = null


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manual_add)
        Log.d("ManualAdd", "ManualAdd activity created")

        // Set window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve token from intent
        val userId = intent.getStringExtra("EXTRA_USER_ID") // Retrieve the token

        // Initialize date, time, title, and amount fields
        dateEditTextManAdd = findViewById(R.id.dateEditTextStatementForm)
        timeEditTextManAdd = findViewById(R.id.timeEditTextStatementForm)
        titleEditTextManAdd = findViewById(R.id.titleEditTextStatementForm)
        amountEditTextManAdd = findViewById(R.id.amountEditTextStatementForm)

        dateEditTextManAdd.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(System.currentTimeMillis())
        val calendar = Calendar.getInstance()

        // Date picker setup
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            dateEditTextManAdd.text = SimpleDateFormat("dd.MM.yyyy", Locale.CHINA).format(calendar.time)
        }
        dateEditTextManAdd.setOnClickListener {
            DatePickerDialog(
                this, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Time picker setup
        timeEditTextManAdd.setOnClickListener {
            val c: Calendar = Calendar.getInstance()
            val hh = c.get(Calendar.HOUR_OF_DAY)
            val mm = c.get(Calendar.MINUTE)
            TimePickerDialog(this, { _, hourOfDay, minute ->
                timeEditTextManAdd.text = "$hourOfDay:$minute"
            }, hh, mm, true).show()
        }

        // Spinner setup
        transactionTypeSpinner = findViewById(R.id.man_add_payment_spinner)
        val transactionTypes = resources.getStringArray(R.array.T_types) // Assuming you have an array in `strings.xml`
        val paymentAdapter = ArrayAdapter(this, R.layout.dropdown_item, transactionTypes)
        paymentAdapter.setDropDownViewResource(R.layout.dropdown_item)
        transactionTypeSpinner.adapter = paymentAdapter
        transactionTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTransactionType = transactionTypes[position]
                (parent?.getChildAt(0) as? TextView)?.setTextColor(Color.BLACK)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedTransactionType = null
            }
        }

        // Button setup
        addButton = findViewById(R.id.addButton)
        cancelButton = findViewById(R.id.cancelButton)

        addButton.setOnClickListener {
            val selectedDate = dateEditTextManAdd.text.toString()
            val selectedTime = timeEditTextManAdd.text.toString()
            val title = titleEditTextManAdd.text.toString()
            val amount = amountEditTextManAdd.text.toString()

            if (selectedTransactionType != null && title.isNotEmpty() && amount.isNotEmpty()) {
                val smsData = SmsData(
                    userId=userId!!.toInt(),
                    amount = amount,
                    transactionType = selectedTransactionType!!,
                    transactionId = title, // Map title to transactionId
                    transactionDate = "$selectedDate $selectedTime", // Combine date and time

                )
                sendSmsDataToServer(smsData)
                finish()
            } else {
                Log.d("ManualAdd", "Please fill all fields")
            }

        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun sendSmsDataToServer(smsData: SmsData) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.137.1:3000/api/") // Replace with your server's URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.sendSms(smsData) // Sending SmsData with userId
                if (response.isSuccessful) {
                    Log.d("ManualAdd", "Data sent successfully")
                } else {
                    Log.e("ManualAdd", "Error sending data: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
