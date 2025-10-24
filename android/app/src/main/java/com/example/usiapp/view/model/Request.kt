package com.example.usiapp.view.model
import java.io.Serializable

data class Request(
    val id: String= "",
    val title: String= "",
    val message: String= "",
    val date: String= "",
    val status: String= "",
    val requesterId: String= "",
    val selectedCategories: List<String> = emptyList(),
    val requesterName: String= "",
    val requesterCategories: String= "",
    val requesterEmail: String= "",
    val requesterPhone: String= "",
    val requesterAddress:String = "" ,
    val adminMessage: String="",
    val adminDocumentId: String="",
    val requesterImage: String?,
    val selectedAcademiciansId:List<String> = emptyList(),
    val requesterType:String = "",   //öğrenci-akademisyen-sanayici
    val requestCategory:String="",  //öğrenci ve akademisyenin talep kategorisi
    val requestType:Boolean=false,       //açık talep olacak mı
    val applyUserCount:Int=0
) : Serializable



