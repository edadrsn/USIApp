package com.example.usiapp.view.academicianView

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityAcademicianMainBinding

class AcademicianMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcademicianMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAcademicianMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // XML'deki BottomNavigationView bileşenine erişim sağlanır
        val bottomNav = binding.bottomNavigation

        // NavHostFragment üzerinden NavController alınır (FragmentContainerView'deki navigation yöneticisi)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        // Navigation yöneticisi alınır
        navController = navHostFragment.navController

        // BottomNavigationView ile NavController bağlanır.
        // Böylece kullanıcı menüden bir sekmeye tıkladığında ilgili Fragment gösterilir.
        NavigationUI.setupWithNavController(bottomNav, navController)

        val goTo = intent.getStringExtra("goToFragment")

        if (savedInstanceState == null) {
            when (goTo) {
                "pendingRequest" -> {
                    bottomNav.selectedItemId = R.id.pendingRequestAcademicianFragment
                    navController.navigate(R.id.pendingRequestAcademicianFragment)
                }
                "preview" -> {
                    bottomNav.selectedItemId = R.id.previewFragment
                    navController.navigate(R.id.previewFragment)
                }
                else -> {
                    bottomNav.selectedItemId = R.id.profileFragment
                    navController.navigate(R.id.profileFragment)
                }
            }
        }
    }
}