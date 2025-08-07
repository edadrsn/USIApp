package com.example.usiapp.view.model
import java.io.Serializable

data class Request(
    val id: String,
    val title: String,
    val message: String,
    val date: String,
    val status: String,
    val requesterId: String,
    val selectedCategories: List<String>,
    val requesterName: String,
    val requesterCategories: String,
    val requesterEmail: String,
    val requesterPhone: String,
    val requesterAddress:String = "",
    val adminMessage: String="",
    val adminDocumentId: String="",
    val selectedAcademiciansId:List<String> = emptyList()
) : Serializable



