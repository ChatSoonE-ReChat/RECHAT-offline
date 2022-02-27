package com.chat_soon_e.re_chat.data.local

import androidx.room.*
import com.chat_soon_e.re_chat.data.entities.PrivacyInformation

@Dao
interface PrivacyInformationDao {
    @Insert
    fun insert(privacyInformation: PrivacyInformation)

    @Update
    fun update(privacyInformation: PrivacyInformation)

    @Delete
    fun delete(privacyInformation: PrivacyInformation)

    @Query("SELECT * FROM PrivacyInformationTable")
    fun getPrivacyInformation(): List<PrivacyInformation>
}