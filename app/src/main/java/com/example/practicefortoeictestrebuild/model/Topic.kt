package com.example.practicefortoeictestrebuild.model

import com.google.gson.annotations.SerializedName

data class Topic(
    @SerializedName("_id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("cards")
    val cards: MutableList<DataOverview>
)
