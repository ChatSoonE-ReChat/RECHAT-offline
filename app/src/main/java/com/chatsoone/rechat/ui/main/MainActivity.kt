package com.chatsoone.rechat.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.chatsoone.rechat.ui.LockViewModel
import com.chatsoone.rechat.ApplicationClass
import com.chatsoone.rechat.ApplicationClass.Companion.ACT
import com.chatsoone.rechat.R
import com.chatsoone.rechat.data.entity.ChatList
import com.chatsoone.rechat.data.entity.Folder
import com.chatsoone.rechat.data.entity.Icon
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.databinding.ActivityMainBinding
import com.chatsoone.rechat.ui.ChatViewModel
import com.chatsoone.rechat.MyNotificationListener
import com.chatsoone.rechat.ui.explain.ExplainActivity
import com.chatsoone.rechat.ui.main.blocklist.BlockListFragment
import com.chatsoone.rechat.ui.main.folder.MyFolderFragment
import com.chatsoone.rechat.ui.main.hiddenfolder.MyHiddenFolderFragment
import com.chatsoone.rechat.ui.main.home.HomeFragment
import com.chatsoone.rechat.ui.pattern.CreatePatternActivity
import com.chatsoone.rechat.ui.pattern.InputPatternActivity
import com.chatsoone.rechat.ui.setting.PrivacyInformationActivity
import com.chatsoone.rechat.utils.getID
import com.chatsoone.rechat.utils.permissionGrantred
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView

class MainActivity : NavigationView.OnNavigationItemSelectedListener, AppCompatActivity() {
    private var userID = getID()
    private var permission: Boolean = true
    private val chatViewModel: ChatViewModel by viewModels()
    private val lockViewModel: LockViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    lateinit var database: AppDatabase
    lateinit var iconList: ArrayList<Icon>
    lateinit var chatList: ArrayList<ChatList>
    lateinit var folderList: ArrayList<Folder>

    // 광고
    private lateinit var mAdview: AdView
    lateinit var adRequest: AdRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getInstance(this)!!

