package com.chatsoone.rechat.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LockViewModel : ViewModel() {
    private var _mode = MutableLiveData<Int>()
    val mode get() = _mode

    init {
        _mode.value = 0 // 0: 패턴 안맞음, 1: 패턴 맞음
    }

    fun setMode(mode: Int) {
        _mode.value = mode
    }
}