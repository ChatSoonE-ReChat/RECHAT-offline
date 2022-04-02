package com.chatsoone.rechat.data.remote

import com.google.gson.annotations.SerializedName

// 폴더 안의 채팅 가져오기
data class FolderContent(
    @SerializedName("chatIdx") var chatIdx: Int,
    @SerializedName("folderName") val folderName: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImgUrl") val profileImgUrl: String?,
    @SerializedName("message") val message: String,
    @SerializedName("chat_date") val chatDate: String?,
    @SerializedName("post_time") val postTime: String
)