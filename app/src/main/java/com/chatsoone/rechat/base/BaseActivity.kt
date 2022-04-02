package com.chatsoone.rechat.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.chatsoone.rechat.R
import com.chatsoone.rechat.utils.getID

abstract class BaseActivity<VB : ViewBinding>(private val inflate: (LayoutInflater) -> VB) :
    AppCompatActivity() {
    protected lateinit var binding: VB private set
    private var imm: InputMethodManager? = null
    var userID = getID()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        setContentView(binding.root)
        imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        afterOnCreate()
    }

    protected abstract fun afterOnCreate()

    // Toast message를 message만 넣어서 불러오도록
    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // 이전 Activity는 남기고, 다음 activity를 켜주는 역할
    fun startNextActivity(activity: Class<*>?) {
        val intent = Intent(this, activity)
        startActivity(intent)
    }

    // 이전 Activity를 모두 날려주고, 다음 activity를 켜주는 역할
    fun startActivityWithClear(activity: Class<*>?) {
        val intent = Intent(this, activity)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    // 키보드 숨기기
    // 디바이스에서 키보드가 뜨는 것을 자동적으로 숨겨주는 역할
    fun hideKeyboard(v: View) {
        imm?.hideSoftInputFromWindow(v.windowToken, 0)
    }

    // 프래그먼트 교체
    fun changeFragmentOnMain(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame_layout, fragment)
            .commitAllowingStateLoss()
    }
}