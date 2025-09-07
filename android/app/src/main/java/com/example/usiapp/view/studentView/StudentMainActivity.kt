package com.example.usiapp.view.studentView

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityStudentMainBinding

class StudentMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStudentMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNav = binding.bottomNavigationStudent

        // NavController’ı host fragment’tan al
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_student_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // BottomNavigationView ile senkronize et
        NavigationUI.setupWithNavController(bottomNav, navController)

        // Eğer başka bir sayfadan "intent" ile gelindiyse
        val goTo = intent.getStringExtra("goToFragment")

        if (savedInstanceState == null) {
            when (goTo) {
                "incomingRequestStudent" -> {
                    bottomNav.selectedItemId = R.id.fragmentIncomingRequestStudent
                    navController.navigate(R.id.fragmentIncomingRequestStudent)
                }

                "createRequestStudent" -> {
                    bottomNav.selectedItemId = R.id.fragmentCreateRequestStudent
                    navController.navigate(R.id.fragmentCreateRequestStudent)
                }

                else -> {
                    bottomNav.selectedItemId = R.id.fragmentStudentProfile
                    navController.navigate(R.id.fragmentStudentProfile)
                }
            }
        }
    }
}
