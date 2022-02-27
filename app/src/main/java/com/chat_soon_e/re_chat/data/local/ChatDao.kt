package com.chat_soon_e.re_chat.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.chat_soon_e.re_chat.data.entities.BlockedChatList
import com.chat_soon_e.re_chat.data.entities.Chat
import com.chat_soon_e.re_chat.data.entities.ChatList

@Dao
interface ChatDao {

    //채팅 추가, 검증된
    @Insert
    fun insert(chat: Chat)

    //해당 chatIdx 대화 가져오기
    @Query("SELECT * FROM ChatTable WHERE chatIdx = :chatIdx")
    fun getChatByChatIdx(chatIdx: Int):Chat

    //해당 chatIdx isNew 바꾸기
    @Query("UPDATE ChatTable SET isNew= :status WHERE chatIdx= :chatIdx")
    fun updateIsNew(chatIdx: Int, status: Int)

    //MainActivity 최근 대화 목록 다 가져오기 -- local db 내용에 맞춰서 설정하기
    @Query("SELECT CM.chatIdx, CL.chatName AS nickName, CL.profileImg AS profileImg, CL.latestTime AS postTime, CM.message, CM.groupName, CM.isNew\n" +
            "FROM\n" +
            "    (SELECT (CASE WHEN C.groupName == 'null' THEN OU.nickname ELSE C.groupName END) AS chatName,\n" +
            "            (CASE WHEN C.groupName == 'null' THEN OU.image ELSE NULL END) AS profileImg,\n" +
            "            MAX(C.postTime) as latestTime\n" +
            "    FROM ChatTable C INNER JOIN OtherUserTable OU on C.otherUserIdx = OU.otherUserIdx\n" +
            "    WHERE OU.kakaoUserIdx = :userIdx AND C.status != 'DELETED'\n" +
            "    GROUP BY chatName, profileImg) CL\n" +
            "    INNER JOIN\n" +
            "    (SELECT DISTINCT (CASE WHEN C.groupName == 'null' THEN OU.nickname ELSE C.groupName END) AS chatName, C.chatIdx, C.message, C.postTime, C.groupName, C.isNew\n" +
            "    FROM ChatTable C INNER JOIN OtherUserTable OU on C.otherUserIdx = OU.otherUserIdx\n" +
            "    WHERE OU.kakaoUserIdx = :userIdx AND C.status != 'DELETED') CM\n" +
            "    ON CL.chatName = CM.chatName AND CL.latestTime = CM.postTime\n" +
            " ORDER BY postTime DESC;")
    fun getRecentChat(userIdx:Long):LiveData<List<ChatList>>

    //갠톡 채팅 가져오기, 검증된
    @Query("SELECT C.chatIdx, OU.nickname as nickName, C.groupName, OU.image as profileImg, C.message, C.postTime, C.isNew\n" +
            "    FROM ChatTable AS C INNER JOIN OtherUserTable AS OU on C.otherUserIdx = OU.otherUserIdx\n" +
            "    WHERE OU.kakaoUserIdx = :userIdx AND C.status != 'DELETED' AND C.otherUserIdx IN (SELECT otherUserIdx FROM ChatTable WHERE chatIdx = :chatIdx) AND groupName is 'null'\n" +
            "ORDER BY C.postTime DESC")
    fun getOneChatList(userIdx:Long, chatIdx:Int):LiveData<List<ChatList>>

    //livedata 없이 갠톡 채팅 가져오기, 검증된
    @Query("SELECT C.chatIdx, OU.nickname as nickName, C.groupName, OU.image as profileImg, C.message, C.postTime, C.isNew\n" +
            "    FROM ChatTable AS C INNER JOIN OtherUserTable AS OU on C.otherUserIdx = OU.otherUserIdx\n" +
            "    WHERE OU.kakaoUserIdx = :userIdx AND C.status != 'DELETED' AND C.otherUserIdx IN (SELECT otherUserIdx FROM ChatTable WHERE chatIdx = :chatIdx) AND groupName is 'null'\n" +
            "ORDER BY C.postTime DESC")
    fun getOneChatNoLiveList(userIdx:Long, chatIdx:Int):List<ChatList>

    //단톡 채팅 가져오기, 검증된
    @Query("SELECT C.chatIdx, OU.nickname as nickName, C.groupName, OU.image as profileImg, C.message, C.postTime, C.isNew" +
            " FROM ChatTable C INNER JOIN OtherUserTable OU on C.otherUserIdx = OU.otherUserIdx" +
            " WHERE OU.kakaoUserIdx = :userIdx AND C.status != 'DELETED' AND groupName = (SELECT groupName FROM ChatTable WHERE chatIdx = :chatIdx)" +
            " ORDER BY C.postTime DESC")
    fun getOrgChatList(userIdx:Long, chatIdx: Int):LiveData<List<ChatList>>

