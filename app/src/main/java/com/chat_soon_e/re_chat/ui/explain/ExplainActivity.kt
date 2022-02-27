package com.chat_soon_e.re_chat.ui.explain

import android.util.Log
import android.view.View
import com.chat_soon_e.re_chat.ApplicationClass
import com.chat_soon_e.re_chat.ApplicationClass.Companion.mSharedPreferences
import com.chat_soon_e.re_chat.R
import com.chat_soon_e.re_chat.databinding.ActivityExplainBinding
import com.chat_soon_e.re_chat.ui.BaseActivity
import com.chat_soon_e.re_chat.ui.PermissionActivity
import com.chat_soon_e.re_chat.utils.permissionGrantred

class ExplainActivity: BaseActivity<ActivityExplainBinding>(ActivityExplainBinding::inflate) {
    var isExplain = 0
    private val tag = "ACT/EXPLAIN"

    override fun onStart() {
        super.onStart()
        Log.d(tag, "onStart()/explain_from_menu: ${getSharedPreferences("explain", MODE_PRIVATE).getInt("explain_from_menu", 0)}")
        Log.d(tag, "onStart()/explain: ${getSharedPreferences("explain", MODE_PRIVATE).getInt("explain", 0)}")

        if(getSharedPreferences("explain", MODE_PRIVATE).getInt("explain_from_menu", 0) == 1) {
            binding.explainCheckbox.visibility = View.INVISIBLE
            return
        }

        if(getSharedPreferences("explain", MODE_PRIVATE).getInt("explain_from_menu", 0) == 0 && getSharedPreferences("explain", MODE_PRIVATE).getInt("explain", 0) == 2) {
            finish()
        }
    }

    override fun initAfterBinding() {
        initFragment()
        initClick()
    }

    private fun initFragment(){
        supportFragmentManager.beginTransaction()
            .replace(R.id.explain_fl, ExplainFragment())
            .commitAllowingStateLoss()
    }

    private fun initClick(){
        mSharedPreferences = getSharedPreferences("explain", MODE_PRIVATE)
        // x 버튼 클릭시
        binding.explainExitBtnIv.setOnClickListener {
            close()
        }
    }

    override fun onBackPressed() {
        close()
    }

    private fun close(){
        if(getSharedPreferences("explain", MODE_PRIVATE).getInt("explain_from_menu", 0) != 1) {
            Log.d(tag, "close()/if")
            isExplain = if(binding.explainCheckbox.isChecked) 2 else 1

            val editor = mSharedPreferences.edit()
            editor.putInt("explain", isExplain)
            editor.apply()

            Log.d(tag, "close()/explain_from_menu: ${getSharedPreferences("explain", MODE_PRIVATE).getInt("explain_from_menu", 0)}")
            Log.d(tag, "close()/explain: ${getSharedPreferences("explain", MODE_PRIVATE).getInt("explain", 0)}")

            if(!permissionGrantred(this)) startNextActivity(PermissionActivity::class.java)
            else finish()
        } else {
            Log.d(tag, "close()/else")
            mSharedPreferences = getSharedPreferences("explain", MODE_PRIVATE)
            val editor = mSharedPreferences.edit()
            editor.putInt("explain_from_menu", 0)
            editor.apply()

            Log.d(tag, "close()/explain_from_menu: ${getSharedPreferences("explain", MODE_PRIVATE).getInt("explain_from_menu", 0)}")
            Log.d(tag, "close()/explain: ${getSharedPreferences("explain", MODE_PRIVATE).getInt("explain", 0)}")

            finish()
        }
    }
}