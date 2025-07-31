package com.example.usiapp.view.model

data class Request(
    val id: String,
    val title: String,
    val message: String,
    val date: String,
    val selectedCategories: List<String>,
    val status:String,
    val requesterId:String,
    val requesterName:String,
    val requesterCategories:String,
    val requesterEmail:String
)


