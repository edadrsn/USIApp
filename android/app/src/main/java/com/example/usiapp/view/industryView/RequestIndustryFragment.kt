package com.example.usiapp.view.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.usiapp.databinding.FragmentRequestIndustryBinding
import com.example.usiapp.view.adapter.RequestAdapter
import com.example.usiapp.view.model.Request
import com.example.usiapp.view.repository.RequestFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



class RequestIndustryFragment : Fragment() {

    private var _binding: FragmentRequestIndustryBinding?= null
    private val binding get()=_binding!!

    private lateinit var db:FirebaseFirestore
    private lateinit var auth:FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=FragmentRequestIndustryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        db=FirebaseFirestore.getInstance()
        auth=FirebaseAuth.getInstance()
        val email=auth.currentUser?.email?: return
        val requestList = mutableListOf<Request>()

        RequestFirebase.getIndustryRequests(
            db,
            email = email,
            onSuccess = { requests ->
                for (doc in requests) {
                    val title = doc.getString("requestTitle") ?: ""
                    val message = doc.getString("requestMessage") ?: ""
                    val date = doc.getString("createdDate") ?: ""
                    val categories = doc.get("selectedCategories") as? List<String> ?: emptyList()

                    requestList.add(Request(title, message, date, categories))
                }

                val adapter = RequestAdapter(requestList)
                binding.requestRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.requestRecyclerView.adapter = adapter
            },
            onFailure = {
                Toast.makeText(requireContext(), "Hata: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        )


        binding.createRequest.setOnClickListener {
            val intent= Intent(requireContext(), CreateRequestActivity::class.java)
            startActivity(intent)
        }

    }
}