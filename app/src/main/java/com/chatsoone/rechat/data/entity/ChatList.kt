package com.chatsoone.rechat.data.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

// 서버와의 통신을 위한 데이터
// chat query를 통해 불러온 데이터 형식
@Entity(tableName = "ChatListTable")
data class ChatList(
    var chatIdx: Int,
    var nickName: String?,      // chat_name
    var groupName: String?,      // null이면 갠톡, 아니면 단톡
    var profileImg: String?,    // 프로필 사진 (단톡인 경우 없음)
    var postTime: String,       // latest_time에 해당
    var message: String?,       // latest_message에 해당
    var isNew: Int = 1
):Serializable {
    @PrimaryKey(autoGenerate = true) var id: Int? = 0;
    @Ignore var viewType: Int = 0;
    @Ignore var isChecked: Boolean = false;
}

object ChatListViewType {
    const val DEFAULT = 0
    const val CHOOSE = 1
}