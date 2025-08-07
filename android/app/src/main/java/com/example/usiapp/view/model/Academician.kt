package com.example.usiapp.view.model

data class Academician(
    val documentId: String = "",
    val academicianEmail:String="",
    val academicianName: String = "",
    val academicianDegree: String = "",
    val academicianImageUrl: String = "",
    val academicianExpertArea: List<String> = emptyList()
)
