package com.chat_soon_e.re_chat.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.chat_soon_e.re_chat.ApplicationClass.Companion.ACTIVE
import com.chat_soon_e.re_chat.ApplicationClass.Companion.mSharedPreferences
import com.chat_soon_e.re_chat.data.entities.User
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.data.remote.USER_ID
import com.chat_soon_e.re_chat.databinding.ActivitySplashBinding
import com.chat_soon_e.re_chat.ui.explain.ExplainActivity
import com.chat_soon_e.re_chat.utils.permissionGrantred
import com.chat_soon_e.re_chat.utils.saveID
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient

@SuppressLint("CustomSplashScreen")
class SplashActivity: AppCompatActivity() {
    private val tag = "ACT/SPLASH"
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        // kakao API를 통해 hash key를 얻을 수 있다.
//        val hashKey = Utility.getKeyHash(this)
//        Log.d(tag, "hash key: $hashKey")

        // defaultValue = 0, 설명창 보임 = 1, 더 이상 보이지 않음 = 2
        mSharedPreferences = getSharedPreferences("explain", MODE_PRIVATE)
        val isExplain = mSharedPreferences.getInt("explain", 0)

        if(isExplain ==0 || isExplain == 1) {
            val intent = Intent(this@SplashActivity, ExplainActivity::class.java)
            startActivity(intent)
        } else if(isExplain == 2) {
            if(!permissionGrantred(this)) {
                val intent = Intent(this@SplashActivity, PermissionActivity::class.java)
                startActivity(intent)
            }
        }

        loginPermission()

        binding.splashKakaoBtn.setOnClickListener {
            if(binding.splashKakaoBtn.isVisible) {
                login()
            }
        }

        binding.splashStartBtn.setOnClickListener {
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 로그인이 되었다면 로그인은 안뜨게 == O
        // 데이터 다운이 완료되면 시작하기 버튼 활성화 == X
    }

    // Token 존재 확인, 즉 로그인 확인
    private fun loginPermission(){
        if (AuthApiClient.instance.hasToken()) {
            // Token 유효성 검증
            UserApiClient.instance.accessTokenInfo { _, error ->
                if (error != null) {
                    // 로그인 필요
                    if (error is KakaoSdkError && error.isInvalidTokenError()) {
//                        binding.splashKakaoIv.visibility = View.VISIBLE
                        binding.splashKakaoBtn.visibility = View.VISIBLE
                    }
                    // 기타 에러
                    else {
                        Log.d(tag, error.message.toString())
                    }
                }
                // 토큰 유효성 체크 성공 (필요 시 토큰 갱신됨)
                else {
                    Log.d(tag, "토큰 유효")
                    Log.d(tag, "user id in loginPermission(): $USER_ID")
                    binding.splashKakaoBtn.visibility = View.INVISIBLE
//                    binding.splashKakaoIv.visibility = View.INVISIBLE
                }
            }
        }
        // 토큰 없음 (로그아웃 혹은 연결 끊김)
        else {
//            binding.splashKakaoIv.visibility=View.VISIBLE
            binding.splashKakaoBtn.visibility=View.VISIBLE
        }
    }

    // 카카오계정 로그인
    private fun login(){
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(tag, "카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                Log.i(tag, "카카오계정으로 로그인 성공 ${token.accessToken}")
//                binding.splashKakaoIv.visibility = View.INVISIBLE
                runOnUiThread {
                    binding.splashKakaoBtn.visibility = View.INVISIBLE
                }
                saveUserInfo("login")
            }
        }

        // 카카오톡 로그인 가능하다면 카카오톡으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e(tag, "카카오톡으로 로그인 실패", error)
                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    Log.i(tag, "카카오톡으로 로그인 성공 ${token.accessToken}")
//                    binding.splashKakaoIv.visibility=View.INVISIBLE
                    runOnUiThread {
                        binding.splashKakaoBtn.visibility = View.INVISIBLE
                    }
                    saveUserInfo("login")
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    // 로그아웃
    private fun logout(){
        UserApiClient.instance.logout { error->
            if(error!=null)
                Log.d(tag, "로그아웃 실패")
            else{
                val user=AppDatabase.getInstance(this)!!.userDao()
            }
        }
    }

    // 탈퇴: 계정 연결 끊기
    private fun withdraw() {
        saveUserInfo("withdraw")
        UserApiClient.instance.unlink { error ->
            if (error != null)
                Log.e(tag, "연결끊기 실패", error)
            else {
                Log.d(tag, "연결 끊기 성공")
            }
        }
    }

    // User 정보 업데이트 및 생성
    private fun saveUserInfo(state: String){
        UserApiClient.instance.me { user, error ->
            if (error != null){
                Log.d(tag, "사용자 정보 가져오기 실패")
            } else {
                if (user != null) {
                    val database = AppDatabase.getInstance(this)!!
                    val dao = database.userDao()
                    if(state == "login"){

                        // id 암호화(encrypted 사용) 후 spf 저장, 일단은 그냥 local 사용해 저장
                        // ----------------------------------
                        USER_ID = user.id
                        saveID(user.id)

                        Log.d(tag, "user id: ${user.id}")
                        // ----------------------------------

                        val users = dao.getUser(user.id)
                        if(users == null) {
                            // 유저 인포 저장
                            dao.insert(User(user.id, user.kakaoAccount?.profile?.nickname.toString(), user.kakaoAccount?.email.toString(), ACTIVE))

                        } else {
                            if(users.status=="delete")
                            // 유저 인포 업데이트
                            dao.update(User(user.id, user.kakaoAccount?.profile?.nickname.toString(), user.kakaoAccount?.email.toString(), ACTIVE))
                        }
                    }
                    // 로그아웃 시
                    else if(state == "logout") {
                        saveID(-1)
                        // dao.updateStatus(user.id, "inactivate")
                    }
                    // 탈퇴 시
                    else if(state == "withdraw")
                        saveID(-1)
                    // dao.updateStatus(user.id, "delete")
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }
}