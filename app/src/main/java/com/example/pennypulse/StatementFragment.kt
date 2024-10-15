package com.example.pennypulse

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.MultiAutoCompleteTextView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.util.Pair
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialDatePicker.Builder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StatementFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatementFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var selectedDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statement, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        selectedDate = view.findViewById(R.id.durationEditTextStatementForm)
        var datePickerButton: ImageButton = view.findViewById(R.id.durationPickerButton)

        datePickerButton.setOnClickListener{
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTheme(R.style.ThemeMaterialCalendar)
                .setTitleText("Select Date range")
                .setSelection(Pair(null, null))
                .build()

            picker.show(requireActivity().supportFragmentManager, "TAG")

            picker.addOnPositiveButtonClickListener {
                selectedDate.setText(convertTimeToDate(it.first) + " - " + convertTimeToDate(it.second))
            }

            picker.addOnNegativeButtonClickListener {
                picker.dismiss()
            }
        }

        val paymentModeSpinner = requireView().findViewById<Spinner>(R.id.payment_spinner)
        val paymentModes = resources.getStringArray(R.array.payment_modes)
        val paymentAdapter = activity?.let{
            ArrayAdapter(
                it,
                R.layout.dropdown_item,
                paymentModes
            )
        }
        paymentAdapter?.setDropDownViewResource(R.layout.dropdown_item)
        paymentModeSpinner.adapter = paymentAdapter

        paymentModeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = paymentModes[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }





    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StatementFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StatementFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun convertTimeToDate(time: Long): String{
        val  utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.timeInMillis = time
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(utc.time)
    }
}