    //livedata 없이 갠톡 채팅 가져오기, 검증된
    @Query("SELECT C.chatIdx, OU.nickname as nickName, C.groupName, OU.image as profileImg, C.message, C.postTime, C.isNew" +
            " FROM ChatTable C INNER JOIN OtherUserTable OU on C.otherUserIdx = OU.otherUserIdx" +
            " WHERE OU.kakaoUserIdx = :userIdx AND C.status != 'DELETED' AND groupName = (SELECT groupName FROM ChatTable WHERE chatIdx = :chatIdx)" +
            " ORDER BY C.postTime DESC")
    fun getOrgChatNoLiveList(userIdx:Long, chatIdx: Int):List<ChatList>

    //message==status로 가져옴
    @Query("SELECT DISTINCT OU.nickname AS blockedName, OU.image AS blockedProfileImg, C.groupName AS groupName, OU.status AS status" +
            "    FROM ChatTable C INNER JOIN OtherUserTable OU on C.otherUserIdx = OU.otherUserIdx\n" +
            "    WHERE OU.kakaoUserIdx = :userIdx AND OU.status = 'BLOCKED' AND C.groupName =='null'\n" +
            "UNION\n" +
            "SELECT DISTINCT C.groupName AS blocked_name, null AS blocked_profileImg, C.groupName AS groupName, C.status AS status\n" +
            "FROM ChatTable C INNER JOIN OtherUserTable OU on C.otherUserIdx = OU.otherUserIdx\n" +
            "WHERE OU.kakaoUserIdx = :userIdx AND C.status = 'BLOCKED' AND C.groupName != 'null'")
    fun getBlockedChatList(userIdx:Long):LiveData<List<BlockedChatList>>

    //하나의 톡 삭제, 검증된
    @Query("DELETE FROM ChatTable WHERE chatIdx = :chatIdx")
    fun deleteByChatIdx(chatIdx:Int)

    //갠톡 전체 삭제, 검증된
    @Query("DELETE FROM ChatTable\n" +
            "WHERE otherUserIdx IN \n" +
            "      (SELECT otherUserIdx From (SELECT otherUserIdx FROM ChatTable WHERE chatIdx = :chatIdx) as DChat)\n" +
            "AND groupName = 'null';")
    fun deleteOneChat(chatIdx: Int)

    //단톡 삭제, 검증된
    @Query("DELETE FROM ChatTable\n" +
            "WHERE groupName IN\n" +
            "      (SELECT groupName FROM (SELECT groupName FROM ChatTable WHERE chatIdx = :chatIdx) as DChat)\n" +
            "AND otherUserIdx IN\n" +
            "    (SELECT otherUserIdx\n" +
            "    FROM (SELECT DISTINCT C.otherUserIdx\n" +
            "        FROM ChatTable C INNER JOIN OtherUserTable OU on C.otherUserIdx = OU.otherUserIdx\n" +
            "        WHERE OU.kakaoUserIdx =:userIdx ) as UChat);")
    fun deleteOrgChat(userIdx: Long, chatIdx: Int)

    //선택한 채팅목록 차단(갠톡), 검증된
    @Query("UPDATE OtherUserTable\n" +
            "SET status = 'BLOCKED'\n" +
            "WHERE nickname = :name AND kakaoUserIdx = :userIdx AND status = 'ACTIVE';")
    fun blockOneChat(userIdx: Long, name:String)

    //선택한 채팅목록 차단(단톡), 검증된
    @Query("UPDATE ChatTable\n" +
            "SET status = 'BLOCKED'\n" +
            "WHERE groupName = :name AND status = 'ACTIVE' AND\n" +
            "      otherUserIdx IN (SELECT otherUserIdx FROM OtherUserTable WHERE kakaoUserIdx = :userIdx);")
    fun blockOrgChat(userIdx: Long, name:String)

    //선택한 채팅목록 차단(갠톡)해제, 검증된
    @Query("UPDATE OtherUserTable\n" +
            "SET status = 'ACTIVE'\n" +
            "WHERE nickname = :name AND kakaoUserIdx = :userIdx AND status = 'BLOCKED';")
    fun unblockOneChat(userIdx: Long, name: String)

    //선택한 채팅목록 차단(단톡)해제, 검증된
    @Query("UPDATE ChatTable\n" +
            "SET status = 'ACTIVE'\n" +
            "WHERE groupName = :name AND status = 'BLOCKED' AND\n" +
            "      otherUserIdx IN (SELECT otherUserIdx FROM OtherUserTable WHERE kakaoUserIdx = :userIdx);")
    fun unblockOrgChat(userIdx: Long, name:String)

}