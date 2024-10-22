package com.example.pennypulse

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
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Locale


class ManualAdd : AppCompatActivity() {
    lateinit var addButton: Button
    lateinit var cancelButton: Button
    lateinit var dateEditTextManAdd: TextView
    lateinit var timeEditTextManAdd: TextView

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manual_add)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dateEditTextManAdd = findViewById(R.id.dateEditTextStatementForm)
        dateEditTextManAdd.text = SimpleDateFormat("dd.MM.yyyy").format(System.currentTimeMillis())

        val calendar = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd.MM.yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.CHINA)
            dateEditTextManAdd.text = sdf.format(calendar.time)
        }
        dateEditTextManAdd.setOnClickListener {
            DatePickerDialog(
                this, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        timeEditTextManAdd = findViewById(R.id.timeEditTextStatementForm)

        timeEditTextManAdd.setOnClickListener {
            val c: Calendar = Calendar.getInstance()
            val hh = c.get(Calendar.HOUR_OF_DAY)
            val mm = c.get(Calendar.MINUTE)
            val timePickerDialog: TimePickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    timeEditTextManAdd.setText("" + hourOfDay + ":" + minute);
                },
                hh,
                mm,
                true
            )
            timePickerDialog.show()


            val paymentModeSpinner = requireViewById<Spinner>(R.id.man_add_payment_spinner)
            val paymentModes = resources.getStringArray(R.array.payment_modes)
            val paymentAdapter = ArrayAdapter(
                this,
                R.layout.dropdown_item,
                paymentModes
            )
            paymentAdapter.setDropDownViewResource(R.layout.dropdown_item)
            paymentModeSpinner.adapter = paymentAdapter

            paymentModeSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedItem = paymentModes[position]
                        (parent!!.getChildAt(0) as TextView).setTextColor(Color.BLACK)

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }
                }

            addButton = findViewById(R.id.addButton)
            cancelButton = findViewById(R.id.cancelButton)

            addButton.setOnClickListener {

            }

            cancelButton.setOnClickListener {
                finish()
            }


        }
    }
}