package com.chatsoone.rechat.data.local

import androidx.room.*
import com.chatsoone.rechat.data.entity.PrivacyInformation

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