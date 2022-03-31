package com.chatsoone.rechat.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// Local
@Entity(tableName = "ChatTable")
data class Chat(
    @SerializedName("otherUserIdx") var otherUserIdx: Int,          // 상대방 유저 idx
    @SerializedName("groupName") var groupName: String? = null,     // 그룹명
    @SerializedName("message") var message: String? = null,         // 내용
    @SerializedName("postTime") var postTime: String,               // 보낸시간
    @SerializedName("folderIdx") var folderIdx: Int = 0,            // 폴더 인덱스, 삭제해야함!
    @SerializedName("status") var status: String = "ACTIVE",        // 상태, (ACTIVE: 기본, BLOCKED, DELETED)
    @SerializedName("viewType") var viewType: Int = 0,
    @SerializedName("isChecked") var isChecked: Boolean = false,
    var isNew: Int? = 1,   // 새로 왔으면 1, 확인한 메시지면 0
) {
    @PrimaryKey(autoGenerate = true) @SerializedName("chatIdx") var chatIdx: Int = 0
}

object ChatViewType {
    const val DEFAULT = 0
    const val CHOOSE = 1
}