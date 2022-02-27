package com.chat_soon_e.re_chat.ui

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import com.chat_soon_e.re_chat.ApplicationClass.Companion.ACTIVE
import com.chat_soon_e.re_chat.data.entities.Chat
import com.chat_soon_e.re_chat.data.entities.OtherUser
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.utils.getID
import com.chat_soon_e.re_chat.utils.saveID
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.M)
class MyNotificationListener: NotificationListenerService() {
    private lateinit var database: AppDatabase

    private var userID = getID()
    private val tag = "MYNOTIFICATION"

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(tag, "onListenerConnected()")
        Log.d(tag, "userID: $userID")
    }

    // 새로운 알림 올 때마다 발생한다.
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if(userID.toInt( )== -1) {
            if(AppDatabase.getInstance(this)!!.userDao().getUsers() == null)
                Log.d(tag, "login error, 잘못된 접근")
            else {
                val data=AppDatabase.getInstance(this)!!.userDao().getUsers()
                userID = if(data == null) {
                    saveID(-1L)//오류 났을시 임시로 해주는 것
                    getID()
                }else{
                    saveID(data[0].kakaoUserIdx)
                    getID()
                }
            }
        }

        super.onNotificationPosted(sbn)
        val notification: Notification = sbn.notification
        val packageName: String = sbn.packageName

        if(packageName != null && packageName == "com.kakao.talk") {
            // 데이터베이스 연결
            database = AppDatabase.getInstance(this)!!
            val extras = sbn.notification.extras
            val name = extras.getString(Notification.EXTRA_TITLE)   // 발신자
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)  // 내용
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)   // 그룹톡일 경우

            // postTime, mill -> Date 변환
            // server에 저장할 때 ISO 8601 형식의 Date를 보내줘야 한다.
            val millisecond = sbn.postTime
            val date = Date(millisecond)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.KOREAN)
            val dateAsString = simpleDateFormat.format(date)
            val dateAsDate = simpleDateFormat.parse(dateAsString)

            Log.d(tag, "millisecond: $millisecond")     // 1644656599649 형식
            Log.d(tag, "date: $date")                   // Sat Feb 12 18:03:19 GMT+09:00 2022 형식 (이렇게 넣으면 서버에서 null 처리)
            Log.d(tag, "dateAsString: $dateAsString")   // 2022-02-12T18:04:28+09:00 형식 (이렇게 넣어야 서버에서 제대로 날짜를 인식, 따라서 String 형식으로 보내줘야 한다.)
            Log.d(tag, "dateAsDate: $dateAsDate")       // Sat Feb 12 18:07:14 GMT+09:00 2022 형식 (이렇게 넣으면 서버에서 null 처리)

            // Icon, cache에 png 전환 후 저장
            val largeIcon: Icon? = notification.getLargeIcon()

            // 알림 메세지(264개의 메세지 등) 제외 대화 내용 DB 저장
            // 음악 메세지(id==2016) 차단
            if(name!=null && sbn.id!=2016){
                val otherUser = database.otherUserDao().getOtherUserByNameId(name.toString(), userID)

                // 이미 있던 유저인지 확인
                if(otherUser != null) {
                    val chat = Chat(otherUser.otherUserIdx, subText.toString(),text.toString(), dateAsString, -1, ACTIVE)

                    // 이미 있던 유저라면
                    var blocked: String?
                    if(subText == null) {
                        // 갠톡이라면
                        if(database.otherUserDao().checkOneBlock(userID, otherUser.otherUserIdx) == null) {
                            database.chatDao().insert(chat)
                        }
                    } else{
                        // 단톡이라면
                        if(database.otherUserDao().checkOrgBlock(userID, subText.toString()) == null) {
                            database.chatDao().insert(chat)
                        }
                    }
                }
                else{
                    // 새로운 유저라면 일단 그냥 넣기, 차단이 됬는지 안됬는지 어떻게 알아? 새로운 유저인데!
                    var fileName = ""
                    val other: OtherUser

                    if(largeIcon != null) {
                        fileName = saveCache(convertIconToBitmap(largeIcon), name + "_" + millisecond.toString())
                        database.otherUserDao().insert(OtherUser(name.toString(), fileName, ACTIVE, userID))
                        other = database.otherUserDao().getOtherUserByNameId(name.toString(), userID)!!
                        database.chatDao().insert(Chat(other.otherUserIdx, subText.toString(),text.toString(), dateAsString, -1, ACTIVE))
                    } else {
                        database.otherUserDao().insert(OtherUser(name.toString(), null, ACTIVE, userID))
                        other = database.otherUserDao().getOtherUserByNameId(name.toString(), userID)!!
                        database.chatDao().insert(Chat(other.otherUserIdx, subText.toString(),text.toString(), dateAsString, -1, ACTIVE))
                    }
                }
            }

            Log.d(tag, "onNotificationPosted ~ " +
                        " id: " + sbn.id +
                        " name: " + name +
                        " text : " + text +
                        "subtext: "+ subText +
                        "postTime: " + date.toString())
        }

    }

    //Icon을 Bitmap으로 전환
    private fun convertIconToBitmap(icon: Icon): Bitmap {
        val drawable = icon.loadDrawable(this)
        return drawable.toBitmap()
    }

    // Bitmap을 캐시 디렉토리에 저장, 파일 이름 저장
    private fun saveCache(bitmap: Bitmap, name: String): String {
        val storage = cacheDir // cacheDir 경로
        val tempFile = File(storage, name)
        try {
            tempFile.createNewFile()
            val out = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.close()

        } catch (e: FileNotFoundException) {
            Log.e("MyTag","FileNotFoundException : " + e.message);
        } catch (e: IOException) {
            Log.e("MyTag", "IOException : " + e.message)
        }
        return name
    }
}