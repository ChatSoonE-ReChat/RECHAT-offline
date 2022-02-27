package com.chat_soon_e.re_chat.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "PrivacyInformationTable")
data class PrivacyInformation (
    @SerializedName("title") var title: String?,
    @SerializedName("content") var content: String
) {
    @PrimaryKey(autoGenerate = true) var idx: Int = 0
}