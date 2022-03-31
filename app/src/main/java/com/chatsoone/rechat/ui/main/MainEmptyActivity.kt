package com.chatsoone.rechat.ui.main

import com.chatsoone.rechat.R
import com.chatsoone.rechat.base.BaseActivity
import com.chatsoone.rechat.databinding.ActivityMainEmptyBinding
import com.chatsoone.rechat.ui.main.home.HomeFragment

class MainEmptyActivity : BaseActivity<ActivityMainEmptyBinding>(ActivityMainEmptyBinding::inflate) {

    override fun afterOnCreate() {
        initFragment()
    }

    private fun initFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_choose_frame_layout, HomeFragment())
            .commitAllowingStateLoss()
    }
}