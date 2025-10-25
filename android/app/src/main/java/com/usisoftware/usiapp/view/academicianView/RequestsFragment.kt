package com.usisoftware.usiapp.view.academicianView

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.FragmentRequestsBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RequestsFragment : Fragment() {

    private var _binding: FragmentRequestsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

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

        // İlk açılışta fragment ve buton rengini ayarlayın
        if (savedInstanceState == null) {
            setActiveButton(binding.btnMyCreateRequests)
            childFragmentManager.beginTransaction()
                .replace(R.id.containerRequests, RequestAcademicianFragment())
                .commit()
        }

        //Oluşturduğu talepler
        binding.btnMyCreateRequests.setOnClickListener {
            setActiveButton(binding.btnMyCreateRequests)
            childFragmentManager.beginTransaction()
                .replace(R.id.containerRequests, RequestAcademicianFragment())
                .commit()
        }


        //Kendisine gelen talepler
        binding.btnIncomingRequests.setOnClickListener {
            setActiveButton(binding.btnIncomingRequests)
            childFragmentManager.beginTransaction()
                .replace(R.id.containerRequests, PendingRequestAcademicianFragment())
                .commit()
        }
    }


    //Tıklanan butonların renklerini ayarlama
    private fun setActiveButton(activeBtn: MaterialButton) {
        val buttons = listOf(binding.btnMyCreateRequests, binding.btnIncomingRequests)
        buttons.forEach { button ->
            if (button == activeBtn) {
                button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#124090"))
                button.setTextColor(Color.WHITE)
            } else {
                button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                button.setTextColor(Color.BLACK)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
