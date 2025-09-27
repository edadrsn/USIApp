package com.example.usiapp.view.academicianView

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.usiapp.databinding.FragmentRequestsBinding
import com.example.usiapp.view.model.Request
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RequestsFragment : Fragment() {

    private var _binding: FragmentRequestsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var allRequests = mutableListOf<Request>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        // Butonlar
        binding.btnAllRequests.setOnClickListener {
            setActiveButton(binding.btnAllRequests)
            //fetchAllRequests()
        }

        binding.btnMyRequests.setOnClickListener {
            setActiveButton(binding.btnMyRequests)
            //fetchMyRequests()
        }

        binding.btnOpenRequests.setOnClickListener {
            setActiveButton(binding.btnOpenRequests)
            //fetchOpenRequests()
        }
    }


    private fun setActiveButton(activeBtn: MaterialButton) {
        val buttons = listOf(binding.btnAllRequests, binding.btnMyRequests, binding.btnOpenRequests)
        for (btn in buttons) {
            if (btn == activeBtn) {
                btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#124090"))
                btn.setTextColor(Color.WHITE)
            } else {
                btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                btn.setTextColor(Color.BLACK)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
