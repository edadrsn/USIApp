package com.usisoftware.usiapp.view.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

object GetAndUpdateAcademician {

    //Bilgileri çek
    fun getAcademicianInfoByEmail(
        db: FirebaseFirestore,
        userId: String,
        onSuccess: (DocumentSnapshot) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("Academician")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onSuccess(document)
                } else {
                    onFailure(Exception("Belge bunamadı !"))
                }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }


    //Verileri güncelle
    fun updateAcademicianInfo(
        db: FirebaseFirestore,
        userId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("Academician")
            .document(userId)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

}