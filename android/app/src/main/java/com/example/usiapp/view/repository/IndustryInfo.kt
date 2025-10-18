package com.example.usiapp.view.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class IndustryInfo(private val db: FirebaseFirestore) {

    //Verileri çek
    fun getIndustryData(
        uid: String,
        onSuccess: (DocumentSnapshot) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("Industry")
            .document(uid)
            .get()
            .addOnSuccessListener { document -> onSuccess(document) }
            .addOnFailureListener { e -> onFailure(e) }
    }

    //Verileri güncelle
    fun updateIndustryData(
        uid: String,
        data: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("Industry").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}

