package com.chat_soon_e.re_chat.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.chat_soon_e.re_chat.ApplicationClass
import com.chat_soon_e.re_chat.R
import com.chat_soon_e.re_chat.data.entities.ChatList
import com.chat_soon_e.re_chat.data.entities.Folder
import com.chat_soon_e.re_chat.data.entities.Icon
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.databinding.ActivityMainBinding
import com.chat_soon_e.re_chat.ui.explain.ExplainActivity
import com.chat_soon_e.re_chat.utils.getID
import com.chat_soon_e.re_chat.utils.permissionGrantred
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView

class MainActivity: NavigationView.OnNavigationItemSelectedListener, AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase
    private lateinit var mainRVAdapter: MainRVAdapter
    private lateinit var mPopupWindow: PopupWindow
    private val lockViewModel: LockViewModel by viewModels()

    private var iconList = ArrayList<Icon>()
    private var folderList = ArrayList<Folder>()
    private var chatList = ArrayList<ChatList>()
    private var permission: Boolean = true
    private val chatViewModel: ChatViewModel by viewModels()
    private var userID = getID()
    private val tag = "ACT/MAIN"
    private var correct=0;

    // 광고
    lateinit var mAdview: AdView
    lateinit var adRequest: AdRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate()/userID: $userID")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initIcon()
        initFolder()
        lockViewModel.setMode(0)
    }

    // 광고 초기화
    private fun initAds() {
        MobileAds.initialize(this)
        val headerView = binding.mainNavigationView.getHeaderView(0)
        mAdview = headerView.findViewById<AdView>(R.id.adViews)
        adRequest = AdRequest.Builder().build()
        mAdview.loadAd(adRequest)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        database = AppDatabase.getInstance(this)!!
        initAds()
        initHiddenFolder()

        initNavi()
        initDrawerLayout()
        initClickListener()
    }

    // 아이콘 초기화
    private fun initIcon() {
        database = AppDatabase.getInstance(this)!!
        iconList = database.iconDao().getIconList() as ArrayList

        // 이 부분은 서버와 통신하지 않고 자체적으로 구현
        if (iconList.isEmpty()) {
            database.iconDao().insert(Icon(R.drawable.chatsoon01))
            database.iconDao().insert(Icon(R.drawable.chatsoon02))
            database.iconDao().insert(Icon(R.drawable.chatsoon03))
            database.iconDao().insert(Icon(R.drawable.chatsoon04))
            database.iconDao().insert(Icon(R.drawable.chatsoon05))
            database.iconDao().insert(Icon(R.drawable.chatsoon06))
            database.iconDao().insert(Icon(R.drawable.chatsoon06))
            database.iconDao().insert(Icon(R.drawable.chatsoon07))
            database.iconDao().insert(Icon(R.drawable.chatsoon08))
            database.iconDao().insert(Icon(R.drawable.chatsoon09))
            database.iconDao().insert(Icon(R.drawable.chatsoon10))
            database.iconDao().insert(Icon(R.drawable.chatsoon11))
            database.iconDao().insert(Icon(R.drawable.chatsoon12))
            database.iconDao().insert(Icon(R.drawable.chatsoon13))
            database.iconDao().insert(Icon(R.drawable.chatsoon14))
            database.iconDao().insert(Icon(R.drawable.chatsoon15))
            database.iconDao().insert(Icon(R.drawable.chatsoon16))
            iconList = database.iconDao().getIconList() as ArrayList
        }
    }

    // 폴더 초기화
    private fun initFolder() {
        database = AppDatabase.getInstance(this)!!

        // 폴더 초기 세팅 (구르미 하나, 구르미 둘)
        val folderCount = database.folderDao().getFolderCount(userID)
        Log.d(tag, "folderCount: $folderCount")
        if (folderCount == 0) {
            database.folderDao().insert(Folder(userID, "구르미 하나", R.drawable.folder_default))
            database.folderDao().insert(Folder(userID, "구르미 둘", R.drawable.folder_default))
        }

        database.folderDao().getFolderList(userID).observe(this) {
            Log.d(tag, "folderList: $folderList")
            folderList = it as ArrayList<Folder>
        }
    }


    // 설정 메뉴 창을 띄우는 DrawerLayout
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initDrawerLayout() {

        binding.mainNavigationView.setNavigationItemSelectedListener(this)
        val menuItem = binding.mainNavigationView.menu.findItem(R.id.navi_setting_alarm_item)
        val drawerSwitch =
            menuItem.actionView.findViewById(R.id.main_drawer_alarm_switch) as SwitchCompat

        // 알림 권한 허용 여부에 따라 스위치(토글) 초기 상태 지정
        if (permissionGrantred(this)) {
            // 알림 권한이 허용되어 있는 경우
            drawerSwitch.toggle()
            drawerSwitch.isChecked = true
            permission = true
        } else {
            // 알림 권한이 허용되어 있지 않은 경우
            drawerSwitch.isChecked = false
            permission = false
        }

        drawerSwitch.setOnClickListener {
            if (drawerSwitch.isChecked) {
                // 알림 권한을 허용했을 때
                permission = true
                Log.d(tag, "toggleListener/is Checked")
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                if (permissionGrantred(this)) {
                    Toast.makeText(this, "알림 권한을 허용합니다.", Toast.LENGTH_SHORT).show()
                    Log.d("serviceStart", "inPermission")
                    startForegroundService(Intent(this, MyNotificationListener::class.java))
                }

            } else {
                // 알림 권한을 허용하지 않았을 때
                permission = false
                Log.d(tag, "toggleListener/is not Checked")
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                if (!permissionGrantred(this)) {
                    stopService(Intent(this, MyNotificationListener::class.java))
                    Toast.makeText(this, "알림 권한을 허용하지 않습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 설정 메뉴 창의 네비게이션 드로어의 아이템들에 대한 이벤트를 처리
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 설정 메뉴창의 아이템(목록)들을 클릭했을 때
            // 알림 설정
            R.id.navi_setting_alarm_item -> {
                Toast.makeText(this, "알림 설정", Toast.LENGTH_SHORT).show()
            }

            // 패턴 변경하기
            R.id.navi_setting_pattern_item -> {
                val lockSPF = getSharedPreferences("lock", 0)
                val pattern = lockSPF.getString("pattern", "0")

                // 앱 삭제할때 같이 DB 저장 X
                // 패턴 모드 설정
                // 0: 숨긴 폴더 목록을 확인하기 위한 입력 모드
                // 1: 메인 화면의 설정창 -> 변경 모드
                // 2: 폴더 화면의 설정창 -> 변경 모드
                // 3: 메인 화면 폴더로 보내기 -> 숨김 폴더 눌렀을 경우
                val modeSPF = getSharedPreferences("mode", 0)
                val editor = modeSPF.edit()
                editor.putInt("mode", 1)
                editor.apply()

                if (pattern.equals("0")) {   // 패턴이 설정되어 있지 않은 경우 패턴 설정 페이지로
                    val intent = Intent(this@MainActivity, CreatePatternActivity::class.java)
                    startActivity(intent)
                } else {    // 패턴이 설정되어 있는 경우 입력 페이지로 (보안을 위해)
                    val intent = Intent(this@MainActivity, InputPatternActivity::class.java)
                    startActivity(intent)
                }
            }
            // 사용 방법 도움말
            R.id.navi_setting_helper_item -> {
//                Toast.makeText(this, "사용 방법 도움말", Toast.LENGTH_SHORT).show()
                ApplicationClass.mSharedPreferences = getSharedPreferences("explain", MODE_PRIVATE)
                val editor = ApplicationClass.mSharedPreferences.edit()
                editor.putInt("explain_from_menu", 1)
                editor.apply()

                val intent = Intent(this, ExplainActivity::class.java)
                startActivity(intent)
            }

            // 개인정보 처리방침
            R.id.navi_setting_privacy_item -> {
                val intent = Intent(this, PrivacyInformationActivity::class.java)
                startActivity(intent)
//                Toast.makeText(this, "개인정보 처리방침", Toast.LENGTH_SHORT).show()
            }

//            else -> Toast.makeText(this, "잘못된 항목입니다.", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    // 드로어가 나와있을 때 뒤로 가기 버튼을 한 경우 뒤로 가기 버튼에 대한 이벤트를 처리
    override fun onBackPressed() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawers()
        } else if (chatViewModel.mode.value == 1) {
            chatViewModel.setMode(mode = 0)
        } else {
            super.onBackPressed()
        }
    }

    private fun initNavi(){
        binding.mainContent.mainMyFolderIv.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_fr, MyFolderFragment())
                .commitAllowingStateLoss()
        }
        binding.mainContent.myFolderToHiddenFolderIv.setOnClickListener{

            Toast.makeText(this, "숨김폴더다", Toast.LENGTH_SHORT).show()
            val lockSPF = getSharedPreferences("lock", 0)
            val pattern = lockSPF.getString("pattern", "0")

            // 패턴 모드 설정
            // 0: 숨긴 폴더 목록을 확인하기 위한 입력 모드
            // 1: 메인 화면의 설정창 -> 변경 모드
            // 2: 폴더 화면의 설정창 -> 변경 모드
            // 3: 메인 화면 폴더 리스트에서 숨김 폴더 클릭 시
            val modeSPF = getSharedPreferences("mode", 0)
            val editor = modeSPF.edit()

            // 여기서는 0번 모드
            editor.putInt("mode", 0)
            editor.apply()

            if(pattern.equals("0")) {   // 패턴이 설정되어 있지 않은 경우 패턴 설정 페이지로
                Toast.makeText(this, "패턴이 설정되어 있지 않습니다.\n패턴을 설정해주세요.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@MainActivity, CreatePatternActivity::class.java))
            } else {
                startActivity(Intent(this@MainActivity, InputPatternActivity::class.java))
            }

            // 올바른 패턴인지 확인
            // 1: 올바른 패턴
            // 2: 올바르지 않은 패턴
            initHiddenFolder()
        }
        binding.mainContent.mainBlockListIv.setOnClickListener {
            replaceFragment(BlockListFragment())
        }
    }

    private fun initHiddenFolder() {
        val spf = getSharedPreferences("lock_correct", 0)
        if (spf.getInt("correct", 0) == 1)
            replaceFragment(MyHiddenFolderFragment())
        else if (spf.getInt("correct", 0) == -1)
            replaceFragment(MyFolderFragment())
        else
            replaceFragment(BlockListFragment())
    }

    private fun replaceFragment(fragment:Fragment){
        this.supportFragmentManager.beginTransaction()
            .replace(R.id.main_fr, fragment).commitAllowingStateLoss()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initClickListener() {
        // 내폴더 아이콘 클릭시 폴더 화면으로 이동
//        binding.mainContent.mainMyFolderIv.setOnClickListener {
//            val intent = Intent(this@MainActivity, MyFolderActivity::class.java)
//            startActivity(intent)
//            Log.d(tag, "내폴더 아이콘 클릭")
//        }

//        binding.mainContent.mainBlockListIv.setOnClickListener {
//            // 차단
//            val chatList = mainRVAdapter.getSelectedItem()
//            for (i in chatList) {
//                if (i.groupName != "null")  // 그룹
//                    i.groupName?.let { it1 -> database.chatDao().blockOrgChat(userID, it1) }
//                else {
//                    // 개인
//                    database.chatDao().blockOneChat(userID, i.groupName!!)
//                }
//            }
//        }

        // 설정 메뉴창을 여는 메뉴 아이콘 클릭시 설정 메뉴창 열리도록
        binding.mainContent.mainSettingMenuIv.setOnClickListener {
            if (!binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                // 설정 메뉴창이 닫혀있을 때
                mAdview.loadAd(adRequest)
                binding.mainDrawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

}
