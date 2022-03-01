package com.chat_soon_e.re_chat.ui

import android.content.Intent
import android.graphics.Insets
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.view.marginBottom
import com.chat_soon_e.re_chat.databinding.ActivityPermissionBinding
import com.chat_soon_e.re_chat.utils.permissionGrantred

class PermissionActivity:BaseActivity<ActivityPermissionBinding>(ActivityPermissionBinding::inflate) {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun initAfterBinding() {

//        // 페이지 사용 여부 체크
//        val spf=this.getSharedPreferences("firstRun", AppCompatActivity.MODE_PRIVATE)
//        val editor=spf.edit()
//        editor.putInt("check", 1)//이 페이지 사용했다는 정보 남기기, 아니면 체크박스로 더이상 보지 않기 하기?
//        editor.apply()

        // 권한 얻기 버튼
        binding.notificationPermissionBtn.setOnClickListener {
            startActivity(Intent( "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            // 권한을 얻었다면 서비스 시작
            if(permissionGrantred(this))
                Log.d("serviceStart", "inPermission")
                startForegroundService(Intent(this, MyNotificationListener::class.java))
            finish()
        // startActivityWithClear(SplashActivity::class.java)
        }

        initImageSize()
    }

    private fun initImageSize() {
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.6f).toInt()
        val height = (size.y * 0.3f).toInt()

        binding.permissionBackgroundImgIv.maxWidth = width
        binding.permissionBackgroundImgIv.maxHeight = height
    }

    // 디바이스 크기에 사이즈를 맞추기 위한 함수
    private fun WindowManager.currentWindowMetricsPointCompat(): Point {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val windowInsets = currentWindowMetrics.windowInsets
            var insets: Insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())

            windowInsets.displayCutout?.run {
                insets = Insets.max(insets, Insets.of(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom))
            }

            val insetsWidth = insets.right + insets.left
            val insetsHeight = insets.top + insets.bottom
            Point(currentWindowMetrics.bounds.width() - insetsWidth, currentWindowMetrics.bounds.height() - insetsHeight)
        } else {
            Point().apply {
                defaultDisplay.getSize(this)
            }
        }
    }
}