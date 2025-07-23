package com.example.usiapp.view.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityIndustryMainBinding
import com.google.firebase.auth.FirebaseAuth

class IndustryMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndustryMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Sistem çubuğu (status bar, navigation bar) alanlarını içeriğe dahil eder

        // ViewBinding ile layout bağlama işlemi
        binding = ActivityIndustryMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // BottomNavigationView referansını alıyoruz
        val bottomNav = binding.bottomNavigationIndustry

        // NavController’ı, Navigation Component’in host fragment’ı üzerinden alıyoruz
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_industry_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // BottomNavigationView ile NavController’ı senkronize ediyoruz
        NavigationUI.setupWithNavController(bottomNav, navController)

        // Eğer başka bir sayfadan "intent" ile geliniyorsa, açılacak fragment'ı belirlemek için kontrol yapılır
        val goTo = intent.getStringExtra("goToFragment")

        if (savedInstanceState == null) {
            // Hangi fragment'ın açılacağını belirle
            val fragment = when (goTo) {
                "request" -> {
                    // Eğer "request" olarak gelindiyse hem fragment'ı değiştir hem de menüdeki işaretlemeyi değiştir
                    bottomNav.selectedItemId = R.id.requestIndustryFragment
                    RequestIndustryFragment()
                }
                else -> {
                    // Aksi durumda profil fragment’ı varsayılan olarak gösterilir
                    bottomNav.selectedItemId = R.id.profileIndustryFragment
                    ProfileIndustryFragment()
                }
            }

            // Fragment’ı manuel olarak yüklüyoruz
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_industry_fragment, fragment)
                .commit()
        }
    }
}
