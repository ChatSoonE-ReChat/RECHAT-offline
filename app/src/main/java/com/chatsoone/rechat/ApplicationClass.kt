package com.chatsoone.rechat

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import com.chatsoone.rechat.ui.ChatViewModel
import retrofit2.Retrofit
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// 앱에서 공통적으로 쓰이는 부분들을 정의해놓은 클래스
// AndroidManifest.xml에서 가장 먼저 실행되도록 해놓았다.
// 따라서 다른 파일에서도 이 안에 정의된 상수, 변수, 메소드 등을 전체적으로 가져와서 활용할 수 있다.
class ApplicationClass : Application() {
    companion object {
        const val USER_INFO: String = "USER_LOGIN_INFO"
        var count: Int = 0

        // log
        const val TAG: String = "RE:CHAT-APP"
        const val ACT: String = "ACT/"
        const val FRAG: String = "FRAG/"

        const val APP_DATABASE = "$TAG-DB"

//        const val BASE_URL: String = DEV_URL    // apk 추출할 때 알맞게 바꾸면[넣어주면] 된다.

        // DB: status
        const val ACTIVE: String = "ACTIVE"
        const val INACTIVE: String = "INACTIVE"
        const val BLOCKED: String = "BLOCKED"
        const val DELETED: String = "DELETED"
        const val HIDDEN: String = "HIDDEN"

        lateinit var mSharedPreferences: SharedPreferences

        // 캐시 저장소로부터 이미지 불러오기
        // 추후 진짜 캐시를 사용해 볼 것
        fun loadBitmap(name: String, context: Context): Bitmap? {
            val file = File(context.cacheDir.toString())
            val files = file.listFiles()
            var list: String = ""
            for (tempFile in files) {
                //Log.d("MyTag", tempFile.name)
                //name이 들어가 있는 파일 찾기
                if (tempFile.name.contains(name)) {
                    list = tempFile.name
                }
            }
            val path = context.cacheDir.toString() + "/" + list
            val bitmap = BitmapFactory.decodeFile(path)
            return bitmap
        }

        // simpleDataFormat 사용할 것
        @RequiresApi(Build.VERSION_CODES.O)
        fun dateToString(date: Date): String {
            //오늘이 아니라면 날짜만
            var str = ""
            val today = Date()
            if (date.year == today.year && date.month == today.month && date.date == today.date) {
                val time = SimpleDateFormat("a hh:mm")
                str = time.format(date).toString()
            } else {
                //simpleDateFormat은 thread에 안전하지 않습니다.
                //DateTimeFormatter을 사용합시다. 아! Date를 LocalDate로도 바꿔야합니다!
                //val time_formatter=DateTimeFormatter.ofPattern("MM월 dd일")
                //date.format(time_formatter)
                val time = SimpleDateFormat("MM월 DD일")
                str = time.format(date).toString()
            }
            return str
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate() {
        super.onCreate()
        mSharedPreferences = applicationContext.getSharedPreferences(TAG, Context.MODE_PRIVATE)
    }
}