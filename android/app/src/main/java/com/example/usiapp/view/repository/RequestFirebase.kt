package com.example.usiapp.view.repository

import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object RequestFirebase {

    //User idye göre bilgileri çek
    fun getUserRequest(
        db:FirebaseFirestore,
        userId:String,
        onSuccess:(List<DocumentSnapshot>) -> Unit,
        onFailure:(Exception) -> Unit
    ){
        db.collection("Requests")
            .whereEqualTo("requesterID",userId)
            .get()
            .addOnSuccessListener { snapshot ->
                onSuccess(snapshot.documents)
            }
            .addOnFailureListener {
                onFailure(it)
            }

    }
}


