package com.example.pennypulse
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.MenuItem
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.pennypulse.network.ApiService
import com.example.pennypulse.network.UserDetailsResponse

class MainActivity : AppCompatActivity() {

    private lateinit var myActionBar: androidx.appcompat.widget.Toolbar
    private lateinit var bottomNavigationBar: BottomNavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarToggle: ActionBarDrawerToggle
    private lateinit var appBarName: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve extras from the intent
        val token = intent.getStringExtra("EXTRA_TOKEN") // Retrieve the token
        token?.let { fetchUserDetails(it) }

        myActionBar = findViewById(R.id.toolbar_actionbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        setSupportActionBar(myActionBar)

        actionBarToggle = ActionBarDrawerToggle(this, drawerLayout, myActionBar, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarToggle)
        actionBarToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.menu_icon)

        bottomNavigationBar = findViewById(R.id.bottomNavigationView)
        bottomNavigationBar.selectedItemId = R.id.quickHitsIcon

        bottomNavigationBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.quickHitsIcon -> {
                    val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    val totalExpense = sharedPreferences.getString("expense", null)
                    loadFragment(QuickHitsFragment.newInstance(totalExpense?.toDouble() ?: 0.0)) // Placeholder for user name and expense
                    true
                }
                R.id.chartsIcon -> {
                    loadFragment(ChartsFragment())
                    true
                }
                R.id.statementsIcon -> {
                    // Load the StatementFragment when the statements icon is selected
                    loadFragment(StatementFragment.newInstance(token!!.toString()))
                    true
                }
                R.id.profileIcon -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> {
                    throw IllegalStateException("Fragment is not correct")
                }
            }
        }
    }

    private fun saveExpense(expense: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("expense", expense)
            apply()
        }
    }

    private fun fetchUserDetails(token: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.137.1:3000/api/") // Replace with your server's IP address
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getUserDetails("Bearer $token") // Include the token in the header

                if (response.isSuccessful) {
                    val userDetails = response.body()
                    userDetails?.let {
                        appBarName= findViewById(R.id.appBarName)
                        appBarName.text = "✨ Hello, ${it.name.replaceFirstChar { char -> char.uppercase() }}! 😊"
                        // Set or change the text of the appBarName TextView

                        // Update the QuickHitsFragment with user name and total expense
                        saveExpense(it.totalExpense.toString())
                        val quickHitsFragment = QuickHitsFragment.newInstance(it.totalExpense)
                        loadFragment(quickHitsFragment)
                    }
                } else {
                    // Handle the error case
                    println("Error fetching user details: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
