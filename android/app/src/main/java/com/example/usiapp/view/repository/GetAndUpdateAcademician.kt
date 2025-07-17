package com.example.usiapp.view.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

object GetAndUpdateAcademician {

    //Emaile göre bilgileri çek
    fun getAcademicianInfoByEmail(
        db: FirebaseFirestore,
        email: String,
        onSuccess: (DocumentSnapshot) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("AcademicianInfo")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    onSuccess(documents.documents[0])
                } else {
                    onFailure(Exception("Belge bulunamadı !"))
                }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }


    //Idye göre verileri güncelle
    fun updateAcademicianInfo(
        db: FirebaseFirestore,
        documentId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("AcademicianInfo")
            .document(documentId)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

}