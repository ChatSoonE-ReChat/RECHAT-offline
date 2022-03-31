package com.chatsoone.rechat.ui.chat

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Insets
import android.graphics.Point

import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.ApplicationClass
import com.chatsoone.rechat.R
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.data.entity.ChatList
import com.chatsoone.rechat.data.entity.Folder
import com.chatsoone.rechat.databinding.ActivityChatBinding
import com.chatsoone.rechat.databinding.ItemFolderListBinding
import androidx.recyclerview.widget.DiffUtil
import com.chatsoone.rechat.ApplicationClass.Companion.ACT
import com.chatsoone.rechat.base.BaseActivity
import com.chatsoone.rechat.ui.ChatViewModel
import com.chatsoone.rechat.ui.DiffUtilCallback
import com.chatsoone.rechat.ui.FolderListRVAdapter
import com.chatsoone.rechat.ui.pattern.CreatePatternActivity
import com.chatsoone.rechat.ui.pattern.InputPatternActivity
import com.chatsoone.rechat.utils.getId
import kotlin.collections.ArrayList

class ChatActivity : BaseActivity<ActivityChatBinding>(ActivityChatBinding::inflate) {
    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation
    private lateinit var database: AppDatabase
    private lateinit var chatRVAdapter: ChatRVAdapter
    private lateinit var mPopupWindow: PopupWindow
    private lateinit var chatListData: ChatList

    private val chatViewModel: ChatViewModel by viewModels()
    private var folderList = ArrayList<Folder>()
    private var isFabOpen = false    // FAB(FloatingActionButton)가 열렸는지 체크해주는 변수
    private var chatList = ArrayList<ChatList>()
    private var isGroup: Boolean = false
    private var isAll: Int = 0 //모든 채팅을 불러오는지(1), 각 채팅방을 불러오는 것인지(-1)
    private val userID = getId()
    private var isDeletedStatus: Boolean = false
    private val tag = "ACT/CHAT"

    override fun afterOnCreate() {
        Log.d(tag, "initAfterBinding()/userID: $userID")

        initFab()
        initData()
        initRecyclerView()
        initClickListener()
    }

    // FAB 애니메이션 초기화
    private fun initFab() {
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)

