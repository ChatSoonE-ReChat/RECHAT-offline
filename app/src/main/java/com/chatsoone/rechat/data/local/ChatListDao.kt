package com.chatsoone.rechat.data.local

import androidx.room.*
import com.chatsoone.rechat.data.entity.ChatList

@Dao
interface ChatListDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(chatList:ChatList)

    @Update
    fun update(chatList : ChatList)

    @Delete
    fun delete(chatList: ChatList)

    @Query("DELETE FROM ChatListTable")
    fun allDelete()

    //chatList insertTable을 써야 가능한 함수!
    //해당 chatIdx isNew 바꾸기
    @Query("UPDATE ChatListTable SET isNew= :status WHERE chatIdx= :chatIdx")
    fun updateIsNew(chatIdx: Int, status:Int)

    @Query("SELECT * FROM ChatListTable")
    fun getChatList(): List<ChatList>

    @Query("SELECT * FROM ChatListTable WHERE chatIdx = :chatIdx")
    fun getChatListByChatIdx(chatIdx: Int): ChatList

    @Query("DELETE FROM ChatListTable WHERE chatIdx = :chatIdx")
    fun deleteChatListByIdx(chatIdx: Int)
}