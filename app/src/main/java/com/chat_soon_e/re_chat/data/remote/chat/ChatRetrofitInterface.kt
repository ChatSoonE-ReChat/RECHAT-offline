package com.chat_soon_e.re_chat.data.remote.chat

import retrofit2.Call
import retrofit2.http.*

interface ChatRetrofitInterface {
    // 채팅 추가하기
    @POST("/app/chats/{kakaoUserIdx}/chat")
    fun addChat(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Body chat: Chat
    ): Call<AddChatResponse>

    // 전체 채팅목록 가져오기 (메인화면)
    @GET("/app/chats/{kakaoUserIdx}/chatlist")
    fun getChatList(
        @Path("kakaoUserIdx") kakaoUserIdx: Long
    ): Call<ChatListResponse>

    //갠톡 or 단톡 채팅 가져오기
    @GET("/app/chat/{kakaoUserIdx}")
    fun getChat(@Path("kakaoUserIdx")kakaoUserIdx: Int, @Query("otherUserIdx")otherUserIdx:Int?=null, @Query("groupName")groupName:String?=null):Call<ChatResponse>

    //폴더 안의 채팅 리스트 가져오기
    @GET("/app/chat-folder/{kakaoUserIdx}")
    fun getFolderChat(@Path("kakaoUserIdx")kakaoUserIdx:Int, @Query("folderIdx")folderIdx:Int):Call<FolderChatResponse>
}