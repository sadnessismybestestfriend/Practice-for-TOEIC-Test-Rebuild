package com.example.practicefortoeictestrebuild.model

import com.google.gson.annotations.SerializedName

data class DataOverview(
    @SerializedName("_id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("status")
    val status: Int,
    @SerializedName("cardId")
    val cardId: String,
    var progress: Int
)