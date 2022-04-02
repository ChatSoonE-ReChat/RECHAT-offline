package com.chatsoone.rechat.utils

import com.chatsoone.rechat.ApplicationClass.Companion.USER_INFO
import com.chatsoone.rechat.ApplicationClass.Companion.mSharedPreferences

fun saveID(user_id: Long) {
    val editor = mSharedPreferences.edit()
    editor.putLong(USER_INFO, user_id)
    editor.apply()
}

fun getID(): Long = mSharedPreferences.getLong(USER_INFO, -1)