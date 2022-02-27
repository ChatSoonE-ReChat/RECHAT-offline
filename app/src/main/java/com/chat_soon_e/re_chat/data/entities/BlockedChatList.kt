package com.chat_soon_e.re_chat.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// 차단된 톡방 목록 가져오기
@Entity(tableName = "BlockedChatList")
data class BlockedChatList(
    @PrimaryKey @SerializedName("blocked_name") val blockedName: String,
    @SerializedName("blocked_profileImg") val blockedProfileImg: String?,
    @SerializedName("groupName") val groupName: String?,
    @SerializedName("status") val status: String
)