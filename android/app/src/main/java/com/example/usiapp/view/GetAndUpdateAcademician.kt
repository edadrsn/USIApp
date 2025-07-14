package com.example.usiapp.view

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

object GetAndUpdateAcademician {

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