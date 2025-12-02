package com.usisoftware.usiapp.view.academicianView

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.FragmentRequestsBinding

class RequestsFragment : Fragment() {

    private var _binding: FragmentRequestsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (savedInstanceState == null) {
            setActiveButton(binding.btnMyCreateRequests)
            childFragmentManager.beginTransaction()
                .replace(R.id.containerRequests, RequestAcademicianFragment())
                .commit()
        }

        // Safe click ile oluşturduğu talepler
        binding.btnMyCreateRequests.setSafeOnClickListener {
            setActiveButton(binding.btnMyCreateRequests)
            childFragmentManager.beginTransaction()
                .replace(R.id.containerRequests, RequestAcademicianFragment())
                .commit()
        }

        // Safe click ile kendisine gelen talepler
        binding.btnIncomingRequests.setSafeOnClickListener {
            setActiveButton(binding.btnIncomingRequests)
            childFragmentManager.beginTransaction()
                .replace(R.id.containerRequests, PendingRequestAcademicianFragment())
                .commit()
        }
    }


    fun View.setSafeOnClickListener(interval: Long = 800, onSafeClick: (View) -> Unit) {
        var lastClickTime = 0L
        setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > interval) {
                lastClickTime = currentTime
                onSafeClick(it)
            }
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

