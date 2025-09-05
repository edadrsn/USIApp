package com.example.usiapp.view.industryView

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityIndustryMainBinding

class IndustryMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndustryMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIndustryMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNav = binding.bottomNavigationIndustry

        // NavController’ı host fragment’tan al
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_industry_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // BottomNavigationView ile senkronize et
        NavigationUI.setupWithNavController(bottomNav, navController)

        // Eğer başka bir sayfadan "intent" ile gelindiyse
        val goTo = intent.getStringExtra("goToFragment")

        if (savedInstanceState == null) {
            when (goTo) {
                "request" -> {
                    bottomNav.selectedItemId = R.id.requestIndustryFragment
                    navController.navigate(R.id.requestIndustryFragment)
                }
                "home" -> {
                    bottomNav.selectedItemId = R.id.homeIndustryFragment
                    navController.navigate(R.id.homeIndustryFragment)
                }
                else -> {
                    bottomNav.selectedItemId = R.id.profileIndustryFragment
                    navController.navigate(R.id.profileIndustryFragment)
                }
            }
        }
    }
}
