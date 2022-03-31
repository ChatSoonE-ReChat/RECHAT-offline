package com.chatsoone.rechat.data.local

import androidx.room.*
import com.chatsoone.rechat.data.entity.User

@Dao
interface UserDao {
    @Insert
    fun insert(user: User)

    @Update
    fun update(user:User)

    //유저의 상태 업데이트
    @Query("UPDATE UserTable SET status= :status WHERE kakaoUserIdx= :kakaoUserIdx")
    fun updateStatus(kakaoUserIdx: Long, status: String)

    @Delete
    fun delete(user:User)

    @Query("SELECT * FROM UserTable WHERE kakaoUserIdx= :id")
    fun getUser(id:Long):User?

    @Query("SELECT * FROM UserTable")
    fun getUsers():List<User>?
}