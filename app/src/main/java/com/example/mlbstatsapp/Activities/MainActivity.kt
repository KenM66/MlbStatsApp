package com.example.mlbstatsapp.Activities

import android.R
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mlbstatsapp.AlarmReciever
import com.example.mlbstatsapp.Fragments.IndividualPlayer
import com.example.mlbstatsapp.Fragments.PlayerSearchResultsFragment
import com.example.mlbstatsapp.HomeFragment
import com.example.mlbstatsapp.HomePlayerSearchSharedViewModel
import com.example.mlbstatsapp.LoadData
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*
import android.view.MenuItem as MenuItem1


class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    lateinit var bottomNav:BottomNavigationView
    lateinit var searchView: SearchView
    private lateinit var navController: NavController
    lateinit var sharedViewModel:HomePlayerSearchSharedViewModel

    /**
     * We set the navController and then call methods to setup bottom navigation and the actionNav
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.mlbstatsapp.R.layout.activity_main)

        val populateDB= LoadData.getInstance(applicationContext)
        populateDB.insertTeams()
        populateDB.updateAllTeams()

        setAlarmManager()

        val navHostFragment = supportFragmentManager
            .findFragmentById(com.example.mlbstatsapp.R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupBottomNav()
        setupActionNav()
    }

    /**
     * Setups the bottom nav
     * Configures it so the bottom nav is hidden on playerSearch and playerIndividual page
     */
    fun setupBottomNav(){
        bottomNav = findViewById(com.example.mlbstatsapp.R.id.bottomNavigationView)
        bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, nd: NavDestination, _ ->
            if (nd.id == com.example.mlbstatsapp.R.id.homeFragment|| nd.id == com.example.mlbstatsapp.R.id.teamsListFragment || nd.id == com.example.mlbstatsapp.R.id.settingsFragment) {
                bottomNav.visibility = View.VISIBLE
            } else {
                bottomNav.visibility = View.GONE
            }
        }

    }

    /**
     * Setups the actionNav so that there is a back button on the appropriate pages
     */
    fun setupActionNav(){
        setupActionBarWithNavController(navController)
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * We inflate the menu in the appBar so that it shows the search icon
     * We also set a textListener onto the searchView that will appear when the search icon is clicked on
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(com.example.mlbstatsapp.R.menu.action_bar_search, menu )
        var searchItem: android.view.MenuItem =
            menu!!.findItem(com.example.mlbstatsapp.R.id.app_bar_menu_search);
        searchView =  MenuItemCompat.getActionView(searchItem) as SearchView
        searchView.queryHint = "Search for Player"
        searchView.setOnQueryTextListener(this)
        return true
    }

    /**
     * When the search icon is clicked on we set the searchView
     */
    override fun onOptionsItemSelected(item: MenuItem1):Boolean {
        when(item.itemId){
            com.example.mlbstatsapp.R.id.app_bar_menu_search->{
                searchView.setIconified(false);
                return true;
            }
        }
        return false;
    }

    /**
     *This method fires when the text is submitted in the searchView
     * It puts the text in a sharedViewModel and then navigates to PlayerSearchResultsPage from the current page
     */
    override fun onQueryTextSubmit(p0: String?): Boolean {
        sharedViewModel = this?.run{
            ViewModelProviders.of(this).get(HomePlayerSearchSharedViewModel::class.java)
        }?: throw Exception("Invalid Activity")
        sharedViewModel.playerSearchTerm.value = p0

        var currentFragment:String = navController.currentDestination!!.label.toString()
        val id = navController.currentDestination?.id
        Log.d(MainActivity::class.java.simpleName, navController.currentDestination!!.label.toString())
        var view:View = findViewById(R.id.content)
        if(currentFragment.equals("Home")){
            navController.navigate(com.example.mlbstatsapp.R.id.action_homeFragment_to_playerSearchResultsFragment)
        }else if(currentFragment.equals("Player Search Results")){
            navController.popBackStack(id!!,true)
            navController.navigate(id)
        }else if(currentFragment.equals("Player Page")){
            navController.navigate(com.example.mlbstatsapp.R.id.action_individualPlayer_to_playerSearchResultsFragment)
            navController.popBackStack(id!!,true)
        }

        return true
    }

    /**
     * Do nothing when the text changes in the searchView
     */
    override fun onQueryTextChange(p0: String?): Boolean {
        return false
    }
    /**
     * Sets the alarm to check for stat changes for 5am every day
     */
    fun setAlarmManager(){
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 5)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReciever::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        Log.d(SplashScreenActivity::class.java.simpleName, "Alarm Set")
    }
}