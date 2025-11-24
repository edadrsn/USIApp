package com.usisoftware.usiapp.view.academicianView

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityOpenRequestsBinding

class OpenRequestsActivity : AppCompatActivity() {

    private lateinit var binding:ActivityOpenRequestsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_open_requests)
        binding=ActivityOpenRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.open_requests_container, OpenRequestsFragment())
                .commit()

        }

    }
}