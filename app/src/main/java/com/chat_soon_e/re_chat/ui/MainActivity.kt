package com.chat_soon_e.re_chat.ui

import android.annotation.SuppressLint
import android.util.Log
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.databinding.ActivityMainBinding
import android.content.Intent
import android.graphics.Insets
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import kotlin.collections.ArrayList
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.chat_soon_e.re_chat.ApplicationClass.Companion.HIDDEN
import com.chat_soon_e.re_chat.R
import com.chat_soon_e.re_chat.data.entities.*
import com.chat_soon_e.re_chat.data.remote.USER_ID
import com.chat_soon_e.re_chat.databinding.ItemFolderListBinding
import com.chat_soon_e.re_chat.utils.getID
import com.chat_soon_e.re_chat.utils.permissionGrantred
import com.chat_soon_e.re_chat.utils.saveID
import com.google.android.material.navigation.NavigationView

class MainActivity: NavigationView.OnNavigationItemSelectedListener, AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase
    private lateinit var mainRVAdapter: MainRVAdapter
    private lateinit var mPopupWindow: PopupWindow

    private var iconList = ArrayList<Icon>()
    private var folderList = ArrayList<Folder>()
    private var chatList = ArrayList<ChatList>()
    private var permission: Boolean = true
    private val chatViewModel: ChatViewModel by viewModels()
    private var userID = getID()
    private val tag = "ACT/MAIN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate()/userID: $userID")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initIcon()
        initFolder()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        database = AppDatabase.getInstance(this)!!

        if (userID.toInt() == -1) {
            if (AppDatabase.getInstance(this)!!.userDao().getUsers() == null)
                Log.d(tag, "login error, 잘못된 접근")
            else {
                val data = AppDatabase.getInstance(this)!!.userDao().getUsers()
                if (data == null)
                    saveID(-1L)//오류 났을시 임시로 해주는 것
                else
                    data[0].let { saveID(it.kakaoUserIdx) }

                userID = getID()
            }
        }
        Log.d(tag, "onStart()/userID: $userID, USER_ID: $USER_ID")

        initRecyclerView()
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

    // RecyclerView
    private fun initRecyclerView() {
//        // RecyclerView 구분선
//        val recyclerView = binding.mainContent.mainChatListRecyclerView
//        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
//        recyclerView.addItemDecoration(dividerItemDecoration)

        // LinearLayoutManager 설정, 새로운 데이터 추가 시 스크롤 맨 위로
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.mainContent.mainChatListRecyclerView.layoutManager = linearLayoutManager

        // RecyclerView Click Listener
        mainRVAdapter = MainRVAdapter(this, object : MainRVAdapter.MyItemClickListener {
            // 선택 모드
            override fun onChooseChatClick(view: View, position: Int) {
                // 해당 item이 선택되면 뷰 바꿔주기
                Log.d(
                    tag,
                    "initRecyclerView()/onChooseChatClick(): profileImg: ${mainRVAdapter.chatList[position].profileImg}"
                )
                mainRVAdapter.setChecked(position)
            }

            // 일반 모드 (= 이동 모드)
            @SuppressLint("RestrictedApi")
            override fun onDefaultChatClick(view: View, position: Int, chat: ChatList) {
                val spf = this@MainActivity.getSharedPreferences("chatAll", MODE_PRIVATE)
                val editor = spf.edit()
                editor.putInt("chatAll", 1)
                editor.apply()

                val intent = Intent(this@MainActivity, ChatActivity::class.java)
                // intent.putExtra("chatListJson", chatJson)
                intent.putExtra("chatListJson", chat)
                startActivity(intent)

                mainRVAdapter.clearSelectedItemList()

                // 눌렀을 경우 확인한 게 되므로 isNew = false(0)이 된다.
                val database = AppDatabase.getInstance(this@MainActivity)!!
                database.chatDao().updateIsNew(chatList[position].chatIdx, 0)
                database.chatListDao().updateIsNew(chatList[position].chatIdx, 0)
            }
        })

        // main chat list view model
        chatViewModel.mode.observe(this) {
            if (it == 0) {
                // 일반 모드 (= 이동 모드)
                mainRVAdapter.clearSelectedItemList()
                binding.mainContent.mainFolderIv.visibility = View.VISIBLE
                binding.mainContent.mainFolderModeIv.visibility = View.GONE
                binding.mainContent.mainCancelIv.visibility = View.GONE
                binding.mainContent.mainBlockListIv.visibility = View.VISIBLE
                binding.mainContent.mainDeleteIv.visibility = View.GONE
                binding.mainContent.mainMyFolderIv.visibility = View.VISIBLE
                binding.mainContent.mainBlockIv.visibility = View.GONE
                binding.mainContent.mainBlockListTv.text = "차단 목록"
                binding.mainContent.mainMyFolderTv.text = "보관함"
            } else {
                // 선택 모드
                mainRVAdapter.clearSelectedItemList()
                binding.mainContent.mainFolderIv.visibility = View.GONE
                binding.mainContent.mainFolderModeIv.visibility = View.VISIBLE
                binding.mainContent.mainCancelIv.visibility = View.VISIBLE
                binding.mainContent.mainBlockListIv.visibility = View.GONE
                binding.mainContent.mainDeleteIv.visibility = View.VISIBLE
                binding.mainContent.mainMyFolderIv.visibility = View.GONE
                binding.mainContent.mainBlockIv.visibility = View.VISIBLE
                binding.mainContent.mainBlockListTv.text = "차단"
                binding.mainContent.mainMyFolderTv.text = "삭제"
            }
            // 모든 데이터의 viewType 바꿔주기
            mainRVAdapter.setViewType(currentMode = it)
        }

        // 어댑터 연결
        binding.mainContent.mainChatListRecyclerView.adapter = mainRVAdapter

        // 최근 챗 목록 데이터 추가
        database = AppDatabase.getInstance(this)!!
        database.chatDao().getRecentChat(userID).observe(this) {
            Log.d(tag, "liveData: $it")
            mainRVAdapter.addItem(it)
//            chatList.addAll(chatList.size, it)
            chatList.clear()
            chatList.addAll(it)
            binding.mainContent.mainChatListRecyclerView.scrollToPosition(mainRVAdapter.itemCount - 1)
        }

        // 취소 버튼 클릭시 다시 초기 화면으로 (폴더 선택 모드 취소)
        binding.mainContent.mainCancelIv.setOnClickListener {
            // 현재 선택 모드 -> 일반 모드로 변경
//            mainRVAdapter.removeSelectedItemList()
            mainRVAdapter.clearSelectedItemList()
            chatViewModel.setMode(mode = 0)

            binding.mainContent.mainFolderIv.visibility = View.VISIBLE
            binding.mainContent.mainFolderModeIv.visibility = View.GONE
            binding.mainContent.mainCancelIv.visibility = View.GONE
            binding.mainContent.mainBackgroundView.visibility = View.INVISIBLE
            binding.mainContent.mainBlockIv.visibility = View.GONE
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

            // 공유하기
            R.id.navi_setting_share_item -> {
                Toast.makeText(this, "공유하기", Toast.LENGTH_SHORT).show()
            }

            // 앱 리뷰하기
            R.id.navi_setting_review_item -> {
                Toast.makeText(this, "앱 리뷰하기", Toast.LENGTH_SHORT).show()
            }

            // 이메일 문의
            R.id.navi_setting_email_item -> {
                Toast.makeText(this, "이메일 문의", Toast.LENGTH_SHORT).show()
            }

            // 사용 방법 도움말
            R.id.navi_setting_helper_item -> {
                Toast.makeText(this, "사용 방법 도움말", Toast.LENGTH_SHORT).show()
            }

            // 개인정보 처리방침
            R.id.navi_setting_privacy_item -> {
                Toast.makeText(this, "개인정보 처리방침", Toast.LENGTH_SHORT).show()
            }

            else -> Toast.makeText(this, "잘못된 항목입니다.", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    // 드로어가 나와있을 때 뒤로 가기 버튼을 한 경우 뒤로 가기 버튼에 대한 이벤트를 처리
    override fun onBackPressed() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawers()
        } else if(chatViewModel.mode.value == 1) {
            chatViewModel.setMode(mode = 0)
        } else {
            super.onBackPressed()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initClickListener() {
        // 내폴더 아이콘 클릭시 폴더 화면으로 이동
        binding.mainContent.mainMyFolderIv.setOnClickListener {
            val intent = Intent(this@MainActivity, MyFolderActivity::class.java)
            startActivity(intent)
            Log.d(tag, "내폴더 아이콘 클릭")
        }

        binding.mainContent.mainBlockListIv.setOnClickListener {
            // 차단
            val chatList = mainRVAdapter.getSelectedItem()
            for (i in chatList) {
                if (i.groupName != "null")  // 그룹
                    i.groupName?.let { it1 -> database.chatDao().blockOrgChat(userID, it1) }
                else {
                    // 개인
                    database.chatDao().blockOneChat(userID, i.groupName!!)
                }
            }
        }

        binding.mainContent.mainBlockListIv.setOnClickListener {
            startActivity(Intent(this@MainActivity, BlockListActivity::class.java))
            Log.d(tag, "차단하기로")
        }

        // 하단 중앙 아이콘 클릭시
        binding.mainContent.mainFolderIv.setOnClickListener {
            if (chatViewModel.mode.value == 0) {
                chatViewModel.setMode(mode = 1)
            } else {
                chatViewModel.setMode(mode = 0)
            }
            Log.d(tag, "mode 변경, ${chatViewModel.mode.value}")
        }

        // 폴더 이동 선택 모드 클릭시 팝업 메뉴
        binding.mainContent.mainFolderModeIv.setOnClickListener {
            popupWindowToFolderMenu()
        }

        // 선택 모드 시
        chatViewModel.mode.observe(this) {
            if (it == 1) {
                // 해당 chat 삭제
                binding.mainContent.mainDeleteIv.setOnClickListener {
                    mainRVAdapter.removeSelectedItemList()
                    Toast.makeText(this@MainActivity, "삭제하기", Toast.LENGTH_SHORT).show()

                    mainRVAdapter.clearSelectedItemList()
                    chatViewModel.setMode(mode = 0)

                    binding.mainContent.mainFolderIv.visibility = View.VISIBLE
                    binding.mainContent.mainFolderModeIv.visibility = View.GONE
                    binding.mainContent.mainCancelIv.visibility = View.GONE
                    binding.mainContent.mainBackgroundView.visibility = View.INVISIBLE
                    binding.mainContent.mainBlockIv.visibility = View.GONE
                }

                // 해당 chat 차단
                binding.mainContent.mainBlockIv.setOnClickListener {
                    mainRVAdapter.blockSelectedItemList()
                    Toast.makeText(this@MainActivity, "차단하기", Toast.LENGTH_SHORT).show()

                    mainRVAdapter.clearSelectedItemList()
                    chatViewModel.setMode(mode = 0)

                    binding.mainContent.mainFolderIv.visibility = View.VISIBLE
                    binding.mainContent.mainFolderModeIv.visibility = View.GONE
                    binding.mainContent.mainCancelIv.visibility = View.GONE
                    binding.mainContent.mainBackgroundView.visibility = View.INVISIBLE
                    binding.mainContent.mainBlockIv.visibility = View.GONE
                }
            }

            // 설정 메뉴창에 있는 메뉴 아이콘 클릭시 설정 메뉴창 닫히도록
            val headerView = binding.mainNavigationView.getHeaderView(0)
            headerView.setOnClickListener {
                binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
            }
        }

        // 설정 메뉴창을 여는 메뉴 아이콘 클릭시 설정 메뉴창 열리도록
        binding.mainContent.mainSettingMenuIv.setOnClickListener {
            if(!binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                // 설정 메뉴창이 닫혀있을 때
                binding.mainDrawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    // 폴더로 보내기 팝업 윈도우
    @SuppressLint("InflateParams")
    private fun popupWindowToFolderMenu() {
        // 팝업 윈도우 사이즈를 잘못 맞추면 아이템들이 안 뜨므로 하드 코딩으로 사이즈 조정해주기
        // 아이콘 16개 (기본)
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val height = (size.y * 0.4f).toInt()

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_to_folder_menu, null)
        mPopupWindow = PopupWindow(popupView, width, height)

        mPopupWindow.animationStyle = 0
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.mainContent.mainBackgroundView.visibility = View.VISIBLE

        // RecyclerView 구분선
        val recyclerView =
            popupView.findViewById<RecyclerView>(R.id.popup_window_to_folder_menu_recycler_view)
//        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
//        recyclerView.addItemDecoration(dividerItemDecoration)

        // RecyclerView 초기화
        val folderListRVAdapter = FolderListRVAdapter(this@MainActivity)
        val popupFolderList = ArrayList<Folder>()
        AppDatabase.getInstance(this)!!.folderDao().getFolderList(userID).observe(this) {
            popupFolderList.addAll(it)
            folderListRVAdapter.addFolderList(popupFolderList)
        }

        recyclerView.adapter = folderListRVAdapter
        folderListRVAdapter.setMyItemClickListener(object: FolderListRVAdapter.MyItemClickListener {
            override fun onFolderClick(itemBinding: ItemFolderListBinding, itemPosition: Int) {
                // 이동하고 싶은 폴더 클릭 시 폴더로 채팅 이동 (뷰에는 그대로 남아 있도록)
                val selectedFolder = folderList[itemPosition]

                if (selectedFolder.status == HIDDEN) {
                    val lockSPF = getSharedPreferences("lock", 0)
                    val pattern = lockSPF.getString("pattern", "0")

                    // 패턴 모드 확인
                    // 0: 숨긴 폴더 목록을 확인하기 위한 입력 모드
                    // 1: 메인 화면의 설정창 -> 변경 모드
                    // 2: 폴더 화면의 설정창 -> 변경 모드
                    // 3: 메인 화면 폴더로 보내기 -> 숨김 폴더 눌렀을 경우
                    val modeSPF = getSharedPreferences("mode", 0)
                    val editor = modeSPF.edit()

                    // 여기서는 3번 모드
                    editor.putInt("mode", 3)
                    editor.apply()

                    if (pattern.equals("0")) {   // 패턴이 설정되어 있지 않은 경우 패턴 설정 페이지로
                        val intent =
                            Intent(this@MainActivity, CreatePatternActivity::class.java)
                        startActivity(intent)
                    } else {    // 패턴이 설정되어 있는 경우 입력 페이지로 (보안을 위해)
                        val intent = Intent(this@MainActivity, InputPatternActivity::class.java)
                        startActivity(intent)
                    }
                }

                val folderContentDao = database.folderContentDao()

                // 선택된 채팅의 아이디 리스트를 가져옴
                val chatList = mainRVAdapter.getSelectedItem()
                Log.d(tag, "chatList: $chatList")

                // 폴더의 id를 가져옴
                val folderIdx = folderList[itemPosition].idx

                // 갠톡 이동: folderIdx, otherUserIdx
                // 단톡 이동: folderIdx, userIdx, groupName
                for (i in chatList) {
                    if (i.groupName != "null") folderContentDao.insertOrgChat(i.chatIdx, folderIdx, userID)
                    else folderContentDao.insertOtOChat(folderIdx, i.chatIdx)
                }

                // 팝업 윈도우를 꺼주는 역할
                mPopupWindow.dismiss()
                binding.mainContent.mainBackgroundView.visibility = View.INVISIBLE
            }
        })

    //팝업 윈도우에 뜨는 목록 중, 삭제된 폴더도 가져오기 때문에 추가를 함
    //folderListRVAdapter.addFolderList(appDB.folderDao().getFolderExceptDeletedFolder(DELETED) as ArrayList)
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

    // 팝업창 닫을 때
    inner class PopupWindowDismissListener() : PopupWindow.OnDismissListener {
        override fun onDismiss() {
            binding.mainContent.mainBackgroundView.visibility = View.INVISIBLE

            mainRVAdapter.clearSelectedItemList()
            chatViewModel.setMode(mode = 0)

            binding.mainContent.mainFolderIv.visibility = View.VISIBLE
            binding.mainContent.mainFolderModeIv.visibility = View.GONE
            binding.mainContent.mainCancelIv.visibility = View.GONE
            binding.mainContent.mainBackgroundView.visibility = View.INVISIBLE
            binding.mainContent.mainBlockIv.visibility = View.GONE
        }
    }
}

//    // 전체 채팅목록 가져오기 (메인화면) API 성공
//    override fun onGetChatListSuccess(chatList: ArrayList<ChatList>) {
//        Log.d(tag, "onGetChatListSuccess()/chatList: $chatList")
//        database = AppDatabase.getInstance(this)!!
//        mainRVAdapter.addItem(chatList)
//        this.chatList.clear()
//        this.chatList.addAll(chatList)
//
//        // RoomDB에 반영
//        database.chatListDao().allDelete()
//        for(i in 0 until this.chatList.size) {
//            database.chatListDao().insert(this.chatList[i])
//        }
//    }
//
//    // 전체 채팅목록 가져오기 (메인화면) API 실패
//    override fun onGetChatListFailure(code: Int, message: String) {
//        Log.d(tag, "onGetChatListFailure()/code: $code, message: $message")
//    }
//
//    // 전체 폴더목록 가져오기 (숨김 폴더 제외) API 성공
//    override fun onFolderListSuccess(folderList: ArrayList<FolderList>) {
//        Log.d(tag, "onFolderListSuccess()/folderList: $folderList")
////        for(i in 0 until folderList.size) {
////            database.folderDao().insert(Folder(userID, folderList[i].folderName, folderList[i].folderImg))
////        }
//    }
//
//    // 전체 폴더목록 가져오기 (숨김 폴더 제외) API 실패
//    override fun onFolderListFailure(code: Int, message: String) {
//        Log.d(tag, "onFolderListFailure()/code: $code, message: $message")
//
//        // 서버 연결 실패한 경우
//        // 폴더 초기 세팅 (새폴더1, 새폴더2)
//        val folderCount = database.folderDao().getFolderCount(userID)
//        if (folderCount == 0) {
//            Log.d(tag, "onFolderListFailure()/folderCount: $folderCount")
//            database.folderDao().insert(Folder(userID, "새폴더1", R.drawable.ic_baseline_folder_24))
//            database.folderDao().insert(Folder(userID, "새폴더2", R.drawable.ic_baseline_folder_24))
//
////            val folderService = FolderService()
////            folderService.createFolder(this, userID)
//        }
//    }