        initIcon()
        initFolder()
        lockViewModel.setMode(0)
    }

    override fun onStart() {
        super.onStart()
        initAds()
        initHiddenFolder()

        initBottomNavigationView()
        initDrawerLayout()
        initClickListener()
    }

    // 광고 초기화
    private fun initAds() {
        MobileAds.initialize(this)
        val headerView = binding.mainNavigationView.getHeaderView(0)
        mAdview = headerView.findViewById<AdView>(R.id.adViews)
        adRequest = AdRequest.Builder().build()
        mAdview.loadAd(adRequest)
    }

    // 설정 메뉴
    private fun initDrawerLayout() {
        binding.mainNavigationView.setNavigationItemSelectedListener(this)
        val menuItem = binding.mainNavigationView.menu.findItem(R.id.navi_setting_alarm_item)
        val drawerSwitch =
            menuItem.actionView.findViewById(R.id.main_drawer_alarm_switch) as SwitchCompat

        // 알림 권한 허용 여부에 따라 스위치 초기 상태 지정
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
                Log.d(ACT, "MAIN/drawerSwitch.isChecked == true")
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))

                if (permissionGrantred(this)) {
                    Toast.makeText(this, "알림 권한을 허용합니다.", Toast.LENGTH_SHORT).show()
                    Log.d(ACT, "MAIN/inPermission")
                    startForegroundService(Intent(this, MyNotificationListener::class.java))
                }
            } else {
                // 알림 권한을 허용하지 않았을 때
                permission = false
                Log.d(ACT, "MAIN/drawerSwitch.isChecked == false")
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                if (!permissionGrantred(this)) {
                    stopService(Intent(this, MyNotificationListener::class.java))
                    Toast.makeText(this, "알림 권한을 허용하지 않습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // bottom navigation view 초기화
    private fun initBottomNavigationView() {
        Log.d(ACT, "MAIN/initBottomNavigationView")

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame_layout, HomeFragment())
            .commitAllowingStateLoss()

        binding.mainLayout.mainBnv.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.main_bnv_home -> {
                    // 전체 채팅
                    replaceFragment(HomeFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.main_bnv_block_list -> {
                    // 차단 목록
                    replaceFragment(BlockListFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.main_bnv_folder -> {
                    // 보관함
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frame_layout, MyFolderFragment())
                        .commitAllowingStateLoss()

                    return@setOnItemSelectedListener true
                }

                R.id.main_bnv_hidden_folder -> {
                    // 숨긴 보관함
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
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    // 아이콘 초기화
    private fun initIcon() {
        iconList = database.iconDao().getIconList() as ArrayList

        if (iconList.isEmpty()) {
            // 아이콘 목록 추가
            // database.iconDao().insert(Icon())
            // iconList = database.iconDao().getIconList() as ArrayList
        }
    }

    // 폴더 초기화
    private fun initFolder() {
        database.folderDao().getFolderList(userID).observe(this) {
            Log.d(ACT, "MAIN/folderList: $folderList")
            folderList = it as ArrayList<Folder>
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

    // click listener 초기화
    private fun initClickListener() {
        Log.d(ACT, "MAIN/initClickListener")

        // 설정 메뉴 아이콘 클릭했을 때 설정 메뉴 뜨도록
        binding.mainLayout.mainSettingMenuIv.setOnClickListener {
            Log.d(ACT, "MAIN/open setting menu")
            if (!binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
//                mAdview.loadAd(adRequest)
                binding.mainDrawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // 설정 메뉴창에 있는 메뉴 아이콘 클릭했을 때 설정 메뉴 닫히도록
        val headerView = binding.mainNavigationView.getHeaderView(0)
        headerView.findViewById<ImageView>(R.id.main_drawer_setting_menu_iv).setOnClickListener {
            Log.d(ACT, "MAIN/close setting menu")
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    // 설정 메뉴
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d(ACT, "MAIN/onNavigationItemSelected")

        // 설정 메뉴창의 아이템 클릭했을 때
        when (item.itemId) {

            // 패턴 변경하기
            R.id.navi_setting_pattern_item -> {
                val lockSPF = getSharedPreferences("lock", 0)
                val pattern = lockSPF.getString("pattern", "0")

                // 패턴 모드 설정
                // 0: 숨긴 폴더 목록을 확인하기 위한 입력 모드
                // 1: 메인 화면의 설정창 -> 변경 모드
                // 2: 폴더 화면의 설정창 -> 변경 모드
                // 3: 메인 화면 폴더로 보내기 -> 숨김 폴더 눌렀을 경우
                val modeSPF = getSharedPreferences("mode", 0)
                val editor = modeSPF.edit()
                editor.putInt("mode", 1)
                editor.apply()

                if (pattern.equals("0")) {
                    // 패턴이 설정되어 있지 않은 경우 패턴 설정 페이지로
                    val intent = Intent(this@MainActivity, CreatePatternActivity::class.java)
                    startActivity(intent)
                } else {
                    // 패턴이 설정되어 있는 경우 입력 페이지로 (보안을 위해)
                    val intent = Intent(this@MainActivity, InputPatternActivity::class.java)
                    startActivity(intent)
                }
            }

            // 사용 방법 도움말
            R.id.navi_setting_helper_item -> {
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
            }
        }
        return false
    }

    // 프래그먼트 교체
    private fun replaceFragment(fragment: Fragment){
        this.supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame_layout, fragment).commitAllowingStateLoss()
    }

    // 뒤로 가기 버튼 눌렀을 때
    override fun onBackPressed() {
        super.onBackPressed()

        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawers()
            return
        }

        if (chatViewModel.mode.value == 1) {
            chatViewModel.setMode(0)
            return
        }
    }
}