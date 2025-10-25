package com.usisoftware.usiapp.view.model

import java.util.UUID

data class Firm(
    val firmaAdi: String,
    val calismaAlani: List<String>,
    val documentId: String,
    val id: String = UUID.randomUUID().toString()
)
