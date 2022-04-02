package com.chatsoone.rechat.data.local

import androidx.room.*
import com.chatsoone.rechat.data.entity.Icon

@Dao
interface IconDao {
    @Insert
    fun insert(icon: Icon)

    @Update
    fun update(icon: Icon)

    @Delete
    fun delete(icon: Icon)

    @Query("SELECT * FROM IconTable")
    fun getIconList(): List<Icon>
}