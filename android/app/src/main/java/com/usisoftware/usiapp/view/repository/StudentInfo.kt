package com.usisoftware.usiapp.view.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class StudentInfo(private val db: FirebaseFirestore) {

    fun getStudentData(
        uid: String,
        onSuccess: (DocumentSnapshot) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("Students")
            .document(uid)
            .get()
            .addOnSuccessListener { document -> onSuccess(document) }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun updateStudentData(
        uid: String,
        data: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("Students").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

}