        binding.chatMainFab.setImageResource(R.drawable.ic_cloud)
        ObjectAnimator.ofFloat(binding.chatCancelFab, "translationY", 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.chatDeleteFab, "translationY", 0f).apply { start() }
        binding.chatCancelFab.visibility = View.INVISIBLE
        binding.chatDeleteFab.visibility = View.INVISIBLE
        binding.chatCancelFab.isClickable = false
        binding.chatDeleteFab.isClickable = false
        isFabOpen = false
        binding.chatBackgroundView.visibility = View.INVISIBLE
        chatViewModel.setMode(mode = 0)
    }

    // MainActivity로 부터 데이터를 가져온다.
    private fun initData() {
        // isAll : 모든 채팅 목록==-1, 특정 채팅방 목록==1
        isAll = getSharedPreferences("chatAll", MODE_PRIVATE).getInt("chatAll", 0)
        if (intent.hasExtra("chatListJson")) {
            chatListData = intent.getSerializableExtra("chatListJson") as ChatList
            if (chatListData.groupName == null || chatListData.groupName == "null") binding.chatNameTv.text = chatListData.nickName
            else binding.chatNameTv.text = chatListData.groupName
            Log.d(tag, "initData()/chatListData: $chatListData")
        }
    }

    // RecyclerView
    private fun initRecyclerView() {
        val size = windowManager.currentWindowMetricsPointCompat()
        database = AppDatabase.getInstance(this)!!

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.chatChatRecyclerView.layoutManager = linearLayoutManager

        chatRVAdapter = ChatRVAdapter(this, size, object : ChatRVAdapter.MyItemClickListener {
            // 채팅 삭제
            override fun onRemoveChat(chatIdx: Int) {
                Log.d(tag, "chatPositionCheck/지우려는 채팅들의 인텍스 chatIdx: $chatIdx")
                Log.d(tag, "chatPositionCheck/지우려는 채팅들 chatLIst: $chatList")

                // 데이터베이스에서 삭제
                AppDatabase.getInstance(this@ChatActivity)!!.chatDao().deleteByChatIdx(chatIdx)
//                AppDatabase.getInstance(this@ChatActivity)!!.chatListDao().deleteChatListByIdx(chatIdx)
            }

            // 선택 모드
            override fun onChooseChatClick(view: View, position: Int) {
                chatRVAdapter.setChecked(position)
                Log.d(tag, "chatPositionCheck/selected position $position")
            }
        })

        chatViewModel.mode.observe(this) {
            if (it == 0) {
                // 일반 모드
                chatRVAdapter.clearSelectedItemList()
                chatRVAdapter.addItem(chatList)
            } else {
                // 선택 모드
                chatRVAdapter.clearSelectedItemList()
                chatRVAdapter.addItem(chatList)
            }
            // 모든 데이터의 viewType 바꿔주기
            chatRVAdapter.setViewType(currentMode = it)
        }

        // 어댑터 연결
        binding.chatChatRecyclerView.adapter = chatRVAdapter

        if (chatListData.groupName == "null")
            database.chatDao().getOneChatList(userID, chatListData.chatIdx).observe(this) {
                if (it.isEmpty()) {
                    Log.d("chatDataCheckLiveData: ", "NULL, get$it,id: ${chatListData.chatIdx}")
                    val data = database.chatDao().getOneChatNoLiveList(userID, chatListData.chatIdx)
                    Log.d("chatDataCheckLiveData: ", "with No LiveData, get$data")
                    chatRVAdapter.addItem(data)
                    chatList.clear()
                    chatList.addAll(data)
                    binding.chatChatRecyclerView.scrollToPosition(0)
                } else {
                    chatRVAdapter.addItem(it)
                    chatList.clear()
                    chatList.addAll(it)
                    binding.chatChatRecyclerView.scrollToPosition(0)
                }

            }
        else
            database.chatDao().getOrgChatList(userID, chatListData.chatIdx).observe(this) {
                if (it.isEmpty()) {
                    Log.d("chatDataCheckLiveData: ", "NULL, get$it,id: ${chatListData.chatIdx}")
                    val data = database.chatDao().getOrgChatNoLiveList(userID, chatListData.chatIdx)
                    Log.d("chatDataCheckLiveData: ", "with No LiveData, get$data")
                    chatRVAdapter.addItem(data)
                    chatList.clear()
                    chatList.addAll(data)
                    binding.chatChatRecyclerView.scrollToPosition(0)
                } else {
                    Log.d("chatDataCheckLiveData: ", "NONE NULL, get$it")
                    chatRVAdapter.addItem(it)
                    chatList.clear()
                    chatList.addAll(it)
                    binding.chatChatRecyclerView.scrollToPosition(0)
                }

            }

        // 폴더 선택 모드를 해제하기 위해
        binding.chatCancelFab.setOnClickListener {
            binding.chatMainFab.setImageResource(R.drawable.ic_cloud)
//            binding.chatCancelFab.startAnimation(fabClose)
            ObjectAnimator.ofFloat(binding.chatCancelFab, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.chatDeleteFab, "translationY", 0f).apply { start() }
//            binding.chatCancelFab.startAnimation(fabClose)
//            binding.chatDeleteFab.startAnimation(fabClose)
            binding.chatCancelFab.visibility = View.INVISIBLE
            binding.chatDeleteFab.visibility = View.INVISIBLE
            binding.chatCancelFab.isClickable = false
            binding.chatDeleteFab.isClickable = false
            isFabOpen = false
            binding.chatBackgroundView.visibility = View.INVISIBLE

            // 일반 모드로
            chatRVAdapter.clearSelectedItemList()
            chatViewModel.setMode(mode = 0)
        }
    }

    private fun updateList(selectedChatList: List<ChatList>?) {
        selectedChatList?.let {
            val diffCallback = DiffUtilCallback(chatList, selectedChatList)
            val diffResult = DiffUtil.calculateDiff(diffCallback)

            this.chatList.run {
                clear()
                addAll(selectedChatList)
                diffResult.dispatchUpdatesTo(chatRVAdapter)
            }
        }
    }

    private fun initClickListener() {
        // 메인 FAB 버튼 눌렀을 때
        binding.chatMainFab.setOnClickListener {
            if (chatViewModel.mode.value == 0) {
                chatViewModel.setMode(mode = 1)
            } else {
                chatViewModel.setMode(mode = 0)
            }

            if (isFabOpen) {
                // fab 버튼이 열려있는 경우 (선택 모드에서 클릭했을 때)
                // 폴더로 보내는 팝업창을 띄운다.
                // 여기서 view는 클릭된 뷰를 의미한다.
                popupWindowToFolderMenu()
            } else {
                // fab 버튼이 닫혀있는 경우 (일반 모드에서 클릭했을 때)
                binding.chatMainFab.setImageResource(R.drawable.ic_cloud_move)
//                binding.chatCancelFab.startAnimation(fabOpen)
                ObjectAnimator.ofFloat(binding.chatCancelFab, "translationY", -450f).apply { start() }
                ObjectAnimator.ofFloat(binding.chatDeleteFab, "translationY", -250f).apply { start() }
//                binding.chatCancelFab.startAnimation(fabOpen)
//                binding.chatDeleteFab.startAnimation(fabOpen)
                binding.chatCancelFab.visibility = View.VISIBLE
                binding.chatDeleteFab.visibility = View.VISIBLE
                binding.chatCancelFab.isClickable = true
                binding.chatDeleteFab.isClickable = true
                isFabOpen = true
                chatViewModel.setMode(mode = 1)
            }
        }
        // 삭제하는 경우
        binding.chatDeleteFab.setOnClickListener {
            val data = chatRVAdapter.removeSelectedItemList()
            if (data != null) chatListData = data

            binding.chatMainFab.setImageResource(R.drawable.ic_cloud)
            ObjectAnimator.ofFloat(binding.chatCancelFab, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.chatDeleteFab, "translationY", 0f).apply { start() }
            binding.chatCancelFab.visibility = View.INVISIBLE
            binding.chatDeleteFab.visibility = View.INVISIBLE
            binding.chatCancelFab.isClickable = false
            binding.chatDeleteFab.isClickable = false
            isFabOpen = false
            binding.chatBackgroundView.visibility = View.INVISIBLE

            // 일반 모드로
            chatRVAdapter.clearSelectedItemList()
            chatViewModel.setMode(mode = 0)
        }

        binding.chatBackIv.setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
//        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    // 폴더로 보내기 팝업 윈도우
    @SuppressLint("InflateParams")
    private fun popupWindowToFolderMenu() {
        database.folderDao().getFolderList(userID).observe(this) {
            folderList.clear()
            folderList.addAll(it as ArrayList<Folder>)
        }

        //채팅 폴더 이동시 필요한 폴더 목록!folderlist
        // 팝업 윈도우 사이즈를 잘못 맞추면 아이템들이 안 뜨므로 하드 코딩으로 사이즈 조정해주기
        // 아이콘 16개 (기본)
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val height = (size.y * 0.4f).toInt()

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_to_folder_menu, null)
        mPopupWindow = PopupWindow(popupView, width, height)

        mPopupWindow.animationStyle = 0        // 애니메이션 설정 (-1: 설정 안 함, 0: 설정)
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true         // 외부 영역 선택 시 팝업 윈도우 종료
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.chatBackgroundView.visibility = View.VISIBLE

        // RecyclerView 구분선
        val recyclerView =
            popupView.findViewById<RecyclerView>(R.id.popup_window_to_folder_menu_recycler_view)
//        val dividerItemDecoration =
//            DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
//        recyclerView.addItemDecoration(dividerItemDecoration)

        // RecyclerView 초기화
        // 더미 데이터와 어댑터 연결
        val folderListRVAdapter = FolderListRVAdapter(this@ChatActivity)
        recyclerView.adapter = folderListRVAdapter
        folderListRVAdapter.setMyItemClickListener(object :
            FolderListRVAdapter.MyItemClickListener {
            override fun onFolderClick(itemBinding: ItemFolderListBinding, itemPosition: Int) {
                // 이동하고 싶은 폴더 클릭 시 폴더로 채팅 이동 (뷰에는 그대로 남아 있도록)
                val selectedFolder = folderList[itemPosition]
                if (selectedFolder.status == ApplicationClass.HIDDEN) {

                    // 읽는 용도
                    val lockSPF = getSharedPreferences("lock", 0)
                    val pattern = lockSPF.getString("pattern", "0")

                    // 패턴 모드 확인
                    // 0: 숨긴 폴더 목록을 확인하기 위한 입력 모드
                    // 1: 메인 화면의 설정창 -> 변경 모드
                    // 2: 폴더 화면의 설정창 -> 변경 모드
                    // 3: 메인 화면 폴더로 보내기 -> 숨김 폴더 눌렀을 경우

                    // 쓰는 용도
                    val modeSPF = getSharedPreferences("mode", 0)
                    val editor = modeSPF.edit()

                    // 여기서는 3번 모드
                    editor.putInt("mode", 3)
                    editor.apply()

                    if (pattern.equals("0")) {   // 패턴이 설정되어 있지 않은 경우 패턴 설정 페이지로
                        startNextActivity(CreatePatternActivity::class.java)
                    } else {    // 패턴이 설정되어 있는 경우 입력 페이지로 (보안을 위해)
                        startNextActivity(InputPatternActivity::class.java)
                    }
                }

                // 만약 비밀번호가 틀렸을경우 제대로 취소가 되는지 확인
                // 폴더로 이동시키는 코드 작성
                val selectedChatIdx = chatRVAdapter.getSelectedItemList()
                for (i in selectedChatIdx) {
                    database.folderContentDao().insertChat(folderList[itemPosition].idx, i)
                }
                //Log.d(TG, "해당 폴더 목록"+database.folderContentDao().getAllfolder().toString())

                // 팝업 윈도우를 꺼주는 역할
                mPopupWindow.dismiss()
                binding.chatBackgroundView.visibility = View.INVISIBLE

                chatRVAdapter.clearSelectedItemList()
                binding.chatMainFab.setImageResource(R.drawable.ic_cloud)
//                binding.chatCancelFab.startAnimation(fabClose)
                ObjectAnimator.ofFloat(binding.chatCancelFab, "translationY", 0f).apply { start() }
                ObjectAnimator.ofFloat(binding.chatDeleteFab, "translationY", 0f).apply { start() }
                binding.chatCancelFab.visibility = View.INVISIBLE
                binding.chatDeleteFab.visibility = View.INVISIBLE
                binding.chatCancelFab.isClickable = false
                binding.chatDeleteFab.isClickable = false
                isFabOpen = false
                binding.chatBackgroundView.visibility = View.INVISIBLE

                // 일반 모드로
                chatRVAdapter.clearSelectedItemList()
                chatViewModel.setMode(mode = 0)
            }
        })
        database.folderDao().getFolderList(userID).observe(this) {
            folderListRVAdapter.addFolderList(it as ArrayList<Folder>)
        }
    }

    fun receiveData(chat: ChatList) {
        Log.d(ACT, "CHAT/chat: $chat")
    }

    // 디바이스 크기에 사이즈를 맞추기 위한 함수
    private fun WindowManager.currentWindowMetricsPointCompat(): Point {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val windowInsets = currentWindowMetrics.windowInsets
            var insets: Insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())
            windowInsets.displayCutout?.run {
                insets = Insets.max(
                    insets,
                    Insets.of(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom)
                )
            }
            val insetsWidth = insets.right + insets.left
            val insetsHeight = insets.top + insets.bottom
            Point(
                currentWindowMetrics.bounds.width() - insetsWidth,
                currentWindowMetrics.bounds.height() - insetsHeight
            )
        } else {
            Point().apply {
                defaultDisplay.getSize(this)
            }
        }
    }

    inner class PopupWindowDismissListener() : PopupWindow.OnDismissListener {
        override fun onDismiss() {
            binding.chatBackgroundView.visibility = View.INVISIBLE
        }
    }
}