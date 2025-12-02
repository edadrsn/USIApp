package com.usisoftware.usiapp.view.academicianView

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityAcademicianMainBinding


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
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        if (navHostFragment !is NavHostFragment) {
            Toast.makeText(this, "Navigation yüklenemedi!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Navigation yöneticisi alınır
        navController = navHostFragment.navController

        // BottomNavigationView ile NavController bağlanır.
        // Böylece kullanıcı menüden bir sekmeye tıkladığında ilgili Fragment gösterilir.
        NavigationUI.setupWithNavController(bottomNav, navController)

        val goTo = intent.getStringExtra("goToFragment")

        if (savedInstanceState == null) {
            when (goTo) {
                "request" -> {
                    bottomNav.selectedItemId = R.id.requestsFragment
                    navController.navigate(R.id.requestsFragment)
                }

                "openRequest" -> {
                    bottomNav.selectedItemId = R.id.openRequestsFragment
                    navController.navigate(R.id.openRequestsFragment)
                }

                else -> {
                    bottomNav.selectedItemId = R.id.profileFragment
                    navController.navigate(R.id.profileFragment)
                }
            }
        }
    }
}
