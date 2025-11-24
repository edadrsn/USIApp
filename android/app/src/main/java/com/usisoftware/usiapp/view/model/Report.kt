package com.usisoftware.usiapp.view.model

import java.io.Serializable

data class Report(
    val id: String = "",
    val message: String = "",
    val requestId: String = "",
    val user: String = ""
) : Serializable

