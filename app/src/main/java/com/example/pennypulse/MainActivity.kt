package com.example.pennypulse

import android.app.Dialog
import android.content.Context
import androidx.fragment.app.Fragment
import android.content.Intent
import android.media.RouteListingPreference
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.compose.runtime.Composable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.pennypulse.R.id.toolbar_actionbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    lateinit var myActionBar: androidx.appcompat.widget.Toolbar
    lateinit var bottomNavigationBar: BottomNavigationView
    lateinit var quickHitsFragment: QuickHitsFragment
    lateinit var chartsFragment: ChartsFragment
    lateinit var statementFragment: StatementFragment
    lateinit var profileFragment: ProfileFragment
    lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarToogle: ActionBarDrawerToggle
    lateinit var floatingButton: FloatingActionButton
    lateinit var slideNavView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadFragment(QuickHitsFragment())

        myActionBar = findViewById(R.id.toolbar_actionbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        setSupportActionBar(myActionBar)

        actionBarToogle = ActionBarDrawerToggle(this, drawerLayout, myActionBar, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarToogle)
        actionBarToogle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.menu_icon)

        slideNavView = findViewById(R.id.slideNavView)

        slideNavView.setNavigationItemSelectedListener (NavigationView.OnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navMyAccount -> {
                    loadFragment(ProfileFragment())
                    true
                }
                R.id.navFeedback -> {
                    loadFragment(StatementFragment())
                    true
                }
                R.id.navLogout -> {
                    val intent = Intent(this, LoginScreen::class.java)
                    startActivity(intent)
                    true
                }

                else -> {false}
            }
        });

        bottomNavigationBar = findViewById(R.id.bottomNavigationView)
        bottomNavigationBar.selectedItemId = R.id.quickHitsIcon

        bottomNavigationBar.setOnItemSelectedListener {
            when(it.itemId){
                R.id.quickHitsIcon -> {
                    loadFragment(QuickHitsFragment())
                    true
                }
                R.id.chartsIcon -> {
                    loadFragment(ChartsFragment())
                    true
                }
                R.id.statementsIcon -> {
                    loadFragment(StatementFragment())
                    true
                }
                R.id.profileIcon -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> {
                    throw IllegalStateException("Fragment is not correct")
                    false
                }
            }
        }

        floatingButton = findViewById(R.id.addFloatingActionButton)

        floatingButton.setOnClickListener{
            val intent = Intent(applicationContext, ManualAdd::class.java)
            startActivity(intent)
        }
    }

    private  fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container,fragment)
        transaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(actionBarToogle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }

}